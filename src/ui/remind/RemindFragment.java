package ui.remind;

import java.util.ArrayList;
import java.util.List;

import tools.Logger;

import bean.RemindEntity;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.vikaa.contactactivityassitant.R;

import db.manager.RemindDBManager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class RemindFragment extends Fragment{

	@ViewInject(R.id.lvRemind)
	private ListView lvRemind;
	private RemindAdapter adapter;
	private List<RemindEntity> datas = new ArrayList<RemindEntity>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new RemindAdapter(getActivity(), datas);
		getReminds();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.remind_fragment, container, false);
		ViewUtils.inject(this, view);
		lvRemind.setAdapter(adapter);
		return view;
	}
	
	private void getReminds() {
		datas.addAll(RemindDBManager.getInstance(getActivity()).getReminds("0"));
		adapter.notifyDataSetChanged();
	}
}
