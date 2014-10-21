package ui;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONObject;

import qiniu.auth.JSONObjectRet;
import qiniu.io.IO;
import qiniu.io.PutExtra;
import bean.Entity;
import bean.FunsPhotoEntity;
import bean.KeyValue;

import com.crashlytics.android.Crashlytics;
import com.vikaa.contactactivityassitant.R;

import tools.AppManager;
import tools.HTMLUtil;
import tools.ImageUtils;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;
import ui.AppActivity.DialogClickListener;
import ui.adapter.PhonebookAdapter;
import config.AppClient;
import config.CommonValue;
import config.AppClient.ClientCallback;
import config.AppClient.FileCallback;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.Html.ImageGetter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CreateTopicFragment extends Fragment implements OnFocusChangeListener{
	private QunTopic activity;
	
	private EditText richET;
	private EditText funsNameET;

	private String theLarge;
	
	public static CreateTopicFragment newInstance() {
		CreateTopicFragment fragment = new CreateTopicFragment();
        return fragment;
    }
	
	 @Override
    public void onAttach(Activity activity) {
    	this.activity = (QunTopic) activity;
    	super.onAttach(activity);
    }
	 
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.pub_topic, container, false);
		funsNameET = (EditText) view.findViewById(R.id.activityName);
		funsNameET.setOnFocusChangeListener(this);
		richET = (EditText) view.findViewById(R.id.richEditText);
		richET.setOnFocusChangeListener(this);
//		richET.addTextChangedListener(RichTextWatcher);
		richET.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				richET.setCursorVisible(true);
			}
		});
		Button btnCamera = (Button) view.findViewById(R.id.cameraButton);
		btnCamera.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				PhotoChooseOption();
			}
		});
		Button btnPub = (Button) view.findViewById(R.id.pubTopicButton);
		btnPub.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				prepareCreateActivity();
			}
		});
		return view;
	}

	private ImageGetter imageGetter = new ImageGetter() {
		@Override
		public Drawable getDrawable(String source) {
			String f = source.substring(0, 1);
			String filePath = source.substring(2);
			if (f.equals("1")) {
				try {
					Bitmap bitmap = ImageUtils.getBitmapByPath(filePath, null, activity.screeWidth);
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
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != -1) {
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
        		newPhotoPath = ImageUtils.getAbsoluteImagePath(activity, thisUri);
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
		activity.closeInput();
		CharSequence[] item = {"相册", "拍照"};
		AlertDialog imageDialog = new AlertDialog.Builder(activity).setTitle(null).setIcon(android.R.drawable.btn_star).setItems(item,
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
								UIHelper.ToastMessage(activity, "无法保存照片，请检查SD卡是否挂载", Toast.LENGTH_SHORT);
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
	

	private String title;
	private String content;
	private BlockingQueue<FunsPhotoEntity> photoQueue = new LinkedBlockingQueue<FunsPhotoEntity>();
	private void prepareCreateActivity() {
		activity.closeInput();
		title = funsNameET.getEditableText().toString();
		SpannableStringBuilder spanStr = (SpannableStringBuilder) richET.getText();
		String rich = Html.toHtml(spanStr);
		content = StringEscapeUtils.unescapeHtml4(rich);;
        if (StringUtils.empty(title)) {
        	activity.WarningDialog("请填写文章标题");
			return;
		}
        if (StringUtils.empty(content)) {
        	activity.WarningDialog("请填写文章内容");
			return;
		}
        if (content.contains("<img src=\"1:")) {
        	parseHtmlImage(content);
		}
        else {
        	activity.loadingPd = UIHelper.showProgress(activity, null, null, true);
        	AppClient.pubTopic(activity.appContext, title, HTMLUtil.htmlToUbb(content), clientCallback);
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
				
			}
			
			@Override
			public void onError(Exception e) {
				Crashlytics.logException(e);
			}
		});
	}
	
	private void upload(final FunsPhotoEntity photo) {
		activity.loadingPd = UIHelper.showProgress(activity, null, null, true);
		String key = IO.UNDEFINED_KEY; 
		PutExtra extra = new PutExtra();
		extra.params = new HashMap<String, String>();
		IO.putFile(uploadToken, key, new File(photo.filePath), extra, new JSONObjectRet() {
			@Override
			public void onProcess(long current, long total) {
			}

			@Override
			public void onSuccess(JSONObject resp) {
				UIHelper.dismissProgress(activity.loadingPd);
				Logger.i(resp.toString());
				String key = resp.optString("key", "");
				photo.fileUrl = "http://pbwci.qiniudn.com/"+key;
				content = content.replace("1:"+photo.filePath, photo.fileUrl);
				if (!photoQueue.isEmpty()) {
					upload(photoQueue.poll());
				}
				else {
					Logger.i(content);
					activity.loadingPd = UIHelper.showProgress(activity, null, null, true);
					AppClient.pubTopic(activity.appContext, title, HTMLUtil.htmlToUbb(content), clientCallback);
				}
			}
			@Override

            public void onFailure(Exception ex) {
				UIHelper.dismissProgress(activity.loadingPd);
				//重新上传
				activity.WarningDialog("上传图片失败，请重试", "确定", "取消", new DialogClickListener() {
					
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
			UIHelper.dismissProgress(activity.loadingPd);
			UIHelper.ToastMessage(activity, R.layout.toastmessage_text, "发表文章成功，正在跳转", Toast.LENGTH_SHORT);
			KeyValue entity = (KeyValue) data;
//			AppManager.getAppManager().finishActivity(activity);
			richET.setText("");
			funsNameET.setText("");
			Intent intent = new Intent(activity, QYWebView.class);
			intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, entity.value);
			startActivityForResult(intent, CommonValue.PhonebookViewUrlRequest.editPhoneview);
		}
		
		@Override
		public void onFailure(String message) {
			UIHelper.dismissProgress(activity.loadingPd);
			activity.WarningDialog("发表文章失败，请重试", "确定", "取消", new DialogClickListener() {
				
				@Override
				public void ok() {
					activity.loadingPd = UIHelper.showProgress(activity, null, null, true);
					AppClient.pubTopic(activity.appContext, title, HTMLUtil.htmlToUbb(content), clientCallback);
				}
				
				@Override
				public void cancel() {
					
				}
			});
		}
		
		@Override
		public void onError(Exception e) {
			UIHelper.dismissProgress(activity.loadingPd);
			activity.WarningDialogAndOpenWechat("bibi100", "发表文章失败，请联系微信客服bibi100");
		}
	};
}
