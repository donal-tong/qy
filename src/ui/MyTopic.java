package ui;

import java.util.ArrayList;
import java.util.List;

import tools.AppManager;
import tools.UIHelper;
import ui.adapter.MyCardAdapter;
import ui.adapter.TopicListAdapter;
import bean.Entity;
import bean.TopicEntity;
import bean.TopicListEntity;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.vikaa.wecontact.R;

import config.AppClient;
import config.CommonValue;
import config.AppClient.ClientCallback;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class MyTopic extends AppActivity implements OnRefreshListener, OnItemClickListener {
	private int lvDataState;
	private int currentPage;
	private List<TopicEntity> topicList = new ArrayList<TopicEntity>();
	private TopicListAdapter topicListAdapter;
	
	private ListView xListView;
	private SwipeRefreshLayout swipeLayout;
	private TextView emptyTV;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mytopic);
		initUI();
		getTopicListFromCache();
	}
	
	private void initUI() {
		emptyTV = (TextView) findViewById(R.id.tv_empty);
		swipeLayout = (SwipeRefreshLayout) findViewById(R.id.xrefresh);
		xListView = (ListView) findViewById(R.id.xlistview);
		topicListAdapter = new TopicListAdapter(this, topicList);
		xListView.setAdapter(topicListAdapter);
		xListView.setOnItemClickListener(this);
		swipeLayout.setOnRefreshListener(this);
	    swipeLayout.setColorScheme(android.R.color.holo_blue_bright, 
	            android.R.color.holo_green_light, 
	            android.R.color.holo_orange_light, 
	            android.R.color.holo_red_light);
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
	
	private void getTopicListFromCache() {
		currentPage = 1;
		String key = String.format("%s-%s", CommonValue.CacheKey.MyTopicLists, appContext.getLoginUid());
		TopicListEntity entity = (TopicListEntity) appContext.readObject(key);
		if(entity != null){
			handleTopics(entity, UIHelper.LISTVIEW_ACTION_INIT);
		}
		getMyTopicList(currentPage+"", UIHelper.LISTVIEW_ACTION_INIT);
	}
	
	private void getMyTopicList(String page, final int action) {
		AppClient.getMyTopicList(appContext, page, new ClientCallback() {
			
			@Override
			public void onSuccess(Entity data) {
				TopicListEntity entity = (TopicListEntity) data;
				handleTopics(entity, action);
			}
			
			@Override
			public void onFailure(String message) {
				
			}
			
			@Override
			public void onError(Exception e) {
				
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
		swipeLayout.setRefreshing(false);
		topicListAdapter.notifyDataSetChanged();
	}

	@Override
	public void onRefresh() {
		if (lvDataState != UIHelper.LISTVIEW_DATA_LOADING) {
			lvDataState = UIHelper.LISTVIEW_DATA_LOADING;
			currentPage = 1;
			getMyTopicList(currentPage+"", UIHelper.LISTVIEW_ACTION_REFRESH);
		}
		else {
			swipeLayout.setRefreshing(false);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		TopicEntity model = topicList.get(position);
		showTopic(model);
	}
	
	private void showTopic(TopicEntity entity) {
		EasyTracker easyTracker = EasyTracker.getInstance(this);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看话题："+entity.url,   // Event label
	                   null)            // Event value
	      .build()
		);
		Intent intent = new Intent(this, QYWebView.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, entity.url);
	    startActivityForResult(intent, CommonValue.PhonebookViewUrlRequest.editPhoneview);
	}
}
