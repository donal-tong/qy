package widget;


import tools.AppManager;
import tools.Logger;
import tools.StringUtils;
import ui.CreateActivity;
import ui.QYWebView;
import bean.AdsEntity;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.vikaa.contactactivityassitant.R;

import config.CommonValue;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class AdImageFragment extends Fragment{

	private static final String KEY_CONTENT = "AdImageFragment:Content";
	
	private AdsEntity ad = new AdsEntity();
	private Activity activity;
	
	public static AdImageFragment newInstance(AdsEntity entity) {
		AdImageFragment fragment = new AdImageFragment();
        fragment.ad = entity;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            ad = (AdsEntity) savedInstanceState.getSerializable(KEY_CONTENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View view = inflater.inflate(R.layout.find_ad_image, container, false);
    	ImageView adsImageView = (ImageView) view.findViewById(R.id.adImageView);
    	adsImageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if (StringUtils.notEmpty(ad.link)) {
					Intent intent = new Intent(activity, QYWebView.class);
					intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, ad.link);
					startActivity(intent);
				}
			}
		});
    	TextView textview = (TextView) view.findViewById(R.id.adTextView);
    	ImageLoader.getInstance().displayImage(ad.thumb, adsImageView, CommonValue.DisplayOptions.default_options);
    	textview.setText(ad.title);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
    	this.activity = activity;
    	super.onAttach(activity);
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_CONTENT, ad);
    }
}
