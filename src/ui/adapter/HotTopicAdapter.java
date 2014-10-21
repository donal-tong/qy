package ui.adapter;

import java.util.List;

import tools.Logger;
import tools.StringUtils;
import ui.adapter.PhonebookAdapter.CellHolder;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.vikaa.contactactivityassitant.R;

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

public class HotTopicAdapter extends BaseAdapter{

	private Context context;
	private List<TopicEntity> datas;
	private LayoutInflater inflater;
	private DisplayImageOptions displayImageOptions;
	
	static class CellHolder {
		TextView tvTitle;
		TextView tvTime;
		ImageView imgThumb;
		TextView tvDes;
	}
	
	public HotTopicAdapter(Context context, List<TopicEntity> datas) {
		super();
		this.context = context;
		this.datas = datas;
		this.inflater = LayoutInflater.from(context);
		this.displayImageOptions = new DisplayImageOptions.Builder()
		.bitmapConfig(Bitmap.Config.RGB_565)
		.showImageOnLoading(R.drawable.content_image_loading)
		.showImageForEmptyUri(R.drawable.logo_120)
		.showImageOnFail(R.drawable.logo_120)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) 
				.considerExifParams(true)
				.displayer(new BitmapDisplayer() {
					@Override
					public void display(Bitmap bitmap, ImageAware imageAware,
							LoadedFrom loadedFrom) {
						imageAware.setImageBitmap(bitmap);
					}
				})
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
			convertView = inflater.inflate(R.layout.cell_hot_topic, null);
			cell.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
			cell.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
			cell.imgThumb = (ImageView) convertView.findViewById(R.id.imgThumb);
			cell.tvDes = (TextView) convertView.findViewById(R.id.tvDes);
		}
		else {
			cell = (CellHolder) convertView.getTag();
		}
		convertView.setTag(cell);
		TopicEntity model = datas.get(position);
		cell.tvTitle.setText(model.title);
		ImageLoader.getInstance().displayImage(StringUtils.notEmpty(model.thumb)?model.thumb.replace("!64",""):MyApplication.getInstance().getUserAvatar(), cell.imgThumb, displayImageOptions);
        Logger.i(model.thumb.replace("!64",""));
        cell.tvTime.setText(model.pubdate);
		cell.tvDes.setText(model.description);
		return convertView;
	}

}
