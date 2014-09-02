package ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import bean.CardIntroEntity;
import bean.Entity;
import bean.Result;
import bean.UserEntity;

import com.crashlytics.android.Crashlytics;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.gson.Gson;
import com.vikaa.wecontact.R;

import config.AppClient;
import config.CommonValue;
import config.AppClient.ClientCallback;
import config.CommonValue.LianXiRenType;
import contact.MobileSynListBean;
import service.MobileSynService;
import tools.AppException;
import tools.AppManager;
import tools.DecodeUtil;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;
import ui.adapter.FriendCardAdapter;
import widget.MyLetterListView;
import widget.MyLetterListView.OnTouchingLetterChangedListener;

public class MobilePhone extends AppActivity implements OnItemClickListener {
//	private TextView messageView;
	
	private List<CardIntroEntity> mobiles = new ArrayList<CardIntroEntity>();
	
	private int lvDataState;
	private int currentPage;
	
	private List<CardIntroEntity> bilaterals = new ArrayList<CardIntroEntity>();
	
	private TextView nobilateralView;
	
	private ImageView indicatorImageView;
	private Animation indicatorAnimation;
	
	private View searchHeaderView;
	private EditText editText;
	private Button searchDeleteButton;
	
	private ListView xlistView;
	private List<CardIntroEntity> contactors = new ArrayList<CardIntroEntity>();
	private FriendCardAdapter mBilateralAdapter;
	
	private MyAsyncQueryHandler asyncQuery;
	private Uri uri ;
	private List<String> contactids;
	
	private static final int count = 200;
	
	private boolean isMobileAuthority= true;
	  
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		letterListView.setVisibility(View.VISIBLE);
		if (resultCode != RESULT_OK) {
			return;
		}
		switch (requestCode) {
		default:
			int position = data.getExtras().getInt("position");
			switch (position) {
			case 0:
				uploadMobile();
				break;

			case 1:
				downMobiles();
				break;
			}
			break;
		}
	}
	  
	private void uploadMobile() {
		loadingPd = UIHelper.showProgress(context, "请稍后...", "正在统计联系人信息", true);
		MobileSynService.actionStartPAY(this);
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("update")) {
				UIHelper.dismissProgress(loadingPd);
				MobileSynListBean model = (MobileSynListBean) intent.getExtras().getSerializable("mobile");
				Gson gson = new Gson();
				String json = gson.toJson(model.data);
				try {
					String encodeJson = DecodeUtil.encodeContact(json);
					syncMobile(encodeJson);
				} catch (AppException e) {
					Logger.i(e);
				}
			}
			else if (action.equals("refresh")) {
				UIHelper.dismissProgress(loadingPd);
				boolean needRefresh = intent.getExtras().getBoolean("refresh");
				if (needRefresh) {
					getAllFriend();
				}
			}
		}
	};
	
	private void syncMobile(String encodeJson) {
		loadingPd = UIHelper.showProgress(context, "请稍后...", "正在上传联系人信息到服务器", true);
		AppClient.syncContact(appContext, encodeJson, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				UIHelper.dismissProgress(loadingPd);
				WarningDialog("联系人信息安全备份成功");
			}
			
			@Override
			public void onFailure(String message) {
				UIHelper.dismissProgress(loadingPd);
			}
			
			@Override
			public void onError(Exception e) {
				Logger.i(e.toString());
				UIHelper.dismissProgress(loadingPd);
			}
	  });
	}
	
	private void downMobiles() {
		loadingPd = UIHelper.showProgress(context, "请稍后...", "正在从服务器下载联系人信息", true);
		AppClient.downContact(appContext, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				UIHelper.dismissProgress(loadingPd);
				final MobileSynListBean mo = (MobileSynListBean)data;
				if (mo.data.size()>0) {
					WarningDialog("此操作会覆盖您的手机通讯录且不能还原，确定覆盖吗？", "覆盖", "取消", new DialogClickListener() {
						
						@Override
						public void ok() {
							loadingPd = UIHelper.showProgress(context, "请稍后...", "正在对联系人进行备份操作");
							MobileSynService.actionStartDown(context, mo);
						}
						
						@Override
						public void cancel() {
							
						}
					});
					
				}
				else {
					WarningDialog("请先备份联系人信息");
				}
			}
			
			@Override
			public void onFailure(String message) {
				UIHelper.dismissProgress(loadingPd);
			}
			
			@Override
			public void onError(Exception e) {
				UIHelper.dismissProgress(loadingPd);
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(receiver);
		super.onDestroy();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mobilephone);
		asyncQuery = new MyAsyncQueryHandler(getContentResolver());
		uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
		initUI();
		getAllFriend();
		IntentFilter filter = new IntentFilter();
		filter.addAction("update");
		filter.addAction("refresh");
		registerReceiver(receiver, filter);
	}
	
	private void initUI() {
		overlay = (TextView) findViewById(R.id.fast_position);
		letterListView = (MyLetterListView) findViewById(R.id.ContactLetterListView);
		((TextView)findViewById(R.id.titleBarView)).setText("手机通讯录");
		nobilateralView = (TextView) findViewById(R.id.noting_view);
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
		letterListView.setOnTouchingLetterChangedListener(new LetterListViewListener());
		alphaIndexer = new HashMap<String, Integer>();
		
		xlistView = (ListView)findViewById(R.id.xlistview);
        xlistView.setDividerHeight(0);
//        xlistView.addHeaderView(searchHeaderView, null, false);
		mBilateralAdapter = new FriendCardAdapter(this, contactors);
		xlistView.setAdapter(mBilateralAdapter);
		xlistView.setOnItemClickListener(this);
	}
	
