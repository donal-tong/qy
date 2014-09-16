package config;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import baidupush.Utils;
import bean.*;
import com.crashlytics.android.Crashlytics;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import contact.MobileSynListBean;
import db.manager.WeFriendManager;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;
import tools.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppClient {
	
    public interface ClientCallback{
        abstract void onSuccess(Entity data);
        abstract void onFailure(String message);
        abstract void onError(Exception e);
    }
    
    
    private static void saveCache(MyApplication appContext, String key, Entity entity) {
    	appContext.saveObject(entity, String.format("%s-%s", key, appContext.getLoginUid()));
    }
	
    public static void getVertifyCode(final MyApplication appContext, String mobile, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		params.add("phone", mobile);
		QYRestClient.post("user/send", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				handleCode(content, callback);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				callback.onFailure("网络不给力，请重新尝试");
			}
		});
	}
    public static void handleCode(final byte[] content, final ClientCallback callback) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					callback.onSuccess((CodeEntity)msg.obj);
					break;
				default:
					callback.onError((Exception)msg.obj);
					break;
				}
			}
		};
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Message msg = new Message();
				try {
					String target = new String(content);
					String decode = DecodeUtil.decode(target);
					target = null;
					CodeEntity data = CodeEntity.parse(decode);
					decode = null;
					msg.obj = data;
					msg.what = 1;
				} catch (Exception e) {
					e.printStackTrace();
					msg.obj = e;
					msg.what = -1;
				}
				handler.sendMessage(msg);
			}
		});
	}
    
	public static void vertifiedCode(final MyApplication appContext, String code, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		params.add("code", code);
		QYRestClient.post("user/wechatlogin", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				handleVertifiedCode(content, callback);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				callback.onFailure("网络不给力，请重新尝试");
			}
		});
	}
	public static void handleVertifiedCode(final byte[] content, final ClientCallback callback) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					callback.onSuccess((UserEntity)msg.obj);
					break;
				default:
					callback.onError((Exception)msg.obj);
					break;
				}
			}
		};
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Message msg = new Message();
				try {
					String target = new String(content);
					String decode = DecodeUtil.decode(target);
					target = null;
					UserEntity data = UserEntity.parse(decode);
					decode = null;
					msg.obj = data;
					msg.what = 1;
				} catch (Exception e) {
					e.printStackTrace();
					msg.obj = e;
					msg.what = -1;
				}
				handler.sendMessage(msg);
			}
		});
	}
	
	public static void vertifiedCode(final MyApplication appContext, String code, String mobile, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		params.add("code", code);
		params.add("phone", mobile);
		QYRestClient.post("user/login", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				handleVertifiedCode(content, callback);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				callback.onFailure("网络不给力，请重新尝试");
			}
		});
	}
	
	
	public static void autoLogin(final MyApplication appContext, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		params.add("hash", appContext.getLoginHashCode());
		params.add("client_browser", "android");
		try {
			params.add("client_version", AppManager.getAppManager().currentActivity().getPackageManager().getPackageInfo(AppManager.getAppManager().currentActivity().getPackageName(), 0).versionCode+"");
		} catch (Exception e) {
			e.printStackTrace();
		}
		params.add("client_push", appContext.getMessageInterupt());
		params.add("push_device_type", "3");
		QYRestClient.post("user/autologin", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				handleVertifiedCode(content, callback);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				callback.onFailure("网络不给力，请重新尝试");
			}
		});
	}
	
	public static void Logout(final MyApplication appContext) {
		QYRestClient.post("user/logout", null, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
			}
		});
	}
	
	public static void setUser(final Context context, final String baiduUserId, String baiduChannelId, String client_push) {
		RequestParams params = new RequestParams();
		params.add("client_browser", "android");
		try {
			params.add("client_version", AppManager.getAppManager().currentActivity().getPackageManager().getPackageInfo(AppManager.getAppManager().currentActivity().getPackageName(), 0).versionCode+"");
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (StringUtils.notEmpty(client_push)) {
			params.add("client_push", client_push);
		}
		if (StringUtils.notEmpty(baiduUserId)) {
			params.add("push_user_id", baiduUserId);
		}
		if (StringUtils.notEmpty(baiduChannelId)) {
			params.add("push_channel_id", baiduChannelId);
		}
		params.add("push_device_type", "3");
		QYRestClient.post("user/set", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				if (StringUtils.notEmpty(baiduUserId)) {
					Utils.setBind(context, true);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
			}
		});
	}
	
	public static void setAvatar(String code, String sign, String key, final ClientCallback callback) {
		RequestParams param = new RequestParams();
		param.put("code", code);
//		param.put("sign", sign);
		param.put("key", key);
		QYRestClient.post("card/setAvatar", param, new AsyncHttpResponseHandler() {
			
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				try {
					JSONObject js = new JSONObject(DecodeUtil.decode(new String(responseBody)));
					Result result = new Result();
					if (js.getInt("status") == 1) {
						result.setError_code(Result.RESULT_OK);
					}
					else {
						result.setError_code(10);
						result.setMessage(js.getString("info"));
					}
					callback.onSuccess(result);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
			    Crashlytics.logException(error);
			}
		});
	}
	
	public static void regUser(final MyApplication appContext, String phone, String password, String realname, String department, String position, String email, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		params.add("phone", phone);
		params.add("password", password);
		params.add("realname", realname);
		params.add("department", department);
		params.add("position", position);
		params.add("email", email);
		QYRestClient.post("user/reg"+"?_sign="+appContext.getLoginSign(), params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				handleRegUser(content, callback);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				callback.onFailure("网络不给力，请重新尝试");
			}
		});
	}
	public static void handleRegUser(final byte[] content, final ClientCallback callback) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					callback.onSuccess((RegUserEntity)msg.obj);
					break;
				default:
					callback.onError((Exception)msg.obj);
					break;
				}
				
			}
		};
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Message msg = new Message();
				try {
					String target = new String(content);
					String decode = DecodeUtil.decode(target);
					target = null;
					RegUserEntity data = RegUserEntity.parse(decode);
					decode = null;
					msg.obj = data;
					msg.what = 1;
				} catch (Exception e) {
					e.printStackTrace();
					msg.obj = e;
					msg.what = -1;
				}
				handler.sendMessage(msg);
			}
		});
	}
	
	public static void getChaterBy(final MyApplication appContext, final String openId, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		params.add("openid", openId);
		QYRestClient.post("user/info"+"?_sign="+appContext.getLoginSign(), params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				handleChater(content, callback, openId, appContext);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				callback.onFailure("网络不给力，请重新尝试");
			}
		});
	}
	public static void handleChater(final byte[] content, final ClientCallback callback, final String openId, final MyApplication appContext) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					callback.onSuccess((ChatterEntity)msg.obj);
					break;

				default:
					callback.onError((Exception)msg.obj);
					break;
				}
				
			}
		};
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Message msg = new Message();
				try {
					String target = new String(content);
					String decode = DecodeUtil.decode(target);
					target = null;
					ChatterEntity data = ChatterEntity.parse(decode);
					saveCache(appContext, CommonValue.CacheKey.ChatterInfo+"-"+openId, data);
					decode = null;
					msg.obj = data;
					msg.what = 1;
				} catch (Exception e) {
					e.printStackTrace();
					msg.obj = e;
					msg.what = -1;
				}
				handler.sendMessage(msg);
			}
		});
	}
	
	//创建通讯录
	//创建活动
		public static void createPhonebook(final MyApplication appContext, 
				String title, 
				String category,
				String logo,
				String content, 
				String privacy,
				String custom,
				String custom_display,
				String required,
				String addfriend,
				final ClientCallback callback) {
			RequestParams params = new RequestParams();
			params.add("title", title);
			params.add("category", category);
			params.add("logo", logo);
			params.add("content", content);
			if (StringUtils.notEmpty(privacy)) {
				params.add("privacy", privacy);
			}
//			if (StringUtils.notEmpty(custom)) {
				params.add("custom", custom);
//			}
//			if (StringUtils.notEmpty(custom_display)) {
				params.add("custom_display", custom_display);
//			}
//			if (StringUtils.notEmpty(required)) {
				params.add("required", required);
//			}
//			if (StringUtils.notEmpty(addfriend)) {
				params.add("addfriend", addfriend);
//			}
			QYRestClient.post("phonebook/create"+"?_sign="+appContext.getLoginSign(), params, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(int statusCode, Header[] headers, byte[] content) {
					handlePhonebookCreated(content, callback, appContext);
				}
				@Override
				public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
					if (appContext.isNetworkConnected()) {
						callback.onFailure(e.getMessage());
					}
				}
			});
		}
		public static void handlePhonebookCreated(final byte[] content, final ClientCallback callback, final MyApplication appContext) {
			final Handler handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case 1:
						callback.onSuccess((PhonebookCreateEntity)msg.obj);
						break;

					default:
						callback.onError((Exception)msg.obj);
						break;
					}
				}
			};
			ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
			singleThreadExecutor.execute(new Runnable() {
				@Override
				public void run() {
					Message msg = new Message();
					try {
						String target = new String(content);
						String decode = DecodeUtil.decode(target);
						target = null;
                        PhonebookCreateEntity data = PhonebookCreateEntity.parse(decode);
						decode = null;
						msg.what = 1;
						msg.obj = data;
					} catch (Exception e) {
						e.printStackTrace();
						Crashlytics.logException(e);
						msg.what = -1;
						msg.obj = e;
					}
					handler.sendMessage(msg);
				}
			});
		}
		
	//通讯录列表
	public static void getPhoneList(final MyApplication appContext, final ClientCallback callback) {
		QYRestClient.post("phonebook/lists"+"?_sign="+appContext.getLoginSign(), null, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				handlePhonelist(content, callback, appContext);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				callback.onFailure("网络不给力，请重新尝试");
			}
		});
	}
	public static void handlePhonelist(final byte[] content, final ClientCallback callback, final MyApplication appContext) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					callback.onSuccess((PhoneListEntity)msg.obj);
					break;

				default:
					callback.onError((Exception)msg.obj);
					break;
				}
			}
		};
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Message msg = new Message();
				try {
					String target = new String(content);
					String decode = DecodeUtil.decode(target);
					target = null;
					PhoneListEntity data = PhoneListEntity.parse(decode);
					saveCache(appContext, CommonValue.CacheKey.PhoneList, data);
					decode = null;
					msg.what = 1;
					msg.obj = data;
					
				} catch (Exception e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}
				handler.sendMessage(msg);
			}
		});
	}
	
	public static void getPhoneView(final MyApplication appContext, final String code, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		params.add("code", code);
		QYRestClient.post("phonebook/view", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					PhoneViewEntity data = PhoneViewEntity.parse(DecodeUtil.decode(new String(content)));
					saveCache(appContext, CommonValue.CacheKey.PhoneView+"-"+code, data);
					callback.onSuccess(data);
				}catch (Exception e) {
					callback.onError(e);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				callback.onFailure("网络不给力，请重新尝试");
			}
		});
	}
	
	public static void setPhonebookRole(final MyApplication appContext,String code, String openid, String role,final ClientCallback callback) {
		RequestParams params = new RequestParams();
		params.add("code", code);
		params.add("openid", openid);
		params.add("role", role);
		QYRestClient.post("phonebook/setrole", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					CodeEntity data = CodeEntity.parse(DecodeUtil.decode(new String(content)));
					callback.onSuccess(data);
				}catch (Exception e) {
					callback.onError(e);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				callback.onFailure("网络不给力，请重新尝试");
			}
		});
	}
	
	public static void setPhonebookPassmember(final MyApplication appContext,String code, String openid, String state, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		params.add("code", code);
		params.add("openid", openid);
		params.add("state", state);
		QYRestClient.post("phonebook/passmember", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					CodeEntity data = CodeEntity.parse(DecodeUtil.decode(new String(content)));
					callback.onSuccess(data);
				}catch (Exception e) {
					callback.onError(e);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
	
	//公开通讯录
	public static void getPhoneSquareList(final MyApplication appContext, final String page, final String keyword, final ClientCallback callback) {
		RequestParams params = new RequestParams();
//		if (StringUtils.notEmpty(page)) {
//			params.add("page", page);
//		}
//		if (StringUtils.notEmpty(keyword)) {
//			params.add("keyword", keyword);
//		}
		params.add("count", "5");
		QYRestClient.post("phonebook/random"+"?_sign="+appContext.getLoginSign(), null, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				handleSquarelist(content, callback, appContext, page, keyword);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				callback.onFailure("网络不给力，请重新尝试");
			}
		});
	}
	public static void handleSquarelist(final byte[] content, final ClientCallback callback, final MyApplication appContext, final String page, final String keyword) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					callback.onSuccess((RecommendListEntity)msg.obj);
					break;

				default:
					callback.onError((Exception)msg.obj);
					break;
				}
				
			}
		};
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Message msg = new Message();
				try {
					String target = new String(content);
					String decode = DecodeUtil.decode(target);
					target = null;
					RecommendListEntity data = RecommendListEntity.parseSquare(decode);
					if (StringUtils.notEmpty(page) && page.equals("1") && StringUtils.empty(keyword)) {
						saveCache(appContext, CommonValue.CacheKey.SquareList, data);
					}
					decode = null;
					msg.obj = data;
					msg.what = 1;
				} catch (Exception e) {
					e.printStackTrace();
					msg.obj = e;
					msg.what = -1;
				}
				handler.sendMessage(msg);
			}
		});
	}
	
	//活动列表
	public static void getActivityList(final MyApplication appContext, final ClientCallback callback) {
		QYRestClient.post("activity/lists"+"?_sign="+appContext.getLoginSign(), null, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				handleActivitylist(content, callback, appContext);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
	public static void handleActivitylist(final byte[] content, final ClientCallback callback, final MyApplication appContext) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					callback.onSuccess((ActivityListEntity)msg.obj);
					break;

				default:
					callback.onError((Exception)msg.obj);
					break;
				}
			}
		};
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Message msg = new Message();
				try {
					String target = new String(content);
					String decode = DecodeUtil.decode(target);
					target = null;
					ActivityListEntity data = ActivityListEntity.parse(decode);
					saveCache(appContext, CommonValue.CacheKey.ActivityList, data);
					decode = null;
					msg.what = 1;
					msg.obj = data;
				} catch (Exception e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}
				handler.sendMessage(msg);
			}
		});
	}
	
	//创建活动
	public static void createActivity(final MyApplication appContext, 
			String title, 
			String logo,
			String content, 
			String begin_at,
			String city_code,
			String address,
			String latlng,
			String privacy,
			String type,
			String cost,
			String guests,
			String punish,
			String max,
			String advisory,
			String custom,
			final ClientCallback callback) {
		RequestParams params = new RequestParams();
		params.add("title", title);
		Logger.i(logo);
		params.add("logo", logo);
		params.add("content", content);
		params.add("begin_at", begin_at);
		if (StringUtils.notEmpty(city_code)) {
			params.add("city_code", city_code);
		}
		if (StringUtils.notEmpty(address)) {
			params.add("address", address);
		}
		if (StringUtils.notEmpty(latlng)) {
			params.add("latlng", latlng);
		}
		if (StringUtils.notEmpty(privacy)) {
			params.add("privacy", privacy);
		}
		if (StringUtils.notEmpty(type)) {
			params.add("type", type);
		}
		if (StringUtils.notEmpty(cost)) {
			params.add("cost", cost);
		}
		if (StringUtils.notEmpty(guests)) {
			params.add("guests", guests);
		}
		if (StringUtils.notEmpty(punish)) {
			params.add("punish", punish);
		}
		if (StringUtils.notEmpty(max)) {
			params.add("max", max);
		}
		if (StringUtils.notEmpty(advisory)) {
			params.add("advisory", advisory);
		}
		if (StringUtils.notEmpty(custom)) {
			params.add("custom", custom);
			params.add("custom_display", "1");
			Logger.i(custom);
		}
        Logger.i(type);
		QYRestClient.post("activity/save"+"?_sign="+appContext.getLoginSign(), params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				handleActivityCreated(content, callback, appContext);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
	public static void handleActivityCreated(final byte[] content, final ClientCallback callback, final MyApplication appContext) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					callback.onSuccess((ActivityCreateEntity)msg.obj);
					break;

				default:
					callback.onError((Exception)msg.obj);
					break;
				}
			}
		};
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Message msg = new Message();
				try {
					String target = new String(content);
					String decode = DecodeUtil.decode(target);
					target = null;
					ActivityCreateEntity data = ActivityCreateEntity.parse(decode);
					decode = null;
					msg.what = 1;
					msg.obj = data;
				} catch (Exception e) {
					e.printStackTrace();
					Crashlytics.logException(e);
					msg.what = -1;
					msg.obj = e;
				}
				handler.sendMessage(msg);
			}
		});
	}
	
	//活动详情	
	public static void getActivityView(final MyApplication appContext, final String code, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		params.add("code", code);
		QYRestClient.post("activity/view", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					ActivityViewEntity data = ActivityViewEntity.parse(DecodeUtil.decode(new String(content)));
					saveCache(appContext, CommonValue.CacheKey.ActivityView+"-"+code, data);
					callback.onSuccess(data);
				}catch (Exception e) {
					callback.onError(e);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
	
	//我的名片
	public static void getCardList(final MyApplication appContext, final ClientCallback callback) {
		QYRestClient.post("card/lists"+"?_sign="+appContext.getLoginSign(), null, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				handleCardlist(content, callback, appContext);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
	public static void handleCardlist(final byte[] content, final ClientCallback callback, final MyApplication appContext) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					callback.onSuccess((CardListEntity)msg.obj);
					break;

				default:
					callback.onError((Exception)msg.obj);
					break;
				}
			}
		};
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Message msg = new Message();
				try {
					String target = new String(content);
					String decode = DecodeUtil.decode(target);
					target = null;
					CardListEntity data = CardListEntity.parse(decode);
					saveCache(appContext, CommonValue.CacheKey.CardList, data);
					decode = null;
					msg.what = 1;
					msg.obj = data;
				} catch (Exception e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}
				handler.sendMessage(msg);
			}
		});
	}
	
	public static void getCard(final MyApplication appContext, String code, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		params.add("code", code);
		QYRestClient.post("card/info", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				handleCardInfo(content, callback);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
	public static void handleCardInfo(final byte[] content, final ClientCallback callback) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					callback.onSuccess((CardIntroEntity)msg.obj);
					break;

				default:
					callback.onError((Exception)msg.obj);
					break;
				}
			}
		};
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Message msg = new Message();
				try {
					String target = new String(content);
					String decode = DecodeUtil.decode(target);
					target = null;
					CardIntroEntity data = CardIntroEntity.parse(decode);
					decode = null;
					msg.what = 1;
					msg.obj = data;
				} catch (Exception e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}
				handler.sendMessage(msg);
			}
		});
	}
	
	@Deprecated
	public static void getFriendCard(final MyApplication appContext, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		QYRestClient.post("card/friend", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					FriendCardListEntity data = FriendCardListEntity.parse(DecodeUtil.decode(new String(content)));
					saveCache(appContext, CommonValue.CacheKey.FriendCardList, data);
					callback.onSuccess(data);
				}catch (Exception e) {
					callback.onError(e);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
	
	public static void getChatFriendCard(final Context context, final MyApplication appContext, final String page, final String keyword, String count, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		if (StringUtils.notEmpty(page)) {
			params.add("page", page);
		}
		if (StringUtils.notEmpty(keyword)) {
			params.add("keyword", keyword);
		}
		if (StringUtils.notEmpty(count)) {
			params.add("count", count);
		}
		QYRestClient.post(context, "card/friendlist"+"?_sign="+appContext.getLoginSign(), params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				handleChatFriendCard(context, content, callback);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				callback.onFailure("网络不给力，请重新尝试");
			}
		});
	}
	public static void handleChatFriendCard(final Context context, final byte[] content, final ClientCallback callback) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					callback.onSuccess((FriendCardListEntity)msg.obj);
					break;

				default:
					callback.onError((Exception)msg.obj);
					break;
				}
			}
		};
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Message msg = new Message();
				try {
					String target = new String(content);
					String decode = DecodeUtil.decode(target);
					target = null;
					FriendCardListEntity data = FriendCardListEntity.parseF(decode);
					WeFriendManager.getInstance(context).saveWeFriends(data.u);
					decode = null;
					msg.what = 1;
					msg.obj = data;
					
				} catch (Exception e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}
				handler.sendMessage(msg);
			}
		});
	}
	
	public static void searchFriendCard(Context context, final MyApplication appContext, final String page, final String keyword, String count, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		if (StringUtils.notEmpty(page)) {
			params.add("page", page);
		}
		if (StringUtils.notEmpty(keyword)) {
			params.add("keyword", keyword);
		}
		if (StringUtils.notEmpty(count)) {
			params.add("count", count);
		}
		QYRestClient.post(context, "network/search"+"?_sign="+appContext.getLoginSign(), params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				handleSearchFriendCard(content, callback);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				callback.onFailure("网络不给力，请重新尝试");
			}
		});
	}
	public static void handleSearchFriendCard(final byte[] content, final ClientCallback callback) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					callback.onSuccess((FriendCardListEntity)msg.obj);
					break;

				default:
					callback.onError((Exception)msg.obj);
					break;
				}
			}
		};
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Message msg = new Message();
				try {
					String target = new String(content);
					String decode = DecodeUtil.decode(target);
					target = null;
					FriendCardListEntity data = FriendCardListEntity.parseSearch(decode);
					decode = null;
					msg.what = 1;
					msg.obj = data;
					
				} catch (Exception e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}
				handler.sendMessage(msg);
			}
		});
	}
	
	public static void getAllOpenid(Context context, final MyApplication appContext, final ClientCallback callback) {
		QYRestClient.post(context, "card/summary"+"?_sign="+appContext.getLoginSign(), null, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
					handleAllOpenidRes(content, callback);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				callback.onFailure("网络不给力，请重新尝试");
			}
		});
	}
	public static void handleAllOpenidRes(final byte[] content, final ClientCallback callback) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					callback.onSuccess((OpenidListEntity)msg.obj);
					break;

				default:
					callback.onError((Exception)msg.obj);
					break;
				}
				
			}
		};
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Message msg = new Message();
				try {
					String target = new String(content);
					String decode = DecodeUtil.decode(target);
					target = null;
					OpenidListEntity data = OpenidListEntity.parse(decode);
					decode = null;
					msg.obj = data;
					msg.what = 1;
				} catch (Exception e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}
				handler.sendMessage(msg);
			}
		});
	}
	
	public static void getAllWeFriendByOpenid(Context context, final MyApplication appContext, String json, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		params.add("openids", json);
		QYRestClient.post(context, "card/friendinfo"+"?_sign="+appContext.getLoginSign(), params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					Logger.i("all1");
					handleAllWeFriendByOpenid(content, callback);
				}catch (Exception e) {
					callback.onError(e);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				callback.onFailure("网络不给力，请重新尝试");
			}
		});
	}
	public static void handleAllWeFriendByOpenid(final byte[] content, final ClientCallback callback) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				
				switch (msg.what) {
				case 1:
					callback.onSuccess((FriendCardListEntity)msg.obj);
					break;

				default:
					callback.onError((Exception)msg.obj);
					break;
				}
			}
		};
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Message msg = new Message();
				try {
					String target = new String(content);
					String decode = DecodeUtil.decode(target);
					target = null;
					FriendCardListEntity data = FriendCardListEntity.parseF(decode);
					decode = null;
					msg.what = 1;
					msg.obj = data;
				} catch (Exception e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				} 
				handler.sendMessage(msg);
			}
		});
	}
	
	public static void followCard(final MyApplication appContext, String openid, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		params.add("openid", openid);
		QYRestClient.post("card/follow", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					CodeEntity data = CodeEntity.parse(DecodeUtil.decode(new String(content)));
					callback.onSuccess(data);
				}catch (Exception e) {
					callback.onError(e);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
	
	public static void getMessageList(final MyApplication appContext, final ClientCallback callback) {
		QYRestClient.post("message/index", null, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					MessageListEntity data = MessageListEntity.parse(DecodeUtil.decode(new String(content)));
					saveCache(appContext, CommonValue.CacheKey.MessageList, data);
					callback.onSuccess(data);
				}catch (Exception e) {
					callback.onError(e);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
	
	public static void getUnReadMessage(final MyApplication appContext, final ClientCallback callback) {
		QYRestClient.post("message/getnewsnumber"+"?_sign="+appContext.getLoginSign(), null, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					MessageUnReadEntity data = MessageUnReadEntity.parse(DecodeUtil.decode(new String(content)));
//					saveCache(appContext, CommonValue.CacheKey.MessageUnRead, data);
					callback.onSuccess(data);
				}catch (Exception e) {
					callback.onError(e);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
	
	public static void setMessageRead(MyApplication appContext) {
		QYRestClient.post("message/read"+"?_sign="+appContext.getLoginSign(), null, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {

			}
		});
	}
	
	@Deprecated
	public static void getRecommendList(final MyApplication appContext, final ClientCallback callback) {
		QYRestClient.post("recommend/lists", null, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					RecommendListEntity data = RecommendListEntity.parse(DecodeUtil.decode(new String(content)));
					callback.onSuccess(data);
				}catch (Exception e) {
					callback.onError(e);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
	
	public static void syncContact(final MyApplication appContext, String data, final ClientCallback callback) {
		RequestParams param = new RequestParams();
		param.add("data", data);
		QYRestClient.post("contact/sync"+"?_sign="+appContext.getLoginSign(), param, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				callback.onSuccess(new Entity() {
				});
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
	
	public static void sendFeedback(final MyApplication appContext, String data, final ClientCallback callback) {
		RequestParams param = new RequestParams();
		data += " | client_browser : android |";
		try {
			data += " | client_version :" + AppManager.getAppManager().currentActivity().getPackageManager().getPackageInfo(AppManager.getAppManager().currentActivity().getPackageName(), 0).versionCode+" |";
		} catch (Exception e) {
			e.printStackTrace();
		}
		data += " |client_push : android |";
		param.add("message", data);
		QYRestClient.post("feedback/send"+"?_sign="+appContext.getLoginSign(), param, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					callback.onSuccess(new Entity() {
						private static final long serialVersionUID = 1L;
					});
				}catch (Exception e) {
					callback.onError(e);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
	
	public static void update(final MyApplication appContext, String data, final ClientCallback callback) {
		RequestParams param = new RequestParams();
		QYRestClient.post("update/check", param, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				handleUpdate(content, callback);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				callback.onFailure("");
			}
		});
	}
	public static void handleUpdate(final byte[] content, final ClientCallback callback) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					callback.onSuccess((Update)msg.obj);
					break;

				default:
					callback.onError((Exception)msg.obj);
					break;
				}
			}
		};
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Message msg = new Message();
				try {
					String target = new String(content);
					String decode = DecodeUtil.decode(target);
					target = null;
					Update data = Update.parse(decode);
					decode = null;
					msg.what = 1;
					msg.obj = data;
				} catch (Exception e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				} 
				handler.sendMessage(msg);
			}
		});
	}
	
	public interface WebCallback{
        abstract void onSuccess(int type, Entity data, String key);
        abstract void onFailure(String message);
        abstract void onError(Exception e);
    }
	
	public static void loadURL(Context context, final MyApplication appContext, final String url, final WebCallback callback) {
		QYRestClient.getWeb(context, url+"?_sign="+appContext.getLoginSign(), null, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					String data = new String(content);
					String md5 = MD5Util.getMD5String(content);
					String key = String.format("%s-%s", MD5Util.getMD5String(url), appContext.getLoginUid());
					WebContent dc = (WebContent) appContext.readObject(key);
					WebContent con = new WebContent();
					con.text = data;
					con.md5 = md5;
					Logger.i(url);
					saveCache(appContext, MD5Util.getMD5String(url), con);
					if(dc == null){
						callback.onSuccess(0, con, MD5Util.getMD5String(url));
					}
					else {
						if (dc.md5.equals(con.md5)) {
							callback.onSuccess(2, con, MD5Util.getMD5String(url));
						}
						else {
							callback.onSuccess(1, con, MD5Util.getMD5String(url));
						}
					}
				}catch (Exception e) {
					callback.onError(e);
				}
//				handleLoadURL(content, callback, appContext, url);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				callback.onFailure(url);
			}
		});
	}
	public static void handleLoadURL(final byte[] content, final WebCallback callback, final MyApplication appContext, final String url) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				WebContent con;
				Exception e;
				switch (msg.what) {
				case 0:
					con = (WebContent) msg.obj;
					callback.onSuccess(0, con, MD5Util.getMD5String(url));
					break;
				case 1:
					con = (WebContent) msg.obj;
					callback.onSuccess(1, con, MD5Util.getMD5String(url));
					break;
				case 2:
					con = (WebContent) msg.obj;
					callback.onSuccess(2, con, MD5Util.getMD5String(url));
					break;
				default:
					e = (Exception) msg.obj;
					callback.onError(e);
					break;
				}
			}
		};
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Message msg = new Message();
				try{
					String data = new String(content);
					String md5 = MD5Util.getMD5String(content);
					String key = String.format("%s-%s", MD5Util.getMD5String(url), appContext.getLoginUid());
					WebContent dc = (WebContent) appContext.readObject(key);
					WebContent con = new WebContent();
					con.text = data;
					con.md5 = md5;
					saveCache(appContext, MD5Util.getMD5String(url), con);
					if(dc == null){
						msg.what = 0;
						msg.obj = con;
					}
					else {
						if (dc.md5.equals(con.md5)) {
							msg.what = 2;
							msg.obj = con;
						}
						else {
							msg.what = 1;
							msg.obj = con;
						}
					}
					
				}catch (Exception e) {
					msg.what = -1;
					msg.obj = e;
				}
				handler.sendMessage(msg);
			}
		});
	}
	
