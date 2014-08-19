package ui;

import java.util.ArrayList;
import java.util.List;

import tools.AppManager;
import tools.UpdateManager;
import ui.adapter.MoreDialogAdapter;
import bean.CardIntroEntity;

import com.vikaa.mycontact.R;

import config.CommonValue;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class QYWebViewMore extends AppActivity implements OnItemClickListener{
	
	private List<CardIntroEntity> options = new ArrayList<CardIntroEntity>();
	private MoreDialogAdapter xAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.more_dialog);
		initValue();
		initUI();
	}
	
	private void initValue() {
		CardIntroEntity op0 = new CardIntroEntity();
		op0.realname = "群发短信";
		op0.department = R.drawable.icon_set_setting+"";
		op0.position = "";
		options.add(op0);
		
		CardIntroEntity op2 = new CardIntroEntity();
		op2.realname = "手机通讯录录入";
		op2.position = "";
		op2.department = R.drawable.icon_set_setting+"";
		options.add(op2);
		
		CardIntroEntity op3 = new CardIntroEntity();
		op3.realname = "手工录入";
		op3.position = "";
		op3.department = R.drawable.icon_set_setting+"";
		options.add(op3);
		
		CardIntroEntity op4 = new CardIntroEntity();
		op4.realname = "Execl录入";
		op4.position = "";
		op4.department = R.drawable.icon_set_setting+"";
		options.add(op4);
	}
	
	private void initUI() {
		ListView listview = (ListView) findViewById(R.id.title_list);
		xAdapter = new MoreDialogAdapter(this, options);
		listview.setAdapter(xAdapter);
		listview.setOnItemClickListener(this);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		AppManager.getAppManager().finishActivity(this);
		return super.onTouchEvent(event);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		Intent intent = new Intent();
		intent.putExtra("position", position);
		setResult(RESULT_OK, intent);
		AppManager.getAppManager().finishActivity(this);
	}
}
