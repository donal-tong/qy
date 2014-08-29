package ui.adapter;

import java.util.List;

import com.vikaa.allcontact.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class FieldAdapter extends BaseAdapter {

	private List<String> datas;
	private Context context;
	private LayoutInflater inflater;
	
	static class ViewHolder {
		CheckBox fieldBox;
	}

	public FieldAdapter(List<String> datas, Context context) {
		super();
		this.datas = datas;
		this.context = context;
		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return datas.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.field_cell, null);
			viewHolder = new ViewHolder();
			viewHolder.fieldBox = (CheckBox) convertView.findViewById(R.id.fieldCheckBox);
			convertView.setTag(viewHolder);
		}
		else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.fieldBox.setText(datas.get(position));
		return convertView;
	}


}
