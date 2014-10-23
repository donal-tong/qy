package ui;

import tools.AppException;
import tools.AppManager;
import tools.ImageUtils;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;
import baidupush.Utils;
import bean.Entity;
import bean.Result;
import bean.UserEntity;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.crashlytics.android.Crashlytics;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.umeng.socialize.controller.RequestType;
import com.umeng.socialize.controller.UMServiceFactory;
import com.vikaa.wecontact.R;

import config.AppClient;
import config.CommonValue;
import config.AppClient.ClientCallback;
import config.MyApplication;
import de.greenrobot.event.EventBus;
import event.MenuClickEvent;
import fragment.ActivityFragment;
import fragment.CardFragment;
import fragment.MenuFragment;
import fragment.PhonebookFragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.IInterface;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends BaseSlidingFragmentActivity{
	
	private MenuFragment menuFragment;
	private PhonebookFragment phonebookFragment;
	private ActivityFragment activityFragment;
	private CardFragment cardFragment;
	private Fragment[] fragments;
	private int index;
	// 当前fragment的index
	private int currentTabIndex;
	
	@ViewInject(R.id.tvTitle)
	private TextView tvTitle;
	@ViewInject(R.id.messageView)
	private TextView messageView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
		setContentView(R.layout.activity_main);
		ViewUtils.inject(this);
		UMServiceFactory.getUMSocialService(DESCRIPTOR, RequestType.SOCIAL).setGlobalConfig(SocializeConfigDemo.getSocialConfig(this));
		checkLogin();
		initUI();
	}
	
	@Override
	protected void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}
	
	private void initUI() {
		setBehindContentView(R.layout.left_menu); 
		menuFragment = new MenuFragment();
		phonebookFragment = new PhonebookFragment();
		activityFragment = new ActivityFragment();
		cardFragment = new CardFragment();
		fragments = new Fragment[]{phonebookFragment, activityFragment, cardFragment};
		currentTabIndex = 0;
		getSupportFragmentManager().beginTransaction().add(R.id.menu_rl, menuFragment)
		.add(R.id.main_rl, phonebookFragment)
		.add(R.id.main_rl, activityFragment)
		.add(R.id.main_rl, cardFragment)
		.hide(activityFragment)
		.hide(cardFragment)
		.show(phonebookFragment)
		.commit();
		
		SlidingMenu localSlidingMenu = getSlidingMenu();
		localSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		localSlidingMenu.setShadowWidthRes(R.dimen.shadow_width);
		localSlidingMenu.setShadowDrawable(R.drawable.shadow);
		localSlidingMenu.setBehindOffset(ImageUtils.getDisplayWidth(this)-ImageUtils.dip2px(this, 205));
		localSlidingMenu.setFadeDegree(0.35F);
		
	}

	private long mExitTime;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if(getSlidingMenu().isMenuShowing()){
				toggle();
			}else {
				if ((System.currentTimeMillis() - mExitTime) > 2000) {
					Toast.makeText(this, "在按一次退出",
							Toast.LENGTH_SHORT).show();
					mExitTime = System.currentTimeMillis();
				} else {
					finish();
				}
			}
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
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
					if (!Utils.hasBind(getApplicationContext())) {
						PushManager.startWork(getApplicationContext(),
								PushConstants.LOGIN_TYPE_API_KEY, 
								Utils.getMetaValue(MainActivity.this, "api_key"));
					}
					webViewLogin();
					if (StringUtils.notEmpty(appContext.getNews())) {
						try {
							setBadgeNumber(appContext.getNews());
							menuFragment.setBadgeNumber(appContext.getNews());
						}
						catch (Exception e) {
							Crashlytics.logException(e);
						}
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
	
	private void webViewLogin() {
		WebView webview = (WebView) findViewById(R.id.webview);
		webview.loadUrl(CommonValue.BASE_URL + "/home/app" + "?_sign=" + appContext.getLoginSign())  ;
		webview.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			};
		});
	}
	
	private void setBadgeNumber(String number) {
		if (StringUtils.empty(number)) {
			messageView.setVisibility(View.INVISIBLE);
		}
		try {
			int i = Integer.valueOf(number);
			if (i == 0 ) {
				messageView.setVisibility(View.INVISIBLE);
			}
			else {
				messageView.setVisibility(View.VISIBLE);
			}
		}
		catch (Exception e ) {
			messageView.setVisibility(View.INVISIBLE);
		}
	}
	
	public void onEventMainThread(MenuClickEvent event){
		toggle();
		if (event.index == 3) {
			Intent intent = new Intent(this, QYWebView.class);
			intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, String.format("%s/message/index", CommonValue.BASE_URL));
			startActivity(intent);
			AppClient.setMessageRead(MyApplication.getInstance());
			messageView.setVisibility(View.INVISIBLE);
		}
		else {
			switch (event.index) {
			case 0:
				tvTitle.setText("通讯录");
				index = 0;
				break;

			case 1:
				tvTitle.setText("活动");
				index = 1;
				break;
				
			case 2:
				tvTitle.setText("名片");
				index = 2;
				break;
			}
			if (currentTabIndex != index) {
				FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
				trx.hide(fragments[currentTabIndex]);
				if (!fragments[index].isAdded()) {
					trx.add(R.id.main_rl, fragments[index]);
				}
				trx.show(fragments[index]).commit();
			}
			currentTabIndex = index;
		}
    }
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			toggle();
			break;

		case R.id.rightBarButton:
			switch (currentTabIndex) {
			case 0:
				startActivity(new Intent(this, CreatePhonebook.class));
				break;

			case 1:
				startActivity(new Intent(this, CreateActivity.class));
				break;
				
			case 2:
				startActivityForResult(new Intent(this, QYWebView.class).
                        putExtra(CommonValue.IndexIntentKeyValue.CreateView, CommonValue.CreateViewUrlAndRequest.CardCreateUrl),
                CommonValue.CreateViewUrlAndRequest.CardCreat);
				break;
			}
			break;
		}
	}
}
