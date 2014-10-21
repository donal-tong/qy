package ui;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import qiniu.auth.JSONObjectRet;
import qiniu.io.IO;
import qiniu.io.PutExtra;
import tools.AppManager;
import tools.ImageUtils;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;
import ui.AppActivity.DialogClickListener;
import bean.Entity;
import bean.FunsPhotoEntity;
import bean.Result;

import com.crashlytics.android.Crashlytics;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.vikaa.contactactivityassitant.R;

import config.AppClient;
import config.AppClient.ClientCallback;
import config.AppClient.FileCallback;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class UploadAvatar extends AppActivity{
	private String code;
	private String sign;
	private String uploadToken;
	private String avatar;
	private String hash;
	private ImageView avatarIV;
	private TextView avatarTV;
	
	private String theLarge;
	
	private BlockingQueue<FunsPhotoEntity> photoQueue = new LinkedBlockingQueue<FunsPhotoEntity>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.upload_avatar);
		code = getIntent().getStringExtra("code");
		uploadToken = getIntent().getStringExtra("token");
		avatar = getIntent().getStringExtra("avatar");
		sign = getIntent().getStringExtra("sign");
		initUI();
	}
	
	private void initUI() {
		avatarIV = (ImageView) findViewById(R.id.mingpian);
		avatarTV = (TextView) findViewById(R.id.mingpianProgress);
		if (StringUtils.empty(avatar)) {
			this.imageLoader.displayImage("drawable://"+ R.drawable.content_image_loading, avatarIV, avatar_options);
		}
		else {
			Logger.i(avatar);
			this.imageLoader.displayImage(avatar, avatarIV, avatar_options);
		}
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			AppManager.getAppManager().finishActivity(this);
			break;
		case R.id.rightBarButton:
			submit();
			break;
		case R.id.mingpianbutton:
			PhotoChooseOption();
			break;
		}
	}
	
//	private void submit() {
//		if (StringUtils.empty(hash)) {
//			UIHelper.ToastMessage(this, "请上传新的头像", Toast.LENGTH_SHORT);
//			return;
//		}
//		AppClient.setAvatar(code, sign, hash);
//		setResult(RESULT_OK);
//		AppManager.getAppManager().finishActivity(this);
//	}
	
	private void PhotoChooseOption() {
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
								UIHelper.ToastMessage(UploadAvatar.this, "无法保存照片，请检查SD卡是否挂载", Toast.LENGTH_SHORT);
								return;
							}

							String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
							String fileName = "c_" + timeStamp + ".jpg";//照片命名
							File out = new File(savePath, fileName);
							Uri uri = Uri.fromFile(out);
							
							theLarge = savePath + fileName;//该照片的绝对路径
