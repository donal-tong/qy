package ui;

import java.util.ArrayList;
import java.util.List;

import tools.StringUtils;
import ui.adapter.MeCardAdapter;
import bean.CardIntroEntity;

import com.github.siyamed.shapeimageview.RoundedImageView;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.zxing.client.android.CaptureActivity;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.vikaa.mycontact.R;

import config.CommonValue;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;
import android.widget.ExpandableListView.OnGroupClickListener;

public class CardFragment extends Fragment{

	private MainActivity activity;
	
	@ViewInject(R.id.iphone_tree_view)
	private ExpandableListView iphoneTreeView;
	
	private List<List<CardIntroEntity>> cards;
	private MeCardAdapter mCardAdapter;
	
	private RoundedImageView avatarView;
	private TextView nameTV;
	private TextView creditTV;
	
	private DisplayImageOptions avatar_options = new DisplayImageOptions.Builder()
	.bitmapConfig(Bitmap.Config.RGB_565)
	.cacheInMemory(true)
	.cacheOnDisc(true)
	.imageScaleType(ImageScaleType.EXACTLY_STRETCHED) 
	.build();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		cards = new ArrayList<List<CardIntroEntity>>();
		mCardAdapter = new MeCardAdapter(iphoneTreeView, activity, cards);
		new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                addCardOp();
            }
        }, CommonValue.UI_DELAY);
	}
	
	@Override
    public void onAttach(Activity activity) {
    	this.activity = (MainActivity) activity;
    	super.onAttach(activity);
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_card, container, false);
		ViewUtils.inject(this, view);
		LayoutInflater mInflater = LayoutInflater.from(activity);
		View footer = mInflater.inflate(R.layout.index_footer, null);
		iphoneTreeView.setGroupIndicator(null);
		iphoneTreeView.addFooterView(footer);
		View header = mInflater.inflate(R.layout.more_headerview, null);
		avatarView = (RoundedImageView) header.findViewById(R.id.avatar);
		nameTV = (TextView) header.findViewById(R.id.title);
		creditTV = (TextView) header.findViewById(R.id.jifen);
		nameTV.setText(activity.appContext.getNickname());
		creditTV.setText("点击修改头像");
		iphoneTreeView.addHeaderView(header);
		header.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (StringUtils.notEmpty(activity.appContext.getUserAvatarCode())) {
					Intent intent = new Intent(activity, UploadAvatar.class);
					intent.putExtra("code", activity.appContext.getUserAvatarCode());
					intent.putExtra("token", "");
					intent.putExtra("avatar", activity.appContext.getUserAvatar());
					intent.putExtra("sign", activity.appContext.getLoginSign());
					startActivityForResult(intent, CommonValue.CreateViewJSType.showUploadAvatar);
				}
			}
		});
		
		iphoneTreeView.setAdapter(mCardAdapter);
		iphoneTreeView.setSelection(0);
		iphoneTreeView.setOnGroupClickListener(new OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView arg0, View arg1, int position,
					long arg3) {
				return true;
			}
		});
		iphoneTreeView.setOnChildClickListener(new OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView arg0, View arg1, int groupPosition,
					int childPosition, long arg4) {
				switch (groupPosition) {
				case 0:
					switch (childPosition) {
					case 0:
						showMyCard();
						break;

					case 1:
						showScan();
						break;
						
					case 2:
						showMyBarcode();
						break;
					}
					break;

				case 1:
					switch (childPosition) {
					case 0:
						showSetting();
						break;

					default:
						break;
					}
					break;
				}
				return true;
			}
		});
		return view;
	}
	
	private void expandView() {
		for (int i = 0; i < cards.size(); i++) {
			iphoneTreeView.expandGroup(i);
		}
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
		
		List<CardIntroEntity> ops1 = new ArrayList<CardIntroEntity>();
		CardIntroEntity op10 = new CardIntroEntity();
		op10.realname = "设置";
		op10.department = R.drawable.icon_set_setting+"";
		op10.position = "";
		ops1.add(op10);
		cards.add(ops1);
		
		mCardAdapter.notifyDataSetChanged();
		expandView();
		activity.imageLoader.displayImage(activity.appContext.getUserAvatar(), avatarView, avatar_options);
	}
	
	private void showMyCard() {
		EasyTracker easyTracker = EasyTracker.getInstance(activity);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看我的名片",   // Event label
	                   null)            // Event value
	      .build()
		);
		Intent intent = new Intent(activity, MyCard.class);
		startActivity(intent);
	}
	
	private void showScan() {
		Intent intent = new Intent(activity, CaptureActivity.class);
		startActivity(intent);
	}
	
	private void showMyBarcode() {
		EasyTracker easyTracker = EasyTracker.getInstance(activity);
		easyTracker.send(MapBuilder
	      .createEvent("ui_action",     // Event category (required)
	                   "button_press",  // Event action (required)
	                   "查看名片二维码："+String.format("%s/card/mybarcode", CommonValue.BASE_URL),   // Event label
	                   null)            // Event value
	      .build()
		);
		Intent intent = new Intent(activity, QYWebView.class);
		intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, String.format("%s/card/mybarcode", CommonValue.BASE_URL));
		startActivity(intent);
	}
	
	private void showSetting() {
		Intent intent = new Intent(activity, Setting.class);
		startActivity(intent);
	}
}
