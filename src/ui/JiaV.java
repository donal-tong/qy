package ui;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONObject;

import qiniu.auth.JSONObjectRet;
import qiniu.io.IO;
import qiniu.io.PutExtra;
import tools.AppManager;
import tools.HTMLUtil;
import tools.ImageUtils;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;
import ui.AppActivity.DialogClickListener;
import bean.Entity;
import bean.FunsPhotoEntity;
import bean.KeyValue;
import bean.Result;

import com.crashlytics.android.Crashlytics;
import com.vikaa.wecontact.R;

import config.AppClient;
import config.CommonValue;
import config.AppClient.ClientCallback;
import config.AppClient.FileCallback;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class JiaV extends AppActivity{
	private RelativeLayout mingpianLayout;
	private ImageView mingpianIV;
	private TextView mingpianTV;
	
	private RelativeLayout idLayout;
	private ImageView idIV;
	private TextView idTV;
	private Button mingpianButton;
	private Button idButton;
	private int type;
	String theLarge;
	
	private String mingpianPath;
	private String idPath;
	
	private String mingpianFile;
	private String idFile;
	
	private String code;
	private String uploadToken;
	
	private BlockingQueue<FunsPhotoEntity> photoQueue = new LinkedBlockingQueue<FunsPhotoEntity>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.jiav);
		initUI();
		code = getIntent().getStringExtra("code");
		uploadToken = getIntent().getStringExtra("token");
	}
	
	private void initUI() {
		mingpianLayout = (RelativeLayout) findViewById(R.id.mingpianLayout);
		mingpianIV = (ImageView) findViewById(R.id.mingpian);
		mingpianTV = (TextView) findViewById(R.id.mingpianProgress);
		idLayout = (RelativeLayout) findViewById(R.id.idLayout);
		idIV = (ImageView) findViewById(R.id.id);
		idTV = (TextView) findViewById(R.id.idProgress);
		mingpianButton = (Button) findViewById(R.id.mingpianbutton);
		idButton = (Button) findViewById(R.id.idbutton);
		
		int w = ImageUtils.getDisplayWidth(this) - ImageUtils.dip2px(this, 40);
		int h = w*320/568;
		
		LayoutParams layout = (LayoutParams) mingpianLayout.getLayoutParams();
		layout.width = w;
		layout.height = h;
		mingpianLayout.setLayoutParams(layout);
		
		LayoutParams layout2 = (LayoutParams) idLayout.getLayoutParams();
		layout2.width = w;
		layout2.height = h;
		idLayout.setLayoutParams(layout2);
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
			type = 0;
			PhotoChooseOption();
			break;
			
		case R.id.idbutton:
			type = 1;
			PhotoChooseOption();
			break;
		}
	}
	
	private void submit() {
		if (StringUtils.empty(mingpianPath) || StringUtils.empty(idPath)) {
			WarningDialog("请先上传完名片和身份证图片");
			return;
		}
		if (StringUtils.empty(mingpianFile) || StringUtils.empty(idFile)) {
			try {
				uploadToQiniu();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			return;
		}
		else {
			loadingPd = UIHelper.showProgress(this, null, null, true);
			AppClient.cardVIP(code, idFile, mingpianFile, clientCallback);
		}
	}
	
	private void uploadToQiniu() throws InterruptedException {
		photoQueue.clear();
		photoQueue.put(new FunsPhotoEntity(mingpianPath, "1"));
		photoQueue.put(new FunsPhotoEntity(idPath, "2"));
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
				Logger.i(uploadToken);
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
				Logger.i(resp.toString());
				UIHelper.dismissProgress(loadingPd);
				String hash = resp.optString("key", "");
				if (photo.fileUrl.equals("1")) {
					mingpianFile = hash;
				}
				else {
					idFile = hash;
				}
				if (!photoQueue.isEmpty()) {
					upload(photoQueue.poll());
				}
				else {
					Logger.i(mingpianFile + " " + idFile);
					loadingPd = UIHelper.showProgress(JiaV.this, null, null, true);
					AppClient.cardVIP(code, idFile, mingpianFile, clientCallback);
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
					ImageUtils.saveImageToSD(imagePathAfterCompass, ImageUtils.getSmallBitmap(theLarge, 300), 90);
					ExifInterface exif = new ExifInterface(imagePathAfterCompass);
					exif.setAttribute(ExifInterface.TAG_ORIENTATION, orientation);
				    exif.saveAttributes();
					newPhotoPath = imagePathAfterCompass;
					upload(type, newPhotoPath);
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
				ImageUtils.saveImageToSD(imagePathAfterCompass, ImageUtils.getSmallBitmap(newPhotoPath, 300), 90);
				ExifInterface exif = new ExifInterface(imagePathAfterCompass);
				exif.setAttribute(ExifInterface.TAG_ORIENTATION, orientation);
			    exif.saveAttributes();
				newPhotoPath = imagePathAfterCompass;
				upload(type, newPhotoPath);
			} catch (IOException e) {
				Crashlytics.logException(e);
			}
			break;
		}
	}
	
	private void upload(int type, String path) {
		switch (type) {
		case 0:
			mingpianPath = path;
			this.imageLoader.displayImage("file://"+path, mingpianIV, CommonValue.DisplayOptions.default_options);
			break;

		case 1:
			idPath = path;
			this.imageLoader.displayImage("file://"+path, idIV, CommonValue.DisplayOptions.default_options);
			break;
		}
	}
	
	
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
								UIHelper.ToastMessage(JiaV.this, "无法保存照片，请检查SD卡是否挂载", Toast.LENGTH_SHORT);
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
	
	private ClientCallback clientCallback = new ClientCallback() {
		
		@Override
		public void onSuccess(Entity data) {
			UIHelper.dismissProgress(loadingPd);
			Result result = (Result) data;
			switch (result.getError_code()) {
			case Result.RESULT_OK:
				UIHelper.ToastMessage(JiaV.this, R.layout.toastmessage_text, "资料提交成功，等待验证", Toast.LENGTH_SHORT);
				setResult(RESULT_OK, new Intent().putExtra("code", code));
				AppManager.getAppManager().finishActivity(JiaV.this);
				break;

			default:
				UIHelper.ToastMessage(JiaV.this, R.layout.toastmessage_text, result.getMessage(), Toast.LENGTH_SHORT);
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
					loadingPd = UIHelper.showProgress(JiaV.this, null, null, true);
					AppClient.cardVIP(code, idFile, mingpianFile, clientCallback);
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