//							
							Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
							intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
							startActivityForResult(intent, ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA);
						}   
					}}).create();
			
			 imageDialog.show();
	}
	
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
				String imagePathAfterCompass = ImageUtils.CACHE_IMAGE_FILE_PATH + "/" + file.getName();
				try {
					ExifInterface sourceExif = new ExifInterface(theLarge);
					String orientation = sourceExif.getAttribute(ExifInterface.TAG_ORIENTATION);
					ImageUtils.saveImageToSD(imagePathAfterCompass, ImageUtils.getSmallBitmap(theLarge, 100), 80);
					ExifInterface exif = new ExifInterface(imagePathAfterCompass);
					exif.setAttribute(ExifInterface.TAG_ORIENTATION, orientation);
				    exif.saveAttributes();
					newPhotoPath = imagePathAfterCompass;
					upload(newPhotoPath);
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
			String imagePathAfterCompass = ImageUtils.CACHE_IMAGE_FILE_PATH + "/" + file.getName();
			try {
				ExifInterface sourceExif = new ExifInterface(newPhotoPath);
				String orientation = sourceExif.getAttribute(ExifInterface.TAG_ORIENTATION);
				ImageUtils.saveImageToSD(imagePathAfterCompass, ImageUtils.getSmallBitmap(newPhotoPath, 100), 80);
				ExifInterface exif = new ExifInterface(imagePathAfterCompass);
				exif.setAttribute(ExifInterface.TAG_ORIENTATION, orientation);
			    exif.saveAttributes();
				newPhotoPath = imagePathAfterCompass;
				upload(newPhotoPath);
			} catch (IOException e) {
				Crashlytics.logException(e);
			}
			break;
		}
	}
	
	public DisplayImageOptions avatar_options = new DisplayImageOptions.Builder()
	.bitmapConfig(Bitmap.Config.RGB_565)
	.cacheInMemory(true)
	.cacheOnDisc(true)
	.imageScaleType(ImageScaleType.EXACTLY_STRETCHED) 
	.displayer(new RoundedBitmapDisplayer(ImageUtils.dip2px(AppManager.getAppManager().currentActivity(), 72)))
    .considerExifParams(true)
	.build();
	
	private void upload(String path) {
		this.imageLoader.displayImage("file://"+path, avatarIV, avatar_options);
		theLarge = path;
	}

	private void submit() {
		if (StringUtils.empty(theLarge)) {
			WarningDialog("请选择头像名片");
			return;
		}
		if (StringUtils.empty(hash)) {
			try {
				uploadToQiniu();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			return;
		}
		else {
			loadingPd = UIHelper.showProgress(this, null, null, true);
			AppClient.setAvatar(code, sign, hash, clientCallback);
		}
	}
	
	private void uploadToQiniu() throws InterruptedException {
		photoQueue.clear();
		photoQueue.put(new FunsPhotoEntity(theLarge, "1"));
		if (StringUtils.empty(uploadToken)) {
			loadingPd = UIHelper.showProgress(this, null, null, true);
			getQiniuToken();
		}
		else {
			upload(photoQueue.poll());
		}
	}
	private void getQiniuToken() {
		AppClient.getQiniuToken(new FileCallback() {
			
			@Override
			public void onSuccess(String filePath) {
				UIHelper.dismissProgress(loadingPd);
				uploadToken = filePath;
				upload(photoQueue.poll());
			}
			
			@Override
			public void onFailure(String message) {
				// TODO Auto-generated method stub
				UIHelper.dismissProgress(loadingPd);
			}
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				UIHelper.dismissProgress(loadingPd);
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
				hash = resp.optString("key", "");
				avatar = "http://pbwci.qiniudn.com/"+hash;
				if (!photoQueue.isEmpty()) {
					upload(photoQueue.poll());
				}
				else {
					loadingPd = UIHelper.showProgress(UploadAvatar.this, null, null, true);
					AppClient.setAvatar(code, sign, hash, clientCallback);
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
	
	private ClientCallback clientCallback = new ClientCallback() {
		
		@Override
		public void onSuccess(Entity data) {
			UIHelper.dismissProgress(loadingPd);
			Result result = (Result) data;
			switch (result.getError_code()) {
			case Result.RESULT_OK:
				
				UIHelper.ToastMessage(UploadAvatar.this, R.layout.toastmessage_text, "头像提交成功", Toast.LENGTH_SHORT);
				setResult(RESULT_OK, new Intent().putExtra("avatar", avatar));
				AppManager.getAppManager().finishActivity(UploadAvatar.this);
				break;

			default:
				UIHelper.ToastMessage(UploadAvatar.this, R.layout.toastmessage_text, result.getMessage(), Toast.LENGTH_SHORT);
				break;
			}
			
//			KeyValue entity = (KeyValue) data;
			
//			Intent intent = new Intent(CreateTopic.this, QYWebView.class);
//			intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, entity.value);
//			startActivityForResult(intent, CommonValue.PhonebookViewUrlRequest.editPhoneview);
		}
		
		@Override
		public void onFailure(String message) {
			UIHelper.dismissProgress(loadingPd);
			WarningDialog("资料上传失败，请重试", "确定", "取消", new DialogClickListener() {
				
				@Override
				public void ok() {
					loadingPd = UIHelper.showProgress(UploadAvatar.this, null, null, true);
					AppClient.cardVIP(code, sign, hash, clientCallback);
				}
				
				@Override
				public void cancel() {
					
				}
			});
		}
		
		@Override
		public void onError(Exception e) {
			UIHelper.dismissProgress(loadingPd);
			WarningDialogAndOpenWechat("bibi100", "资料上传失败，请联系微信客服bibi100");
		}
	};

}
