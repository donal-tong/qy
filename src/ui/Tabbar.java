package ui;


import baidupush.Utils;
import bean.Entity;
import bean.Result;
import bean.UserEntity;
//import cn.sharesdk.framework.ShareSDK;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.umeng.socialize.controller.RequestType;
import com.umeng.socialize.controller.UMServiceFactory;
import com.vikaa.mycontact.R;

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
	
	private Intent phonebookIntent;
	private Intent activityIntent;
	private Intent discoverIntent;
	private Intent cardIntent;
	
	private final static String TAB_TAG_PHONEBOOK = "tab_tag_phonebook";
	private final static String TAB_TAG_ACTIVITY = "tab_tag_activity";
	private final static String TAB_TAG_DISCOVER = "tab_tag_discover";
	private final static String TAB_TAG_CARD = "tab_tag_card";
	
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
		phonebookIntent = new Intent(this, Phonebook.class);
		activityIntent = new Intent(this, Assistant.class);
		discoverIntent = new Intent(this, Find.class);
		cardIntent = new Intent(this, Me.class);
	}
	
	private void setupIntent() {
		mTabHost = getTabHost();
		TabHost localTabHost = mTabHost;
		localTabHost.addTab(buildTabSpec(TAB_TAG_PHONEBOOK, R.string.main_home, R.drawable.btn_phone, phonebookIntent));
		localTabHost.addTab(buildTabSpec(TAB_TAG_ACTIVITY, R.string.main_my_card, R.drawable.btn_phone, activityIntent));
		localTabHost.addTab(buildTabSpec(TAB_TAG_DISCOVER, R.string.main_message, R.drawable.btn_phone, discoverIntent));
		localTabHost.addTab(buildTabSpec(TAB_TAG_CARD, R.string.main_more, R.drawable.btn_phone, cardIntent));
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
			Tabbar.mTabHost.setCurrentTabByTag(TAB_TAG_PHONEBOOK);
			break;
		case R.id.radio_button2:
			Tabbar.mTabHost.setCurrentTabByTag(TAB_TAG_ACTIVITY);
			break;
		case R.id.radio_button3:
			Tabbar.mTabHost.setCurrentTabByTag(TAB_TAG_DISCOVER);
			break;
		case R.id.radio_button4:
			layout1.setSelected(false);
			Tabbar.mTabHost.setCurrentTabByTag(TAB_TAG_CARD);
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
			Tabbar.mTabHost.setCurrentTabByTag(TAB_TAG_PHONEBOOK);
			break;
		case R.id.radio_button2:
			layout1.setSelected(false);
	        layout2.setSelected(true);
	        layout3.setSelected(false);
	        layout4.setSelected(false);
			Tabbar.mTabHost.setCurrentTabByTag(TAB_TAG_ACTIVITY);
			break;
		case R.id.radio_button3:
			layout1.setSelected(false);
	        layout2.setSelected(false);
	        layout3.setSelected(true);
	        layout4.setSelected(false);
			Tabbar.mTabHost.setCurrentTabByTag(TAB_TAG_DISCOVER);
			break;
		case R.id.radio_button4:
			layout1.setSelected(false);
	        layout2.setSelected(false);
	        layout3.setSelected(false);
	        layout4.setSelected(true);
			Tabbar.mTabHost.setCurrentTabByTag(TAB_TAG_CARD);
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
					appContext.setMessageInterupt("1");
//					if (!Utils.hasBind(getApplicationContext())) {
						PushManager.startWork(getApplicationContext(),
								PushConstants.LOGIN_TYPE_API_KEY, 
								Utils.getMetaValue(Tabbar.this, "api_key"));
//					}
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
	
	private void showLogin() {
		appContext.setUserLogout();
		Intent intent = new Intent(this,LoginCode1.class);
        startActivity(intent);
        AppManager.getAppManager().finishActivity(this);
	}
}
