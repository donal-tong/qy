package ui;

import java.util.ArrayList;
import java.util.List;

import tools.AppManager;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;
import ui.adapter.PhonebookAdapter;

import bean.Entity;
import bean.PhoneIntroEntity;
import bean.RecommendListEntity;

import com.crashlytics.android.Crashlytics;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.vikaa.contactactivityassitant.R;

import config.AppClient;
import config.CommonValue;
import config.AppClient.ClientCallback;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;

public class RecommendActivity extends AppActivity implements OnRefreshListener{
	
	@ViewInject(R.id.xrefresh)
    private SwipeRefreshLayout srlRefresh;
    private int lvDataState;

    @ViewInject(R.id.xindicator)
    private ImageView indicatorImageView;
    
    private Animation indicatorAnimation;
    
	@ViewInject(R.id.xlistview)
	private ExpandableListView elvPhonebook;
	
	private List<PhoneIntroEntity> comQuns = new ArrayList<PhoneIntroEntity>();
	private List<List<PhoneIntroEntity>> quns = new ArrayList<List<PhoneIntroEntity>>();
	private PhonebookAdapter phoneAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recommend);
		ViewUtils.inject(this);
		
		srlRefresh.setOnRefreshListener(this);
        srlRefresh.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        elvPhonebook.setDividerHeight(0);
        elvPhonebook.setGroupIndicator(null);
        quns.add(comQuns);
        phoneAdapter = new PhonebookAdapter(this, quns);
        elvPhonebook.setAdapter(phoneAdapter);
        elvPhonebook.expandGroup(0);
        elvPhonebook.setOnGroupClickListener(new OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView arg0, View arg1, int arg2,
					long arg3) {
				return true;
			}
		});
        elvPhonebook.setOnChildClickListener(new OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView arg0, View convertView, int groupPosition, int childPosition, long arg4) {
				try {
					switch (groupPosition) {

					default:
						showPhonebook(quns.get(groupPosition).get(childPosition));
						break;
					}
				}
				catch (Exception e) {
					Crashlytics.logException(e);
				}
				return true;
			}
		});
        indicatorAnimation = AnimationUtils.loadAnimation(this, R.anim.refresh_button_rotation);
        indicatorAnimation.setDuration(500);
        indicatorAnimation.setInterpolator(new Interpolator() {
            private final int frameCount = 10;
            @Override
            public float getInterpolation(float input) {
                return (float)Math.floor(input*frameCount)/frameCount;
            }
        });
        getSquareListFromCache();
	}

	@Override
	public void onRefresh() {
		if (lvDataState == UIHelper.LISTVIEW_DATA_MORE) {
			lvDataState = UIHelper.LISTVIEW_DATA_LOADING;
			getSquareList(UIHelper.LISTVIEW_ACTION_REFRESH);
		}
		else {
			srlRefresh.setRefreshing(false);
		}
	}
	
	private void getSquareListFromCache() {
		String key = String.format("%s-%s", CommonValue.CacheKey.SquareList, appContext.getLoginUid());
		RecommendListEntity entity = (RecommendListEntity) appContext.readObject(key);
		if(entity != null){
			handlerSquare(entity, UIHelper.LISTVIEW_ACTION_INIT);
		}
		getSquareList(UIHelper.LISTVIEW_ACTION_INIT);
	}
	
	private void getSquareList(final int action) {
		if (null != indicatorImageView && comQuns.isEmpty()) {
            indicatorImageView.setVisibility(View.VISIBLE);
            indicatorImageView.startAnimation(indicatorAnimation);
        }
		AppClient.getPhoneSquareList(appContext, 1+"", "", new ClientCallback() {
			
			@Override
			public void onSuccess(Entity data) {
				if (null != indicatorImageView) {
                    indicatorImageView.setVisibility(View.INVISIBLE);
                    indicatorImageView.clearAnimation();
                }
				RecommendListEntity entity = (RecommendListEntity)data;
				handlerSquare(entity, action);
			}
			
			@Override
			public void onFailure(String message) {
				 if (null != indicatorImageView) {
	                    indicatorImageView.setVisibility(View.INVISIBLE);
	                    indicatorImageView.clearAnimation();
	                }
                srlRefresh.setRefreshing(false);
				UIHelper.ToastMessage(RecommendActivity.this, message, Toast.LENGTH_SHORT);
			}
			
			@Override
			public void onError(Exception e) {
				if (null != indicatorImageView) {
                    indicatorImageView.setVisibility(View.INVISIBLE);
                    indicatorImageView.clearAnimation();
                }
                srlRefresh.setRefreshing(false);
				Crashlytics.logException(e);
			}
		});
	}
	
	private void handlerSquare(RecommendListEntity entity, int action) {
		comQuns.clear();
		comQuns.addAll(entity.squares);
		phoneAdapter.notifyDataSetChanged();
		lvDataState = UIHelper.LISTVIEW_DATA_MORE;
		srlRefresh.setRefreshing(false);
	}
	
	private void showPhonebook(PhoneIntroEntity entity) {
        Logger.i(entity.link);
        if (StringUtils.empty(entity.link)) {
            return;
        }
		EasyTracker easyTracker = EasyTracker.getInstance(this);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看群友通讯录："+entity.link,   // Event label
	                   null)            // Event value
	      .build()
		);
		Intent intent = new Intent(this, QYWebView.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, entity.link);
	    startActivityForResult(intent, CommonValue.PhonebookViewUrlRequest.editPhoneview);
	}

	public void ButtonClick(View v) {
		AppManager.getAppManager().finishActivity(this);
	}
}