//	private void getFriendCardFromCache() {
//		asyncQuery.startQuery(0, null, uri, null, null, null, "sort_key COLLATE LOCALIZED asc"); 
//	}
	
//	private void checkLogin() {
//		loadingPd = UIHelper.showProgress(this, null, null, true);
//		AppClient.autoLogin(appContext, new ClientCallback() {
//			@Override
//			public void onSuccess(Entity data) {
//				UIHelper.dismissProgress(loadingPd);
//				UserEntity user = (UserEntity)data;
//				switch (user.getError_code()) {
//				case Result.RESULT_OK:
//					appContext.saveLoginInfo(user);
//					showReg(user);
//					getAllFriend();
////					if (!Utils.hasBind(getApplicationContext())) {
////						blindBaidu();
////					}
//					WebView webview = (WebView) findViewById(R.id.webview);
//					webview.loadUrl(CommonValue.BASE_URL + "/home/app" + "?_sign=" + appContext.getLoginSign())  ;
//					webview.setWebViewClient(new WebViewClient() {
//						public boolean shouldOverrideUrlLoading(WebView view, String url) {
//							view.loadUrl(url);
//							return true;
//						};
//					});
//					break;
//				case CommonValue.USER_NOT_IN_ERROR:
//					forceLogout();
//					break;
//				default:
//					UIHelper.ToastMessage(getApplicationContext(), user.getMessage(), Toast.LENGTH_SHORT);
//					break;
//				}
//			}
//			@Override
//			public void onFailure(String message) {
//				UIHelper.dismissProgress(loadingPd);
//				UIHelper.ToastMessage(getApplicationContext(), message, Toast.LENGTH_SHORT);
//			}
//			@Override
//			public void onError(Exception e) {
//				UIHelper.dismissProgress(loadingPd);
//				Logger.i(e);
//			}
//		});
//	}
	
//	private void showReg(UserEntity user) {
//		String reg = "手机用户.*";
//		Pattern p = Pattern.compile(reg);
//		Matcher m = p.matcher(user.nickname);
//		if (m.matches()) {
//			Intent intent = new Intent(this, Register.class);
//			intent.putExtra("mobile", user.username);
//			intent.putExtra("jump", false);
//	        startActivity(intent);
//		}
//	}
	
//	public void showMessage() {
//		Intent intent = new Intent(this, MessageView.class);
//		startActivity(intent);
//	}
	
//	private void blindBaidu() {
//		PushManager.startWork(getApplicationContext(),
//				PushConstants.LOGIN_TYPE_API_KEY, 
//				Utils.getMetaValue(this, "api_key"));
//	}
	
	private void getAllFriend() {
		asyncQuery.startQuery(0, null, uri, null, null, null, "sort_key COLLATE LOCALIZED asc");
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			AppManager.getAppManager().finishActivity(this);
			break;
		case R.id.rightBarButton:
			if (!isMobileAuthority) {
				WarningDialog();
				return;
			}
			startActivityForResult(new Intent(this, MobilePhoneMore.class), 1);
			break;
		}
	}
	
