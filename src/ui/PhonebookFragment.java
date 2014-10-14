package ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.CookieStore;

import tools.AppManager;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;
import ui.adapter.PhonebookAdapter;
import bean.Entity;
import bean.PhoneIntroEntity;
import bean.PhoneListEntity;
import bean.RecommendListEntity;
import bean.Result;

import com.crashlytics.android.Crashlytics;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.loopj.android.http.PersistentCookieStore;
import com.vikaa.mycontact.R;

import config.AppClient;
import config.CommonValue;
import config.AppClient.ClientCallback;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;

public class PhonebookFragment extends Fragment implements OnRefreshListener, OnClickListener{
	
	private MainActivity activity;
	
	@ViewInject(R.id.elvPhonebook)
	private ExpandableListView elvPhonebook;
	
	private List<PhoneIntroEntity> myQuns = new ArrayList<PhoneIntroEntity>();
	private List<PhoneIntroEntity> ownedQuns = new ArrayList<PhoneIntroEntity>();
	private List<PhoneIntroEntity> joinedQuns = new ArrayList<PhoneIntroEntity>();
	private List<PhoneIntroEntity> comQuns = new ArrayList<PhoneIntroEntity>();
	private List<List<PhoneIntroEntity>> quns = new ArrayList<List<PhoneIntroEntity>>();
	private PhonebookAdapter phoneAdapter;
	
	
	@ViewInject(R.id.searchEditView)
	private EditText editText;
	@ViewInject(R.id.navbar)
	private RelativeLayout navbar;
	
	private int mobileNum = 0;

	@ViewInject(R.id.srlRefresh)
    private SwipeRefreshLayout srlRefresh;
    private int lvDataState;

    @ViewInject(R.id.xindicator)
    private ImageView indicatorImageView;
    
