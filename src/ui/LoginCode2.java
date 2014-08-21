package ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bean.CodeEntity;
import bean.Entity;
import bean.Result;
import bean.UserEntity;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.vikaa.mycontact.R;

import config.AppClient;
import config.AppClient.ClientCallback;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import tools.AppManager;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;

public class LoginCode2 extends AppActivity{
	
	private GetCodeReceiver getCodeReceiver;
	
	@ViewInject(R.id.edtPhone)
	private EditText edtPhone;
	
	@ViewInject(R.id.edtPassword)
	private EditText edtPassword;
	
	@ViewInject(R.id.edtCode)
	private EditText edtCode;
	
	private String mobile;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_code2);
		registerGetReceiver();
		ViewUtils.inject(this);
		edtCode.setEnabled(false);
		edtPassword.setEnabled(false);
	}
	
	@Override
	protected void onDestroy() {
		unregisterGetReceiver();
		super.onDestroy();
	}
	
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			AppManager.getAppManager().finishActivity(this);
			break;
		case R.id.btnGetCode:
			edtCode.setEnabled(true);
			edtPassword.setEnabled(true);
			checkPhoneToGetCode();
			break;
		case R.id.btnRegister:
			closeInput();
			register();
			break;
		}
	}
	
	private void checkPhoneToGetCode() {
		mobile = edtPhone.getEditableText().toString();
		if (!StringUtils.isMobileNO(mobile)) {
			WarningDialog("请输入正确的手机号码");
			return;
		}
		getVertifyCode(mobile);
	}
	
	private void getVertifyCode(final String mobile) {
		loadingPd = UIHelper.showProgress(this, "请稍后", "正在获取验证码...", true);
		AppClient.getVertifyCode(appContext, mobile, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				UIHelper.dismissProgress(loadingPd);
				CodeEntity code = (CodeEntity)data;
				switch (code.getError_code()) {
				case Result.RESULT_OK:
					break;
				default:
					UIHelper.ToastMessage(getApplicationContext(), code.getMessage(), Toast.LENGTH_SHORT);
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
				Logger.i(e);
			}
		});
	}
	
	private void register() {
		String code = edtCode.getEditableText().toString();
		if (StringUtils.empty(code)) {
			WarningDialog("请输入6位验证码");
			return;
		}
		Pattern regex = Pattern.compile("^([0-9]{6})$");
		Matcher matcher = regex.matcher(code);
		if (!matcher.find()) {
			WarningDialog("请输入6位验证码");
			return;
		}
		String password = edtPassword.getEditableText().toString();
		if (!(password.length()>=6&&password.length()<=32)) {
			WarningDialog("请输入6-32位英文或数字的密码");
			return;
		}
		vertifiedCode(matcher.group(1), password);
	}
	
	private void vertifiedCode(final String code, final String password) {
		loadingPd = UIHelper.showProgress(this, "请稍后", "正在验证...", true);
		AppClient.vertifiedCode(appContext, code, mobile, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				UIHelper.dismissProgress(loadingPd);
				UserEntity user = (UserEntity) data;
				switch (user.getError_code()) {
				case Result.RESULT_OK:
					appContext.saveLoginInfo(user);
//					enterIndex(user);
					setPassword(password, user);
					break;
				default:
					UIHelper.ToastMessage(LoginCode2.this, user.getMessage(), Toast.LENGTH_SHORT);
					break;
				}
			}
			
			@Override
			public void onFailure(String message) {
				UIHelper.dismissProgress(loadingPd);
				UIHelper.ToastMessage(LoginCode2.this, message, Toast.LENGTH_SHORT);
			}
			
			@Override
			public void onError(Exception e) {
				UIHelper.dismissProgress(loadingPd);
				Logger.i(e);
			}
		});
	}
	
	private void setPassword(String password, final UserEntity auser) {
		loadingPd = UIHelper.showProgress(this, "请稍后", "正在为您保存密码...", true);
        AppClient.setPassword(appContext, mobile, password, new AppClient.ClientCallback() {
            @Override
            public void onSuccess(Entity data) {
                UIHelper.dismissProgress(loadingPd);
                UserEntity user = (UserEntity) data;
                switch (user.getError_code()) {
                    case Result.RESULT_OK:
                    	Logger.i("aaaa");
                        enterIndex(auser);
                        break;
                    default:
                        UIHelper.ToastMessage(LoginCode2.this, user.getMessage(), Toast.LENGTH_SHORT);
                        break;
                }
            }

            @Override
            public void onFailure(String message) {
                UIHelper.dismissProgress(loadingPd);
                UIHelper.ToastMessage(LoginCode2.this, message, Toast.LENGTH_SHORT);
            }

            @Override
            public void onError(Exception e) {
                UIHelper.dismissProgress(loadingPd);
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
			Logger.i("aaaa");
		}
	}
	
//	private class NoLineClickSpan extends ClickableSpan { 
//	    String text;
//
//	    public NoLineClickSpan(String text) {
//	        super();
//	        this.text = text;
//	    }
//
//	    @Override
//	    public void updateDrawState(TextPaint ds) {
//	        ds.setColor(getResources().getColor(R.color.red));
//	        ds.setTextSize(30);
//	        ds.setUnderlineText(true);
//	    }
//
//		@Override
//		public void onClick(View arg0) {
//		}
//	}
	
	class GetCodeReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			String code = intent.getStringExtra("code");
			Logger.i(code);
			try {
				edtCode.setText(code);
			} catch (Exception e) {
				Logger.i(e);
			}
		}
	}
	
	private void registerGetReceiver() {
		getCodeReceiver =  new  GetCodeReceiver();
        IntentFilter postFilter = new IntentFilter();
        postFilter.addAction("get");
        registerReceiver(getCodeReceiver, postFilter);
	}
	
	private void unregisterGetReceiver() {
		unregisterReceiver(getCodeReceiver);
	}
}
