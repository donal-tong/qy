package ui.adapter;

import java.util.List;

import ui.adapter.QunGridViewAdapter.ViewHolder;

import com.vikaa.wecontact.R;

import bean.FunsEntity;
import bean.QunsEntity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class FunTypeAdapter extends BaseAdapter {

	private List<FunsEntity> datas;
	private Context context;
	private LayoutInflater inflater;
	
	static class ViewHolder {
		TextView btnType;
	}
	
	public FunTypeAdapter(Context context, List<FunsEntity> datas) {
		this.context = context;
		this.datas = datas;
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
			convertView = inflater.inflate(R.layout.cell_qun_type, null);
			viewHolder = new ViewHolder();
			viewHolder.btnType = (TextView) convertView.findViewById(R.id.btnType);
			convertView.setTag(viewHolder);
		}
		else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.btnType.setSelected(datas.get(position).isSelected);
		viewHolder.btnType.setText(datas.get(position).label);
		return convertView;
	}

}
