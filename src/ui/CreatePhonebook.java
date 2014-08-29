package ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import bean.*;
import org.apache.commons.lang3.StringEscapeUtils;

import tools.AppManager;
import tools.HTMLUtil;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;
import ui.AppActivity.DialogClickListener;
import ui.adapter.FieldAdapter;
import ui.adapter.PrivacyAdapter;
import ui.adapter.QunTypeAdapter;
import widget.GridViewForScrollView;

import com.vikaa.allcontact.R;

import config.AppClient;
import config.CommonValue;
import config.AppClient.ClientCallback;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class CreatePhonebook extends AppActivity implements OnItemClickListener{
	
	private EditText richET;
	private EditText qunNameET;
	private LinearLayout moreLayout;
	private Button moreButton;
    private EditText edtCustome;

	private Spinner privacySP;
	private Spinner questionSP;
	private Spinner exchangeSP;
	
	private GridViewForScrollView fieldGridView;
	private FieldAdapter fieldAdapter;
	private List<String> fields = new ArrayList<String>();
	private List<QunsEntity> quns = new ArrayList<QunsEntity>();
	private QunTypeAdapter adapterType;
	private GridViewForScrollView gvQun;
	
	private QunsEntity qunOfChoosing ;
	
	private String title;
	private String content;
	private String privacy;
	private String custome;
	private String customeDisplay;
	private String exchangeSetting;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_phonebook);
		initUI();
		changeType();
	}
	
	private void initUI() {
		gvQun = (GridViewForScrollView) findViewById(R.id.typeGridView);
		quns.addAll(QunsListEntity.parse(this).quns);
		qunOfChoosing = quns.get(0);
		quns.get(0).isSelected = true;
		adapterType = new QunTypeAdapter(this, quns);
		gvQun.setAdapter(adapterType);
		gvQun.setOnItemClickListener(this);
		
		moreButton = (Button) findViewById(R.id.more);
		moreLayout = (LinearLayout) findViewById(R.id.moreLayout);
		qunNameET = (EditText) findViewById(R.id.qunName);
		richET = (EditText) findViewById(R.id.richEditText);
        edtCustome = (EditText) findViewById(R.id.qunQuestion);
		privacySP = (Spinner) findViewById(R.id.privacySP);
		List<KeyValue> privacys = new ArrayList<KeyValue>();
		privacys.add(new KeyValue("对群友可见","0"));
		privacys.add(new KeyValue("群友需申请可见","1"));
		privacys.add(new KeyValue("对任何人公开","2"));
		privacys.add(new KeyValue("申通通过后才能进群","3"));
		PrivacyAdapter pAdapter = new PrivacyAdapter(this, privacys);
		privacySP.setAdapter(pAdapter);
		privacySP.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				privacy = position+"";
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		});
		
		questionSP = (Spinner) findViewById(R.id.questionSP);
		List<KeyValue> questions = new ArrayList<KeyValue>();
		questions.add(new KeyValue("公开显示回答的问题","0"));
		questions.add(new KeyValue("答案仅管理员可见","1"));
		PrivacyAdapter qAdapter = new PrivacyAdapter(this, questions);
		questionSP.setAdapter(qAdapter);
		questionSP.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				customeDisplay = position+"";
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		});
		
		exchangeSP = (Spinner) findViewById(R.id.exchangeSP);
		List<KeyValue> exchanges = new ArrayList<KeyValue>();
		exchanges.add(new KeyValue("成员自由交换名片","0"));
		exchanges.add(new KeyValue("群主引见","1"));
		PrivacyAdapter eAdapter = new PrivacyAdapter(this, exchanges);
		exchangeSP.setAdapter(eAdapter);
		exchangeSP.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				exchangeSetting = position+"";
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		});
		
		privacy = "0";
		customeDisplay = "0";
		exchangeSetting = "0";
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			closeInput();
			AppManager.getAppManager().finishActivity(this);
			break;

		case R.id.rightBarButton:
			prepareCreatePhonebook();
			break;
			
		case R.id.more:
			moreButton.setVisibility(View.GONE);
			moreLayout.setVisibility(View.VISIBLE);
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View convertView, int position, long arg3) {
        closeInput();
		for (QunsEntity qun : quns) {
			qun.isSelected = false;
		}
		qunOfChoosing = quns.get(position);
		quns.get(position).isSelected = true;
		adapterType.notifyDataSetChanged();
		changeType();
	}
	
	private void changeType() {
		richET.setText(qunOfChoosing.descriptions.get(0));
	}
	
	private void prepareCreatePhonebook() {
		title = qunNameET.getEditableText().toString();
		content = richET.getEditableText().toString();
        custome = edtCustome.getEditableText().toString();
        if (StringUtils.empty(title)) {
        	WarningDialog("请填写通讯录名字");
			return;
		}
        if (StringUtils.empty(content)) {
        	WarningDialog("请填写通讯录内容");
			return;
		}
    	loadingPd = UIHelper.showProgress(this, null, null, true);
    	AppClient.createPhonebook(appContext, title, qunOfChoosing.id, qunOfChoosing.logo, content, privacy, custome, customeDisplay, "", "", clientCallback);
	}
	
	private ClientCallback clientCallback = new ClientCallback() {
		
		@Override
		public void onSuccess(Entity data) {
			UIHelper.dismissProgress(loadingPd);
			UIHelper.ToastMessage(CreatePhonebook.this, R.layout.toastmessage_text, "发起通讯录成功，正在跳转", Toast.LENGTH_SHORT);
            PhonebookCreateEntity entity = (PhonebookCreateEntity) data;
			Intent intent0 = new Intent();
			intent0.setAction(CommonValue.PHONEBOOK_CREATE_ACTION);
			intent0.setAction(CommonValue.PHONEBOOK_DELETE_ACTION);
			sendBroadcast(intent0);
			AppManager.getAppManager().finishActivity(CreatePhonebook.this);
			Intent intent = new Intent(CreatePhonebook.this, QYWebView.class);
			intent.putExtra(CommonValue.IndexIntentKeyValue.CreateView, entity.link);
			startActivityForResult(intent, CommonValue.PhonebookViewUrlRequest.editPhoneview);
		}
		
		@Override
		public void onFailure(String message) {
			UIHelper.dismissProgress(loadingPd);
			WarningDialog("发起通讯录失败，请重试", "确定", "取消", new DialogClickListener() {
				
				@Override
				public void ok() {
					loadingPd = UIHelper.showProgress(CreatePhonebook.this, null, null, true);
                    AppClient.createPhonebook(appContext, title, qunOfChoosing.id, qunOfChoosing.logo, content, privacy, custome, customeDisplay, "", "", clientCallback);
				}
				
				@Override
				public void cancel() {
					
				}
			});
		}
		
		@Override
		public void onError(Exception e) {
			UIHelper.dismissProgress(loadingPd);
			WarningDialogAndOpenWechat("bibi100", "发起通讯录失败，请联系微信客服bibi100");
		}
	};
}