//	private String[] projection = {Data.MIMETYPE, Phone.NUMBER, "display_name", "contact_id", "sort_key", "photo_thumb_uri"};
//	private final static int MIMETYPE_INDEX = 0;
//	private final static int NUMBER_INDEX = 1;
//	private final static int NAME_INDEX = 2;
//	private final static int ID_INDEX = 3;
//	private final static int SORT_INDEX = 4;
//	private final static int PHOTO_INDEX = 5;
	private class MyAsyncQueryHandler extends AsyncQueryHandler {

		public MyAsyncQueryHandler(ContentResolver cr) {
			super(cr);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			handleCursor(cursor);
		}
	}
	
	private void handleCursor(final Cursor cursor) {
		final Handler handler1 = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 1) {
					isMobileAuthority = true;
					indicatorImageView.clearAnimation();
					indicatorImageView.setVisibility(View.INVISIBLE);
					contactors.addAll(mobiles);
					try {
						Collections.sort(contactors);
					} 
					catch(Exception e) {
						Crashlytics.logException(e);
					}
					mBilateralAdapter.notifyDataSetChanged();
					sortPY();
					letterListView.setVisibility(View.VISIBLE);
				}
				else if (msg.what == -1) {
					isMobileAuthority = false;
					indicatorImageView.clearAnimation();
					indicatorImageView.setVisibility(View.INVISIBLE);
					WarningDialog();
				}
				else {
					isMobileAuthority = true;
					indicatorImageView.clearAnimation();
					indicatorImageView.setVisibility(View.INVISIBLE);
					WarningDialog("手机通讯录还没有联系人哦");
				}
				updateMobileNum();
			}
		};
		indicatorImageView.setVisibility(View.VISIBLE);
    	indicatorImageView.startAnimation(indicatorAnimation);
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				if (cursor.getColumnCount() == 1) {
					handler1.sendEmptyMessage(-1);
					return;
				}
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					contactids = new ArrayList<String>();
					for (int i = 0; i < cursor.getCount(); i++) {
						cursor.moveToPosition(i);
						String mimetype = cursor.getString(cursor.getColumnIndex(Data.MIMETYPE));
						if (Phone.CONTENT_ITEM_TYPE.equals(mimetype)) {
							CardIntroEntity ce = new CardIntroEntity();
							ce.realname = cursor.getString(cursor.getColumnIndex("display_name"));
							ce.phone = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
							ce.code = ""+cursor.getInt(cursor.getColumnIndex("contact_id"));
							ce.pinyin = cursor.getString(cursor.getColumnIndex("sort_key"));
							ce.cardSectionType = LianXiRenType.mobile;
							ce.avatar = cursor.getString(cursor.getColumnIndex("photo_thumb_uri"));
							ce.department = "来自手机通讯录";
							ce.position = "";
							ce.py = StringUtils.getAlpha(ce.pinyin);
							if (!contactids.contains(ce.code)) {
								mobiles.add(ce);
								contactids.add(ce.code);
							}
						}
					}
					handler1.sendEmptyMessage(1);
				}
				else {
					handler1.sendEmptyMessage(2);
				}
				
			}
		});
	}
	
	private void sortPY() {
		alphaIndexer .clear();
		for (int i = 0; i < contactors.size(); i++) {
			String currentStr = contactors.get(i).py;
			String previewStr = (i - 1) >= 0 ? contactors.get(i - 1).py : " ";
			if (!previewStr.equals(currentStr)) {
				if (currentStr.equals("~")) {
					alphaIndexer.put("#", i);
				}
				else {
					alphaIndexer.put(currentStr, i);
				}
			}
		}
		alphaIndexer.put("搜", -1);
	}
	
	protected void WarningDialog() {
		String message = "请在手机的【设置】->【应用】->【群友通讯录】->底部的【权限管理】->【信任该程序】即可备份您的通讯录，下次再也不用担心通讯录丢失了。";
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage(message);
		builder.setTitle("【备份】温馨提示：");
		builder.setPositiveButton("确定", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
	   builder.create().show();
	}
	
	
	
	private TextView overlay;
	MyLetterListView letterListView = null;
	private HashMap<String, Integer> alphaIndexer;
	
	private class LetterListViewListener implements OnTouchingLetterChangedListener {
		@Override
		public void onTouchingLetterChanged(final String s) {
			if (alphaIndexer.get(s) != null) {
				int position = alphaIndexer.get(s);
				int xposition = (position);
				xlistView.setSelection(xposition);
			}
			overlay.setText(s);
			overlay.setVisibility(View.VISIBLE);
		}
		@Override
		public void onTouchingUp() {
			overlay.setVisibility(View.GONE);
		}
	}	
	
	@Override
	public void onItemClick(AdapterView<?> parent, View convertView, int position, long arg3) {
		CardIntroEntity model = (CardIntroEntity) parent.getAdapter().getItem(position);
		if (model.cardSectionType.equals(LianXiRenType.mobile)) {
			showMobileView(model);
		}
		else { 
//			showCardView(model);
		}
	}
	
//	private void showCardView(CardIntroEntity entity) {
//		EasyTracker easyTracker = EasyTracker.getInstance(this);
//		easyTracker.send(MapBuilder
//	      .createEvent("ui_action",     // Event category (required)
//	                   "button_press",  // Event action (required)
//	                   "查看名片："+entity.link,   // Event label
//	                   null)            // Event value
//	      .build()
//		);
//		Intent intent = new Intent(context, CardView.class);
//		intent.putExtra(CommonValue.CardViewIntentKeyValue.CardView, entity);
//		((MobilePhone)context).startActivityForResult(intent, CommonValue.CardViewUrlRequest.editCard);
//	}
	
	private void showMobileView(CardIntroEntity entity) {
		EasyTracker easyTracker = EasyTracker.getInstance(this);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看手机名片",   // Event label
	                   null)            // Event value
	      .build()
		);
		Intent intent = new Intent(context, MobileVIew.class);
		intent.putExtra(CommonValue.CardViewIntentKeyValue.CardView, entity);
		startActivity(intent);
	}
	
	private void updateMobileNum() {
		Intent intent = new Intent();
		intent.putExtra("mobileCount", contactors.size());
		intent.setAction("mobileCountUpdate");
		sendBroadcast(intent);
	}
}
