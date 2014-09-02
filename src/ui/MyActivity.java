package ui;

import java.util.ArrayList;
import java.util.List;

import com.crashlytics.android.Crashlytics;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.vikaa.wecontact.R;

import config.AppClient;
import config.CommonValue;
import config.AppClient.ClientCallback;

import tools.AppManager;
import tools.UIHelper;
import ui.adapter.PhonebookAdapter;
import ui.adapter.PhonebookAdapter.CellHolder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import bean.ActivityListEntity;
import bean.Entity;
import bean.PhoneIntroEntity;
import bean.Result;

public class MyActivity extends AppActivity implements SwipeRefreshLayout.OnRefreshListener{

	private int lvDataState;
	private List<PhoneIntroEntity> myQuns = new ArrayList<PhoneIntroEntity>();
	private List<PhoneIntroEntity> comQuns = new ArrayList<PhoneIntroEntity>();
	private List<List<PhoneIntroEntity>> quns = new ArrayList<List<PhoneIntroEntity>>();
	private PhonebookAdapter phoneAdapter;
    private SwipeRefreshLayout swipeLayout;

    private ImageView indicatorImageView;
    private Animation indicatorAnimation;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.myactivity);
    	 IntentFilter filter = new IntentFilter();
 		filter.addAction(CommonValue.ACTIVITY_CREATE_ACTION);
 		filter.addAction(CommonValue.ACTIVITY_DELETE_ACTION);
 		registerReceiver(receiver, filter);
         quns.add(myQuns);
         quns.add(comQuns);
 		phoneAdapter = new PhonebookAdapter(this, quns);
 		initView();
         new Handler().postDelayed(new Runnable() {
             @Override
             public void run() {
                 getActivityListFromCache();
             }
         }, CommonValue.UI_DELAY);
    }
    
    @Override
    protected void onDestroy() {
    	unregisterReceiver(receiver);
    	super.onDestroy();
    }

    public void ButtonClick(View v) {
    	switch (v.getId()) {
		case R.id.leftBarButton:
			AppManager.getAppManager().finishActivity(this);
			break;

		case R.id.rightBarButton:
			startActivity(new Intent(this, CreateActivity.class));
			break;
		}
    }
    
    private void initView() {
    	swipeLayout = (SwipeRefreshLayout) findViewById(R.id.xrefresh);
    	ExpandableListView xlistView = (ExpandableListView) findViewById(R.id.xlistview);
        xlistView.setDividerHeight(0);
        xlistView.setGroupIndicator(null);
		xlistView.setAdapter(phoneAdapter);
		xlistView.expandGroup(0);
		xlistView.expandGroup(1);
		xlistView.setOnGroupClickListener(new OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView arg0, View arg1, int arg2,
					long arg3) {
				return true;
			}
		});
		xlistView.setOnChildClickListener(new OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView arg0, View convertView, int groupPosition, int childPosition, long arg4) {
				try {
					PhonebookAdapter.CellHolder cell = (CellHolder) convertView.getTag();
					PhoneIntroEntity model = (PhoneIntroEntity) cell.titleView.getTag();
					showPhonebook(model);
				}
				catch (Exception e) {
					Crashlytics.logException(e);
				}
				return true;
			}
		});
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        indicatorImageView = (ImageView) findViewById(R.id.xindicator);
        indicatorAnimation = AnimationUtils.loadAnimation(this, R.anim.refresh_button_rotation);
        indicatorAnimation.setDuration(500);
        indicatorAnimation.setInterpolator(new Interpolator() {
            private final int frameCount = 10;
            @Override
            public float getInterpolation(float input) {
                return (float)Math.floor(input*frameCount)/frameCount;
            }
        });
        if (null != indicatorImageView && myQuns.isEmpty()) {
            indicatorImageView.setVisibility(View.VISIBLE);
            indicatorImageView.startAnimation(indicatorAnimation);
        }
    }
    
    private void showPhonebook(PhoneIntroEntity entity) {
		EasyTracker easyTracker = EasyTracker.getInstance(this);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看活动："+entity.link,   // Event label
	                   null)            // Event value
	      .build()
		);
		Intent intent = new Intent(this, QYWebView.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, entity.link);
	    startActivityForResult(intent, CommonValue.PhonebookViewUrlRequest.editPhoneview);
    }
	

	@Override
	public void onRefresh() {
		if (lvDataState == UIHelper.LISTVIEW_DATA_MORE) {
            lvDataState = UIHelper.LISTVIEW_DATA_LOADING;
            getActivityList();
        }
        else {
            swipeLayout.setRefreshing(false);
        }
	}
	
	 private void getActivityListFromCache() {
			String key = String.format("%s-%s", CommonValue.CacheKey.ActivityList, appContext.getLoginUid());
			ActivityListEntity entity = (ActivityListEntity) appContext.readObject(key);
			if(entity != null){
				handlerActivitySection(entity);
			}
	        Handler jumpHandler = new Handler();
	        jumpHandler.postDelayed(new Runnable() {
	            public void run() {
	                getActivityList();
	            }
	        }, 1000);

		}
	 
	 private void getActivityList() {
//			if (myQuns.isEmpty()) {
//				activity.loadingPd = UIHelper.showProgress(activity, null, null, true);
//			}
	        if (null != indicatorImageView && myQuns.isEmpty()) {
	            indicatorImageView.setVisibility(View.VISIBLE);
	            indicatorImageView.startAnimation(indicatorAnimation);
	        }
			AppClient.getActivityList(appContext, new ClientCallback() {
				@Override
				public void onSuccess(Entity data) {
	                if (null != indicatorImageView) {
	                    indicatorImageView.setVisibility(View.INVISIBLE);
	                    indicatorImageView.clearAnimation();
	                }
					ActivityListEntity entity = (ActivityListEntity)data;
					switch (entity.getError_code()) {
					case Result.RESULT_OK:
						handlerActivitySection(entity);
						break;
					default:
						UIHelper.ToastMessage(MyActivity.this, entity.getMessage(), Toast.LENGTH_SHORT);
						break;
					}
				}

				@Override
				public void onFailure(String message) {
	                if (null != indicatorImageView) {
	                    indicatorImageView.setVisibility(View.INVISIBLE);
	                    indicatorImageView.clearAnimation();
	                }
					UIHelper.ToastMessage(MyActivity.this, message, Toast.LENGTH_SHORT);
				}
				@Override
				public void onError(Exception e) {
	                if (null != indicatorImageView) {
	                    indicatorImageView.setVisibility(View.INVISIBLE);
	                    indicatorImageView.clearAnimation();
	                }
					Crashlytics.logException(e);
				}
			});
		}
		
		private void handlerActivitySection(ActivityListEntity entity) {
			myQuns.clear();
			myQuns.addAll(entity.owned);
			myQuns.addAll(entity.joined);
			phoneAdapter.notifyDataSetChanged();
	        lvDataState = UIHelper.LISTVIEW_DATA_MORE;
	        if (null != swipeLayout) {
	            swipeLayout.setRefreshing(false);
	        }
	        if (null != indicatorImageView ) {
	            indicatorImageView.setVisibility(View.INVISIBLE);
	            indicatorImageView.clearAnimation();
	        }
		}
		
		private BroadcastReceiver receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if (CommonValue.ACTIVITY_CREATE_ACTION.equals(action) 
						|| CommonValue.ACTIVITY_DELETE_ACTION.equals(action)) {
					getActivityList();
				}
			}

		};
}
