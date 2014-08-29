package widget;

import tools.ImageUtils;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnClosedListener;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.vikaa.allcontact.R;

public class SlidingDrawerView implements OnClickListener{

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
//		Button btnHome = (Button) localSlidingMenu.findViewById(R.id.btnHome);
//		btnHome.setOnClickListener(this);
//		Button btnMessage = (Button) localSlidingMenu.findViewById(R.id.btnMessage);
//		btnMessage.setOnClickListener(this);
//		Button btnFriend = (Button) localSlidingMenu.findViewById(R.id.btnFriend);
//		btnFriend.setOnClickListener(this);
//		Button btnMe = (Button) localSlidingMenu.findViewById(R.id.btnMe);
//		btnMe.setOnClickListener(this);
//		Button btnContact = (Button) localSlidingMenu.findViewById(R.id.btnContact);
//		btnContact.setOnClickListener(this);
//		Button btnSetting = (Button) localSlidingMenu.findViewById(R.id.btnSetting);
//		btnSetting.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
//		switch (v.getId()) {
//		case R.id.btnHome:
//			localSlidingMenu.showContent();
//			break;
//        case R.id.btnContact:
//            activity.startActivity(new Intent(activity, ContactActivity.class));
//            break;
//		case R.id.btnSetting:
//			activity.startActivity(new Intent(activity, SettingActivity.class));
//			break;
//		default:
//			break;
//		}
	}
}
