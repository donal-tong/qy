package ui;


import baidupush.Utils;
import bean.Entity;
import bean.Result;
import bean.UserEntity;
//import cn.sharesdk.framework.ShareSDK;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.kuyue.openchat.api.Observers.LoginListener;
import com.kuyue.openchat.api.WmOpenChatSdk;
import com.kuyue.openchat.api.constant.LoginStatus;
import com.umeng.socialize.controller.RequestType;
import com.umeng.socialize.controller.UMServiceFactory;
import com.vikaa.allcontact.R;

import config.AppClient;
import config.CommonValue;
import config.AppClient.ClientCallback;
import config.MyApplication;
import tools.*;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;


public class Tabbar extends TabActivity implements OnCheckedChangeListener{
	private MyApplication appContext;
	public static TabHost mTabHost;
	private RelativeLayout layout1;
	private RelativeLayout layout2;
	private RelativeLayout layout3;
	private RelativeLayout layout4;
	
	private Intent homeIntent;
	private Intent nearmeIntent;
	private Intent meIntent;
	private Intent moreIntent;
	
	private final static String TAB_TAG_HOME = "tab_tag_home";
	private final static String TAB_TAG_NEARME = "tab_tag_nearme";
	private final static String TAB_TAG_ME = "tab_tag_me";
	private final static String TAB_TAG_MORE = "tab_tag_more";
	
	private ProgressDialog loadingPd;

	public static final String DESCRIPTOR = "com.umeng.share";
	
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabbar);
        AppManager.getAppManager().addActivity(this);
        prepareIntent();
        setupIntent();
        layout1 = (RelativeLayout)findViewById(R.id.radio_button1);
        layout1.setSelected(true);
        layout2 = (RelativeLayout)findViewById(R.id.radio_button2);
        layout2.setSelected(false);
        layout3 = (RelativeLayout)findViewById(R.id.radio_button3);
        layout3.setSelected(false);
        layout4 = (RelativeLayout)findViewById(R.id.radio_button4);
        layout4.setSelected(false);
        appContext = (MyApplication) getApplication();
        if (!appContext.isNetworkConnected()) {
    		UIHelper.ToastMessage(getApplicationContext(), "当前网络不可用,请检查你的网络设置", Toast.LENGTH_SHORT);
    		return;
    	}
        checkLogin();
        UpdateManager.getUpdateManager().checkAppUpdate(this, false);
        UMServiceFactory.getUMSocialService(DESCRIPTOR, RequestType.SOCIAL).setGlobalConfig(SocializeConfigDemo.getSocialConfig(this));
	}
	
	private void prepareIntent() {
		homeIntent = new Intent(this, Find.class);
		nearmeIntent = new Intent(this, Phonebook.class);
		meIntent = new Intent(this, Assistant.class);
		moreIntent = new Intent(this, Me.class);
	}
	
	private void setupIntent() {
		mTabHost = getTabHost();
		TabHost localTabHost = mTabHost;
		localTabHost.addTab(buildTabSpec(TAB_TAG_HOME, R.string.main_home, R.drawable.btn_phone, homeIntent));
		localTabHost.addTab(buildTabSpec(TAB_TAG_NEARME, R.string.main_my_card, R.drawable.btn_phone, nearmeIntent));
		localTabHost.addTab(buildTabSpec(TAB_TAG_ME, R.string.main_message, R.drawable.btn_phone, meIntent));
		localTabHost.addTab(buildTabSpec(TAB_TAG_MORE, R.string.main_more, R.drawable.btn_phone, moreIntent));
	}
	
	/**
	 * 构建TabHost的Tab页
	 * @param tag 标记
	 * @param resLabel 标签
	 * @param resIcon 图标
	 * @param content 该tab展示的内容
	 * @return 一个tab
	 */
	private TabHost.TabSpec buildTabSpec(String tag, int resLabel, int resIcon,final Intent content) {
		return Tabbar.mTabHost.newTabSpec(tag).setIndicator(getString(resLabel),
				getResources().getDrawable(resIcon)).setContent(content);
	} 
	
	
	@Override
	public void onCheckedChanged(RadioGroup arg0, int checkedId) {
		switch(checkedId){
		case R.id.radio_button1:
			Tabbar.mTabHost.setCurrentTabByTag(TAB_TAG_HOME);
			break;
		case R.id.radio_button2:
			Tabbar.mTabHost.setCurrentTabByTag(TAB_TAG_NEARME);
			break;
		case R.id.radio_button3:
			Tabbar.mTabHost.setCurrentTabByTag(TAB_TAG_ME);
			break;
		case R.id.radio_button4:
			layout1.setSelected(false);
			Tabbar.mTabHost.setCurrentTabByTag(TAB_TAG_MORE);
			break;
		}
	}
	
	public void ButtonClick(View v) {
		switch(v.getId()){
		case R.id.radio_button1:
			layout1.setSelected(true);
	        layout2.setSelected(false);
	        layout3.setSelected(false);
	        layout4.setSelected(false);
			Tabbar.mTabHost.setCurrentTabByTag(TAB_TAG_HOME);
			break;
		case R.id.radio_button2:
			layout1.setSelected(false);
	        layout2.setSelected(true);
	        layout3.setSelected(false);
	        layout4.setSelected(false);
			Tabbar.mTabHost.setCurrentTabByTag(TAB_TAG_NEARME);
			break;
		case R.id.radio_button3:
			layout1.setSelected(false);
	        layout2.setSelected(false);
	        layout3.setSelected(true);
	        layout4.setSelected(false);
			Tabbar.mTabHost.setCurrentTabByTag(TAB_TAG_ME);
			break;
		case R.id.radio_button4:
			layout1.setSelected(false);
	        layout2.setSelected(false);
	        layout3.setSelected(false);
	        layout4.setSelected(true);
			Tabbar.mTabHost.setCurrentTabByTag(TAB_TAG_MORE);
			break;
		}
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
//					getUnReadMessage();
					if (!Utils.hasBind(getApplicationContext())) {
						blindBaidu();
					}
					Intent intent = new Intent(CommonValue.Login_SUCCESS_ACTION);
					sendBroadcast(intent);
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
	
	private void blindBaidu() {
//		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
//		singleThreadExecutor.execute(new Runnable() {
//			@Override
//			public void run() {
				PushManager.startWork(getApplicationContext(),
						PushConstants.LOGIN_TYPE_API_KEY, 
						Utils.getMetaValue(Tabbar.this, "api_key"));
//			}
//		});
		
	}
	
	private void showLogin() {
		appContext.setUserLogout();
		Intent intent = new Intent(this,LoginCode1.class);
        startActivity(intent);
        AppManager.getAppManager().finishActivity(this);
	}
}
