package ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import bean.Entity;
import bean.Result;
import bean.UserEntity;
import com.vikaa.contactactivityassitant.R;
import config.AppClient;
import tools.AppManager;
import tools.StringUtils;
import tools.UIHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by donal on 14-7-28.
 */
public class LoginbyPassword extends AppActivity{

    private EditText codeET;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_password);
        initUI();
        phone = appContext.getLoginPhone();
    }

    private void initUI() {
        Button leftBarButton = (Button) findViewById(R.id.leftBarButton);
        accretionArea(leftBarButton);
        Button rightBarButton = (Button) findViewById(R.id.rightBarButton);
        accretionArea(rightBarButton);
        codeET = (EditText) findViewById(R.id.editTextCode);
    }

    public void ButtonClick(View v) {
        switch (v.getId()) {
            case R.id.leftBarButton:
                AppManager.getAppManager().finishActivity(this);
                break;
            case R.id.rightBarButton:
                closeInput();
                loginByPassword();
                break;
            case R.id.btnSms:
                setResult(RESULT_FIRST_USER);
                AppManager.getAppManager().finishActivity(this);
                break;
        }
    }

    private void loginByPassword() {
        String password = codeET.getEditableText().toString();
        if (StringUtils.empty(password)) {
            WarningDialog("请输入密码");
            return;
        }
        loadingPd = UIHelper.showProgress(this, null, null, true);
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
                        UIHelper.ToastMessage(LoginbyPassword.this, user.getMessage(), Toast.LENGTH_SHORT);
                        break;
                }
            }

            @Override
            public void onFailure(String message) {
                UIHelper.dismissProgress(loadingPd);
                UIHelper.ToastMessage(LoginbyPassword.this, message, Toast.LENGTH_SHORT);
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
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            setResult(RESULT_OK);
            AppManager.getAppManager().finishActivity(this);
        }
    }
}
