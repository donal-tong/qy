package widget;

import tools.ImageUtils;
import tools.Logger;
import tools.StringUtils;
import ui.Me;
import ui.MyActivity;
import ui.QYWebView;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnClosedListener;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.vikaa.allcontact.R;

import config.AppClient;
import config.CommonValue;
import config.MyApplication;

public class SlidingDrawerView implements OnClickListener{

	private TextView tvMessage;
	private final Activity activity;
	SlidingMenu localSlidingMenu;
	
	public SlidingDrawerView(Activity activity) {
		this.activity = activity;
	}
	
	public SlidingMenu initSlidingMenu() {
		localSlidingMenu = new SlidingMenu(activity);
		localSlidingMenu.setMode(SlidingMenu.LEFT);
		localSlidingMenu.setTouchModeAbove(SlidingMenu.SLIDING_WINDOW);
//		localSlidingMenu.setShadowWidthRes(R.dimen.shadow_width);
//		localSlidingMenu.setShadowDrawable(R.drawable.shadow);
		localSlidingMenu.setBehindOffset(ImageUtils.getDisplayWidth(activity)-ImageUtils.dip2px(activity, 205));
		localSlidingMenu.setFadeDegree(0.35F);
		localSlidingMenu.attachToActivity(activity, SlidingMenu.RIGHT);
		localSlidingMenu.setMenu(R.layout.left_menu);
//		localSlidingMenu.toggle();
//		localSlidingMenu.setSecondaryMenu(R.layout.left_menu);
//		localSlidingMenu.setSecondaryShadowDrawable(R.drawable.shadowright);
		localSlidingMenu.setOnOpenedListener(new SlidingMenu.OnOpenedListener() {
					public void onOpened() {
						
					}
				});
		localSlidingMenu.setOnClosedListener(new OnClosedListener() {
			
			@Override
			public void onClosed() {
				// TODO Auto-generated method stub
				
			}
		});
		initListView();
		return localSlidingMenu;
	}
	
	private void initListView() {
		tvMessage = (TextView) localSlidingMenu.findViewById(R.id.messageView);
		RelativeLayout btnHome = (RelativeLayout) localSlidingMenu.findViewById(R.id.rlPhonebook);
		btnHome.setOnClickListener(this);
		RelativeLayout btnMessage = (RelativeLayout) localSlidingMenu.findViewById(R.id.rlMessage);
		btnMessage.setOnClickListener(this);
		RelativeLayout btnActivity = (RelativeLayout) localSlidingMenu.findViewById(R.id.rlActivity);
		btnActivity.setOnClickListener(this);
		RelativeLayout btnCard = (RelativeLayout) localSlidingMenu.findViewById(R.id.rlCard);
		btnCard.setOnClickListener(this);
	}
	
	public void setBadgeNumber(String number) {
		Logger.i(number);
		if (StringUtils.empty(number)) {
			tvMessage.setVisibility(View.INVISIBLE);
		}
		try {
			int i = Integer.valueOf(number);
			if (i == 0 ) {
				tvMessage.setVisibility(View.INVISIBLE);
			}
			else {
				tvMessage.setVisibility(View.VISIBLE);
				tvMessage.setText(number);
			}
		}
		catch (Exception e ) {
			tvMessage.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rlPhonebook:
			localSlidingMenu.showContent();
			break;
        case R.id.rlActivity:
            activity.startActivity(new Intent(activity, MyActivity.class));
            break;
		case R.id.rlCard:
			activity.startActivity(new Intent(activity, Me.class));
			break;
		case R.id.rlMessage:
			EasyTracker easyTracker = EasyTracker.getInstance(activity);
			easyTracker.send(MapBuilder
		      .createEvent("ui_action",     // Event category (required)
		                   "button_press",  // Event action (required)
		                   "查看通知："+String.format("%s/message/index", CommonValue.BASE_URL),   // Event label
		                   null)            // Event value
		      .build()
			);
			Intent intent = new Intent(activity, QYWebView.class);
			intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, String.format("%s/message/index", CommonValue.BASE_URL));
			activity.startActivity(intent);
			AppClient.setMessageRead(MyApplication.getInstance());
			tvMessage.setVisibility(View.INVISIBLE);
			break;
		}
	}
}
