package ui;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONObject;

import qiniu.auth.JSONObjectRet;
import qiniu.io.IO;
import qiniu.io.PutExtra;
import bean.ActivityCreateEntity;
import bean.Entity;
import bean.FunsEntity;
import bean.FunsListEntity;
import bean.FunsPhotoEntity;
import bean.KeyValue;
import bean.QunsEntity;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.crashlytics.android.Crashlytics;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.vikaa.mycontact.R;

import config.AppClient;
import config.CommonValue;
import config.AppClient.ClientCallback;
import config.AppClient.FileCallback;
import tools.AppManager;
import tools.HTMLUtil;
import tools.ImageUtils;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;
import ui.adapter.FieldAdapter;
import ui.adapter.FunTypeAdapter;
import ui.adapter.PrivacyAdapter;
import ui.adapter.QunTypeAdapter;
import widget.GridViewForScrollView;
import widget.ResizeLinearLayout;
import widget.ResizeRelativeLayout;
import widget.ResizeRelativeLayout.OnSizeChangedListener;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.ScrollView;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

public class CreateActivity extends AppActivity implements OnSizeChangedListener, OnFocusChangeListener, AMapLocationListener, OnItemClickListener {
	private final static int IMG_COVER = 1;
	private final static int IMG_CONTENT = 2;
	private String activityCover;
	private int imgType;
	private ImageView imgActivityCover;
	private EditText richET;
	private EditText funsNameET;
	private LinearLayout moreLayout;
	private Button moreButton;
	private Button timeButton;
	private EditText locationET;
	private EditText feeET;
	private EditText memberET;
	private Spinner publicSP;
	private EditText questionET;
	private int year;
	private int month;
	private int date;
	private int hour;
	private int minute;

	private String theLarge;
	
	private GridViewForScrollView fieldGridView;
	private FieldAdapter fieldAdapter;
	private List<String> fields = new ArrayList<String>();
	
	private FunsEntity fun;
	
	private BlockingQueue<FunsPhotoEntity> photoQueue = new LinkedBlockingQueue<FunsPhotoEntity>();
	
	private String address="";
	private String cost="";
	private String guests="";
	private String privacy = "1";
	private String question = "";
	
	private String title;
	private String rich;
	private String begin_at;
	
	private LocationManagerProxy mAMapLocManager = null;
	
	private List<FunsEntity> funs = new ArrayList<FunsEntity>();
	private FunTypeAdapter adapterType;
	private GridViewForScrollView gvQun;
	
	@ViewInject(R.id.mainScrollView)
	private ScrollView mainScrollView;
	
	@ViewInject(R.id.timeLayout)
	private RelativeLayout timeLayout;
	@ViewInject(R.id.datePicker)
	private DatePicker datePicker;
	@ViewInject(R.id.timePicker)
	private TimePicker timePicker;
	
	@Override
	protected void onResume() {
		super.onResume();
		richET.setCursorVisible(false);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (mAMapLocManager != null) {
			mAMapLocManager.removeUpdates(this);
		}
	}

