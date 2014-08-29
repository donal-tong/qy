package ui;

import java.util.ArrayList;
import java.util.List;

import android.widget.*;
import tools.AppManager;
import tools.Logger;
import tools.UIHelper;
import ui.adapter.MyCardAdapter;
import bean.CardIntroEntity;
import bean.CardListEntity;
import bean.Entity;
import bean.Result;

import com.crashlytics.android.Crashlytics;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.vikaa.allcontact.R;

import config.AppClient;
import config.CommonValue;
import config.AppClient.ClientCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.View;

public class MyCard extends AppActivity implements OnRefreshListener, AdapterView.OnItemClickListener{
	private int lvDataState;
	private List<CardIntroEntity> cards = new ArrayList<CardIntroEntity>();
	private MyCardAdapter xAdapter;
	private ListView xListView;
	private SwipeRefreshLayout swipeLayout;
	private TextView emptyTV;
	  
	@Override
	protected void onDestroy() {
		unregisterReceiver(receiver);
		super.onDestroy();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mycard);
		IntentFilter filter = new IntentFilter();
		filter.addAction(CommonValue.CARD_CREATE_ACTION);
		filter.addAction(CommonValue.CARD_DELETE_ACTION);
		registerReceiver(receiver, filter);
		initUI();
		getCardListFromCache();
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			AppManager.getAppManager().finishActivity(this);
			break;

            case R.id.rightBarButton:
                startActivityForResult(new Intent(this, QYWebView.class).
                                putExtra(CommonValue.IndexIntentKeyValue.CreateView, CommonValue.CreateViewUrlAndRequest.CardCreateUrl),
                        CommonValue.CreateViewUrlAndRequest.CardCreat);
                break;
		default:
			startActivityForResult(new Intent(this, QYWebView.class).
					putExtra(CommonValue.IndexIntentKeyValue.CreateView, CommonValue.CreateViewUrlAndRequest.CardCreateUrl), 
					CommonValue.CreateViewUrlAndRequest.CardCreat);
			break;
		}
	}
	
	public void showCardViewWeb(CardIntroEntity entity) {
		EasyTracker easyTracker = EasyTracker.getInstance(this);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看名片："+entity.link,   // Event label
	                   null)            // Event value
	      .build()
		);
		Intent intent = new Intent(context, CardView.class);
		intent.putExtra(CommonValue.CardViewIntentKeyValue.CardView, entity);
		startActivityForResult(intent, CommonValue.CardViewUrlRequest.editCard);
	}
	
	private void initUI() {
		emptyTV = (TextView) findViewById(R.id.tv_empty);
		swipeLayout = (SwipeRefreshLayout) findViewById(R.id.xrefresh);
		xListView = (ListView) findViewById(R.id.xlistview);
		xAdapter = new MyCardAdapter(this, cards, imageLoader);
		xListView.setAdapter(xAdapter);
        xListView.setOnItemClickListener(this);
		swipeLayout.setOnRefreshListener(this);
	    swipeLayout.setColorScheme(android.R.color.holo_blue_bright, 
	            android.R.color.holo_green_light, 
	            android.R.color.holo_orange_light, 
	            android.R.color.holo_red_light);
	}
	
	private void getCardListFromCache() {
		String key = String.format("%s-%s", CommonValue.CacheKey.CardList, appContext.getLoginUid());
		CardListEntity entity = (CardListEntity) appContext.readObject(key);
		if(entity != null){
			cards.clear();
			if (entity.owned.size()>0) {
				cards.addAll(entity.owned);
			}
			xAdapter.notifyDataSetChanged();
		}
		getCardList();
	}
	
	private void getCardList() {
		if (!appContext.isNetworkConnected() && cards.isEmpty()) {
    		UIHelper.ToastMessage(getApplicationContext(), "当前网络不可用,请检查你的网络设置", Toast.LENGTH_SHORT);
    		swipeLayout.setRefreshing(false);
    		return;
    	}
		loadingPd = UIHelper.showProgress(this, null, null, true);
		AppClient.getCardList(appContext, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				UIHelper.dismissProgress(loadingPd);
				CardListEntity entity = (CardListEntity)data;
				switch (entity.getError_code()) {
				case Result.RESULT_OK:
					cards.clear();
					if (entity.owned.size()>0) {
						cards.addAll(entity.owned);
						emptyTV.setVisibility(View.GONE);
					}
					else {
						emptyTV.setVisibility(View.VISIBLE);
					}
					xAdapter.notifyDataSetChanged();
					break;
				case CommonValue.USER_NOT_IN_ERROR:
					forceLogout();
					break;
				default:
					UIHelper.ToastMessage(getApplicationContext(), entity.getMessage(), Toast.LENGTH_SHORT);
					break;
				}
				lvDataState = UIHelper.LISTVIEW_DATA_MORE;
				swipeLayout.setRefreshing(false);
			}
			
			@Override
			public void onFailure(String message) {
				UIHelper.dismissProgress(loadingPd);
				UIHelper.ToastMessage(getApplicationContext(), message, Toast.LENGTH_SHORT);
				lvDataState = UIHelper.LISTVIEW_DATA_MORE;
				swipeLayout.setRefreshing(false);
			}
			@Override
			public void onError(Exception e) {
				UIHelper.dismissProgress(loadingPd);
				Crashlytics.logException(e);
				lvDataState = UIHelper.LISTVIEW_DATA_MORE;
				swipeLayout.setRefreshing(false);
			}
		});
	}
	
	@Override
	public void onRefresh() {
		if (lvDataState == UIHelper.LISTVIEW_DATA_MORE) {
			lvDataState = UIHelper.LISTVIEW_DATA_LOADING;
			getCardList();
		}
		else {
			swipeLayout.setRefreshing(false);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Logger.i(resultCode+"");
		if (resultCode != RESULT_OK) {
			return;
		}
		if (requestCode == 10) {
			String code = data.getExtras().getString("code");
			for (CardIntroEntity card : cards) {
				if (card.code.equals(code)) {
					card.certified_state = "1";
				}
			}
			xAdapter.notifyDataSetChanged();
		}
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (CommonValue.CARD_CREATE_ACTION.equals(action) 
					|| CommonValue.CARD_DELETE_ACTION.equals(action)) {
				getCardList();
			}
		}

	};

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Logger.i("a");
        CardIntroEntity model = cards.get(position);
        ((MyCard)context).showCardViewWeb(model);
    }
}
