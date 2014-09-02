package ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.LogRecord;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import tools.AppContext;
import tools.Logger;
import tools.UIHelper;
import ui.adapter.HotTopicAdapter;
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
import android.os.Handler;
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

public class HotTopicFragment extends Fragment implements OnItemClickListener, OnScrollListener, OnClickListener{
	private QunTopic activity;
	
	private ListView topicListView;

	private int lvDataState;
	private int currentPage;
	private List<TopicEntity> topicList = new ArrayList<TopicEntity>();
	private TopicListAdapter topicListAdapter;
	private String ids = "";

    private ImageView indicatorImageView;
    private Animation indicatorAnimation;

	public static HotTopicFragment newInstance() {
		HotTopicFragment fragment = new HotTopicFragment();
		return fragment;
	}
	
	@Override
	public void onAttach(Activity activity) {
		this.activity = (QunTopic) activity;
		super.onAttach(activity);
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        topicListAdapter = new TopicListAdapter(activity, topicList);
        Handler jumpHandler = new Handler();
        jumpHandler.postDelayed(new Runnable() {
            public void run() {
                getTopicListFromCache(ids);
            }
        }, 500);
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
    	topicListView.setAdapter(topicListAdapter);
    	topicListView.setOnScrollListener(this);
    	topicListView.setOnItemClickListener(this);
        topicListView.setVisibility(View.VISIBLE);
        return view;
    }

	private void getTopicListFromCache(String id) {
		currentPage = 1;
		String key = String.format("%s-%s", CommonValue.CacheKey.TopicLists+id, activity.appContext.getLoginUid());
		TopicListEntity entity = (TopicListEntity) activity.appContext.readObject(key);
		if(entity != null){
			handleTopics(entity, UIHelper.LISTVIEW_ACTION_INIT);
		}
		getTopicList(id, currentPage+"", UIHelper.LISTVIEW_ACTION_INIT);
	}
	
	private void getTopicList(String id, String page, final int action) {
        if (null != indicatorImageView && topicList.isEmpty()) {
            indicatorImageView.setVisibility(View.VISIBLE);
            indicatorImageView.startAnimation(indicatorAnimation);
        }

		AppClient.getTopicList(activity.appContext, id, page, new ClientCallback() {
			
			@Override
			public void onSuccess(Entity data) {
                if (null != indicatorImageView && topicList.isEmpty()) {
                    indicatorImageView.setVisibility(View.INVISIBLE);
                    indicatorImageView.clearAnimation();
                }
				TopicListEntity entity = (TopicListEntity) data;
				switch (entity.getError_code()) {
				case Result.RESULT_OK:
					handleTopics(entity, action);
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
	
	private void handleTopics(TopicListEntity entity, int action) {
		switch (action) {
		case UIHelper.LISTVIEW_ACTION_INIT:
		case UIHelper.LISTVIEW_ACTION_REFRESH:
			topicList.clear();
			topicList.addAll(entity.datas);
			break;

		default:
			topicList.addAll(entity.datas);
			break;
		}
		if(entity.next >= 1){
			lvDataState = UIHelper.LISTVIEW_DATA_MORE;
		}
		else if (entity.next == -1) {
			lvDataState = UIHelper.LISTVIEW_DATA_FULL;
		}
		topicListAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View convertView, int position, long arg3) {
        TopicEntity model = topicList.get(position);
        showTopic(model);
	}
	
	private void showTopic(TopicEntity entity) {
		EasyTracker easyTracker = EasyTracker.getInstance(activity);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看话题："+entity.url,   // Event label
	                   null)            // Event value
	      .build()
		);
		Intent intent = new Intent(activity, QYWebView.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, entity.url);
	    startActivityForResult(intent, CommonValue.PhonebookViewUrlRequest.editPhoneview);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (lvDataState != UIHelper.LISTVIEW_DATA_MORE) {
            return;
        }
        if (firstVisibleItem + visibleItemCount >= totalItemCount && totalItemCount != 0) {
        	lvDataState = UIHelper.LISTVIEW_DATA_LOADING;
        	currentPage++;
        	getTopicList(ids, currentPage+"", UIHelper.LISTVIEW_ACTION_SCROLL);
        }
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
