package ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.widget.*;
import baidupush.Utils;
import bean.*;
import service.AddMobileService;
import tools.AppException;
import tools.AppManager;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;
import ui.adapter.CardViewAdapter;

import com.crashlytics.android.Crashlytics;
import com.vikaa.wecontact.R;

import config.AppClient;
import config.AppClient.ClientCallback;
import config.CommonValue;
import db.manager.RelationshipManager;
import db.manager.WeFriendManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.AdapterView.OnItemClickListener;

public class CardView extends AppActivity implements OnItemClickListener  {
	private CardIntroEntity card;
	private ImageView avatarImageView;
	private TextView titleView;
	private TextView nameView;
	private ImageView imgV;
//	private List<RelationshipEntity> relationships = new ArrayList<RelationshipEntity>();
	private List<KeyValue> summarys = new ArrayList<KeyValue>();
	private ListView mListView;
	private CardViewAdapter mCardViewAdapter;
	
	private Button callMobileButton;
	private Button saveMobileButton;
//	private Button editMyMobileButton;
    private LinearLayout layoutSelf;
	private Button exchangeButton;
	private TextView exchangeView;
	
	private MobileReceiver mobileReceiver;
	
	private ImageView indicatorImageView;
	private Animation indicatorAnimation;
	
	private boolean cardAuthority;
	
	private String INTRO_TEXT ;
	
	private String wmid;
	
	@Override
	protected void onDestroy() {
//		EasyTracker.getInstance(this).activityStop(this);
		unregisterGetReceiver();
		super.onDestroy();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		EasyTracker.getInstance(this).activityStart(this);  
		setContentView(R.layout.card_view);
		cardAuthority = false;
		registerGetReceiver();
		initUI();
		initData();
	}
	
	private void getWMId(String openid) {
		AppClient.queryWMIdByOpenid(appContext, openid, new ClientCallback() {
			
			@Override
			public void onSuccess(Entity data) {
				UserEntity user = (UserEntity)data;
				switch (user.getError_code()) {
				case Result.RESULT_OK:
					Logger.i(user.openid);
					wmid = user.openid;
					break;
				default:
					break;
				}
			}
			
			@Override
			public void onFailure(String message) {
				
			}
			
			@Override
			public void onError(Exception e) {
				
			}
		});
	}
	
	private void initUI() {
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
		LayoutInflater inflater = LayoutInflater.from(this);
		View header = inflater.inflate(R.layout.card_view_header, null);
		avatarImageView = (ImageView) header.findViewById(R.id.avatarImageView);
		nameView = (TextView) header.findViewById(R.id.name);
		titleView = (TextView) header.findViewById(R.id.title);
		imgV = (ImageView) header.findViewById(R.id.imgV);
		View footer = inflater.inflate(R.layout.card_view_footer, null);
		callMobileButton = (Button) footer.findViewById(R.id.callContactButton);
		saveMobileButton = (Button) footer.findViewById(R.id.saveContactButton);
//		editMyMobileButton = (Button) footer.findViewById(R.id.editMyMobile);
        layoutSelf = (LinearLayout) footer.findViewById(R.id.selfLayout);
		exchangeButton = (Button) footer.findViewById(R.id.exchangeMobile); 
		exchangeView = (TextView) footer.findViewById(R.id.exchangeView);
		mListView = (ListView) findViewById(R.id.listView);
		mListView.addHeaderView(header, null, false);
		mListView.addFooterView(footer, null, false);
		mListView.setDividerHeight(0);
		mCardViewAdapter = new CardViewAdapter(this, summarys);
		mListView.setAdapter(mCardViewAdapter);
		mListView.setOnItemClickListener(this);
	}
	
	private void initData() {
		card = (CardIntroEntity) getIntent().getSerializableExtra(CommonValue.CardViewIntentKeyValue.CardView);
		getWMId(card.openid);
		CardIntroEntity data = WeFriendManager.getInstance(this).getCardByOpenid(card.openid);
		if ( data != null ) {
			data.re.addAll(RelationshipManager.getInstance(this).getRelationships(card.openid));
			setData(data);
		}
		else {
			card.re.addAll(RelationshipManager.getInstance(this).getRelationships(card.openid));
			setData(card);
		}
		getCard(card.code);
	}
	
