package config;

import java.util.List;
import java.util.Properties;

import org.apache.http.client.CookieStore;

import pomelo.PomeloClient;

import com.kuyue.openchat.api.WmOpenChatSdk;
import com.loopj.android.http.PersistentCookieStore;
import com.nostra13.universalimageloader.utils.L;
import com.vikaa.mycontact.BuildConfig;

import service.QYEnterService;
import tools.AppContext;
import tools.AppException;
import tools.Logger;
import tools.NetworkStateService;
import tools.StringUtils;
import bean.UserEntity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.view.WindowManager;

public class MyApplication extends AppContext {
	private static MyApplication mApplication;
	
	private NotificationManager mNotificationManager;
	
	private boolean login = false;	//登录状态
	private String loginUid = "0";	//登录用户的id
	
	private PomeloClient pomeloClient;
		
	
	public synchronized static MyApplication getInstance() {
		return mApplication;
	}
	
	public NotificationManager getNotificationManager() {
		if (mNotificationManager == null)
			mNotificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		return mNotificationManager;
	}
	
	private WindowManager.LayoutParams wmParams=new WindowManager.LayoutParams();
	public WindowManager.LayoutParams getMywmParams(){
		return wmParams;
	}
	
	public void setPolemoClient(PomeloClient pomeloClient) {
		this.pomeloClient = pomeloClient;
	}
	
	public PomeloClient getPolemoClient() {
		return this.pomeloClient;
	}
	
	public void onCreate() {
		super.onCreate();
		mApplication = this;
		Logger.getLogger().setTag("MyContact");
		ImageCacheUtil.init(this);
		Thread.setDefaultUncaughtExceptionHandler(AppException.getAppExceptionHandler());
		if (BuildConfig.DEBUG) {
			L.enableLogging();
		}
		Logger.setDebug(BuildConfig.DEBUG);
		mNotificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		CookieStore cookieStore = new PersistentCookieStore(this);  
		QYRestClient.getIntance().setCookieStore(cookieStore);
		WmOpenChatSdk.getInstance().init(this);
//        Intent service = new Intent(this, NetworkStateService.class);
//		startService(service);
	}
	
	
	@Override
	public void onTerminate() {
		Logger.i("ter");
		super.onTerminate();
	}
	/**
	 * 用户是否登录
	 * @return
	 */
	public boolean isLogin() {
		try {
			String loginStr = getProperty("user.login");
			if (StringUtils.empty(loginStr)) {
				login = false;
			}
			else {
				login = (loginStr.equals("1")) ? true : false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return login;
	}

    public void saveNeedSetPassword(final boolean need) {
        setProperties(new Properties(){
            {
                setProperty("password.set",need?"1":"0");
            }
        });
    }

    public boolean getNeedSetPassword() {
        boolean need = true;
        try {
            String needStr = getProperty("password.set");
            if (StringUtils.empty(needStr)) {
                need = false;
            }
            else {
                need = (needStr.equals("1")) ? true : false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return need;
    }

    public void saveLoginPhone(final String phone) {
        setProperties(new Properties(){
            {
                setProperty("login.phone",phone);
            }
        });
    }

    public String getLoginPhone() {
        return (getProperty("login.phone"));
    }

	/**
	 * 保存登录信息
	 */
	@SuppressWarnings("serial")
	public void saveLoginInfo(final UserEntity user) {
		this.loginUid = user.openid;
		this.login = true;
		setProperties(new Properties(){
			{
				setProperty("user.login","1");
				setProperty("user.uid", user.openid);
				setProperty("user.name", user.nickname);
				setProperty("user.face", user.headimgurl);
				setProperty("user.hashcode", user.hash);
				setProperty("user.sign", user._sign);
				
				setProperty("user.credit", user.credit);
				setProperty("user.deg1", user.deg1);
				setProperty("user.deg2", user.deg2);
				setProperty("user.card", user.card);
				setProperty("user.news", user.news);
			}
		});		
	}

	public String getLoginUid() {
		return (getProperty("user.uid"));
	}
	
	public String getLoginHashCode() {
		return (getProperty("user.hashcode"));
	}
	
	public String getLoginSign() {
		return (getProperty("user.sign"));
	}
	
	public String getCredits() {
		return (getProperty("user.credit"));
	}
	
	public String getDeg1() {
		return (getProperty("user.deg1"));
	}
	
	public String getDeg2() {
		return (getProperty("user.deg2"));
	}
	
	public String getCard() {
		return (getProperty("user.card"));
	}
	
	public String getNews() {
		return (getProperty("user.news"));
	}
	
	public String getMessageInterupt() {
		return (getProperty("messageinterupt"));
	}
	
	public void setMessageInterupt(final String interupt) {
		setProperties(new Properties(){
			{
				setProperty("messageinterupt", interupt);
			}
		});	
	}

	/**
	 * 获取登录信息
	 * @return
	 */
	public UserEntity getLoginInfo() {		
		UserEntity lu = new UserEntity();		
		lu.openid = (getProperty("user.uid"));
		lu.nickname = (getProperty("user.name"));
		lu.headimgurl = (getProperty("user.face"));
		return lu;
	}
	
	public String getNickname() {		
		return (getProperty("user.name"));
	}
	
	public String getUserAvatar() {		
		return (getProperty("user.face"));
	}
	
	public void setUserAvatar(final String avatar) {		
		setProperties(new Properties(){
			{
				setProperty("user.face", avatar);
			}
		});
	}
	
	public String getUserAvatarCode() {		
		return (getProperty("user.facecode"));
	}
	
	public void setUserAvatarCode(final String code) {		
		setProperties(new Properties(){
			{
				setProperty("user.facecode", code);
			}
		});
	}
	
	/**
	 * 退出登录
	 */
	public void setUserLogout() {
		this.login = false;
		setProperties(new Properties(){
			{
				setProperty("user.login","0");
			}
		});	
	}
	
	public boolean isNeedCheckLogin() {
		try {
			String loginStr = getProperty("user.needchecklogin");
			if (StringUtils.empty(loginStr)) {
				return false;
			}
			else {
				return (loginStr.equals("1")) ? true : false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void setNeedCheckLogin() {
		setProperties(new Properties(){
			{
				setProperty("user.needchecklogin","1");
			}
		});
	}
	
	public void saveNotiWhen(final String when) {
		setProperties(new Properties(){
			{
				setProperty("noti.when",when);
			}
		});
	}
	
	public String getNotiWhen() {
		try {
			String loginStr = getProperty("noti.when");
			if (StringUtils.empty(loginStr)) {
				return "0";
			}
			else {
				return loginStr;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "0";
	}
	
}
