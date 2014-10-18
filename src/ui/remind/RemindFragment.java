package ui.remind;

import com.lidroid.xutils.ViewUtils;
import com.vikaa.mycontact.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RemindFragment extends Fragment{

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.remind_fragment, container, false);
		ViewUtils.inject(this, view);
		return view;
	}
}
