package ui;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONObject;

import qiniu.auth.JSONObjectRet;
import qiniu.io.IO;
import qiniu.io.PutExtra;
import bean.ActivityCreateEntity;
import bean.Entity;
import bean.FunsEntity;
import bean.FunsPhotoEntity;
import bean.KeyValue;

import com.crashlytics.android.Crashlytics;
import com.vikaa.allcontact.R;

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
import ui.AppActivity.DialogClickListener;
import ui.adapter.FieldAdapter;
import widget.GridViewForScrollView;
import widget.ResizeLinearLayout;
import widget.ResizeLinearLayout.OnSizeChangedListener;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard.Key;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.style.ImageSpan;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

public class CreateTopic extends AppActivity implements OnSizeChangedListener, OnFocusChangeListener {
	private EditText richET;
	private EditText funsNameET;

	private String theLarge;
	
//	private FunsEntity fun;
	
	@Override
	protected void onResume() {
		super.onResume();
		richET.setCursorVisible(false);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_topic);
//		fun = (FunsEntity) getIntent().getExtras().getSerializable("fun");
		initUI();
	}

	private void initUI() {
		ResizeLinearLayout resizeLayout = (ResizeLinearLayout) findViewById(R.id.resizeLayout);
		resizeLayout.setOnSizeChangedListener(this);
		funsNameET = (EditText) findViewById(R.id.activityName);
		funsNameET.setOnFocusChangeListener(this);
		richET = (EditText) findViewById(R.id.richEditText);
		richET.setOnFocusChangeListener(this);
		richET.addTextChangedListener(RichTextWatcher);
		richET.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				richET.setCursorVisible(true);
			}
		});
//		funsNameET.setText(fun.title);
//		richET.setText(fun.description);
	}

	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.cameraButton:
			PhotoChooseOption();
			break;
		case R.id.leftBarButton:
			closeInput();
			AppManager.getAppManager().finishActivity(this);
			break;
		case R.id.rightBarButton:
			prepareCreateActivity();
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
		richET.setCursorVisible(false);
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
								UIHelper.ToastMessage(CreateTopic.this, "无法保存照片，请检查SD卡是否挂载", Toast.LENGTH_SHORT);
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
		switch (v.getId()) {
		case R.id.richEditText:
			if (hasFocus) {
				richET.setCursorVisible(true);
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

	private String title;
	private String content;
	private BlockingQueue<FunsPhotoEntity> photoQueue = new LinkedBlockingQueue<FunsPhotoEntity>();
	private void prepareCreateActivity() {
		closeInput();
		title = funsNameET.getEditableText().toString();
		SpannableStringBuilder spanStr = (SpannableStringBuilder) richET.getText();
		String rich = Html.toHtml(spanStr);
		content = StringEscapeUtils.unescapeHtml4(rich);;
        if (StringUtils.empty(title)) {
        	WarningDialog("请填写文章标题");
			return;
		}
        if (StringUtils.empty(content)) {
        	WarningDialog("请填写文章内容");
			return;
		}
        if (content.contains("<img src=\"1:")) {
        	parseHtmlImage(content);
		}
        else {
        	loadingPd = UIHelper.showProgress(this, null, null, true);
        	AppClient.pubTopic(appContext, title, HTMLUtil.htmlToUbb(content), clientCallback);
        }
	}
	
	private void parseHtmlImage(String html) {
		content = html;
		photoQueue.clear();
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
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				
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
				content = content.replace("1:"+photo.filePath, photo.fileUrl);
				if (!photoQueue.isEmpty()) {
					upload(photoQueue.poll());
				}
				else {
					Logger.i(content);
					loadingPd = UIHelper.showProgress(CreateTopic.this, null, null, true);
					AppClient.pubTopic(appContext, title, HTMLUtil.htmlToUbb(content), clientCallback);
				}
			}

			@Override
			public void onFailure(Exception ex) {
				UIHelper.dismissProgress(loadingPd);
				//重新上传
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
					photoQueue.put(new FunsPhotoEntity(filePath, ""));
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
			UIHelper.ToastMessage(CreateTopic.this, R.layout.toastmessage_text, "发表文章成功，正在跳转", Toast.LENGTH_SHORT);
			KeyValue entity = (KeyValue) data;
			AppManager.getAppManager().finishActivity(CreateTopic.this);
			Intent intent = new Intent(CreateTopic.this, QYWebView.class);
			intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, entity.value);
			startActivityForResult(intent, CommonValue.PhonebookViewUrlRequest.editPhoneview);
		}
		
		@Override
		public void onFailure(String message) {
			UIHelper.dismissProgress(loadingPd);
			WarningDialog("发表文章失败，请重试", "确定", "取消", new DialogClickListener() {
				
				@Override
				public void ok() {
					loadingPd = UIHelper.showProgress(CreateTopic.this, null, null, true);
					AppClient.pubTopic(appContext, title, HTMLUtil.htmlToUbb(content), clientCallback);
				}
				
				@Override
				public void cancel() {
					
				}
			});
		}
		
		@Override
		public void onError(Exception e) {
			UIHelper.dismissProgress(loadingPd);
			WarningDialogAndOpenWechat("bibi100", "发表文章失败，请联系微信客服bibi100");
		}
	};
}
