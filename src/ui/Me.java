package ui;

import java.util.ArrayList;
import java.util.List;

import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;
import tools.UpdateManager;
import ui.adapter.MeCardAdapter;
import bean.CardIntroEntity;
import bean.CardListEntity;
import bean.Entity;
import bean.Result;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.zxing.client.android.CaptureActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.vikaa.allcontact.R;

import config.AppClient;
import config.CommonValue;
import config.AppClient.ClientCallback;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnGroupClickListener;

public class Me extends AppActivity{
	private ExpandableListView iphoneTreeView;
	private List<List<CardIntroEntity>> cards;
	private MeCardAdapter mCardAdapter;
	
	private ImageView avatarView;
	private TextView nameTV;
	private TextView creditTV;
	private DisplayImageOptions avatar_options = new DisplayImageOptions.Builder()
	.bitmapConfig(Bitmap.Config.RGB_565)
	.cacheInMemory(true)
	.cacheOnDisc(true)
	.imageScaleType(ImageScaleType.EXACTLY_STRETCHED) 
	.displayer(new RoundedBitmapDisplayer(10))
	.build();
	
//	private TitlePopup titlePopup;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.me);
		initUI();
		addCardOp();
		mCardAdapter.notifyDataSetChanged();
		expandView();
		this.imageLoader.displayImage(appContext.getUserAvatar(), avatarView, avatar_options);
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.rightBarButton:
			startActivityForResult(new Intent(this, MoreDialog.class), 1);
			break;
		}
	}
	
	private void initUI() {
		LayoutInflater inflater = LayoutInflater.from(this);
		View footer = inflater.inflate(R.layout.index_footer, null);
		iphoneTreeView = (ExpandableListView) findViewById(R.id.iphone_tree_view);
		iphoneTreeView.setGroupIndicator(null);
		iphoneTreeView.addFooterView(footer);
		View header = inflater.inflate(R.layout.more_headerview, null);
		avatarView = (ImageView) header.findViewById(R.id.avatar);
		nameTV = (TextView) header.findViewById(R.id.title);
		creditTV = (TextView) header.findViewById(R.id.jifen);
		nameTV.setText(appContext.getNickname());
		creditTV.setText("点击修改头像");
		iphoneTreeView.addHeaderView(header);
		header.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if (StringUtils.notEmpty(appContext.getUserAvatarCode())) {
					Intent intent = new Intent(Me.this, UploadAvatar.class);
					intent.putExtra("code", appContext.getUserAvatarCode());
					intent.putExtra("token", "");
					intent.putExtra("avatar", appContext.getUserAvatar());
					intent.putExtra("sign", appContext.getLoginSign());
					startActivityForResult(intent, CommonValue.CreateViewJSType.showUploadAvatar);
				}
			}
		});
		cards = new ArrayList<List<CardIntroEntity>>();
		mCardAdapter = new MeCardAdapter(iphoneTreeView, this, cards);
		iphoneTreeView.setAdapter(mCardAdapter);
		iphoneTreeView.setSelection(0);
		iphoneTreeView.setOnGroupClickListener(new OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView arg0, View arg1, int position,
					long arg3) {
				return true;
			}
		});
	}
	
	private void expandView() {
		for (int i = 0; i < cards.size(); i++) {
			iphoneTreeView.expandGroup(i);
		}
	}
	
	private void getCardListFromCache() {
		String key = String.format("%s-%s", CommonValue.CacheKey.CardList, appContext.getLoginUid());
		CardListEntity entity = (CardListEntity) appContext.readObject(key);
		if(entity == null){
			
			getCardList();
			return;
		}
		cards.clear();
		if (entity.owned.size()>0) {
			cards.add(entity.owned);
		}
		mCardAdapter.notifyDataSetChanged();
		expandView();
		getCardList();
	}
	
	private void addCardOp() {
		List<CardIntroEntity> ops = new ArrayList<CardIntroEntity>();
		CardIntroEntity op0 = new CardIntroEntity();
		op0.realname = "我的名片";
		op0.cardSectionType = CommonValue.CardSectionType .BarcodeSectionType;
		op0.department = R.drawable.icon_set_card+"";
		op0.position = "";
		ops.add(op0);
		
		CardIntroEntity op2 = new CardIntroEntity();
		op2.realname = "扫一扫";
		op2.cardSectionType = CommonValue.CardSectionType .BarcodeSectionType;
		op2.position = "";
		op2.department = R.drawable.icon_set_ocr+"";
		ops.add(op2);
		
		CardIntroEntity op1 = new CardIntroEntity();
		op1.realname = "我的二维码";
		op1.cardSectionType = CommonValue.CardSectionType .BarcodeSectionType;
		op1.position = "";
		op1.department = R.drawable.icon_set_barcode+"";
		ops.add(op1);
		cards.add(ops);
		
	}
	
	private void getCardList() {
//		indicatorImageView.setVisibility(View.VISIBLE);
//    	indicatorImageView.startAnimation(indicatorAnimation);
		AppClient.getCardList(appContext, new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
//				UIHelper.dismissProgress(loadingPd);
//				indicatorImageView.clearAnimation();
//				indicatorImageView.setVisibility(View.INVISIBLE);
				CardListEntity entity = (CardListEntity)data;
				switch (entity.getError_code()) {
				case Result.RESULT_OK:
					cards.clear();
					if (entity.owned.size()>0) {
						cards.add(entity.owned);
					}
					Logger.i(cards.size()+"");
					addCardOp();
					mCardAdapter.notifyDataSetChanged();
					expandView();
					break;
				case CommonValue.USER_NOT_IN_ERROR:
//					forceLogout();
					break;
				default:
					UIHelper.ToastMessage(getApplicationContext(), entity.getMessage(), Toast.LENGTH_SHORT);
					break;
				}
			}
			
			@Override
			public void onFailure(String message) {
//				indicatorImageView.clearAnimation();
//				indicatorImageView.setVisibility(View.INVISIBLE);
				UIHelper.ToastMessage(getApplicationContext(), message, Toast.LENGTH_SHORT);
			}
			@Override
			public void onError(Exception e) {
//				indicatorImageView.clearAnimation();
//				indicatorImageView.setVisibility(View.INVISIBLE);
				e.printStackTrace();
				Logger.i(e);
			}
		});
	}
	
	public void showMyCard() {
		EasyTracker easyTracker = EasyTracker.getInstance(this);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看我的名片",   // Event label
	                   null)            // Event value
	      .build()
		);
		Intent intent = new Intent(this, MyCard.class);
		startActivity(intent);
	}
	
	public void showMyBarcode() {
		EasyTracker easyTracker = EasyTracker.getInstance(this);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看名片二维码："+String.format("%s/card/mybarcode", CommonValue.BASE_URL),   // Event label
	                   null)            // Event value
	      .build()
		);
		Intent intent = new Intent(this, QYWebView.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, String.format("%s/card/mybarcode", CommonValue.BASE_URL));
		startActivity(intent);
	}
	
	public void showScan() {
		Intent intent = new Intent(this, CaptureActivity.class);
		startActivity(intent);
	}
	
	public void showSetting() {
		Intent intent = new Intent(Me.this, Setting.class);
		startActivity(intent);
	}
	
	public void showFeedback() {
		Intent intent = new Intent(this, Feedback.class);
		startActivity(intent);
	}
	
	public void showUpdate() {
		UpdateManager.getUpdateManager().checkAppUpdate(this, true);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		switch (requestCode) {
		case CommonValue.CreateViewJSType.showUploadAvatar:
			String avatar = data.getExtras().getString("avatar");
			appContext.setUserAvatar(avatar);
			this.imageLoader.displayImage(avatar, avatarView, avatar_options);
			break;

		default:
			int position = data.getExtras().getInt("position");
			switch (position) {
			case 0:
				showSetting();
				break;

			case 1:
				showFeedback();
				break;
			case 2:
				showUpdate();
				break;
			}
			break;
		}
	}
}	
