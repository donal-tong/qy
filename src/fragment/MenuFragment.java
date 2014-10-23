package fragment;

import tools.Logger;
import tools.StringUtils;

import com.lidroid.xutils.ViewUtils;
import com.vikaa.wecontact.R;

import de.greenrobot.event.EventBus;
import event.MenuClickEvent;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MenuFragment extends Fragment implements OnClickListener{

	private TextView tvMessage;
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View localSlidingMenu = inflater.inflate(R.layout.left_menu, null);
		ViewUtils.inject(this, localSlidingMenu);
		tvMessage = (TextView) localSlidingMenu.findViewById(R.id.messageView);
		RelativeLayout btnHome = (RelativeLayout) localSlidingMenu.findViewById(R.id.rlPhonebook);
		btnHome.setOnClickListener(this);
		RelativeLayout btnMessage = (RelativeLayout) localSlidingMenu.findViewById(R.id.rlMessage);
		btnMessage.setOnClickListener(this);
		RelativeLayout btnActivity = (RelativeLayout) localSlidingMenu.findViewById(R.id.rlActivity);
		btnActivity.setOnClickListener(this);
		RelativeLayout btnCard = (RelativeLayout) localSlidingMenu.findViewById(R.id.rlCard);
		btnCard.setOnClickListener(this);
		return localSlidingMenu;
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
			EventBus.getDefault().post(new MenuClickEvent(0));
			break;

		case R.id.rlActivity:
			tvMessage.setVisibility(View.INVISIBLE);
			EventBus.getDefault().post(new MenuClickEvent(1));
			break;
			
		case R.id.rlCard:
			EventBus.getDefault().post(new MenuClickEvent(2));
			break;
			
		case R.id.rlMessage:
			EventBus.getDefault().post(new MenuClickEvent(3));
			break;
		}
	}
}
