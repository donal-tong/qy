package ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tools.AppManager;
import tools.UIHelper;
import ui.adapter.FamilyPhonebookAdapter;
import ui.adapter.MyCardAdapter;
import bean.CardIntroEntity;
import bean.CardListEntity;
import bean.Entity;
import bean.FamilyListEntity;
import bean.PhoneIntroEntity;
import bean.PhoneListEntity;
import bean.Result;

import com.crashlytics.android.Crashlytics;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.vikaa.mycontact.R;

import config.AppClient;
import config.CommonValue;
import config.AppClient.ClientCallback;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FamilyPhonebook extends AppActivity implements OnRefreshListener, OnItemClickListener{

	private int lvDataState;
	private List<PhoneIntroEntity> familys = new ArrayList<PhoneIntroEntity>();
	private FamilyPhonebookAdapter xAdapter;
	private ListView xListView;
	private SwipeRefreshLayout swipeLayout;
	private TextView emptyTV;
	  
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			AppManager.getAppManager().finishActivity(this);
			break;

		default:
			startActivityForResult(new Intent(this, QYWebView.class).
					putExtra(CommonValue.IndexIntentKeyValue.CreateView, CommonValue.CreateViewUrlAndRequest.CardCreateUrl), 
					CommonValue.CreateViewUrlAndRequest.CardCreat);
			break;
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.family_phonebook);
		initUI();
		getFamilyListFromCache();
	}
	
	private void initUI() {
		emptyTV = (TextView) findViewById(R.id.tv_empty);
		swipeLayout = (SwipeRefreshLayout) findViewById(R.id.xrefresh);
		xListView = (ListView) findViewById(R.id.xlistview);
		xAdapter = new FamilyPhonebookAdapter(this, familys);
		xListView.setAdapter(xAdapter);
		xListView.setOnItemClickListener(this);
		swipeLayout.setOnRefreshListener(this);
	    swipeLayout.setColorScheme(android.R.color.holo_blue_bright, 
	            android.R.color.holo_green_light, 
	            android.R.color.holo_orange_light, 
	            android.R.color.holo_red_light);
	}
	
	private void getFamilyListFromCache() {
		String key = String.format("%s-%s", CommonValue.CacheKey.FamilyList, appContext.getLoginUid());
		FamilyListEntity entity = (FamilyListEntity) appContext.readObject(key);
		if(entity != null){
			familys.clear();
			handlerFamilySection(entity);
		}
		getFamilyList();
	}
	
	private void getFamilyList() {
		if (!appContext.isNetworkConnected() && familys.isEmpty()) {
    		UIHelper.ToastMessage(getApplicationContext(), "当前网络不可用,请检查你的网络设置", Toast.LENGTH_SHORT);
    		swipeLayout.setRefreshing(false);
    		return;
    	}
		AppClient.getFamilyList(appContext, new ClientCallback() {
			
			@Override
			public void onSuccess(Entity data) {
				FamilyListEntity entity = (FamilyListEntity)data;
				switch (entity.getError_code()) {
				case Result.RESULT_OK:
					handlerFamilySection(entity);
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
				UIHelper.ToastMessage(getApplicationContext(), message, Toast.LENGTH_SHORT);
				lvDataState = UIHelper.LISTVIEW_DATA_MORE;
				swipeLayout.setRefreshing(false);
			}
			
			@Override
			public void onError(Exception e) {
				Crashlytics.logException(e);
				lvDataState = UIHelper.LISTVIEW_DATA_MORE;
				swipeLayout.setRefreshing(false);
			}
		});
	}
	
	private void handlerFamilySection(FamilyListEntity entity) {
		familys.clear();
		familys.addAll(entity.family);
		familys.addAll(entity.clan);
		xAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void onRefresh() {
		if (lvDataState == UIHelper.LISTVIEW_DATA_MORE) {
			lvDataState = UIHelper.LISTVIEW_DATA_LOADING;
			getFamilyList();
		}
		else {
			swipeLayout.setRefreshing(false);
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View convertView, int position, long arg3) {
		PhoneIntroEntity entity = familys.get(position);
		EasyTracker easyTracker = EasyTracker.getInstance(this);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看群友通讯录："+entity.link,   // Event label
	                   null)            // Event value
	      .build()
		);
		Intent intent = new Intent(this, QYWebView.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, entity.link);
	    startActivityForResult(intent, CommonValue.PhonebookViewUrlRequest.editPhoneview);
	}
}
