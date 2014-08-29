package ui;


import com.viewpagerindicator.TabPageIndicator;
import com.vikaa.allcontact.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

public class Assistant extends AppActivity {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.assistant);
		initUI();
	}
	
	private void initUI() {
		ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
		AssistantFragmentAdapter adapter = new AssistantFragmentAdapter(getSupportFragmentManager());
		viewPager.setAdapter(adapter);
		
		TabPageIndicator indicator = (TabPageIndicator)findViewById(R.id.indicator);
        indicator.setViewPager(viewPager);
        
        viewPager.setCurrentItem(1);
	}
	
	class AssistantFragmentAdapter extends FragmentPagerAdapter{

		public AssistantFragmentAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				return ActivityFragment.newInstance();
			case 1:
				return TopicFragment.newInstance();
			default:
				return ActivityFragment.newInstance();
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
				return "活动";
			case 1:
				return "话题";
			default:
				return "";
			}
		}
	}
}
