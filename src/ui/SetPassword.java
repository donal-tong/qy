package ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import bean.Entity;
import bean.Result;
import bean.UserEntity;
import com.vikaa.contactactivityassitant.R;
import config.AppClient;
import tools.AppManager;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;

/**
 * Created by donal on 14-7-28.
 */
public class SetPassword extends AppActivity {

    private EditText edtPassword;
    private EditText edtPasswordEnsure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_password);
        initUI();
    }

    private void initUI() {
        Button leftBarButton = (Button) findViewById(R.id.leftBarButton);
        accretionArea(leftBarButton);
        Button rightBarButton = (Button) findViewById(R.id.rightBarButton);
        accretionArea(rightBarButton);
        edtPassword = (EditText) findViewById(R.id.editTextCode);
        edtPasswordEnsure = (EditText) findViewById(R.id.editTextCodeEnsure);
    }

    public void ButtonClick(View v) {
        switch (v.getId()) {
            case R.id.leftBarButton:
                AppManager.getAppManager().finishActivity(this);
                break;
            case R.id.rightBarButton:
                closeInput();
                submitPassword();
                break;
        }
    }

    private void submitPassword() {
        String password = edtPassword.getEditableText().toString();
        String passwordEnsure = edtPasswordEnsure.getEditableText().toString();
        if (StringUtils.empty(password) || StringUtils.empty(passwordEnsure)) {
            WarningDialog("请输入密码");
            return;
        }
        if (!password.equals(passwordEnsure)) {
            WarningDialog("两次输入的密码不一样");
            return;
        }
        if (StringUtils.empty(appContext.getLoginPhone())) {
			Logger.i("adsfsadf");
        	return;
		}
        Logger.i(appContext.getLoginPhone());
        loadingPd = UIHelper.showProgress(this, "请稍后", "正在为您保存密码...", true);
        AppClient.setPassword(appContext, appContext.getLoginPhone(), password, new AppClient.ClientCallback() {
            @Override
            public void onSuccess(Entity data) {
                UIHelper.dismissProgress(loadingPd);
                UserEntity user = (UserEntity) data;
                switch (user.getError_code()) {
                    case Result.RESULT_OK:
                        AppManager.getAppManager().finishActivity(SetPassword.this);
                        break;
                    default:
                        UIHelper.ToastMessage(SetPassword.this, user.getMessage(), Toast.LENGTH_SHORT);
                        break;
                }
            }

            @Override
            public void onFailure(String message) {
                UIHelper.dismissProgress(loadingPd);
                UIHelper.ToastMessage(SetPassword.this, message, Toast.LENGTH_SHORT);
            }

            @Override
            public void onError(Exception e) {
                UIHelper.dismissProgress(loadingPd);
            }
        });
    }
}
