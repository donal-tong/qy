package ui;


import tools.AppManager;
import tools.Logger;

import com.viewpagerindicator.TabPageIndicator;
import com.vikaa.wecontact.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;

public class QunTopic extends AppActivity {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.qun_topic);
		initUI();
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			AppManager.getAppManager().finishActivity(this);
			break;

		default:
			break;
		}
	}
	
	private void initUI() {
		ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
		QunTopicFragmentAdapter adapter = new QunTopicFragmentAdapter(getSupportFragmentManager());
		viewPager.setAdapter(adapter);
		TabPageIndicator indicator = (TabPageIndicator)findViewById(R.id.indicator);
        indicator.setViewPager(viewPager);
        indicator.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				closeInput();
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				closeInput();
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				closeInput();
			}
		});
	}
	
	class QunTopicFragmentAdapter extends FragmentPagerAdapter{

		public QunTopicFragmentAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				return HotTopicFragment.newInstance();
			case 1:
				return CreateTopicFragment.newInstance();
			default:
				return HotTopicFragment.newInstance();
			}
			
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return "推荐";
			case 1:
				return "发布";
			default:
				return "";
			}
		}
		
	}
}
