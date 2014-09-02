package ui.adapter;

import java.util.List;

import tools.ImageUtils;
import tools.Logger;
import widget.ListViewForScrollView;

import com.vikaa.wecontact.R;

import bean.KeyValue;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class CardViewAdapter extends BaseAdapter {

	private Context context;
	private LayoutInflater inflater;
	private List<KeyValue> summarys;
	
	static class CellHolder {
		TextView titleView;
		TextView desView;
		ListViewForScrollView xlistView;
		TextView tipView;
	}
	
	public CardViewAdapter(Context context, List<KeyValue> summarys) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.summarys = summarys;
	}
	
	@Override
	public int getCount() {
		return summarys.size();
	}

	@Override
	public Object getItem(int arg0) {
		return summarys.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return summarys.get(arg0).getId();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup arg2) {
		CellHolder cell = null;
		if (convertView == null) {
			cell = new CellHolder();
			convertView = inflater.inflate(R.layout.card_view_cell, null);
			cell.titleView = (TextView) convertView.findViewById(R.id.title);
			cell.desView = (TextView) convertView.findViewById(R.id.des);
			cell.xlistView = (ListViewForScrollView) convertView.findViewById(R.id.xlistview);
			cell.tipView = (TextView) convertView.findViewById(R.id.tip);
			convertView.setTag(cell);
		}
		else {
			cell = (CellHolder) convertView.getTag();
		}
		KeyValue model = summarys.get(position);
		cell.titleView.setText(model.key);
		cell.desView.setText(Html.fromHtml(model.value));
		if (model.relations.size() > 0) {
			RelationAdapter relationAdapter = new RelationAdapter(context, model.relations);
			cell.xlistView.setAdapter(relationAdapter);
			cell.xlistView.setVisibility(View.VISIBLE);
			cell.tipView.setVisibility(View.VISIBLE);
		}
		else {
			cell.tipView.setVisibility(View.GONE);
			cell.xlistView.setVisibility(View.GONE);
		}
		return convertView;
	}
	
//	private void setListViewHeightBasedOnChildren(ListView listView) { 
//	    if(listView == null) return;
//
//	    ListAdapter listAdapter = listView.getAdapter(); 
//	    if (listAdapter == null) { 
//	        return; 
//	    } 
//	    int totalHeight = listAdapter.getCount() * ImageUtils.dip2px(context, 40); 
//	    ViewGroup.LayoutParams params = listView.getLayoutParams(); 
//	    params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1)); 
//	    listView.setLayoutParams(params); 
//	}
}
