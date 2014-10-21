package config;

import tools.AppManager;
import tools.ImageUtils;
import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.vikaa.contactactivityassitant.R;

public class CommonValue {
    public static final String APP_ID = "wx8b5b960fc0311f3e";
    public static final String SECRET = "918774b3b42e14e42e84d67f54549a98";

    public static final String WMAppID = "1-20178-d86ca85fa3bf8db970f11a9fb89f49af";
    public static final String WMSECRET = "e468cf291e106ac6c9460395b8b105ab";
    
    public static final int UI_DELAY = 200;

	public static final int USER_NOT_IN_ERROR = 1001;
	
	public static final String PackageName = "com.vikaa.mycontact";
	
	public static String BASE_API = "http://qun.hk/api/";
	public static String BASE_URL = "http://qun.hk";
	
//	public static String BASE_API = "http://pb.wcl.m0.hk/api/";
//	public static String BASE_URL = "http://pb.wcl.m0.hk";
	
	public static final String KEY_GUIDE_SHOWN = "preferences_guide_shown";
	
	public interface LianXiRenType {
		String mobile = "电话本";
		String yidu = "一度好友";
		String erdu = "二度好友";
	}
	
	public interface subTitle {
		String subtitle1 = "查看手机通讯录";
		String subtitle2 = "查看交换名片的朋友";
		String subtitle3 = "查看手机通讯录";
		
		String subtitle4 = "对方扫描将录入到自己通讯录里面,并交换名片";
		String subtitle5 = "扫描别人的二维码,加入到TA的通讯录里面";
		String subtitle6 = "如有问题或者合作欢迎随时留言联系我们";
	}
	
	public interface CacheKey {
		String FamilyList = "FamilyList";
		String PhoneList = "PhoneList";
		String PhoneView = "PhoneView";
		String ActivityList = "ActivityList";
		String ActivityView = "ActivityView";
		String CardList = "CardList";
		String FriendCardList = "FriendCardList";
		String FriendCardList1 = "FriendCardList1";
		String MessageList = "MessageList";
		String SquareList = "SquareList";
		
		String ADS = "ADS";
		
		String TopicTypes = "TopicTypes";
		String TopicLists = "TopicLists";
		String MyTopicLists = "MyTopicLists";
		String UserTopicOptions = "UserTopicOptions ";
		String MessageUnRead = "MessageUnRead";
		String ChatterInfo = "ChatterInfo";
	}
	
	public interface FunsType {
		int BUSINESS = 0;
		int DINNER = 1;
		int OUTSPORT = 2;
		int CARD = 3;
		int TOPIC = 4;
		int KARAOKE = 5;
	}
	
	public interface LoginRequest {
		int LoginMobile = 1;
		int LoginWechat = 2;
		int Register = 3;
        int LoginByPassword = 4;
	}
	
	// options
	public interface DisplayOptions {
		public DisplayImageOptions default_options 
		= new DisplayImageOptions.Builder()
				.bitmapConfig(Bitmap.Config.RGB_565)
				.showImageOnLoading(R.drawable.content_image_loading)
				.showImageForEmptyUri(R.drawable.content_image_loading)
				.showImageOnFail(R.drawable.content_image_loading)
				.cacheInMemory(true)
				.cacheOnDisc(true)
				.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) 
				.considerExifParams(true)
				.displayer(new BitmapDisplayer() {
					@Override
					public void display(Bitmap bitmap, ImageAware imageAware,
							LoadedFrom loadedFrom) {
						imageAware.setImageBitmap(bitmap);
					}
				})
				.build();
//		new DisplayImageOptions.Builder()
//		.showImageOnLoading(R.drawable.ic_launcher)
//		.showImageForEmptyUri(R.drawable.ic_launcher)
//		.showImageOnFail(R.drawable.ic_launcher)
//		.cacheInMemory(true)
//		.cacheOnDisc(true)
//		.bitmapConfig(Bitmap.Config.RGB_565)
//		.build();
		
