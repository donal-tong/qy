package ui.adapter;

import java.util.List;

import tools.StringUtils;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.vikaa.mycontact.R;

import config.CommonValue;

import bean.PhoneIntroEntity;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PhonebookAdapter extends BaseExpandableListAdapter{

	private Context context;
	private List<List<PhoneIntroEntity>> datas;
	private LayoutInflater inflater;
	private DisplayImageOptions displayImageOptions;
	
	static class SectionHolder {
		TextView typeView;
		View divider;
	}
	
	public class CellHolder {
		public ImageView avatarView;
		public TextView titleView;
		public TextView desView;
		public TextView creatorView;
	}
	
	public PhonebookAdapter(Context context, List<List<PhoneIntroEntity>> datas) {
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
		.imageScaleType(ImageScaleType.EXACTLY_STRETCHED) 
		.displayer(new RoundedBitmapDisplayer(10))
		.build();
	}
	
	@Override
	public Object getChild(int arg0, int arg1) {
		return null;
	}

	@Override
	public long getChildId(int arg0, int arg1) {
		return 0;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean arg2, View convertView, ViewGroup arg4) {
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
		final PhoneIntroEntity model = datas.get(groupPosition).get(childPosition);
		cell.titleView.setTag(model);
		cell.titleView.setText(model.title);
		String des = String.format("浏览:%s 人数:%s\n", model.hits, model.member);
		ImageLoader.getInstance().displayImage(model.logo, cell.avatarView, displayImageOptions);
		if (StringUtils.notEmpty(model.creator)) {
			cell.creatorView.setText(Html.fromHtml("由<font color=\"#088ec1\">"+model.creator+"</font>发起"));
		}
		else {
			cell.creatorView.setText(model.subtitle);
		}
		if (StringUtils.notEmpty(model.member)) {
			cell.desView.setText(des);
		}
		else {
			cell.desView.setText("");
		}
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupIndex) {
		return datas.get(groupIndex).size();
	}

	@Override
	public Object getGroup(int arg0) {
		return null;
	}

	@Override
	public int getGroupCount() {
		return datas.size();
	}

	@Override
	public long getGroupId(int arg0) {
		return 0;
	}

	@Override
	public View getGroupView(int groupPosition, boolean arg1, View convertView, ViewGroup arg3) {
		SectionHolder section = null;
		if (convertView == null) {
			section = new SectionHolder();
			convertView = inflater.inflate(R.layout.index_section, null);
			section.typeView = (TextView) convertView.findViewById(R.id.titleView);
			section.divider = (View) convertView.findViewById(R.id.divider);
			convertView.setTag(section);
		}
		else {
			section = (SectionHolder) convertView.getTag();
		}
		
		if (getChildrenCount(groupPosition) == 0) {
			section.typeView.setVisibility(View.GONE);
			section.divider.setVisibility(View.GONE);
		}
		else if (getChildrenCount(groupPosition) !=0 && datas.get(groupPosition).get(0).phoneSectionType.equals(CommonValue.PhoneSectionType.MobileSectionType)) {
			section.typeView.setVisibility(View.GONE);
			section.divider.setVisibility(View.GONE);
		}
		else {
			section.typeView.setVisibility(View.VISIBLE);
			section.divider.setVisibility(View.VISIBLE);
			section.typeView.setText(datas.get(groupPosition).get(0).phoneSectionType);
		}
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int arg0, int arg1) {
		return true;
	}

}
