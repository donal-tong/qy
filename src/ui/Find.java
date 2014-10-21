package ui;


import java.util.ArrayList;
import java.util.List;

import qiniu.conf.Conf;
import qiniu.utils.Config;
import qiniu.utils.Mac;
import qiniu.utils.PutPolicy;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;
import ui.adapter.FindAdFragmentAdapter;
import ui.adapter.FunsGridViewAdapter;
import ui.adapter.QunGridViewAdapter;
import baidupush.Utils;
import bean.AdsEntity;
import bean.AdsListEntity;
import bean.CardListEntity;
import bean.Entity;
import bean.FunsEntity;
import bean.FunsListEntity;
import bean.QunsEntity;
import bean.QunsListEntity;
import bean.Result;
import bean.TopicOptionListEntity;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.crashlytics.android.Crashlytics;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.vikaa.contactactivityassitant.R;

import config.AppClient;
import config.CommonValue;
import config.MyApplication;
import config.AppClient.ClientCallback;
import config.CommonValue.CreateViewUrlAndRequest;
import config.CommonValue.FunsType;
import android.R.string;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Find extends Fragment implements OnClickListener{
	@ViewInject(R.id.viewPager)
	private ViewPager adsViewPager;
	
	@ViewInject(R.id.btnActivity)
	private RelativeLayout btnActivity;
	
	@ViewInject(R.id.btnQun)
	private RelativeLayout btnQun;
	
	@ViewInject(R.id.btnTopic)
	private RelativeLayout btnTopic;
	
	@ViewInject(R.id.btnCard)
	private RelativeLayout btnCard;
	
	@ViewInject(R.id.btnPC)
	private RelativeLayout btnPC;
	
	private List<AdsEntity> ads = new ArrayList<AdsEntity>();
	private FindAdFragmentAdapter adsAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.find, container, false);
		ViewUtils.inject(this, view);
		adsViewPager.setAdapter(adsAdapter);
		btnActivity.setOnClickListener(this);
		btnQun.setOnClickListener(this);
		btnTopic.setOnClickListener(this);
		btnPC.setOnClickListener(this);
		btnCard.setOnClickListener(this);
		return view;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adsAdapter = new FindAdFragmentAdapter(getChildFragmentManager(), ads);
		new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
            	getAdsFromCache();
            }
        }, CommonValue.UI_DELAY);
	}
	
    public void showMyCard() {
        EasyTracker easyTracker = EasyTracker.getInstance(getActivity());
        easyTracker.send(MapBuilder
                        .createEvent("ui_action",     // Event category (required)
                                "button_press",  // Event action (required)
                                "查看我的名片",   // Event label
                                null)            // Event value
                        .build()
        );
        Intent intent = new Intent(getActivity(), MyCard.class);
        startActivity(intent);
    }
	
	private void getAdsFromCache() {
		ads.add(new AdsEntity(CommonValue.ADS_TITLE, CommonValue.AD_THUMB+R.drawable.ad_default, CommonValue.AD_LINK));
		adsAdapter.notifyDataSetChanged();
		String key = String.format("%s-%s", CommonValue.CacheKey.ADS, MyApplication.getInstance().getLoginUid());
		AdsListEntity entity = (AdsListEntity) MyApplication.getInstance().readObject(key);
		if(entity != null){
			ads.clear();
			ads.addAll(entity.ads);
			adsAdapter.notifyDataSetChanged();
		}
		getAds();
	}
	
	private void getAds() {
		AppClient.getSlideAds(MyApplication.getInstance(), new ClientCallback() {
			@Override
			public void onSuccess(Entity data) {
				AdsListEntity adsList = (AdsListEntity) data;
				ads.clear();
				ads.addAll(adsList.ads);
				adsAdapter.notifyDataSetChanged();
			}
			
			@Override
			public void onFailure(String message) {
				getAds();
			}
			
			@Override
			public void onError(Exception e) {
				getAds();
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnQun:
			startActivity(new Intent(getActivity(), CreatePhonebook.class));
			break;
		case R.id.btnActivity:
			startActivity(new Intent(getActivity(), CreateActivity.class));
			break;
		case R.id.btnTopic:
			startActivity(new Intent(getActivity(), QunTopic.class));
			break;
		case R.id.btnCard:
            showMyCard();
			break;
		case R.id.btnPC:
			startActivity(new Intent(getActivity(), PCTIP.class));
			break;
		default:
			break;
		}
	}
	
	
}
