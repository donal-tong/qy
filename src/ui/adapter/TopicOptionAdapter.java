package ui.adapter;

import java.util.List;

import android.graphics.Bitmap;
import bean.TopicOptionEntity;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.vikaa.allcontact.R;

import config.CommonValue;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import config.MyApplication;
import tools.StringUtils;

public class TopicOptionAdapter extends BaseAdapter {

	private Context context;
	private List<TopicOptionEntity> datas;
	private LayoutInflater inflater;
    private DisplayImageOptions displayImageOptions;
	public static class ViewHolder {
        ImageView avatarView;
        TextView titleView;
        TextView desView;
        TextView creatorView;
	}
	
	public TopicOptionAdapter(Context context, List<TopicOptionEntity> datas) {
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
		ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.index_phone_cell, null);
            viewHolder.titleView = (TextView) convertView.findViewById(R.id.title);
            viewHolder.desView = (TextView) convertView.findViewById(R.id.des);
            viewHolder.avatarView = (ImageView) convertView.findViewById(R.id.avatarImageView);
            viewHolder.creatorView = (TextView) convertView.findViewById(R.id.creator);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
		TopicOptionEntity model = datas.get(position);
        viewHolder.titleView.setText(model.title);
        ImageLoader.getInstance().displayImage(model.thumb, viewHolder.avatarView, displayImageOptions);
        viewHolder.creatorView.setText(model.subTitle);
		return convertView;
	}

}