	private void setData(CardIntroEntity entity) {
		if (this.isFinishing()) {
			return;
		}
		card = entity;
		dbUpdate(card);
		for (RelationshipEntity re : entity.re) {
			if (re.openid.equals(appContext.getLoginUid())) {
				cardAuthority = true;
				break;
			}
		}
		imageLoader.displayImage(entity.avatar, avatarImageView, CommonValue.DisplayOptions.avatar_options);
		nameView.setText(entity.realname);
		titleView.setText(entity.department +" " +entity.position);
		if (StringUtils.notEmpty(entity.certified)) {
			if (entity.certified.equals("1")) {
				imgV.setVisibility(View.VISIBLE);
			}
			else {
				imgV.setVisibility(View.INVISIBLE);
			}
		}
		summarys.clear();
		if (StringUtils.notEmpty(entity.wechat)) {
			KeyValue value = new KeyValue();
			value.key = "微信号";
			value.value = (entity.isfriend.equals(CommonValue.PhonebookLimitRight.Frined_Yes)||cardAuthority)?entity.wechat : "*******(交换名片可见)";
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.email)) {
			KeyValue value = new KeyValue();
			value.key = "邮箱";
			value.value = (entity.isfriend.equals(CommonValue.PhonebookLimitRight.Frined_Yes)||cardAuthority)?entity.email : "*******(交换名片可见)";
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.phone)) {
			KeyValue value = new KeyValue();
			value.key = "手机";
			value.value = (entity.isfriend.equals(CommonValue.PhonebookLimitRight.Frined_Yes)||cardAuthority)?entity.phone : "*******(交换名片可见)";
			if (!(entity.isfriend.equals(CommonValue.PhonebookLimitRight.Frined_Yes)||cardAuthority)) {
				value.relations.addAll(entity.re);
				if (entity.isfriend.equals(CommonValue.PhonebookLimitRight.Friend_No)) {
					RelationshipEntity r = new RelationshipEntity();
					r.realname = "直接和他交互名片";
					r.title = "";
					r.type = "";
					value.relations.add(r);
				}
			}
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.birthday)) {
            Logger.i(entity.birthday);
            if (!entity.birthday.equals("0") && !entity.birthday.equals("1970-01-01")) {
                KeyValue value = new KeyValue();
                value.key = "生日";
                value.value = entity.birthday;
                summarys.add(value);
            }
		}
		if (StringUtils.notEmpty(entity.address)) {
			KeyValue value = new KeyValue();
			value.key = "地址";
			value.value = entity.address;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.intro)) {
			KeyValue value = new KeyValue();
			value.key = "个人介绍";
			value.value = entity.intro;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.supply)) {
			KeyValue value = new KeyValue();
			value.key = "供需关系";
			value.value = entity.supply;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.needs)) {
			KeyValue value = new KeyValue();
			value.key = "需求关系";
			value.value = entity.needs;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.hometown)) {
			KeyValue value = new KeyValue();
			value.key = "籍贯";
			value.value = entity.hometown;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.interest)) {
			KeyValue value = new KeyValue();
			value.key = "兴趣";
			value.value = entity.interest;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.school)) {
			KeyValue value = new KeyValue();
			value.key = "学校";
			value.value = entity.school;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.homepage)) {
			KeyValue value = new KeyValue();
			value.key = "个人主页";
			value.value = entity.homepage;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.company_site)) {
			KeyValue value = new KeyValue();
			value.key = "公司网站";
			value.value = entity.company_site;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.qq)) {
			KeyValue value = new KeyValue();
			value.key = "QQ";
			value.value = entity.qq;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.weibo)) {
			KeyValue value = new KeyValue();
			value.key = "新浪微博";
			value.value = entity.weibo;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.tencent)) {
			KeyValue value = new KeyValue();
			value.key = "腾讯微博";
			value.value = entity.tencent;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.renren)) {
			KeyValue value = new KeyValue();
			value.key = "人人";
			value.value = entity.renren;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.zhihu)) {
			KeyValue value = new KeyValue();
			value.key = "知乎";
			value.value = entity.zhihu;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.qzone)) {
			KeyValue value = new KeyValue();
			value.key = "QQ空间";
			value.value = entity.qzone;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.facebook)) {
			KeyValue value = new KeyValue();
			value.key = "FACEBOOK";
			value.value = entity.facebook;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.twitter)) {
			KeyValue value = new KeyValue();
			value.key = "Twitter";
			value.value = entity.twitter;
			summarys.add(value);
		}
		if (StringUtils.notEmpty(entity.intentionen)) {
			KeyValue value = new KeyValue();
			value.key = "希望接受的名片";
			value.value = entity.intentionen;
			summarys.add(value);
		}
		mCardViewAdapter.notifyDataSetChanged();
		
		//second listview
