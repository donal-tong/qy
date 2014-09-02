package ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import bean.CardIntroEntity;

import com.crashlytics.android.Crashlytics;
import com.vikaa.wecontact.R;

import config.AppClient;
import config.AppClient.FileCallback;
import config.CommonValue.LianXiRenType;
import tools.AppManager;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;
import ui.adapter.FriendCardAdapter;
import widget.MyLetterListView;
import widget.MyLetterListView.OnTouchingLetterChangedListener;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MobileSelect extends AppActivity implements OnItemClickListener{
	private ImageView indicatorImageView;
	private Animation indicatorAnimation;
	
	private FriendCardAdapter mBilateralAdapter;
	private List<CardIntroEntity> mobiles = new ArrayList<CardIntroEntity>();
	private ListView xlistView;
	private MyAsyncQueryHandler asyncQuery;
	private Uri uri ;
	
	private String code;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mobile_select);
		code = getIntent().getStringExtra("code");
		asyncQuery = new MyAsyncQueryHandler(getContentResolver());
		uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
		initUI();
		asyncQuery.startQuery(0, null, uri, projection, null, null, "sort_key COLLATE LOCALIZED asc"); 
	}
	
	private void initUI() {
		overlay = (TextView) findViewById(R.id.fast_position);
		letterListView = (MyLetterListView) findViewById(R.id.ContactLetterListView);
		
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
        mBilateralAdapter = new FriendCardAdapter(this, mobiles);
		xlistView.setAdapter(mBilateralAdapter);
		xlistView.setOnItemClickListener(this);
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
	
	private void sortPY() {
		alphaIndexer .clear();
		for (int i = 0; i < mobiles.size(); i++) {
			String currentStr = mobiles.get(i).py;
			String previewStr = (i - 1) >= 0 ? mobiles.get(i - 1).py : " ";
			if (!previewStr.equals(currentStr)) {
				if (currentStr.equals("~")) {
					alphaIndexer.put("#", i);
				}
				else {
					alphaIndexer.put(currentStr, i);
				}
			}
		}
	}
	
	private String[] projection = {Data.MIMETYPE, Phone.NUMBER, "display_name", "contact_id", "sort_key", "photo_thumb_uri"};
	private final static int MIMETYPE_INDEX = 0;
	private final static int NUMBER_INDEX = 1;
	private final static int NAME_INDEX = 2;
	private final static int ID_INDEX = 3;
	private final static int SORT_INDEX = 4;
	private final static int PHOTO_INDEX = 5;
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
				try {
					indicatorImageView.setVisibility(View.INVISIBLE);
			    	indicatorImageView.clearAnimation();
					Collections.sort(mobiles);
					mBilateralAdapter.notifyDataSetChanged();
					sortPY();
					letterListView.setVisibility(View.VISIBLE);
				} 
				catch(Exception e) {
					Crashlytics.logException(e);
				}
				
			}
		};
		indicatorImageView.setVisibility(View.VISIBLE);
    	indicatorImageView.startAnimation(indicatorAnimation);
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					for (int i = 0; i < cursor.getCount(); i++) {
						cursor.moveToPosition(i);
						String mimetype = cursor.getString(MIMETYPE_INDEX);
						if (Phone.CONTENT_ITEM_TYPE.equals(mimetype)) {
							CardIntroEntity ce = new CardIntroEntity();
							ce.realname = cursor.getString(NAME_INDEX);
							String phone = cursor.getString(NUMBER_INDEX);
							phone = phone.replace(" ", "");
					     	phone = phone.replace("+86", "");
					     	phone = phone.replace("-", "");
					     	ce.phone = phone;
							ce.code = ""+cursor.getInt(ID_INDEX);
							ce.pinyin = cursor.getString(SORT_INDEX);
							ce.cardSectionType = LianXiRenType.mobile;
							ce.avatar = cursor.getString(PHOTO_INDEX);
							ce.department = phone;
							ce.position = "";
							ce.py = StringUtils.getAlpha(ce.pinyin);
							mobiles.add(ce);
						}
					}
				}
				handler1.sendEmptyMessage(1);
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		showContactDialog(mobiles.get(position));
	}
	
	protected void showContactDialog(final CardIntroEntity cb) {
		String message = "协助录入该联系人吗？";
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage(message);
		builder.setTitle("通讯录提示");
		builder.setPositiveButton("确定", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				phonebookAssist(cb.realname, cb.phone);
			}
		});
		builder.setNegativeButton("取消", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
	   builder.create().show();
	}
	
	private void phonebookAssist(String realname, String phone) {
		if (phone.length() == 12) {
			phone = phone.substring(1, 12);
		}
		loadingPd = UIHelper.showProgress(MobileSelect.this, null, null, true);
		AppClient.phonebookAssist(appContext, realname, StringUtils.doEmpty(phone), code, new FileCallback() {
			@Override
			public void onSuccess(String url) {
				UIHelper.dismissProgress(loadingPd);
				Logger.i(url);
				Intent intent = new Intent();
				intent.putExtra("url", url);
				setResult(RESULT_OK, intent);
				UIHelper.ToastMessage(context, "录入成功", Toast.LENGTH_SHORT);
				AppManager.getAppManager().finishActivity(MobileSelect.this);
			}

			@Override
			public void onFailure(String message) {
				UIHelper.dismissProgress(loadingPd);
				if (StringUtils.notEmpty(message)) {
					WarningDialog(message);
				}
			}

			@Override
			public void onError(Exception e) {
				UIHelper.dismissProgress(loadingPd);
			}
		});
	}
	
}