	@Override
	protected void onDestroy() {
		if (mAMapLocManager != null) {
			mAMapLocManager.removeUpdates(this);
			mAMapLocManager.destroy();
		}
		mAMapLocManager = null;
		super.onDestroy();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_activity);
		ViewUtils.inject(this);
		funs.addAll(FunsListEntity.parse(this).funs);
		funs.get(0).isSelected = true;
		fun = funs.get(0);
		activityCover = fun.cover;
		initUI();
		initTimeLayout();
		mAMapLocManager = LocationManagerProxy.getInstance(this);
		Handler jumpHandler = new Handler();
		jumpHandler.postDelayed(new Runnable() {
			public void run() {
				try {
					mAMapLocManager.requestLocationUpdates(LocationProviderProxy.AMapNetwork, 1000, 10, CreateActivity.this);
				} 
				catch (Exception e) {
					Crashlytics.logException(e);
				}
		 	}
		}, 1000);
		
	}

	private void initUI() {
		gvQun = (GridViewForScrollView) findViewById(R.id.typeGridView);
		adapterType = new FunTypeAdapter(this, funs);
		gvQun.setAdapter(adapterType);
		gvQun.setOnItemClickListener(this);
		ResizeRelativeLayout resizeLayout = (ResizeRelativeLayout) findViewById(R.id.resizeLayout);
		resizeLayout.setOnSizeChangedListener(this);
		moreButton = (Button) findViewById(R.id.more);
		moreLayout = (LinearLayout) findViewById(R.id.moreLayout);
		Calendar calendar=Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		year = calendar.get(Calendar.YEAR);
		month = calendar.get(Calendar.MONTH);
		date = calendar.get(Calendar.DAY_OF_MONTH);;
		hour = calendar.get(Calendar.HOUR_OF_DAY);
		minute = calendar.get(Calendar.MINUTE);
		datePicker.setMinDate(calendar.getTimeInMillis());
		imgActivityCover = (ImageView) findViewById(R.id.imgActivityCover);
		if (StringUtils.notEmpty(activityCover)) {
			imageLoader.displayImage(activityCover, imgActivityCover, CommonValue.DisplayOptions.default_options);
		}
		funsNameET = (EditText) findViewById(R.id.activityName);
		funsNameET.setOnFocusChangeListener(this);
		richET = (EditText) findViewById(R.id.richEditText);
		richET.setOnFocusChangeListener(this);
		richET.addTextChangedListener(RichTextWatcher);
		richET.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
//				Logger.i(edit.getSelectionStart()+" " + edit.getSelectionEnd());
//				SpannableStringBuilder spanStr = (SpannableStringBuilder) edit.getText();
//				String html = StringUtils.doEmpty(Html.toHtml(spanStr));
//				Logger.i(html);
//				if (StringUtils.empty(html)) {
//					return ;
//				}
//				html = html.replaceAll("<p>", "");
//				html = html.replaceAll("</p>", "");
//				Logger.i(html);
				richET.setCursorVisible(true);
//				inputToolLayout.setVisibility(View.VISIBLE);
			}
		});
		timeButton = (Button) findViewById(R.id.activityTime);
		String time = year+"-"+(month+1)+"-"+date+" " +hour+":"+minute;
		timeButton.setText(time);
		
		fieldGridView = (GridViewForScrollView) findViewById(R.id.fieldGridView);
		fields.add("公司");
		fields.add("职位");
		fields.add("邮箱");
		fields.add("QQ");
		fields.add("微信");
		fields.add("身份证号");
		fieldAdapter = new FieldAdapter(fields, context);
		fieldGridView.setAdapter(fieldAdapter);
		
		funsNameET.setText(fun.title);
		richET.setText(fun.description);
		
		locationET = (EditText) findViewById(R.id.activityAddress);
		feeET = (EditText) findViewById(R.id.activityFee);
		memberET = (EditText) findViewById(R.id.activityMember);
		
		publicSP = (Spinner) findViewById(R.id.publicSP);
		List<KeyValue> exchanges = new ArrayList<KeyValue>();
		exchanges.add(new KeyValue("公开(让更多人参与)","0"));
		exchanges.add(new KeyValue("不公开(内部活动)","1"));
		PrivacyAdapter eAdapter = new PrivacyAdapter(this, exchanges);
		publicSP.setAdapter(eAdapter);
		publicSP.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				privacy = position+"";
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		});
		
		questionET =  (EditText) findViewById(R.id.activityQues);
	}

	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.cameraButton:
			imgType = IMG_CONTENT;
			PhotoChooseOption();
			break;
		case R.id.leftBarButton:
			closeInput();
			AppManager.getAppManager().finishActivity(this);
			break;
		case R.id.activityTime:
			closeInput();
			mainScrollView.scrollTo(0, 1500);
			timeLayout.setVisibility(View.VISIBLE);
			break;
		case R.id.rightBarButton:
			prepareCreateActivity();
			break;
		case R.id.more:
			moreButton.setVisibility(View.GONE);
			moreLayout.setVisibility(View.VISIBLE);
			privacy = 0+"";
			break;
		case R.id.btnActivityCover:
			imgType = IMG_COVER;
			PhotoChooseOption();
			break;
		case R.id.btnTimeOK:
			timeLayout.setVisibility(View.INVISIBLE);
			break;
		}
	}

	private ImageGetter imageGetter = new ImageGetter() {
		@Override
		public Drawable getDrawable(String source) {
			String f = source.substring(0, 1);
			String filePath = source.substring(2);
			if (f.equals("1")) {
				try {
					Bitmap bitmap = ImageUtils.getBitmapByPath(filePath, null, screeWidth);
					Drawable drawable = new BitmapDrawable(bitmap);
			        drawable.setBounds(-1, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
					return drawable;
				} catch (Exception e) {
					Crashlytics.logException(e);
					return null;
				}
			} else {
				return null;
			}
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		String newPhotoPath;
		switch (requestCode) {
		case 10:
			year = data.getExtras().getInt("year");
			month = data.getExtras().getInt("month");
			date = data.getExtras().getInt("date");
			hour = data.getExtras().getInt("hour");
			minute = data.getExtras().getInt("minute");
			String time = year+"-"+(month+1)+"-"+date+" " +hour+":"+minute;
			timeButton.setText(time);
			break;
		case ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA:
			if (StringUtils.notEmpty(theLarge)) {
				File file = new File(theLarge);
				File dir = new File( ImageUtils.CACHE_IMAGE_FILE_PATH);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				String imagePathAfterCompass = ImageUtils.CACHE_IMAGE_FILE_PATH + file.getName();
				try {
					ExifInterface sourceExif = new ExifInterface(theLarge);
					String orientation = sourceExif.getAttribute(ExifInterface.TAG_ORIENTATION);
					ImageUtils.saveImageToSD(imagePathAfterCompass, ImageUtils.getSmallBitmap(theLarge, 200), 80);
					ExifInterface exif = new ExifInterface(imagePathAfterCompass);
					exif.setAttribute(ExifInterface.TAG_ORIENTATION, orientation);
				    exif.saveAttributes();
					newPhotoPath = imagePathAfterCompass;
					displayAtEditText(newPhotoPath);
				} catch (IOException e) {
					Crashlytics.logException(e);
				}
				
			}
			break;
		case ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD:
			if(data == null)  return;
			Uri thisUri = data.getData();
        	String thePath = ImageUtils.getAbsolutePathFromNoStandardUri(thisUri);
        	if(StringUtils.empty(thePath)) {
        		newPhotoPath = ImageUtils.getAbsoluteImagePath(this,thisUri);
        	}
        	else {
        		newPhotoPath = thePath;
        	}
        	File file = new File(newPhotoPath);
			File dir = new File( ImageUtils.CACHE_IMAGE_FILE_PATH);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			String imagePathAfterCompass = ImageUtils.CACHE_IMAGE_FILE_PATH + file.getName();
			try {
				ExifInterface sourceExif = new ExifInterface(newPhotoPath);
				String orientation = sourceExif.getAttribute(ExifInterface.TAG_ORIENTATION);
				ImageUtils.saveImageToSD(imagePathAfterCompass, ImageUtils.getSmallBitmap(newPhotoPath, 200), 80);
				ExifInterface exif = new ExifInterface(imagePathAfterCompass);
				exif.setAttribute(ExifInterface.TAG_ORIENTATION, orientation);
			    exif.saveAttributes();
				newPhotoPath = imagePathAfterCompass;
				displayAtEditText(newPhotoPath);
			} catch (IOException e) {
				Crashlytics.logException(e);
			}
			break;
		}
	}
	
	private void displayAtEditText(String path) {
		if (imgType == IMG_COVER) {
			activityCover = path;
			imageLoader.displayImage("file://"+path, imgActivityCover);
		}
		else {
			if (StringUtils.notEmpty(StringUtils.doEmpty(richET.getEditableText().toString()))) {
				richET.getEditableText().insert(
						richET.getSelectionStart(),
						Html.fromHtml("<br><img src='1:" + path
								+ "'/><br>\n", imageGetter, null));
			}
			else {
				richET.getEditableText().insert(
						richET.getSelectionStart(),
						Html.fromHtml("<img src='1:" + path
								+ "'/><br>\n", imageGetter, null));
			}
		}
	}
	
	private void PhotoChooseOption() {
		closeInput();
		CharSequence[] item = {"相册", "拍照"};
		AlertDialog imageDialog = new AlertDialog.Builder(this).setTitle(null).setIcon(android.R.drawable.btn_star).setItems(item,
				new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int item)
					{
						//手机选图
						if( item == 0 )
						{
							Intent intent = new Intent(Intent.ACTION_PICK,
									Media.EXTERNAL_CONTENT_URI);
							startActivityForResult(Intent.createChooser(intent, "选择图片"),ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD); 
						}
						//拍照
						else if( item == 1 )
						{	
							String savePath = "";
							//判断是否挂载了SD卡
							String storageState = Environment.getExternalStorageState();		
							if(storageState.equals(Environment.MEDIA_MOUNTED)){
								savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + ImageUtils.DCIM;//存放照片的文件夹
								File savedir = new File(savePath);
								if (!savedir.exists()) {
									savedir.mkdirs();
								}
							}
							//没有挂载SD卡，无法保存文件
							if(StringUtils.empty(savePath)){
								UIHelper.ToastMessage(CreateActivity.this, "无法保存照片，请检查SD卡是否挂载", Toast.LENGTH_SHORT);
								return;
							}
							String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
							String fileName = "c_" + timeStamp + ".jpg";//照片命名
							File out = new File(savePath, fileName);
							Uri uri = Uri.fromFile(out);
							theLarge = savePath + fileName;//该照片的绝对路径
							Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
							intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
							startActivityForResult(intent, ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA);
						}   
					}}).create();
			 imageDialog.show();
	}

	TextWatcher RichTextWatcher = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}
		
		@Override
		public void afterTextChanged(Editable s) {
		}
	};

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
//		inputToolLayout.setVisibility(View.GONE);
		switch (v.getId()) {
		case R.id.richEditText:
			if (hasFocus) {
				richET.setCursorVisible(true);
//				inputToolLayout.setVisibility(View.VISIBLE);
			}
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (oldh-h<-100) {
			richET.setCursorVisible(false);
		}
	}

	private void prepareCreateActivity() {
		title = funsNameET.getEditableText().toString();
		SpannableStringBuilder spanStr = (SpannableStringBuilder) richET.getText();
		rich = Html.toHtml(spanStr);
		content = StringEscapeUtils.unescapeHtml4(rich);
		begin_at = year+"-"+(month+1)+"-"+date+" " +hour+":"+minute;
		Date date = new Date();  
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d HH:mm");  
        try {  
            date = sdf.parse(begin_at);  
            begin_at = date.getTime()/1000+"";
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        if (StringUtils.empty(title)) {
        	WarningDialog("请填写活动标题");
			return;
		}
        if (StringUtils.empty(content)) {
        	WarningDialog("请填写活动内容");
			return;
		}
        address = StringUtils.doEmpty(locationET.getText().toString());
        cost = StringUtils.doEmpty(feeET.getText().toString());
        guests = StringUtils.doEmpty(memberET.getText().toString());
        question = StringUtils.doEmpty(questionET.getText().toString());
        if (content.contains("<img src=\"1:") || !activityCover.contains("http")) {
        	parseHtmlImage(content);
		}
        else {
        	loadingPd = UIHelper.showProgress(this, null, null, true);
        	AppClient.createActivity(appContext, title, activityCover, HTMLUtil.htmlToUbb(content), begin_at, "", address, "", privacy, fun.id, cost, "", "", guests, "", question, clientCallback);
        }
	}
	
	private String content;
	private void parseHtmlImage(String html) {
		content = html;
		photoQueue.clear();
		try {
			photoQueue.put(new FunsPhotoEntity(activityCover, "", IMG_COVER));
		} catch (InterruptedException e) {
			Crashlytics.logException(e);
		}
		Html.fromHtml(html, imagePathGetter, null);
		getQiniuToken();
	}
	private String uploadToken;
	private void getQiniuToken() {
		AppClient.getQiniuToken(new FileCallback() {
			
			@Override
			public void onSuccess(String filePath) {
				uploadToken = filePath;
				upload(photoQueue.poll());
			}
			
			@Override
			public void onFailure(String message) {
				WarningDialog("上传图片失败，请重试", "确定", "取消", new DialogClickListener() {
					
					@Override
					public void ok() {
						getQiniuToken();
					}
					
					@Override
					public void cancel() {
						
					}
				});	
			}
			
			@Override
			public void onError(Exception e) {
				WarningDialog("上传图片失败，请重试", "确定", "取消", new DialogClickListener() {
					
					@Override
					public void ok() {
						getQiniuToken();
					}
					
					@Override
					public void cancel() {
						
					}
				});		
			}
		});
	}
	
	private void upload(final FunsPhotoEntity photo) {
		loadingPd = UIHelper.showProgress(this, null, null, true);
		String key = IO.UNDEFINED_KEY; 
		PutExtra extra = new PutExtra();
		extra.params = new HashMap<String, String>();
		IO.putFile(uploadToken, key, new File(photo.filePath), extra, new JSONObjectRet() {
			@Override
			public void onProcess(long current, long total) {
			}

			@Override
			public void onSuccess(JSONObject resp) {
				UIHelper.dismissProgress(loadingPd);
				Logger.i(resp.toString());
				String key = resp.optString("key", "");
				photo.fileUrl = "http://pbwci.qiniudn.com/"+key;
				if (photo.type == IMG_COVER) {
					activityCover = photo.fileUrl;
				}
				else {
					content = content.replace("1:"+photo.filePath, photo.fileUrl);
				}
				if (!photoQueue.isEmpty()) {
					upload(photoQueue.poll());
				}
				else {
					loadingPd = UIHelper.showProgress(CreateActivity.this, null, null, true);
					AppClient.createActivity(appContext, title, activityCover, HTMLUtil.htmlToUbb(content), begin_at, "", address, "", privacy, fun.id, cost, "", "", guests, "", question, clientCallback);
				}
			}

			@Override
			public void onFailure(Exception ex) {
				UIHelper.dismissProgress(loadingPd);
				//重新上传
				photoQueue.add(photo);
				WarningDialog("上传图片失败，请重试", "确定", "取消", new DialogClickListener() {
					
					@Override
					public void ok() {
						if (!photoQueue.isEmpty()) {
							upload(photoQueue.poll());
						}
					}
					
					@Override
					public void cancel() {
						
					}
				});
			}
		});
	}
	
	private ImageGetter imagePathGetter = new ImageGetter() {
		@Override
		public Drawable getDrawable(String source) {
			String f = source.substring(0, 1);
			String filePath = source.substring(2);
			if (f.equals("1")) {
				try {
					photoQueue.put(new FunsPhotoEntity(filePath, "", IMG_CONTENT));
				} catch (InterruptedException e) {
					e.printStackTrace();
				};
			} 
			return null;
		}
	};
	
	private ClientCallback clientCallback = new ClientCallback() {
		
		@Override
		public void onSuccess(Entity data) {
			UIHelper.dismissProgress(loadingPd);
			UIHelper.ToastMessage(CreateActivity.this, R.layout.toastmessage_text, "发起活动成功，正在跳转", Toast.LENGTH_SHORT);
			ActivityCreateEntity entity = (ActivityCreateEntity) data;
			Intent intent0 = new Intent();
			intent0.setAction(CommonValue.ACTIVITY_CREATE_ACTION);
			intent0.setAction(CommonValue.ACTIVITY_DELETE_ACTION);
			sendBroadcast(intent0);
			AppManager.getAppManager().finishActivity(CreateActivity.this);
			Intent intent = new Intent(CreateActivity.this, QYWebView.class);
			intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, entity.link);
			startActivityForResult(intent, CommonValue.PhonebookViewUrlRequest.editPhoneview);
		}
		
		@Override
		public void onFailure(String message) {
			UIHelper.dismissProgress(loadingPd);
			WarningDialog("发起活动失败，请重试", "确定", "取消", new DialogClickListener() {
				
				@Override
				public void ok() {
					loadingPd = UIHelper.showProgress(CreateActivity.this, null, null, true);
					AppClient.createActivity(appContext, title, activityCover, HTMLUtil.htmlToUbb(content), begin_at, "", address, "", "", fun.id, cost, "", "", guests, "", question, clientCallback);
				}
				
				@Override
				public void cancel() {
					
				}
			});
		}
		
		@Override
		public void onError(Exception e) {
			UIHelper.dismissProgress(loadingPd);
			WarningDialogAndOpenWechat("bibi100", "发起活动失败，请联系微信客服bibi100");
		}
	};

	@Override
	public void onLocationChanged(Location arg0) {
		
	}

	@Override
	public void onProviderDisabled(String arg0) {
		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		
	}

	@Override
	public void onLocationChanged(AMapLocation location) {
		if (location != null) {
			location.getLatitude();
			location.getLongitude();
			locationET.setText(location.getProvince() +""+location.getCity()+""+ location.getDistrict());
			mAMapLocManager.removeUpdates(this);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        closeInput();
		for (FunsEntity temp : funs) {
			temp.isSelected = false;
		}
		fun = funs.get(position);
		funs.get(position).isSelected = true;
		adapterType.notifyDataSetChanged();
		changeType();
	}
	
	private void changeType() {
		funsNameET.setText(fun.title);
		richET.setText(fun.description);
		if (StringUtils.notEmpty(fun.cover)) {
            activityCover = fun.cover;
			imageLoader.displayImage(fun.cover, imgActivityCover, CommonValue.DisplayOptions.default_options);
		}
	}
	
	private void initTimeLayout() {
		timePicker.setIs24HourView(true);
		if (year == 0) {
			return;
		}
		datePicker.init(year, month, date, new OnDateChangedListener() {
			@Override
			public void onDateChanged(DatePicker view, int currentYear, int monthOfYear,
					int dayOfMonth) {
				year = currentYear;
				month = monthOfYear;
				date = dayOfMonth;
				String time = year+"-"+(month+1)+"-"+date+" " +hour+":"+minute;
				timeButton.setText(time);
			}
		});
		timePicker.setCurrentHour(hour);
		timePicker.setCurrentMinute(minute);
		timePicker.setOnTimeChangedListener(new OnTimeChangedListener() {
			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				String time = year+"-"+(month+1)+"-"+date+" " +hourOfDay+":"+minute;
				timeButton.setText(time);
			}
		});
	}
	
}
