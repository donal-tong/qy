package ui;

import tools.AppManager;

import com.vikaa.allcontact.R;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class PCTIP extends AppActivity  {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pc_tip);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		AppManager.getAppManager().finishActivity(this);
		return super.onTouchEvent(event);
	}
	
	
}
