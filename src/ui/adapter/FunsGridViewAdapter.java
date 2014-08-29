package ui.adapter;

import java.util.List;

import bean.FunsEntity;

import com.vikaa.allcontact.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FunsGridViewAdapter extends BaseAdapter {
	
	private List<FunsEntity> datas;
	private Context context;
	private LayoutInflater inflater;
	
	static class ViewHolder {
		ImageView icon;
		TextView nameTV;
	}
	
	public FunsGridViewAdapter(Context context, List<FunsEntity> datas) {
		this.context = context;
		this.datas = datas;
		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return datas.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.find_cell, null);
			viewHolder = new ViewHolder();
			viewHolder.icon = (ImageView) convertView.findViewById(R.id.icon);
			viewHolder.nameTV = (TextView) convertView.findViewById(R.id.nameTV);
			convertView.setTag(viewHolder);
		}
		else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.nameTV.setText(datas.get(position).label);
		viewHolder.icon.setBackgroundResource(datas.get(position).icon);
		return convertView;
	}

}
