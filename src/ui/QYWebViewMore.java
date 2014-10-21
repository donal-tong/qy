package ui;

import java.util.ArrayList;
import java.util.List;

import tools.AppManager;
import tools.UpdateManager;
import ui.adapter.MoreDialogAdapter;
import bean.CardIntroEntity;
import bean.OptionListBean;

import com.vikaa.contactactivityassitant.R;

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
	private OptionListBean optionsBean;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.more_dialog);
		optionsBean = (OptionListBean) getIntent().getExtras().getSerializable("options");
		initValue();
		initUI();
	}
	
	private void initValue() {
		options.clear();
		options.addAll(optionsBean.optionsMore);
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
