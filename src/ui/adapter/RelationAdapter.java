package ui.adapter;

import java.util.List;

import tools.StringUtils;
import ui.CardView;
import ui.WeFriendCardSearch;
import ui.adapter.FriendCardSearchAdapter.CellHolder;
import bean.CardIntroEntity;
import bean.RelationshipEntity;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.vikaa.wecontact.R;

import config.CommonValue.LianXiRenType;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RelationAdapter extends BaseAdapter{

	private Context context;
	private LayoutInflater inflater;
	private List<RelationshipEntity> cards;
	private DisplayImageOptions displayOption;
	
	static class CellHolder {
		TextView from;
		TextView titleView;
		TextView desView;
	}
	
	public RelationAdapter(Context context, List<RelationshipEntity> cards) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.cards = cards;
		this.displayOption = new DisplayImageOptions.Builder()
		.bitmapConfig(Bitmap.Config.RGB_565)
		.showImageOnLoading(R.drawable.avatar_placeholder)
		.showImageForEmptyUri(R.drawable.avatar_placeholder)
		.showImageOnFail(R.drawable.avatar_placeholder)
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
	@Override
	public int getCount() {
		return cards.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CellHolder cell = null;
		if (convertView == null) {
			cell = new CellHolder();
			convertView = inflater.inflate(R.layout.relation_cell, null);
			cell.titleView = (TextView) convertView.findViewById(R.id.name);
			cell.desView = (TextView) convertView.findViewById(R.id.from);
			convertView.setTag(cell);
		}
		else {
			cell = (CellHolder) convertView.getTag();
		}
		final RelationshipEntity model = cards.get(position);
		cell.titleView.setText((position+1)+"."+model.realname);
		cell.desView.setText(model.title);
		convertView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				((CardView)context).showIntroOption(model);
			}
		});
		return convertView;
	}

}