		public DisplayImageOptions avatar_options = new DisplayImageOptions.Builder()
		.bitmapConfig(Bitmap.Config.RGB_565)
		.showImageOnLoading(R.drawable.avatar_placeholder)
		.showImageForEmptyUri(R.drawable.avatar_placeholder)
		.showImageOnFail(R.drawable.avatar_placeholder)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.considerExifParams(true)
		.imageScaleType(ImageScaleType.EXACTLY_STRETCHED) 
		.displayer(new RoundedBitmapDisplayer(ImageUtils.dip2px(AppManager.getAppManager().currentActivity(), 30)))
		.build();
	}
	
	public interface FamilySectionType {
		String FamilySectionType = "家庭通讯录(长按可分享)";
		String ClanSectionType = "宗亲通讯录(长按可分享)";
	}
	
	public interface PhoneSectionType {
		String MobileSectionType = "我的通讯录";
		String OwnedSectionType = "我发起的";
		String JoinedSectionType = "我参与的";
		String RecommendSectionType = "公开推荐群";
	}
	
	public interface IndexIntentKeyValue {
		String PhoneView = "phoneview";
		String ActivityView = "activityview";
		String CreateView = "createview";
	}
	
	public interface ActivitySectionType {
		String OwnedSectionType = "我发起的";
		String JoinedSectionType = "我参与的";
	}
	
	public interface CardSectionType {
		String OwnedSectionType = "我的名片(长按可分享)";
		String BarcodeSectionType = "二维码扫一扫";
		String VSectionType = "名片加V认证";
		String FeedbackSectionType = "信息反馈";
		String SettingsSectionType = "设置";
		String LogoutSectionType = "退出";
	}
	
	public interface CardViewIntentKeyValue {
		String CardView = "cardview";
	}
	
	public interface CreateViewUrlAndRequest {
		String ContactCreateUrl = String.format("%s/index/create", BASE_URL);
		int ContactCreat = 1;
		String ActivityCreateUrl = String.format("%s/activity/create", BASE_URL);
		int ActivityCreateCreat = 2;
		String CardCreateUrl = String.format("%s/card/setting/id/0", BASE_URL);
		int CardCreat = 3;
		
		String FamilyPhonebookCreateUrl = String.format("%s/family/create", BASE_URL);
		int FamilyPhonebookCreat = 4;
		
		String CardCreateUrl1 = String.format("%s/card/setting", BASE_URL);
	}
	
	public interface CreateViewJSType {
		int	goPhonebookView = 1;
		int	goPhonebookList = 2;
		int	goActivityView = 3;
		int	goActivityList = 4;
		int goCardView = 5;
		int share = 6;
		int savePhoneBook = 7;
		int showPhonebookSmsButton = 8;
		int showActivitySmsButton = 9;
		int webNotSign = 10;
		int showChat = 11;
		int phonebookAssistSelect = 12;
		int showJiaV = 13;
		int showUploadAvatar = 14;
		int webViewReady = 15;
	}
	
	public interface PhonebookViewIntentKeyValue {
		String SMS = "sms";
		int SMSRequest = 6;
		
		String SMSPersons = "sms_person";
		int SMSPersonRequest = 7;
	}
	
	public interface PhonebookViewUrlRequest {
		int editPhoneview = 4;
		int deletePhoneview = 5;
	}
	
	public interface ActivityViewUrlRequest {
		int editActivity = 14;
		int deleteActivity = 15;
	}
	
	public interface CardViewUrlRequest {
		int editCard = 24;
	}
	
	public interface PhonebookViewIsAdd {
		int No = 0;
	}
	
	public interface PhonebookViewIsReadable {
		int UnApply = -1;
		int Applying = 0;
		int Pass = 1;
		int Refuse = 2;
	}
	
	public interface PhonebookViewIsAdmin {
		int AdminNo = 0;
		int AdminYes = 1;
	}
	public interface PhonebookLimitRight {
		String PBprivacy_Yes = "1";
		
		String Admin_Yes = "1";
		
		String Add_No = "0";
		
		String PBreadable_Yes = "1"; 
		
		String Memreadable_Yes = "1"; 
		
		String Friend_No = "-1";
		String Friend_Wait = "0";
		String Frined_Yes =  "1";
		
		String RoleAdmin = "1";
		String RoleCreator = "2";
		String RolePublic = "0";
		String RoleNone = "9";
		
		String QunReadable_Yes = "1";
		String QunReadable_No = "2";
	}
	
	public interface ContactOperationResult {
		int NOT_AUTHORITY = 0;
		int EXIST = 1;
		int SAVE_SUCCESS = 2;
		int SAVE_FAILURE = 3;
		
		String ContactOperationResultType = "ContactOperationResultType";
		
		String ContactBCAction = "ContactOperation";
		
		
	}
	
	//login action
	public static final String RELOGIN_ACTION = "RELOGIN_ACTION";
	public static final String Login_SUCCESS_ACTION = "Login_SUCCESS_ACTION";
	
	//im
	public static final String NEW_MESSAGE_ACTION = "chat.newmessage";
	public static final String UPDATE_MESSAGE_ACTION = "chat.updatemessage";
	//im reconnect
	public static final String RECONNECT_ACTION = "chat.reconnect";
	
	//phonebook
	public static final String PHONEBOOK_CREATE_ACTION = "PHONEBOOK_CREATE_ACTION";
	public static final String PHONEBOOK_DELETE_ACTION = "PHONEBOOK_DELETE_ACTION";
	
	//ACTIVITY
	public static final String ACTIVITY_CREATE_ACTION = "ACTIVITY_CREATE_ACTION";
	public static final String ACTIVITY_DELETE_ACTION = "ACTIVITY_DELETE_ACTION";
	
	//CARD
	public static final String CARD_CREATE_ACTION = "CARD_CREATE_ACTION";
	public static final String CARD_DELETE_ACTION = "CARD_DELETE_ACTION";
	
	//AD
	public static final String ADS_TITLE = "【官方】为1000个组织俱乐部，免费建在线微信站，给有需要的朋友";
	public static final String AD_LINK = "http://mp.weixin.qq.com/s?__biz=MzA4NzA4NzcxMw==&mid=200725411&idx=1&sn=32bc170189696378ec86b2b035bb8db4#rd";
	public static final String AD_THUMB = "drawable://";

    //wechat
    public static final String ACTION_WECHAT_CODE = "ACTION_WECHAT_CODE";
}