//	private void writeHtml2File(String fileName, String content) {
//		String savePath;
//		String htmlFilePath = "";
//		String storageState = Environment.getExternalStorageState();
//		if(storageState.equals(Environment.MEDIA_MOUNTED)){
//			savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/qy/";
//			File file = new File(savePath);
//			if(!file.exists()){
//				file.mkdirs();
//			}
//			htmlFilePath = savePath + fileName;
//			File ApkFile = new File(htmlFilePath);
//			try {
//				FileOutputStream outStream = new FileOutputStream(ApkFile);
//				outStream.write(content.getBytes());
//				outStream.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			
//		}
//	}
	
	public interface FileCallback{
        abstract void onSuccess(String filePath);
        abstract void onFailure(String message);
        abstract void onError(Exception e);
    }
	public static void downFile(Context context, final MyApplication appContext, final String url, final String format, final FileCallback callback) {
		handleDownloadFile(callback, appContext, url, format);
	}
	public static void handleDownloadFile(final FileCallback callback, final MyApplication appContext, final String url, final String format) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					callback.onSuccess((String)msg.obj);
					break;

				default:
					callback.onError((Exception)msg.obj);
					break;
				}
			}
		};
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Message msg = new Message();
				String storageState = Environment.getExternalStorageState();	
				String savePath = null;
				if(storageState.equals(Environment.MEDIA_MOUNTED)){
					savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/qy/";
					File dir = new File(savePath);
					if(!dir.exists()){
						dir.mkdirs();
					}
				}
				String md5FilePath = savePath + MD5Util.getMD5String(url) + format;
				File ApkFile = new File(md5FilePath);
				if(ApkFile.exists()){
					ApkFile.delete();
				}
				File tmpFile = new File(md5FilePath);
