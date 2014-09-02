package ui.adapter;

import java.util.List;

import tools.StringUtils;
import ui.adapter.PhonebookAdapter.CellHolder;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.vikaa.wecontact.R;

import config.CommonValue;
import config.MyApplication;
import bean.PhoneIntroEntity;
import bean.TopicEntity;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TopicListAdapter extends BaseAdapter{

	private Context context;
	private List<TopicEntity> datas;
	private LayoutInflater inflater;
	private DisplayImageOptions displayImageOptions;
	
	static class CellHolder {
		ImageView avatarView;
		TextView titleView;
		TextView desView;
		TextView creatorView;
	}
	
	public TopicListAdapter(Context context, List<TopicEntity> datas) {
		super();
		this.context = context;
		this.datas = datas;
		this.inflater = LayoutInflater.from(context);
		this.displayImageOptions = new DisplayImageOptions.Builder()
		.bitmapConfig(Bitmap.Config.RGB_565)
		.showImageOnLoading(R.drawable.icon_topic)
		.showImageForEmptyUri(R.drawable.icon_topic)
		.showImageOnFail(R.drawable.icon_topic)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.imageScaleType(ImageScaleType.EXACTLY_STRETCHED) 
		.displayer(new RoundedBitmapDisplayer(10))
		.build();
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
			convertView = inflater.inflate(R.layout.index_phone_cell, null);
			cell.titleView = (TextView) convertView.findViewById(R.id.title);
			cell.desView = (TextView) convertView.findViewById(R.id.des);
			cell.avatarView = (ImageView) convertView.findViewById(R.id.avatarImageView);
			cell.creatorView = (TextView) convertView.findViewById(R.id.creator);
			convertView.setTag(cell);
		}
		else {
			cell = (CellHolder) convertView.getTag();
		}
		TopicEntity model = datas.get(position);
		cell.titleView.setText(model.title);
		ImageLoader.getInstance().displayImage(model.thumb, cell.avatarView, displayImageOptions);
		cell.creatorView.setText(model.pubdate + "  "+ model.from);
		return convertView;
	}

}
