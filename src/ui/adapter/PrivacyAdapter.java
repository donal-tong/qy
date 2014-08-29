package ui.adapter;

import java.util.List;

import com.vikaa.allcontact.R;

import bean.KeyValue;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class PrivacyAdapter extends BaseAdapter{

	private Context context;
	private List<KeyValue> datas;
	private LayoutInflater inflater;
	
	static class ViewHolder {
		TextView text;
	}
	
	public PrivacyAdapter(Context context, List<KeyValue> datas) {
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
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.spinner_item, null);
			viewHolder = new ViewHolder();
			viewHolder.text = (TextView) convertView.findViewById(R.id.text);
			convertView.setTag(viewHolder);
		}
		else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.text.setText(datas.get(position).key);
		return convertView;
	}

}
