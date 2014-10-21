package ui;

import tools.AppManager;

import com.vikaa.contactactivityassitant.R;

import de.greenrobot.event.EventBus;
import event.NotificationEvent;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;

public class AddSometingActivity extends AppActivity implements OnClickListener{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_options);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		AppManager.getAppManager().finishActivity(this);
		return super.onTouchEvent(event);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnPhone:
			EventBus.getDefault().post(new NotificationEvent("1"));
			AppManager.getAppManager().finishActivity(this);
			break;

		case R.id.btnActivity:
			EventBus.getDefault().post(new NotificationEvent("2"));
			AppManager.getAppManager().finishActivity(this);
			break;
			
		case R.id.btnCard:
			EventBus.getDefault().post(new NotificationEvent("3"));
			AppManager.getAppManager().finishActivity(this);
			break;
		}
	}

}
