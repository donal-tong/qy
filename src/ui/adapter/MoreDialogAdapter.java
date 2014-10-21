package ui.adapter;

import java.util.List;

import widget.ActionItem;

import com.vikaa.contactactivityassitant.R;

import bean.CardIntroEntity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

public class MoreDialogAdapter extends BaseAdapter{

	private Context context;
	private LayoutInflater inflater;
	private List<CardIntroEntity> cards;
	
	public MoreDialogAdapter(Context context, List<CardIntroEntity> cards) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.cards = cards;
	}
	
	static class CellHolder {
		TextView titleView;
		ImageView iconView;
	}
	
	@Override
	public int getCount() {
		return cards.size();
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
			convertView = LayoutInflater.from(context).inflate(R.layout.more_cell, null);
			cell.titleView = (TextView) convertView.findViewById(R.id.title);
			cell.iconView = (ImageView) convertView.findViewById(R.id.icon);
			convertView.setTag(cell);
		}
		else {
			cell = (CellHolder) convertView.getTag();
		}
		CardIntroEntity model = cards.get(position);
		cell.titleView.setText(model.realname);
		cell.iconView.setBackgroundResource(Integer.valueOf(model.department));
		return convertView;
	}

}
