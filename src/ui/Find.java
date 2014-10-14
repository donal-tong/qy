package ui;


import java.util.ArrayList;
import java.util.List;

import qiniu.conf.Conf;
import qiniu.utils.Config;
import qiniu.utils.Mac;
import qiniu.utils.PutPolicy;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;
import ui.adapter.FindAdFragmentAdapter;
import ui.adapter.FunsGridViewAdapter;
import ui.adapter.QunGridViewAdapter;
import baidupush.Utils;
import bean.AdsEntity;
import bean.AdsListEntity;
import bean.CardListEntity;
import bean.Entity;
import bean.FunsEntity;
import bean.FunsListEntity;
import bean.QunsEntity;
import bean.QunsListEntity;
import bean.Result;
import bean.TopicOptionListEntity;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.crashlytics.android.Crashlytics;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.vikaa.mycontact.R;

import config.AppClient;
import config.CommonValue;
import config.AppClient.ClientCallback;
import config.CommonValue.CreateViewUrlAndRequest;
import config.CommonValue.FunsType;
import android.R.string;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class Find extends AppActivity implements OnItemClickListener{
	private List<AdsEntity> ads = new ArrayList<AdsEntity>();
	private FindAdFragmentAdapter adsAdapter;
	
	private TextView tvMessage;
	@Override
	protected void onPause() {
		unregisterReceiver(receiver);
		super.onPause();
	}

	@Override
	protected void onResume() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(CommonValue.Login_SUCCESS_ACTION);
		registerReceiver(receiver, filter);
		super.onResume();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.find);
		initUI();
//        if (appContext.getNeedSetPassword()) {
//            WarningDialog("您的账号还没有设置密码,现在设置?", "好", "下次再说", new DialogClickListener() {
//                @Override
//                public void ok() {
//                    startActivity(new Intent(Find.this, SetPassword.class));
//                }
//
//                @Override
//                public void cancel() {
//
//                }
//            });
//        }
	}
	
	private void initUI() {
		ViewPager adsViewPager = (ViewPager) findViewById(R.id.viewPager);
		adsAdapter = new FindAdFragmentAdapter(getSupportFragmentManager(), ads);
		adsViewPager.setAdapter(adsAdapter);
		getAdsFromCache();
		tvMessage = (TextView) findViewById(R.id.messageView);
		if (StringUtils.notEmpty(appContext.getNews())) {
			try {
				if (Integer.valueOf(appContext.getNews()) > 0) {
					tvMessage.setVisibility(View.VISIBLE);
					tvMessage.setText(Integer.valueOf(appContext.getNews())<99?appContext.getNews():"99+");
				}
			}
			catch (Exception e) {
				Crashlytics.logException(e);
			}
		}
		
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			showNotification();
			tvMessage.setVisibility(View.INVISIBLE);
			break;
		case R.id.btnQun:
			startActivity(new Intent(this, CreatePhonebook.class));
			break;
		case R.id.btnActivity:
			startActivity(new Intent(this, CreateActivity.class));
			break;
		case R.id.btnTopic:
			startActivity(new Intent(this, QunTopic.class));
			break;
		case R.id.btnCard:
            showMyCard();
			break;
		case R.id.btnPC:
			startActivity(new Intent(this, PCTIP.class));
			break;
		default:
			break;
		}
	}

    public void showMyCard() {
        EasyTracker easyTracker = EasyTracker.getInstance(this);
        easyTracker.send(MapBuilder
                        .createEvent("ui_action",     // Event category (required)
                                "button_press",  // Event action (required)
                                "查看我的名片",   // Event label
                                null)            // Event value
                        .build()
        );
        Intent intent = new Intent(this, MyCard.class);
        startActivity(intent);
    }
	
	public void showNotification() {
		EasyTracker easyTracker = EasyTracker.getInstance(this);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看通知："+String.format("%s/message/index", CommonValue.BASE_URL),   // Event label
	                   null)            // Event value
	      .build()
		);
		Intent intent = new Intent(this, QYWebView.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, String.format("%s/message/index", CommonValue.BASE_URL));
		startActivity(intent);
		AppClient.setMessageRead(appContext);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
//		if (parent.getAdapter() == qunAdapter) {
//			startActivity(new Intent(this, QYWebView.class)
//			.putExtra(CommonValue.IndexIntentKeyValue.CreateView, CreateViewUrlAndRequest.ContactCreateUrl+"/id/"+quns.get(position).id));
//		}
//		if (parent.getAdapter() == funsAdapter) {
//			if (funs.get(position).id.equals("20")) {
//				startActivity(new Intent(this, CreateTopic.class).putExtra("fun", funs.get(position)));
//			}
//			else if (funs.get(position).id.equals("19")) {
//				startActivity(new Intent(this, QYWebView.class)
//				.putExtra(CommonValue.IndexIntentKeyValue.CreateView, CreateViewUrlAndRequest.CardCreateUrl1));
//			}
//			else {
//				startActivity(new Intent(this, CreateActivity.class).putExtra("fun", funs.get(position)));
//			}
//		}
	}
	
	private void getAdsFromCache() {
		ads.add(new AdsEntity(CommonValue.ADS_TITLE, CommonValue.AD_THUMB+R.drawable.ad_default, CommonValue.AD_LINK));
		adsAdapter.notifyDataSetChanged();
		String key = String.format("%s-%s", CommonValue.CacheKey.ADS, appContext.getLoginUid());
		AdsListEntity entity = (AdsListEntity) appContext.readObject(key);
		if(entity != null){
			ads.clear();
			ads.addAll(entity.ads);
			adsAdapter.notifyDataSetChanged();
		}
		getAds();
	}
	
	private void getAds() {
		AppClient.getSlideAds(appContext, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				AdsListEntity adsList = (AdsListEntity) data;
				ads.clear();
				ads.addAll(adsList.ads);
				adsAdapter.notifyDataSetChanged();
			}
			
			@Override
			public void onFailure(String message) {
				getAds();
			}
			
			@Override
			public void onError(Exception e) {
				getAds();
			}
		});
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (CommonValue.Login_SUCCESS_ACTION.equals(action)) {
				webViewLogin();
			}
		}

	};
	
	private void webViewLogin() {
		WebView webview = (WebView) findViewById(R.id.webview);
		webview.loadUrl(CommonValue.BASE_URL + "/home/app" + "?_sign=" + appContext.getLoginSign())  ;
		webview.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			};
		});
		getCardList();
	}
	
	private void getCardList() {
		AppClient.getCardList(appContext, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				UIHelper.dismissProgress(loadingPd);
				CardListEntity entity = (CardListEntity)data;
				switch (entity.getError_code()) {
				case Result.RESULT_OK:
					if (entity.owned.size()>0) {
						appContext.setUserAvatar(entity.owned.get(0).avatar);
						appContext.setUserAvatarCode(entity.owned.get(0).code);
						Logger.i(entity.owned.get(0).code);
					}
					break;
				default:
					break;
				}
			}
			
			@Override
			public void onFailure(String message) {
			}
			@Override
			public void onError(Exception e) {
			}
		});
	}
}
