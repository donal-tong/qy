package ui;

import tools.AppException;
import tools.AppManager;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;
import ui.remind.NewRemindActivity;
import ui.remind.RemindFragment;
import baidupush.Utils;
import bean.CardListEntity;
import bean.Entity;
import bean.Result;
import bean.UserEntity;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.crashlytics.android.Crashlytics;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.readystatesoftware.viewbadger.BadgeView;
import com.vikaa.mycontact.R;

import config.AppClient;
import config.CommonValue;
import config.AppClient.ClientCallback;
import de.greenrobot.event.EventBus;
import event.NotificationEvent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppActivity{

	@ViewInject(R.id.leftBarButton)
	private Button btnMessage;
	
	@ViewInject(R.id.badge)
	private BadgeView badge;
	
	@ViewInject(R.id.badgeDiscover)
	private BadgeView badgeDiscover;
	
	@ViewInject(R.id.navTitle)
	private TextView navTitle;
	
	@ViewInject(R.id.webview)
	private WebView webview;
	
	private RelativeLayout[] mTabs;
	private PhonebookFragment phonebookFragment;
	private RemindFragment remindFragment;
	private DiscoverFragment discoverFragment;
	private CardFragment cardFragment;
	private Fragment[] fragments;
	private int index;
	// 当前fragment的index
	private int currentTabIndex;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ViewUtils.inject(this);
		EventBus.getDefault().register(this);
		IntentFilter filter = new IntentFilter();
		filter.addAction(CommonValue.RELOGIN_ACTION);
		registerReceiver(receiver, filter);
		initView();
		phonebookFragment = new PhonebookFragment();
		remindFragment = new RemindFragment();
		discoverFragment = new DiscoverFragment();
		cardFragment = new CardFragment();
		fragments = new Fragment[]{phonebookFragment, remindFragment, discoverFragment, cardFragment};
		getSupportFragmentManager().beginTransaction()
		.add(R.id.fragment_container, phonebookFragment)
		.add(R.id.fragment_container, remindFragment)
		.hide(remindFragment)
		.show(phonebookFragment)
		.commit();
		checkLogin();
		
		badgeDiscover.setVisibility(View.INVISIBLE);
	}
	
	@Override
	protected void onDestroy() {
		EventBus.getDefault().unregister(this);
		unregisterReceiver(receiver);
		super.onDestroy();
	}
	
	private void initView() {
		mTabs = new RelativeLayout[4];
		mTabs[0] = (RelativeLayout) findViewById(R.id.radio_button1);
		mTabs[1] = (RelativeLayout) findViewById(R.id.radio_button2);
		mTabs[2] = (RelativeLayout) findViewById(R.id.radio_button3);
		mTabs[3] = (RelativeLayout) findViewById(R.id.radio_button4);
		mTabs[0].setSelected(true);
		navTitle.setText(getString(R.string.phonebook));
		accretionArea(btnMessage);
		if (StringUtils.notEmpty(appContext.getNews())) {
			try {
				if (Integer.valueOf(appContext.getNews()) > 0) {
					badge.setVisibility(View.VISIBLE);
					badge.setText(Integer.valueOf(appContext.getNews())<99?appContext.getNews():"99+");
				}
				else {
					badge.setVisibility(View.INVISIBLE);
				}
			}
			catch (Exception e) {
				Crashlytics.logException(e);
			}
		}
	}
	
	public void showMessage(View v) {
		badge.setVisibility(View.INVISIBLE);
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
	
	public void addSomething(View v) {
		if (currentTabIndex == 1) {
			startActivity(new Intent(this, NewRemindActivity.class));
		}
		else {
			startActivity(new Intent(this, AddSometingActivity.class));
		}
	}
	
	public void ButtonClick(View v) {
		switch(v.getId()){
		case R.id.radio_button1:
			navTitle.setText(getString(R.string.phonebook));
			index = 0;
			break;
		case R.id.radio_button2:
			navTitle.setText(getString(R.string.activity));
			index = 1;
			break;
		case R.id.radio_button3:
			navTitle.setText(getString(R.string.discover));
			index = 2;
			break;
		case R.id.radio_button4:
			navTitle.setText(getString(R.string.card));
			index = 3;
			break;
		}
		if (currentTabIndex != index) {
			FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
			trx.hide(fragments[currentTabIndex]);
			if (!fragments[index].isAdded()) {
				trx.add(R.id.fragment_container, fragments[index]);
			}
			trx.show(fragments[index]).commit();
		}
		mTabs[currentTabIndex].setSelected(false);
		mTabs[index].setSelected(true);
		currentTabIndex = index;
	}
	
	private void checkLogin() {
		loadingPd = UIHelper.showProgress(this, "请稍后", "正在登录中...", true);
		AppClient.autoLogin(appContext, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				UIHelper.dismissProgress(loadingPd);
				UserEntity user = (UserEntity)data;
				switch (user.getError_code()) {
				case Result.RESULT_OK:
					appContext.saveLoginInfo(user);
					appContext.setMessageInterupt("1");
//					if (!Utils.hasBind(getApplicationContext())) {
						PushManager.startWork(getApplicationContext(),
								PushConstants.LOGIN_TYPE_API_KEY, 
								Utils.getMetaValue(MainActivity.this, "api_key"));
//					}
					sendBroadcast(new Intent(CommonValue.Login_SUCCESS_ACTION));
					webview.loadUrl(CommonValue.BASE_URL + "/home/app" + "?_sign=" + appContext.getLoginSign())  ;
					webview.setWebViewClient(new WebViewClient() {
						public boolean shouldOverrideUrlLoading(WebView view, String url) {
							view.loadUrl(url);
							return true;
						};
					});
					getCardList();
					
					if (StringUtils.notEmpty(appContext.getNews())) {
						try {
							if (Integer.valueOf(appContext.getNews()) > 0) {
								badge.setVisibility(View.VISIBLE);
								badge.setText(Integer.valueOf(appContext.getNews())<99?appContext.getNews():"99+");
							}
							else {
								badge.setVisibility(View.INVISIBLE);
							}
						}
						catch (Exception e) {
							Crashlytics.logException(e);
						}
					}
					if(!appContext.isBeenGuide1()){
						startActivity(new Intent(MainActivity.this, AddSomethingGuideActviity.class));
					}
					
					break;
				default:
					UIHelper.ToastMessage(getApplicationContext(), user.getMessage(), Toast.LENGTH_SHORT);
					showLogin();
					break;
				}
			}
			@Override
			public void onFailure(String message) {
				UIHelper.dismissProgress(loadingPd);
				UIHelper.ToastMessage(getApplicationContext(), message, Toast.LENGTH_SHORT);
			}
			@Override
			public void onError(Exception e) {
				UIHelper.dismissProgress(loadingPd);
				((AppException)e).makeToast(getApplicationContext());
			}
		});
	}
	
	private void showLogin() {
		appContext.setUserLogout();
		Intent intent = new Intent(this,LoginCode1.class);
        startActivity(intent);
        AppManager.getAppManager().finishActivity(this);
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
	
	public void onEventMainThread(NotificationEvent event) {
		if (event.getMsg().equals("1")) {
			startActivity(new Intent(this, CreatePhonebook.class));
		}
		else if (event.getMsg().equals("2")) {
			startActivity(new Intent(this, CreateActivity.class));
		}
		else {
			showMyCard();
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
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (CommonValue.RELOGIN_ACTION.equals(action)) {
				checkLogin();
			}
		}

	};
}
