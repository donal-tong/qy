package ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import bean.Entity;
import bean.Result;
import bean.TopicEntity;
import bean.TopicListEntity;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.vikaa.contactactivityassitant.R;
import config.AppClient;
import config.AppClient.ClientCallback;
import config.CommonValue;
import tools.AppManager;
import tools.Logger;
import tools.UIHelper;
import ui.adapter.TopicListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by donal on 14-6-30.
 */
public class TopicTypeAll extends AppActivity implements AdapterView.OnItemClickListener, AbsListView.OnScrollListener, SwipeRefreshLayout.OnRefreshListener {
    private int currentPage;
    private SwipeRefreshLayout swipeLayout;
    private int lvDataState;
    private ListView lvTopic;

    private TopicListAdapter adapterTopic;
    private List<TopicEntity> listTopic = new ArrayList<TopicEntity>();

    private ImageView indicatorImageView;
    private Animation indicatorAnimation;

    private String catagoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.topic_type_all);
        catagoryId = getIntent().getStringExtra("catagory");
        initUI();
        getTopicListFromCache(catagoryId);
    }

    private void initUI() {
        indicatorImageView = (ImageView) findViewById(R.id.xindicator);
        indicatorAnimation = AnimationUtils.loadAnimation(this, R.anim.refresh_button_rotation);
        indicatorAnimation.setDuration(500);
        indicatorAnimation.setInterpolator(new Interpolator() {
            private final int frameCount = 10;
            @Override
            public float getInterpolation(float input) {
                return (float) Math.floor(input * frameCount) / frameCount;
            }
        });
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.xrefresh);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        lvTopic = (ListView) findViewById(R.id.lvTopic);
        adapterTopic = new TopicListAdapter(this, listTopic);
        lvTopic.setAdapter(adapterTopic);
        lvTopic.setOnScrollListener(this);
        lvTopic.setOnItemClickListener(this);
    }

    private void getTopicListFromCache(String id) {
        currentPage = 1;
        String key = String.format("%s-%s", CommonValue.CacheKey.TopicLists+id, appContext.getLoginUid());
        TopicListEntity entity = (TopicListEntity) appContext.readObject(key);
        if(entity != null){
            handleTopics(entity, UIHelper.LISTVIEW_ACTION_INIT);
        }
        getTopicList(id, currentPage+"", UIHelper.LISTVIEW_ACTION_INIT);
    }

    private void getTopicList(String id, String page, final int action) {
        if (null != indicatorImageView && listTopic.isEmpty()) {
            indicatorImageView.setVisibility(View.VISIBLE);
            indicatorImageView.startAnimation(indicatorAnimation);
        }
        AppClient.getTopicList(appContext, id, page, new ClientCallback() {

            @Override
            public void onSuccess(Entity data) {
                if (null != indicatorImageView) {
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
            }
        });
    }

    private void handleTopics(TopicListEntity entity, int action) {
        switch (action) {
            case UIHelper.LISTVIEW_ACTION_INIT:
            case UIHelper.LISTVIEW_ACTION_REFRESH:
                listTopic.clear();
                listTopic.addAll(entity.datas);
                break;

            default:
                listTopic.addAll(entity.datas);
                break;
        }
        if (entity.next >= 1) {
            lvDataState = UIHelper.LISTVIEW_DATA_MORE;
        } else if (entity.next == -1) {
            lvDataState = UIHelper.LISTVIEW_DATA_FULL;
        }
        swipeLayout.setRefreshing(false);
        adapterTopic.notifyDataSetChanged();
    }

    private void showTopic(TopicEntity entity) {
        EasyTracker easyTracker = EasyTracker.getInstance(this);
        easyTracker.send(MapBuilder
                        .createEvent("ui_action",     // Event category (required)
                                "button_press",  // Event action (required)
                                "查看话题：" + entity.url,   // Event label
                                null)            // Event value
                        .build()
        );
        Intent intent = new Intent(this, QYWebView.class);
        intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, entity.url);
        startActivityForResult(intent, CommonValue.PhonebookViewUrlRequest.editPhoneview);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        TopicEntity model = listTopic.get(position);
        showTopic(model);
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (lvDataState != UIHelper.LISTVIEW_DATA_MORE) {
            return;
        }
        if (firstVisibleItem + visibleItemCount >= totalItemCount && totalItemCount != 0) {
            lvDataState = UIHelper.LISTVIEW_DATA_LOADING;
            currentPage++;
            getTopicList(catagoryId, currentPage+"", UIHelper.LISTVIEW_ACTION_SCROLL);
        }
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

    @Override
    public void onRefresh() {
        if (lvDataState != UIHelper.LISTVIEW_DATA_LOADING) {
            lvDataState = UIHelper.LISTVIEW_DATA_LOADING;
            currentPage = 1;
            getTopicList(catagoryId, currentPage+"", UIHelper.LISTVIEW_ACTION_INIT);
        }
        else {
            swipeLayout.setRefreshing(false);
        }
    }
}