    private Animation indicatorAnimation;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		IntentFilter filter = new IntentFilter();
		filter.addAction(CommonValue.PHONEBOOK_CREATE_ACTION);
		filter.addAction(CommonValue.PHONEBOOK_DELETE_ACTION);
		filter.addAction("mobileCountUpdate");
		activity.registerReceiver(receiver, filter);
		phoneAdapter = new PhonebookAdapter(activity, quns);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
            	addFixSection();
                getPhoneListFromCache();
//                getSquareListFromCache();
            }
        }, CommonValue.UI_DELAY);
	}
	
	@Override
	public void onDestroy() {
		activity.unregisterReceiver(receiver);
		super.onDestroy();
	}
	
	@Override
    public void onAttach(Activity activity) {
    	this.activity = (MainActivity) activity;
    	super.onAttach(activity);
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_phonebook, container, false);
		ViewUtils.inject(this, view);
		editText.setOnClickListener(this);
		navbar.setOnClickListener(this);
        srlRefresh.setOnRefreshListener(this);
        srlRefresh.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        elvPhonebook.setDividerHeight(0);
        elvPhonebook.setGroupIndicator(null);
        quns.add(myQuns);
        quns.add(ownedQuns);
        quns.add(joinedQuns);
        quns.add(comQuns);
        elvPhonebook.setAdapter(phoneAdapter);
        elvPhonebook.expandGroup(0);
        elvPhonebook.expandGroup(1);
        elvPhonebook.expandGroup(2);
        elvPhonebook.expandGroup(3);
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
					case 0:
						switch (childPosition) {
						case 0:
							showFamily();
							break;

						case 1:
							showFriend();
							break;
							
						case 2:
							showMobile();
							break;
						}
						break;

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
        indicatorAnimation = AnimationUtils.loadAnimation(activity, R.anim.refresh_button_rotation);
        indicatorAnimation.setDuration(500);
        indicatorAnimation.setInterpolator(new Interpolator() {
            private final int frameCount = 10;
            @Override
            public float getInterpolation(float input) {
                return (float)Math.floor(input*frameCount)/frameCount;
            }
        });
        if (null != indicatorImageView) {
            indicatorImageView.setVisibility(View.VISIBLE);
            indicatorImageView.startAnimation(indicatorAnimation);
        }
		return view;
	}

	private void getPhoneListFromCache() {
		String key = String.format("%s-%s", CommonValue.CacheKey.PhoneList, activity.appContext.getLoginUid());
		PhoneListEntity entity = (PhoneListEntity) activity.appContext.readObject(key);
		if(entity != null){
			handlerPhoneSection(entity);
		}
		getPhoneList();
	}
	
	private void getPhoneList() {
        if (null != indicatorImageView && myQuns.isEmpty()) {
            indicatorImageView.setVisibility(View.VISIBLE);
            indicatorImageView.startAnimation(indicatorAnimation);
        }
		AppClient.getPhoneList(activity.appContext, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
                if (null != indicatorImageView) {
                    indicatorImageView.setVisibility(View.INVISIBLE);
                    indicatorImageView.clearAnimation();
                }
				PhoneListEntity entity = (PhoneListEntity)data;
				switch (entity.getError_code()) {
				case Result.RESULT_OK:
					handlerPhoneSection(entity);
					break;
				case CommonValue.USER_NOT_IN_ERROR:
					getActivity().sendBroadcast(new Intent(CommonValue.RELOGIN_ACTION));
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
                srlRefresh.setRefreshing(false);
				UIHelper.ToastMessage(activity, message, Toast.LENGTH_SHORT);
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
	
	private void handlerPhoneSection(PhoneListEntity entity) {
		ownedQuns.clear();
		joinedQuns.clear();
		ownedQuns.addAll(entity.owned);
		joinedQuns.addAll(entity.joined);
		phoneAdapter.notifyDataSetChanged();
        srlRefresh.setRefreshing(false);
        lvDataState = UIHelper.LISTVIEW_DATA_FULL;
        if (null != indicatorImageView) {
            indicatorImageView.setVisibility(View.INVISIBLE);
            indicatorImageView.clearAnimation();
        }
	}
	
	private void addFixSection() {
		PhoneIntroEntity family = new PhoneIntroEntity();
		family.title = "家族通讯录";
		family.subtitle ="亲情无间，家族宗亲按谱排序";
		family.logo = "drawable://" + R.drawable.family_phone_icon;
		family.phoneSectionType = CommonValue.PhoneSectionType.MobileSectionType;
		myQuns.add(family);
		PhoneIntroEntity friend = new PhoneIntroEntity();
		friend.title = "名片好友";
		friend.subtitle ="与我交换过名片的好友";
		friend.logo = "drawable://" + R.drawable.wefriend_phone_icon;
		friend.phoneSectionType = CommonValue.PhoneSectionType.MobileSectionType;
		myQuns.add(friend);
		PhoneIntroEntity mobile = new PhoneIntroEntity();
		mobile.title = "同步手机通讯录";
		mobile.phoneSectionType = CommonValue.PhoneSectionType.MobileSectionType;
		mobile.subtitle ="手机通讯录同步备份";
		mobile.logo = "drawable://"+R.drawable.mobile_phone_icon;
		myQuns.add(mobile);
	}
	
	public void forceLogout() {
		UIHelper.ToastMessage(activity, "用户未登录,1秒后重新进入登录界面", Toast.LENGTH_SHORT);
		Handler jumpHandler = new Handler();
        jumpHandler.postDelayed(new Runnable() {
			public void run() {
				AppClient.Logout(activity.appContext);
				CookieStore cookieStore = new PersistentCookieStore(activity);  
				cookieStore.clear();
				AppManager.getAppManager().finishAllActivity();
				activity.appContext.setUserLogout();
				Intent intent = new Intent(activity, LoginCode1.class);
				startActivity(intent);
			}
		}, 1000);
	}
	
	private void getSquareListFromCache() {
		String key = String.format("%s-%s", CommonValue.CacheKey.SquareList, activity.appContext.getLoginUid());
		RecommendListEntity entity = (RecommendListEntity) activity.appContext.readObject(key);
		if(entity != null){
			handlerSquare(entity, UIHelper.LISTVIEW_ACTION_INIT);
		}
		getSquareList(UIHelper.LISTVIEW_ACTION_INIT);
	}
	
	private void getSquareList(final int action) {
		AppClient.getPhoneSquareList(activity.appContext, 1+"", "", new ClientCallback() {
			
			@Override
			public void onSuccess(Entity data) {
				RecommendListEntity entity = (RecommendListEntity)data;
				handlerSquare(entity, action);
			}
			
			@Override
			public void onFailure(String message) {
			}
			
			@Override
			public void onError(Exception e) {
			}
		});
	}
	
	private void handlerSquare(RecommendListEntity entity, int action) {
		comQuns.clear();
		comQuns.addAll(entity.squares);
		phoneAdapter.notifyDataSetChanged();
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (CommonValue.PHONEBOOK_CREATE_ACTION.equals(action) 
					|| CommonValue.PHONEBOOK_DELETE_ACTION.equals(action)) {
				getPhoneList();
			}
//			else if (action.equals("mobileCountUpdate")) {
//				int count = intent.getExtras().getInt("mobileCount");
//				myQuns.get(0).subtitle = "共"+count+"位好友";
//				phoneAdapter.notifyDataSetChanged();
//				mobileNum = count;
//				editText.setHint("您共有"+(Integer.valueOf(appContext.getDeg2()) + count)+"位二度人脉可搜索");
//			}
		}
	};

	@Override
	public void onRefresh() {
		if (lvDataState != UIHelper.LISTVIEW_DATA_LOADING) {
            lvDataState = UIHelper.LISTVIEW_DATA_LOADING;
            getPhoneList();
        }
        else {
            srlRefresh.setRefreshing(false);
        }
	}
	
	private void showMobile() {
		EasyTracker easyTracker = EasyTracker.getInstance(activity);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看手机通讯录",   // Event label
	                   null)            // Event value
	      .build()
		);
		Intent intent = new Intent(activity, MobilePhone.class);
		startActivity(intent);
	}
	
	private void showFriend() {
		EasyTracker easyTracker = EasyTracker.getInstance(activity);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看微友通讯录",   // Event label
	                   null)            // Event value
	      .build()
		);
		Intent intent = new Intent(activity, WeFriendCard.class);
		startActivity(intent);
	}
	
	private void showFamily() {
		EasyTracker easyTracker = EasyTracker.getInstance(activity);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看家族通讯录",   // Event label
	                   null)            // Event value
	      .build()
		);
		Intent intent = new Intent(activity, FamilyPhonebook.class);
		startActivity(intent);
	}
	
	private void showPhonebook(PhoneIntroEntity entity) {
        Logger.i(entity.link);
        if (StringUtils.empty(entity.link)) {
            return;
        }
		EasyTracker easyTracker = EasyTracker.getInstance(activity);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看群友通讯录："+entity.link,   // Event label
	                   null)            // Event value
	      .build()
		);
		Intent intent = new Intent(activity, QYWebView.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, entity.link);
	    startActivityForResult(intent, CommonValue.PhonebookViewUrlRequest.editPhoneview);
	}

	@Override
	public void onClick(View arg0) {
		Intent intent = new Intent(activity, WeFriendCardSearch.class);
		intent.putExtra("mobileNum", mobileNum);
        startActivityForResult(intent, 12);
	}
}