//		Logger.i(entity.re.size()+"");
//		relations.addAll(entity.re);
//		relationAdapter.notifyDataSetChanged();
//		setListViewHeightBasedOnChildren(fListView);
		if (StringUtils.notEmpty(entity.openid)) {
			if (entity.openid.equals(appContext.getLoginUid())) {
				saveMobileButton.setVisibility(View.GONE);
				layoutSelf.setVisibility(View.VISIBLE);
			}
			else {
                layoutSelf.setVisibility(View.GONE);
				if (StringUtils.notEmpty(entity.isfriend)) {
					if (entity.isfriend.equals(CommonValue.PhonebookLimitRight.Friend_No)) {
						exchangeButton.setVisibility(View.VISIBLE);
						callMobileButton.setVisibility(View.GONE);
						saveMobileButton.setVisibility(View.GONE);
					}
					else if (entity.isfriend.equals(CommonValue.PhonebookLimitRight.Friend_Wait)) {
						exchangeButton.setVisibility(View.GONE);
						exchangeView.setVisibility(View.VISIBLE);
						callMobileButton.setVisibility(View.GONE);
						saveMobileButton.setVisibility(View.GONE);
					}
					else {
						callMobileButton.setVisibility(View.VISIBLE);
						saveMobileButton.setVisibility(View.VISIBLE);
					}
				}
			}
		}
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			AppManager.getAppManager().finishActivity(this);
			break;
		case R.id.shareFriendButton:
			cardSharePre(false, null, card);
			break;
//		case R.id.shareTimelineButton:
//			showShare(false, WechatMoments.NAME);
//			break;
		case R.id.saveContactButton:
			addContact(card);
			break;
		case R.id.editMyMobile:
			String url1 = String.format("%s/card/setting/id/%s", CommonValue.BASE_URL, card.code);
			showCreate(url1, CommonValue.CardViewUrlRequest.editCard);
			break;
        case R.id.deleteMyMobile:
            deleteMycard();
            break;
		case R.id.exchangeMobile:
			exchangeCard(card);
			break;
		case R.id.callContactButton:
