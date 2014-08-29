package ui;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.crypto.spec.SecretKeySpec;

import org.apache.http.client.params.AuthPolicy;
import org.json.JSONObject;

import qiniu.auth.JSONObjectRet;
import qiniu.io.IO;
import qiniu.io.PutExtra;
import qiniu.utils.AuthException;
import qiniu.utils.Config;
import qiniu.utils.DigestAuth;
import qiniu.utils.Mac;
import qiniu.utils.PutPolicy;
import tools.AppManager;
import tools.ImageUtils;
import tools.Logger;
import bean.FunsPhotoEntity;
import bean.QiniuPutPolicy;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.zxing.common.StringUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.DiscCacheUtil;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.vikaa.allcontact.R;

import config.AppClient;
import config.AppClient.FileCallback;
import config.CommonValue.DisplayOptions;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

public class CreateActivityPreview extends AppActivity{
	private TextView contentPreviewTV;
	private String content;
	private BlockingQueue<FunsPhotoEntity> photoQueue = new LinkedBlockingQueue<FunsPhotoEntity>();
	
	private String uploadToken;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_activity_preview);
		content = getIntent().getExtras().getString("content");
		Logger.i(content);
		
		
		initUI();
		getQiniuToken();
	}
	
	private void initUI() {
		contentPreviewTV = (TextView) findViewById(R.id.contentPreview);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		contentPreviewTV.setText(Html.fromHtml(content, imageGetter, null));
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			AppManager.getAppManager().finishActivity(this);
			break;

		case R.id.rightBarButton:
			if (!photoQueue.isEmpty()) {
				upload(photoQueue.poll());
//				upload(photoQueue.poll().filePath, "a");
			}
			
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
					Logger.i(filePath);
					photoQueue.put(new FunsPhotoEntity(filePath, ""));;
					Bitmap bitmap = ImageUtils.getBitmapByPath(filePath, null, screeWidth);
					Drawable drawable = new BitmapDrawable(bitmap);
			        drawable.setBounds(-1, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
					return drawable;
				} catch (Exception e) {
					Crashlytics.logException(e);
					return null;
				}
			} 
			else if (f.equals("2")) {
				try {
					Logger.i(filePath);
					Drawable drawable;
					File file = DiscCacheUtil.findInCache(filePath, imageLoader.getDiscCache());
					if (file != null) {
						Bitmap bitmap = ImageUtils.getBitmapByPath(file.getAbsolutePath(), null, screeWidth);
						drawable = new BitmapDrawable(bitmap);
				        drawable.setBounds(-1, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
					}
					else {
						drawable = getResources().getDrawable(R.drawable.content_image_loading);
				        drawable.setBounds(-1, 0, 200, 200);
				        getPhotoFromQiniu(filePath);
					}
					return drawable;
				} catch (Exception e) {
					Crashlytics.logException(e);
					return null;
				}
			}
			else {
				Logger.i(filePath);
				return null;
			}
		}
	};
	
	private void getQiniuToken() {
		AppClient.getQiniuToken(new FileCallback() {
			
			@Override
			public void onSuccess(String filePath) {
				uploadToken = filePath;
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
		String key = IO.UNDEFINED_KEY; 
		PutExtra extra = new PutExtra();
		extra.params = new HashMap<String, String>();
		IO.putFile(uploadToken, key, new File(photo.filePath), extra, new JSONObjectRet() {
			@Override
			public void onProcess(long current, long total) {
				
				float percent = (float) (current*1.0/total)*100;
//				if ((int)percent < 100) {
//					avatarTV.setText((int)percent+"%");
//				}
//				else if ((int)percent == 100) {
//					avatarTV.setText("处理中...");
//				}
			}

			@Override
			public void onSuccess(JSONObject resp) {
				Logger.i(resp.toString());
				String key = resp.optString("key", "");
				photo.fileUrl = "http://pbwci.qiniudn.com/"+key;
				content = content.replace("1:"+photo.filePath, "2:"+photo.fileUrl);
				if (!photoQueue.isEmpty()) {
					upload(photoQueue.poll());
				}
				else {
					contentPreviewTV.setText(Html.fromHtml(content, imageGetter, null));
				}
			}

			@Override
			public void onFailure(Exception ex) {
				Logger.i(ex.toString());
				if (!photoQueue.isEmpty()) {
					upload(photoQueue.poll());
				}
				else {
					contentPreviewTV.setText(Html.fromHtml(content, imageGetter, null));
				}
			}
		});
	}
	
	private void getPhotoFromQiniu(final String uri) {
		imageLoader.loadImage(uri, DisplayOptions.default_options, new ImageLoadingListener() {
			
			@Override
			public void onLoadingStarted(String imageUri, View view) {
				
			}
			
			@Override
			public void onLoadingFailed(String imageUri, View view,
					FailReason failReason) {
				
			}
			
			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				Logger.i(imageUri);
				File file = DiscCacheUtil.findInCache(imageUri, imageLoader.getDiscCache());
				if (file != null) {
					Logger.i(file.getAbsolutePath());
					content = content.replace("2:"+imageUri, "1:"+file.getAbsolutePath());
					Logger.i(content);
					contentPreviewTV.setText(Html.fromHtml(content, imageGetter, null));
				}
			}
			
			@Override
			public void onLoadingCancelled(String imageUri, View view) {
				
			}
		});
	}
	
	
}