//				try {
//					FileOutputStream fos = new FileOutputStream(tmpFile);
//					fos.write(binaryData);
//					fos.close();
//					msg.what = 1;
//					msg.obj = md5FilePath;
//				} catch (Exception e) {
//					e.printStackTrace();
//					msg.what = -1;
//					msg.obj = e;
//					Logger.i(e);
//				} 
//				handler.sendMessage(msg);
				try {
					System.setProperty("http.keepAlive", "false");
					URL Url = new URL(url);
					URLConnection conn = Url.openConnection();
					conn.connect();
					InputStream is = conn.getInputStream();
					if (is == null) { // 没有下载流
						
					}
					FileOutputStream FOS = new FileOutputStream(tmpFile); 
                    Logger.i(conn.getContentLength()+"");
					byte buf[] = new byte[1024];
					// downLoadFilePosition = 0;
					int numread;
					while ((numread = is.read(buf)) != -1) {
						FOS.write(buf, 0, numread);
						// downLoadFilePosition += numread
					}
					is.close();
					FOS.flush();
					if (FOS != null) {
						FOS.close();
					}
					msg.what = 1;
					msg.obj = md5FilePath;

				} catch (Exception e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}
				handler.sendMessage(msg);
			}
		});
	}
	
	public static void getFamilyList(final MyApplication appContext, final ClientCallback callback) {
		QYRestClient.post("family/lists"+"?_sign="+appContext.getLoginSign(), null, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				try{
					FamilyListEntity data = FamilyListEntity.parse(DecodeUtil.decode(new String(content)));
					saveCache(appContext, CommonValue.CacheKey.FamilyList, data);
					callback.onSuccess(data);
				}catch (Exception e) {
					callback.onError(e);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
	
	public static void getChatHistory(String roomId, String maxId, final ClientCallback callback) {
		RequestParams param = new RequestParams();
		param.add("hash", roomId);
		param.add("maxid", maxId);
		QYRestClient.post("chat/load"+"?_sign="+MyApplication.getInstance().getLoginSign(), param, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				handleChatHistory(content, callback);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				callback.onFailure("");
			}
		});
	}
	public static void handleChatHistory(final byte[] content, final ClientCallback callback) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					callback.onSuccess((ChatHistoryListEntity)msg.obj);
					break;

				default:
					callback.onError((Exception)msg.obj);
					break;
				}
			}
		};
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Message msg = new Message();
				try {
					String target = new String(content);
					String decode = DecodeUtil.decode(target);
					target = null;
					ChatHistoryListEntity data = ChatHistoryListEntity.parse(decode);
					decode = null;
					msg.what = 1;
					msg.obj = data;
				} catch (Exception e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				} 
				handler.sendMessage(msg);
			}
		});
	}
	
	//录入
	public static void phonebookAssist(MyApplication appContext, String name, String phone, String code, final FileCallback callback) {
		RequestParams param = new RequestParams();
		param.add("realname", name);
		param.add("phone", phone);
		param.add("code", code);
		QYRestClient.post("phonebook/assist"+"?_sign="+MyApplication.getInstance().getLoginSign(), param, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				handlePhonebookAssist(content, callback);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				callback.onFailure("");
			}
		});
	}
	public static void handlePhonebookAssist(final byte[] content, final FileCallback callback) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					callback.onSuccess((String)msg.obj);
					break;
				case 2:
					callback.onFailure((String)msg.obj);
				default:
					callback.onError((Exception)msg.obj);
					break;
				}
			}
		};
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Message msg = new Message();
				try {
					String target = new String(content);
					String decode = DecodeUtil.decode(target);
					target = null;
					JSONObject json = new JSONObject(decode);
					if (json.getInt("status") == 1) {
						msg.what = 1;
						msg.obj = json.getString("url");
					}
					else {
						msg.what = 2;
						msg.obj = json.getString("info");
					}
				} catch (Exception e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				} 
				handler.sendMessage(msg);
			}
		});
	}
	
	public static void cardVIP(String code, String material_idcard, String material_card, final ClientCallback callback) {
		RequestParams param = new RequestParams();
		param.put("material_idcard", material_idcard);
		param.put("material_card", material_card);
//		param.put("code", code);
		QYRestClient.post("card/certify/code/"+code, param, new AsyncHttpResponseHandler() {
			
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				Logger.i(DecodeUtil.decode(new String(responseBody)));
				try {
					JSONObject js = new JSONObject(DecodeUtil.decode(new String(responseBody)));
					Result result = new Result();
					if (js.getInt("status") == 1) {
						result.setError_code(Result.RESULT_OK);
					}
					else {
						result.setError_code(10);
						result.setMessage(js.getString("info"));
					}
					callback.onSuccess(result);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				Crashlytics.logException(error);
			}
		});
	}
	
	//get qiniu token
	public static void getQiniuToken(final FileCallback callback) {
		QYRestClient.post("user/uploadToken", null, new AsyncHttpResponseHandler() {
			
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				handleQiniuToken(responseBody, callback);
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
//				Logger.i(new String(responseBody));
			}
		});
	}
	public static void handleQiniuToken(final byte[] content, final FileCallback callback) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					callback.onSuccess((String)msg.obj);
					break;
				case 2:
					callback.onFailure((String)msg.obj);
				default:
					callback.onError((Exception)msg.obj);
					break;
				}
			}
		};
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Message msg = new Message();
				try {
					String target = new String(content);
					String decode = DecodeUtil.decode(target);
					target = null;
					JSONObject json = new JSONObject(decode);
					String token = json.getString("token");
					msg.what = 1;
					msg.obj = token;
				} catch (Exception e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				} 
				handler.sendMessage(msg);
			}
		});
	}
	
	//广告
	public static void getSlideAds(final MyApplication appContext, final ClientCallback callback) {
		QYRestClient.post("update/slide", null, new AsyncHttpResponseHandler() {
			
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				handleSlideAds(appContext, responseBody, callback);
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
//				Logger.i(new String(responseBody));
			}
		});
	}
	public static void handleSlideAds(final MyApplication appContext, final byte[] content, final ClientCallback callback) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					callback.onSuccess((AdsListEntity)msg.obj);
					break;
				case 2:
					callback.onFailure((String)msg.obj);
				default:
					callback.onError((Exception)msg.obj);
					break;
				}
			}
		};
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Message msg = new Message();
				try {
					String target = new String(content);
					String decode = DecodeUtil.decode(target);
					target = null;
					AdsListEntity ads = AdsListEntity.parse(decode);
					saveCache(appContext, CommonValue.CacheKey.ADS, ads);
					msg.what = 1;
					msg.obj = ads;
				} catch (Exception e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				} 
				handler.sendMessage(msg);
			}
		});
	}
	
	//发布文章话题
	public static void pubTopic(final MyApplication appContext, 
			String title, 
			String content, 
			final ClientCallback callback) {
		RequestParams params = new RequestParams();
		params.add("title", title);
		params.add("description", content);
		QYRestClient.post("news/post"+"?_sign="+appContext.getLoginSign(), params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				handleTopicPubed(content, callback, appContext);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
	public static void handleTopicPubed(final byte[] content, final ClientCallback callback, final MyApplication appContext) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					callback.onSuccess((KeyValue)msg.obj);
					break;
				case 0:
					callback.onFailure((String)msg.obj);
					break;
				default:
					callback.onError((Exception)msg.obj);
					break;
				}
			}
		};
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Message msg = new Message();
				try {
					String target = new String(content);
					String decode = DecodeUtil.decode(target);
					target = null;
					Logger.i(decode);
					JSONObject js = new JSONObject(decode);
					if (js.getInt("status") == 1) {
						KeyValue value = new KeyValue("link",  js.getJSONObject("info").getString("link"));
						msg.what = 1;
						msg.obj = value;
					}
					else {
						msg.what = 0;
						msg.obj = js.getString("info");
					}
					decode = null;
				} catch (Exception e) {
					e.printStackTrace();
					Crashlytics.logException(e);
					msg.what = -1;
					msg.obj = e;
				}
				handler.sendMessage(msg);
			}
		});
	}
	
	//资讯分类
	public static void getTopicTypes(final MyApplication appContext, final ClientCallback callback) {
		QYRestClient.post("news/category"+"?_sign="+appContext.getLoginSign(), null, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				handleTopicTypes(content, callback, appContext);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
	public static void handleTopicTypes(final byte[] content, final ClientCallback callback, final MyApplication appContext) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					callback.onSuccess((TopicOptionListEntity)msg.obj);
					break;
				default:
					callback.onError((Exception)msg.obj);
					break;
				}
			}
		};
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Message msg = new Message();
				try {
					String target = new String(content);
					String decode = DecodeUtil.decode(target);
					target = null;
					TopicOptionListEntity data = TopicOptionListEntity.parse(decode);
					saveCache(appContext, CommonValue.CacheKey.TopicTypes, data);
					msg.what = 1;
					msg.obj = data;
					decode = null;
				} catch (Exception e) {
					e.printStackTrace();
					Crashlytics.logException(e);
					msg.what = -1;
					msg.obj = e;
				}
				handler.sendMessage(msg);
			}
		});
	}
	
	//资讯列表
	public static void getTopicList(final MyApplication appContext, final String id, final String page, final ClientCallback callback) {
		RequestParams params = new RequestParams();
        if (StringUtils.notEmpty(id)) {
            params.add("id", id);
        }
		params.add("page", page);
		QYRestClient.post("news/lists"+"?_sign="+appContext.getLoginSign(), params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				handleTopicList(content, id, page, callback, appContext);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
	public static void handleTopicList(final byte[] content, final String id, final String page, final ClientCallback callback, final MyApplication appContext) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					callback.onSuccess((TopicListEntity)msg.obj);
					break;
				default:
					callback.onError((Exception)msg.obj);
					break;
				}
			}
		};
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Message msg = new Message();
				try {
					String target = new String(content);
					String decode = DecodeUtil.decode(target);
					target = null;
					TopicListEntity data = TopicListEntity.parse(decode);
                    if (StringUtils.notEmpty(page) && page.equals("1")) {
                        if (StringUtils.notEmpty(id)) {
                            saveCache(appContext, CommonValue.CacheKey.TopicLists + id, data);
                        } else {
                            saveCache(appContext, CommonValue.CacheKey.TopicLists, data);
                        }
                    }
					msg.what = 1;
					msg.obj = data;
					decode = null;
				} catch (Exception e) {
					e.printStackTrace();
					Crashlytics.logException(e);
					msg.what = -1;
					msg.obj = e;
				}
				handler.sendMessage(msg);
			}
		});
	}
	
	//我的资讯列表
	public static void getMyTopicList(final MyApplication appContext, String page, final ClientCallback callback) {
		RequestParams params = new RequestParams();
//		params.add("page", page);
		QYRestClient.post("news/my"+"?_sign="+appContext.getLoginSign(), params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				handleMyTopicList(content, callback, appContext);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
	public static void handleMyTopicList(final byte[] content, final ClientCallback callback, final MyApplication appContext) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					callback.onSuccess((TopicListEntity)msg.obj);
					break;
				default:
					callback.onError((Exception)msg.obj);
					break;
				}
			}
		};
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Message msg = new Message();
				try {
					String target = new String(content);
					String decode = DecodeUtil.decode(target);
					target = null;
					TopicListEntity data = TopicListEntity.parseMyTopic(decode);
					saveCache(appContext, CommonValue.CacheKey.MyTopicLists, data);
					msg.what = 1;
					msg.obj = data;
					decode = null;
				} catch (Exception e) {
					e.printStackTrace();
					Crashlytics.logException(e);
					msg.what = -1;
					msg.obj = e;
				}
				handler.sendMessage(msg);
			}
		});
	}

    //wechat token
    public static void getAccessToken(String code, String appId, String secret, final FileCallback callback) {
        QYRestClient.get("https://api.weixin.qq.com/sns/oauth2/access_token?appid="+appId+"&secret="+secret+"&code="+code+"&grant_type=authorization_code",
                null,
        new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                callback.onSuccess(new String(bytes));
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {

            }
        });
    }

    //wechat login
    public static void loginByWechat(final MyApplication appContext, String openid, String accessToken, final ClientCallback callback) {
        RequestParams params = new RequestParams();
        params.add("client_browser", "android");
        try {
            params.add("client_version", AppManager.getAppManager().currentActivity().getPackageManager().getPackageInfo(AppManager.getAppManager().currentActivity().getPackageName(), 0).versionCode+"");
        } catch (Exception e) {
            e.printStackTrace();
        }
        params.add("client_push", "android");
        params.add("push_device_type", "3");
        params.add("openid", openid);
        params.add("access_token", accessToken);
        params.add("code", MD5Util.getMD5String(openid+"Fj34&^%$d"));
        QYRestClient.post("user/loginByOpenId", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] content) {
                handleVertifiedCode(content, callback);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
                callback.onFailure("网络不给力，请重新尝试");
            }
        });
    }

    //usercheck
    public static void userCheck(final MyApplication appContext, String phone, final ClientCallback callback) {
        RequestParams params = new RequestParams();
        params.add("phone", phone);
        QYRestClient.post("user/check", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] content) {
                handleUserCheck(content, callback);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
                callback.onFailure("网络不给力，请重新尝试");
            }
        });
    }
    public static void handleUserCheck(final byte[] content, final ClientCallback callback) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        callback.onSuccess((UserEntity)msg.obj);
                        break;
                    default:
                        callback.onError((Exception)msg.obj);
                        break;
                }
            }
        };
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                try {
                    String target = new String(content);
                    String decode = DecodeUtil.decode(target);
                    target = null;
                    UserEntity data = UserEntity.userCheckParse(decode);
                    decode = null;
                    msg.obj = data;
                    msg.what = 1;
                } catch (Exception e) {
                    e.printStackTrace();
                    msg.obj = e;
                    msg.what = -1;
                }
                handler.sendMessage(msg);
            }
        });
    }

    //login password
    public static void loginByPassword(final MyApplication appContext, String phone, String password, final ClientCallback callback) {
        RequestParams params = new RequestParams();
        params.add("client_browser", "android");
        try {
            params.add("client_version", AppManager.getAppManager().currentActivity().getPackageManager().getPackageInfo(AppManager.getAppManager().currentActivity().getPackageName(), 0).versionCode+"");
        } catch (Exception e) {
            e.printStackTrace();
        }
        params.add("client_push", "android");
        params.add("push_device_type", "3");
        params.add("phone", phone);
        params.add("password", password);
        QYRestClient.post("user/loginByPassword", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] content) {
                handleVertifiedCode(content, callback);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
                callback.onFailure("网络不给力，请重新尝试");
            }
        });
    }

    //set password
    public static void setPassword(final MyApplication appContext, String phone, String password, final ClientCallback callback) {
        RequestParams params = new RequestParams();
        params.add("phone", phone);
        params.add("password", password);
        QYRestClient.post("user/setPassword", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] content) {
                handleSetPassword(content, callback);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
                callback.onFailure("网络不给力，请重新尝试");
            }
        });
    }
    public static void handleSetPassword(final byte[] content, final ClientCallback callback) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        callback.onSuccess((UserEntity)msg.obj);
                        break;
                    default:
                        callback.onError((Exception)msg.obj);
                        break;
                }
            }
        };
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                try {
                    String target = new String(content);
                    String decode = DecodeUtil.decode(target);
                    target = null;
                    UserEntity data = UserEntity.UserPasswordParse(decode);
                    decode = null;
                    msg.obj = data;
                    msg.what = 1;
                } catch (Exception e) {
                    e.printStackTrace();
                    msg.obj = e;
                    msg.what = -1;
                }
                handler.sendMessage(msg);
            }
        });
    }

    //set password
    public static void deleteCard(final MyApplication appContext, String code, final ClientCallback callback) {
        RequestParams params = new RequestParams();
        params.add("code", code);
        QYRestClient.post("card/remove", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] content) {
                handleRemoveCard(content, callback);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
                callback.onFailure("网络不给力，请重新尝试");
            }
        });
    }
    public static void handleRemoveCard(final byte[] content, final ClientCallback callback) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        callback.onSuccess((UserEntity)msg.obj);
                        break;
                    default:
                        callback.onError((Exception)msg.obj);
                        break;
                }
            }
        };
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                try {
                    String target = new String(content);
                    String decode = DecodeUtil.decode(target);
                    target = null;
                    UserEntity data = UserEntity.userCardDelete(decode);
                    decode = null;
                    msg.obj = data;
                    msg.what = 1;
                } catch (Exception e) {
                    e.printStackTrace();
                    msg.obj = e;
                    msg.what = -1;
                }
                handler.sendMessage(msg);
            }
        });
    }
    
    //bind openid with wmid
    public static void bindOpenidWithWMId(final MyApplication appContext, String openid, String wmid, final ClientCallback callback) {
        RequestParams params = new RequestParams();
        params.add("openid", openid);
        params.add("uuid", wmid);	
        QYRestClient.post("user/setUuidByOpenid", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] content) {
            	handleBindOpenidWithWMId(content, callback);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
                callback.onFailure("网络不给力，请重新尝试");
                Logger.i("a");
            }
        });
    }
    public static void handleBindOpenidWithWMId(final byte[] content, final ClientCallback callback) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        callback.onSuccess((UserEntity)msg.obj);
                        break;
                    default:
                        callback.onError((Exception)msg.obj);
                        break;
                }
            }
        };
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                try {
                    String target = new String(content);
                    String decode = DecodeUtil.decode(target);
                    target = null;
                    UserEntity data = UserEntity.userCheckParse(decode);
                    decode = null;
                    msg.obj = data;
                    msg.what = 1;
                } catch (Exception e) {
                    e.printStackTrace();
                    msg.obj = e;
                    msg.what = -1;
                }
                handler.sendMessage(msg);
            }
        });
    }
    
    public static void queryWMIdByOpenid(final MyApplication appContext, String openid, final ClientCallback callback) {
        RequestParams params = new RequestParams();
        params.add("openid", openid);
        QYRestClient.post("user/getUuidByOpenid", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] content) {
            	handleQueryWMIdByOpenid(content, callback);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
                callback.onFailure("网络不给力，请重新尝试");
            }
        });
    }
    public static void handleQueryWMIdByOpenid(final byte[] content, final ClientCallback callback) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        callback.onSuccess((UserEntity)msg.obj);
                        break;
                    default:
                        callback.onError((Exception)msg.obj);
                        break;
                }
            }
        };
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                try {
                    String target = new String(content);
                    String decode = DecodeUtil.decode(target);
                    target = null;
                    UserEntity data = UserEntity.userCheckParse(decode);
                    decode = null;
                    msg.obj = data;
                    msg.what = 1;
                } catch (Exception e) {
                    e.printStackTrace();
                    msg.obj = e;
                    msg.what = -1;
                }
                handler.sendMessage(msg);
            }
        });
    }
    
    public static void downContact(final MyApplication appContext, final ClientCallback callback) {
		QYRestClient.post("contact/download"+"?_sign="+appContext.getLoginSign(), null, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				handleDownContact(content, callback);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
    public static void handleDownContact(final byte[] content, final ClientCallback callback) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        callback.onSuccess((MobileSynListBean)msg.obj);
                        break;
                    default:
                        callback.onError((Exception)msg.obj);
                        break;
                }
            }
        };
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                try {
                    String target = new String(content);
                    String decode = DecodeUtil.decode(target);
                    target = null;
                    MobileSynListBean data = MobileSynListBean.parse(decode);
                    decode = null;
                    msg.obj = data;
                    msg.what = 1;
                } catch (Exception e) {
                    e.printStackTrace();
                    msg.obj = e;
                    msg.what = -1;
                }
                handler.sendMessage(msg);
            }
        });
    }
    
    public static void fileSync(final MyApplication appContext, String code, final ClientCallback callback) {
    	RequestParams params = new RequestParams();
        params.add("code", code);
		QYRestClient.post("phonebook/imports"+"?_sign="+appContext.getLoginSign(), params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] content) {
				handleFileSync(content, callback);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable e) {
				if (appContext.isNetworkConnected()) {
					callback.onFailure(e.getMessage());
				}
			}
		});
	}
    public static void handleFileSync(final byte[] content, final ClientCallback callback) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        callback.onSuccess((UserEntity)msg.obj);
                        break;
                    default:
                        callback.onError((Exception)msg.obj);
                        break;
                }
            }
        };
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                try {
                    String target = new String(content);
                    String decode = DecodeUtil.decode(target);
                    target = null;
                    UserEntity data = UserEntity.FileSyncParse(decode);
                    decode = null;
                    msg.obj = data;
                    msg.what = 1;
                } catch (Exception e) {
                    e.printStackTrace();
                    msg.obj = e;
                    msg.what = -1;
                }
                handler.sendMessage(msg);
            }
        });
    }
}