//			WmOpenChatSdk.getInstance().gotoChatPage(wmid, this);
			callMobile(card.phone);
			break;
		case R.id.lookupContactButton:
			String url2 = String.format("%s/card/%s", CommonValue.BASE_URL, card.code);
			showCreate(url2, CommonValue.CardViewUrlRequest.editCard);
			break;
		}
	}

    private void deleteMycard() {
        loadingPd = UIHelper.showProgress(this, null, null, true);
        AppClient.deleteCard(appContext, card.code, new ClientCallback() {
            @Override
            public void onSuccess(Entity data) {
                UIHelper.dismissProgress(loadingPd);
                UserEntity user = (UserEntity) data;
                switch (user.getError_code()) {
                    case Result.RESULT_OK:
                        UIHelper.ToastMessage(CardView.this, "删除成功", Toast.LENGTH_SHORT);
                        Intent intent = new Intent();
                        intent.setAction(CommonValue.CARD_CREATE_ACTION);
                        intent.setAction(CommonValue.CARD_DELETE_ACTION);
                        sendBroadcast(intent);
                        AppManager.getAppManager().finishActivity(CardView.this);
                        break;
                    default:
                        UIHelper.ToastMessage(CardView.this, user.getMessage(), Toast.LENGTH_SHORT);
                        break;
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

	private void deleteAndSaveRelationships(final List<RelationshipEntity> list) {
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				RelationshipManager.getInstance(context).deleteRelationships(card.openid);
				RelationshipManager.getInstance(context).saveRelationships(list, card.openid);
			}
		});
	}
	
	private void getCard(String code) {
		if (!appContext.isNetworkConnected()) {
			return;
		}
//		loadingPd = UIHelper.showProgress(this, null, null, true);
		indicatorImageView.setVisibility(View.VISIBLE);
    	indicatorImageView.startAnimation(indicatorAnimation);
		AppClient.getCard(appContext, code, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
//				UIHelper.dismissProgress(loadingPd);
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
				CardIntroEntity entity = (CardIntroEntity)data;
				switch (entity.getError_code()) {
				case Result.RESULT_OK:
					deleteAndSaveRelationships(entity.re);
					setData(entity);
					break;
				default:
					UIHelper.ToastMessage(getApplicationContext(), entity.getMessage(), Toast.LENGTH_SHORT);
					break;
				}
			}
			
			@Override
			public void onFailure(String message) {
//				UIHelper.dismissProgress(loadingPd);
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
				UIHelper.ToastMessage(getApplicationContext(), message, Toast.LENGTH_SHORT);
			}
			@Override
			public void onError(Exception e) {
//				UIHelper.dismissProgress(loadingPd);
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
				((AppException)e).makeToast(getApplicationContext());
			}
		});
	}
	
	private void dbUpdate(final CardIntroEntity entity) {
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				WeFriendManager.getInstance(CardView.this).updateWeFriend(entity);
			}
		});
	}

	public void addContact(CardIntroEntity entity){
		loadingPd = UIHelper.showProgress(this, null, null, true);
		AddMobileService.actionStartPAY(this, entity, true);
    }
	
	class MobileReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			UIHelper.dismissProgress(loadingPd);
			int type = intent.getIntExtra(CommonValue.ContactOperationResult.ContactOperationResultType, CommonValue.ContactOperationResult.SAVE_FAILURE);
			String message = "";
			switch (type) {
			case CommonValue.ContactOperationResult.EXIST:
				message = "名片已保存了";
				WarningDialog(message);
				break;
			case CommonValue.ContactOperationResult.SAVE_FAILURE:
				message = "保存名片失败";
				WarningDialog(message);
				break;
			case CommonValue.ContactOperationResult.SAVE_SUCCESS:
				message = "保存名片成功";
				WarningDialog(message);
				break;
			case CommonValue.ContactOperationResult.NOT_AUTHORITY:
				message = "请在手机的【设置】->【应用】->【群友通讯录】->底部的【权限管理】->【信任该程序】即可备份您的通讯录，下次再也不用担心通讯录丢失了。";
				WarningDialog(message);
				break;
			}
		}
	}
	
	private void registerGetReceiver() {
		mobileReceiver =  new  MobileReceiver();
        IntentFilter postFilter = new IntentFilter();
        postFilter.addAction(CommonValue.ContactOperationResult.ContactBCAction);
        registerReceiver(mobileReceiver, postFilter);
	}
	
	private void unregisterGetReceiver() {
		unregisterReceiver(mobileReceiver);
	}
	
	private void showCreate(String url, int RequestCode) {
		Intent intent = new Intent(this,QYWebView.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, url);
        startActivityForResult(intent, RequestCode);
	}
	
	private void exchangeCard(final CardIntroEntity model) {
//		loadingPd = UIHelper.showProgress(this, null, null, true);
		indicatorImageView.setVisibility(View.VISIBLE);
    	indicatorImageView.startAnimation(indicatorAnimation);
		AppClient.followCard(appContext, model.openid, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
//				UIHelper.dismissProgress(loadingPd);
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
				switch (data.getError_code()) {
				case Result.RESULT_OK:
					model.isfriend = CommonValue.PhonebookLimitRight.Friend_Wait;
					exchangeButton.setVisibility(View.GONE);
					exchangeView.setVisibility(View.VISIBLE);
					break;
				default:
					UIHelper.ToastMessage(getApplicationContext(), data.getMessage(), Toast.LENGTH_SHORT);
					break;
				}
			}
			@Override
			public void onFailure(String message) {
//				UIHelper.dismissProgress(loadingPd);
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
				UIHelper.ToastMessage(getApplicationContext(), message, Toast.LENGTH_SHORT);
			}
			@Override
			public void onError(Exception e) {
//				UIHelper.dismissProgress(loadingPd);
				indicatorImageView.clearAnimation();
				indicatorImageView.setVisibility(View.INVISIBLE);
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		position = position - 1;
		if (position>=0 && position <summarys.size()) {
			KeyValue model = summarys.get(position);
			if(model.key.equals("手机") && !model.value.contains("*")) {
				if (!card.openid.equals(appContext.getLoginUid())) {
					showContactDialog(model);
				}
			}
			else if (model.key.equals("微信号")) {
				RelationshipEntity entity = new RelationshipEntity();
				entity.realname = card.realname;
				entity.wechat = model.value;
				showIntroWechatOptionDialog(entity);
			}
		}
	}
	
	private String[] lianxiren1 = new String[] { "拨打电话", "发送短信"};
	private void showContactDialog(final KeyValue model){
		if(!model.key.equals("手机") || model.value.contains("*")) {
			return;
		}
		new AlertDialog.Builder(this).setTitle("").setItems(lianxiren1,
				new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				switch(which){
				case 0:
					callMobile(model.value);
					break;
				case 1:
					sendSMS(model.value, "");
					break;
				}
			}
		}).show();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		switch (requestCode) {
		case CommonValue.CardViewUrlRequest.editCard:
			getCard(card.code);
			setResult(RESULT_OK);
			break;
		}
	}
	
	public void showIntroOption(RelationshipEntity model) {
		if (StringUtils.empty(model.type)) {
			exchangeCard(card);
			return;
		}
		if (StringUtils.notEmpty(model.wechat) && StringUtils.notEmpty(model.phone)) {
			showIntroOptionDialog(model);
			return;
		}
		if (StringUtils.notEmpty(model.phone)) {
			showIntroPhoneOptionDialog(model);
			return;
		}
		if (StringUtils.notEmpty(model.wechat)) {
			showIntroWechatOptionDialog(model);
			return;
		}
	}
	private String[] IntroOption = new String[] { "拨打电话", "发送短信", "加微信好友"};
	private void showIntroOptionDialog(final RelationshipEntity model){
		new AlertDialog.Builder(this).setTitle(model.realname).setItems(IntroOption,
				new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				switch(which){
				case 0:
					callMobile(model.phone);
					break;
				case 1:
					INTRO_TEXT = "你好，我想认识"+ card.realname+",麻烦你帮我引见介绍,谢谢";
					sendSMS(model.phone, INTRO_TEXT);
					break;
				case 2:
					try{
						WarningDialogAndOpenWechat(model.wechat, "微信号已保存到剪切板,需要打开微信吗？");
					}catch (Exception e) {
						Crashlytics.logException(e);
					}
					break;
				}
			}
		}).show();
	}
	
	private String[] IntroPhoneOption = new String[] { "拨打电话", "发送短信"};
	private void showIntroPhoneOptionDialog(final RelationshipEntity model){
		new AlertDialog.Builder(this).setTitle(model.realname).setItems(IntroPhoneOption,
				new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				switch(which){
				case 0:
					callMobile(model.phone);
					break;
				case 1:
					INTRO_TEXT = "你好，我想认识"+ card.realname+",麻烦你帮我引见介绍,谢谢";
					sendSMS(model.phone, INTRO_TEXT);
					break;
				}
			}
		}).show();
	}
	
	private String[] IntroWechatOption = new String[] { "加微信好友"};
	private void showIntroWechatOptionDialog(final RelationshipEntity model){
		new AlertDialog.Builder(this).setTitle(model.realname).setItems(IntroWechatOption,
				new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				switch(which){
				case 0:
					try{
						WarningDialogAndOpenWechat(model.wechat, "微信号已保存到剪切板,需要打开微信吗？");
					}catch (Exception e) {
						Crashlytics.logException(e);
					}
					break;
				}
			}
		}).show();
	}
}
