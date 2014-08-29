package ui;

import tools.AppManager;
import tools.Logger;
import tools.UIHelper;

import bean.Entity;
import bean.Result;
import bean.UserEntity;

import com.kuyue.openchat.api.WmOpenChatSdk;
import com.vikaa.allcontact.R;

import config.AppClient;
import config.AppClient.ClientCallback;
import config.CommonValue;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ExeclSync extends AppActivity{
	private String code;
	private TextView shortUrlTV;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.execlsync);
		code = getIntent().getExtras().getString("code");
		shortUrlTV = (TextView) findViewById(R.id.shortUrlTV);
		getShortUrl();
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			AppManager.getAppManager().finishActivity(this);
			break;
		}
	}
	
	private void getShortUrl(){
		loadingPd = UIHelper.showProgress(this, "请稍后", "正在获取短链接...", true);
		AppClient.fileSync(appContext, code, new ClientCallback() {
			
			@Override
			public void onSuccess(Entity data) {
				UIHelper.dismissProgress(loadingPd);
				UserEntity user = (UserEntity)data;
				switch (user.getError_code()) {
				case Result.RESULT_OK:
					shortUrlTV.setText(user.headimgurl);
					break;
				default:
					break;
				}
			}
			
			@Override
			public void onFailure(String message) {
				UIHelper.dismissProgress(loadingPd);
				WarningDialog(message);
			}
			
			@Override
			public void onError(Exception e) {
				UIHelper.dismissProgress(loadingPd);
			}
		});
	}
}
