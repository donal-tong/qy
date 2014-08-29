package ui.adapter;

import java.util.List;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.vikaa.allcontact.R;

import config.CommonValue;
import config.CommonValue.PhoneSectionType;
import bean.PhoneIntroEntity;
import tools.Logger;
import tools.StringUtils;
import ui.Index;
import ui.MyCard;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class IndexSquareAdapter extends BaseAdapter {
	private Context context;
	private LayoutInflater inflater;
	private List<PhoneIntroEntity> phones;
	private DisplayImageOptions displayImageOptions;
	
	public IndexSquareAdapter(Context context, List<PhoneIntroEntity> phones) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.phones = phones;
		this.displayImageOptions = new DisplayImageOptions.Builder()
		.bitmapConfig(Bitmap.Config.RGB_565)
		.showImageOnLoading(R.drawable.content_image_loading)
		.showImageForEmptyUri(R.drawable.logo_120)
		.showImageOnFail(R.drawable.logo_120)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) 
		.displayer(new BitmapDisplayer() {
			@Override
			public void display(Bitmap bitmap, ImageAware imageAware,
					LoadedFrom loadedFrom) {
				imageAware.setImageBitmap(bitmap);
			}
		})
		.build();
	}

	static class CellHolder {
		ImageView avatarView;
		TextView titleView;
		TextView desView;
		TextView timeView;
	}

	@Override
	public int getCount() {
		return phones.size();
	}
	
	@Override
	public Object getItem(int arg0) {
		return phones.get(arg0);
	}
	
	@Override
	public long getItemId(int arg0) {
		return phones.get(arg0).getId();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		CellHolder cell = null;
		if (convertView == null) {
			cell = new CellHolder();
			convertView = inflater.inflate(R.layout.index_cell, null);
			cell.titleView = (TextView) convertView.findViewById(R.id.title);
			cell.desView = (TextView) convertView.findViewById(R.id.des);
			cell.avatarView = (ImageView) convertView.findViewById(R.id.avatarImageView);
			cell.timeView = (TextView) convertView.findViewById(R.id.time);
			convertView.setTag(cell);
		}
		else {
			cell = (CellHolder) convertView.getTag();
		}
		final PhoneIntroEntity model = phones.get(position);
		ImageLoader.getInstance().displayImage(model.logo, cell.avatarView, displayImageOptions);
		cell.titleView.setText(model.title);
		cell.desView.setText(String.format("人数:%s 点击数:%s", model.member, model.hits));
		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				((Index)context).showSquarePhoneViewWeb(model);
			}
		});
		return convertView;
	}

}
