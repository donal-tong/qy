package ui;

import tools.AppManager;

import com.vikaa.mycontact.R;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;

public class AddSomethingGuideActviity extends AppActivity implements OnClickListener{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide_addsomething);
	}
	
	@Override
	public void onClick(View arg0) {
		appContext.saveGuide1();
		AppManager.getAppManager().finishActivity(this);
	}

}
