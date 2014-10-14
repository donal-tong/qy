package ui;


import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.readystatesoftware.viewbadger.BadgeView;
import com.vikaa.mycontact.R;

import config.AppClient;
import config.CommonValue;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

public class DiscoverFragment extends Fragment implements OnClickListener{

	@ViewInject(R.id.btn_download)
	private RelativeLayout btnDownload;
	
	@ViewInject(R.id.btn_recommend)
	private RelativeLayout btnRecommend;
	
	@ViewInject(R.id.btn_topic)
	private RelativeLayout btnTopic;
	
	@ViewInject(R.id.btn_fileshare)
	private RelativeLayout btnFileShare;
	
	@ViewInject(R.id.badgeRecommend)
	private BadgeView badgeRecommend;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_discover, container, false);
		ViewUtils.inject(this, view);
		btnDownload.setOnClickListener(this);
		btnRecommend.setOnClickListener(this);
		btnTopic.setOnClickListener(this);
		btnFileShare.setOnClickListener(this);
		badgeRecommend.setVisibility(View.INVISIBLE);
		return view;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_download:
			
			break;

		case R.id.btn_recommend:
			startActivity(new Intent(getActivity(), RecommendActivity.class));
			
			break;
			
		case R.id.btn_topic:
			startActivity(new Intent(getActivity(), QunTopic.class));
			break;
			
		case R.id.btn_fileshare:
			EasyTracker easyTracker = EasyTracker.getInstance(getActivity());
			easyTracker.send(MapBuilder
		      .createEvent("ui_action",     // Event category (required)
		                   "button_press",  // Event action (required)
		                   "查看通知："+String.format("%s/message/index", CommonValue.BASE_URL),   // Event label
		                   null)            // Event value
		      .build()
			);
			Intent intent = new Intent(getActivity(), QYWebView.class);
			intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, String.format("%s/docs/index", CommonValue.BASE_URL));
			startActivity(intent);
			break;
		}
	}
}
