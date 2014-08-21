package ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import bean.Entity;
import bean.Result;

import bean.UserEntity;
import com.crashlytics.android.Crashlytics;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.vikaa.mycontact.R;

import config.AppClient;
import config.AppClient.ClientCallback;
import config.CommonValue;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONObject;
import tools.AppManager;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginCode1 extends AppActivity{
	private boolean jumpRegister = false;
	
	@ViewInject(R.id.edtPhone)
	private EditText edtPhone;
	
	@ViewInject(R.id.edtPassword)
	private EditText edtPassword;
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (resultCode == RESULT_FIRST_USER) {
//            switch (requestCode) {
//                case CommonValue.LoginRequest.LoginByPassword:
//                    getVertifyCode(mobileET.getText().toString());
//                    break;
//                default:
//                    break;
//            }
//            return;
//        }
		if (resultCode != RESULT_OK) {
			return;
		}
		switch (requestCode) {
		case CommonValue.LoginRequest.LoginMobile:
        case CommonValue.LoginRequest.LoginByPassword:
			AppManager.getAppManager().finishActivity(this);
			break;
		case CommonValue.LoginRequest.LoginWechat:
		case CommonValue.LoginRequest.Register:
			AppManager.getAppManager().finishActivity(this);
			break;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_code1);
        IntentFilter filter = new IntentFilter();
        filter.addAction(CommonValue.ACTION_WECHAT_CODE);
        registerReceiver(receiver, filter);
        ViewUtils.inject(this);
        jumpRegister = getIntent().getBooleanExtra("register", false);
        if (jumpRegister) {
        	closeInput();
			startActivityForResult(new Intent(this, LoginCode2.class), CommonValue.LoginRequest.LoginMobile);
		}
	}

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.btnWechat:
			closeInput();
			this.wechat();
			break;
			
		case R.id.btnLogin:
			closeInput();
			loginByPassword();
			break;
			
		case R.id.tvRegister:
			closeInput();
			startActivityForResult(new Intent(this, LoginCode2.class), CommonValue.LoginRequest.LoginMobile);
			break;
		case R.id.tvForget:
			startActivityForResult(new Intent(this, ForgetPassword.class), CommonValue.LoginRequest.LoginMobile);
			break;
		}
	}
	
	private void loginByPassword() {
		String phone = edtPhone.getText().toString();
		String password = edtPassword.getText().toString();
		if (!StringUtils.isMobileNO(phone)) {
			WarningDialog("请输入正确的手机号码");
			return;
		}
		if (!(password.length()>=6&&password.length()<=32)) {
			WarningDialog("请输入6-32位英文或数字的密码");
			return;
		}
		loadingPd = UIHelper.showProgress(this, "请稍后", "正在验证...", true);
        AppClient.loginByPassword(appContext, phone, password, new AppClient.ClientCallback() {
            @Override
            public void onSuccess(Entity data) {
                UIHelper.dismissProgress(loadingPd);
                UserEntity user = (UserEntity) data;
                switch (user.getError_code()) {
                    case Result.RESULT_OK:
                        appContext.saveLoginInfo(user);
                        enterIndex(user);
                        break;
                    default:
                        UIHelper.ToastMessage(LoginCode1.this, user.getMessage(), Toast.LENGTH_SHORT);
                        break;
                }
            }

            @Override
            public void onFailure(String message) {
                UIHelper.dismissProgress(loadingPd);
                UIHelper.ToastMessage(LoginCode1.this, message, Toast.LENGTH_SHORT);
            }

            @Override
            public void onError(Exception e) {
                UIHelper.dismissProgress(loadingPd);
            }
        });
	}
	
	
	private void wechat() {
//		Intent intent = new Intent(LoginCode1.this, LoginWechat.class);
//		startActivityForResult(intent, CommonValue.LoginRequest.LoginWechat);
        if (!api.isWXAppInstalled()){
            WarningDialog("本机没有安装微信");
            return;
        }
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        api.sendReq(req);
	}
	
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (CommonValue.ACTION_WECHAT_CODE.equals(intent.getAction())) {
                closeInput();
                String code = intent.getStringExtra("code");
                loadingPd = UIHelper.showProgress(LoginCode1.this, "请稍后", "授权中...");
                AppClient.getAccessToken(code, CommonValue.APP_ID, CommonValue.SECRET, new AppClient.FileCallback() {
                    @Override
                    public void onSuccess(String filePath) {
                        UIHelper.dismissProgress(loadingPd);
                        try {
                            JSONObject json = new JSONObject(filePath);
                            String openid = json.getString("openid");
                            String accessToken = json.getString("access_token");
                            loginByWechat(openid, accessToken);
                        }
                        catch (Exception e) {
                            Crashlytics.logException(e);
                        }
                    }

                    @Override
                    public void onFailure(String message) {
                        UIHelper.dismissProgress(loadingPd);
                        WarningDialog("访问微信出错，请使用手机号码登录");
                    }

                    @Override
                    public void onError(Exception e) {
                        UIHelper.dismissProgress(loadingPd);
                        WarningDialog("访问微信出错，请使用手机号码登录");
                    }
                });
            }
        }
    };

    private void loginByWechat(String openid, String accessToken) {
        loadingPd = UIHelper.showProgress(this, "请稍后", "登录中...", true);
        AppClient.loginByWechat(appContext, openid, accessToken, new ClientCallback() {
            @Override
            public void onSuccess(Entity data) {
                UIHelper.dismissProgress(loadingPd);
                UserEntity user = (UserEntity) data;
                switch (user.getError_code()) {
                    case Result.RESULT_OK:
                    	UIHelper.ToastMessage(LoginCode1.this, "正在跳转中...", Toast.LENGTH_SHORT);
                        appContext.saveLoginInfo(user);
                        enterIndex(user);
                        break;
                    default:
                    	WarningDialog(user.getMessage());
                        break;
                }
            }

            @Override
            public void onFailure(String message) {
                UIHelper.dismissProgress(loadingPd);
                WarningDialog("访问微信出错，请使用手机号码登录");
            }

            @Override
            public void onError(Exception e) {
                UIHelper.dismissProgress(loadingPd);
                WarningDialog("访问微信出错，请使用手机号码登录");
                Logger.i(e);
            }
        });
    }

    private void enterIndex(UserEntity user) {
        String reg = "手机用户.*";
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(user.nickname);
        if (m.matches()) {
            Intent intent = new Intent(this, Register.class);
            intent.putExtra("mobile", user.username);
            intent.putExtra("jump", true);
            startActivity(intent);
            setResult(RESULT_OK);
            AppManager.getAppManager().finishActivity(this);
        }
        else {
            Intent intent = new Intent(this, Tabbar.class);
            startActivity(intent);
            setResult(RESULT_OK);
            AppManager.getAppManager().finishActivity(this);
        }
    }
}
