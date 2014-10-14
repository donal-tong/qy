package ui;

import tools.AppManager;
import tools.Logger;
import tools.UIHelper;

import bean.Entity;
import bean.Result;
import bean.UserEntity;

import com.crashlytics.android.Crashlytics;
import com.vikaa.mycontact.R;

import config.AppClient;
import config.AppClient.ClientCallback;
import config.CommonValue;
import de.greenrobot.event.EventBus;
import event.NotificationEvent;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ExeclSync extends AppActivity{
	private String code;
	private TextView shortUrlTV;
	
	private String keyCode;
	
	protected void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.execlsync);
		code = getIntent().getExtras().getString("code");
		shortUrlTV = (TextView) findViewById(R.id.shortUrlTV);
		getShortUrl();
		EventBus.getDefault().register(this);
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
	
	protected void SMSDialog(final int type) {
		try {
			AlertDialog.Builder builder = new Builder(context);
			builder.setMessage("发送短信通知好友?\n建议一次发送不超过50条短信");
			builder.setTitle("提示");
			builder.setPositiveButton("确认", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					showSMS(type);
				}
			});

		   builder.setNegativeButton("取消", new OnClickListener() {
			   @Override
			   public void onClick(DialogInterface dialog, int which) {
				   dialog.dismiss();
			   }
		   });
		   builder.create().show();
		} catch (Exception e) {
			Crashlytics.logException(e);
		}
	}
	
	private void showSMS(int type) {
		Intent intent = new Intent(this,PhonebookSMS.class);
		intent.putExtra(CommonValue.PhonebookViewIntentKeyValue.SMS, keyCode);
		intent.putExtra("type", type);
        startActivityForResult(intent, CommonValue.PhonebookViewIntentKeyValue.SMSRequest);
	}
	
	public void onEventMainThread(NotificationEvent event) {
    	keyCode = event.getMsg();
    	SMSDialog(1);
    }
}
