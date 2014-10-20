package ui.remind;

import java.util.List;

import com.vikaa.mycontact.R;


import bean.RemindEntity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class RemindAdapter extends BaseAdapter{

	private Context context;
	private LayoutInflater inflater;
	private List<RemindEntity> datas;
	
	static class CellHolder {
		TextView tvName;
	}
	
	public RemindAdapter(Context context, List<RemindEntity> datas) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.datas = datas;
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
		CellHolder cell = null;
		if (convertView == null) {
			cell = new CellHolder();
			convertView = inflater.inflate(R.layout.cell_remind, null);
			cell.tvName = (TextView) convertView.findViewById(R.id.tvName);
			convertView.setTag(cell);
		}
		else {
			cell = (CellHolder) convertView.getTag();
		}
		cell.tvName.setText(datas.get(position).remindName);
		return convertView;
	}

}
