package ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import tools.AppContext;
import tools.Logger;
import tools.UIHelper;
import ui.adapter.TopicListAdapter;
import ui.adapter.TopicOptionAdapter;
import ui.adapter.TopicOptionAdapter.ViewHolder;
import bean.CardIntroEntity;
import bean.Entity;
import bean.FriendCardListEntity;
import bean.FunsEntity;
import bean.PhoneIntroEntity;
import bean.PhoneListEntity;
import bean.RecommendListEntity;
import bean.Result;
import bean.TopicEntity;
import bean.TopicListEntity;
import bean.TopicOptionEntity;
import bean.TopicOptionListEntity;

import com.crashlytics.android.Crashlytics;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.vikaa.wecontact.R;

import config.AppClient;
import config.CommonValue;
import config.AppClient.ClientCallback;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;

public class TopicFragment extends Fragment implements OnItemClickListener, OnScrollListener, OnClickListener{
	private Assistant activity;
	
	private ListView topicListView;
	
	private TopicOptionAdapter topicOptionAdapter;
	private List<TopicOptionEntity> topicTypes = new ArrayList<TopicOptionEntity>();
	
    private ImageView indicatorImageView;
    private Animation indicatorAnimation;

	public static TopicFragment newInstance() {
		TopicFragment fragment = new TopicFragment();
		return fragment;
	}
	
	@Override
	public void onAttach(Activity activity) {
		this.activity = (Assistant) activity;
		super.onAttach(activity);
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        topicOptionAdapter = new TopicOptionAdapter(activity, topicTypes);
        Handler jumpHandler = new Handler();
        jumpHandler.postDelayed(new Runnable() {
            public void run() {
                getTopicTypesFromCache();
            }
        }, 1000);
    }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View view = inflater.inflate(R.layout.topic_fragment, container, false);
        indicatorImageView = (ImageView) view.findViewById(R.id.xindicator);
        indicatorAnimation = AnimationUtils.loadAnimation(activity, R.anim.refresh_button_rotation);
        indicatorAnimation.setDuration(500);
        indicatorAnimation.setInterpolator(new Interpolator() {
            private final int frameCount = 10;
            @Override
            public float getInterpolation(float input) {
                return (float)Math.floor(input*frameCount)/frameCount;
            }
        });
    	topicListView = (ListView) view.findViewById(R.id.topiclistview);
    	View listHeader = inflater.inflate(R.layout.topic_list_header, null);
    	ImageView imgAvatar = (ImageView) listHeader.findViewById(R.id.avatarImageView);
    	ImageLoader.getInstance().displayImage(activity.appContext.getUserAvatar(), imgAvatar, CommonValue.DisplayOptions.avatar_options);
    	Button btnMyTopic = (Button) listHeader.findViewById(R.id.myTopicButton);
    	Button btnPubTopic = (Button) listHeader.findViewById(R.id.pubTopicButton);
    	btnMyTopic.setOnClickListener(this);
    	btnPubTopic.setOnClickListener(this);
    	topicListView.addHeaderView(listHeader);
    	topicListView.setAdapter(topicOptionAdapter);
    	topicListView.setOnScrollListener(this);
    	topicListView.setOnItemClickListener(this);
        return view;
    }
	
	private void getTopicTypesFromCache() {
		String key = String.format("%s-%s", CommonValue.CacheKey.TopicTypes, activity.appContext.getLoginUid());
		TopicOptionListEntity entity = (TopicOptionListEntity) activity.appContext.readObject(key);
		if(entity != null){
			handleTopicTypes(entity);
		}
        if (null != indicatorImageView && topicTypes.isEmpty()) {
            indicatorImageView.setVisibility(View.VISIBLE);
            indicatorImageView.startAnimation(indicatorAnimation);
        }
		AppClient.getTopicTypes(activity.appContext, new ClientCallback() {
			
			@Override
			public void onSuccess(Entity data) {
                if (null != indicatorImageView) {
                    indicatorImageView.setVisibility(View.INVISIBLE);
                    indicatorImageView.clearAnimation();
                }
				TopicOptionListEntity entity = (TopicOptionListEntity)data;
				switch (entity.getError_code()) {
				case Result.RESULT_OK:
					handleTopicTypes(entity);
					break;
				default:
					break;
				}
			}
			
			@Override
			public void onFailure(String message) {
                if (null != indicatorImageView) {
                    indicatorImageView.setVisibility(View.INVISIBLE);
                    indicatorImageView.clearAnimation();
                }
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
	
	private void handleTopicTypes(TopicOptionListEntity entity) {
		topicTypes.clear();
		topicTypes.addAll(entity.options);
		topicOptionAdapter.notifyDataSetChanged();
	}
	

	@Override
	public void onItemClick(AdapterView<?> parent, View convertView, int position, long arg3) {
        TopicOptionEntity entity = topicTypes.get(position-1);
        showTopicType(entity);
	}
	
	private void showTopicType(TopicOptionEntity entity) {
		EasyTracker easyTracker = EasyTracker.getInstance(activity);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看话题："+entity.title,   // Event label
	                   null)            // Event value
	      .build()
		);
	    startActivity(new Intent(activity, TopicTypeAll.class).putExtra("catagory", entity.category_id));
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int arg1) {
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.myTopicButton:
			activity.startActivity(new Intent(activity, MyTopic.class));
			break;
		case R.id.pubTopicButton:
			activity.startActivity(new Intent(activity, CreateTopic.class).putExtra("fun", new FunsEntity()));
			break;
		default:
			break;
		}
	}
}
