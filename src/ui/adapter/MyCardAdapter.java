package ui.adapter;

import java.util.List;

import android.widget.ImageView;
import tools.Logger;
import tools.StringUtils;
import ui.JiaV;
import ui.MyCard;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.vikaa.allcontact.R;

import bean.CardIntroEntity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class MyCardAdapter extends BaseAdapter{
	private Context context;
	private LayoutInflater inflater;
	private List<CardIntroEntity> cards;
	private ImageLoader imageLoader;
	private DisplayImageOptions displayOptions ;
	
	static class CellHolder {
		ImageView avatarImageView;
		TextView titleView;
		TextView desView;
        Button btnCertified;
	}

	public MyCardAdapter(Context context, List<CardIntroEntity> cards, ImageLoader imageLoader) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.cards = cards;
		this.imageLoader = imageLoader;
		this.displayOptions = new DisplayImageOptions.Builder()
		.bitmapConfig(Bitmap.Config.RGB_565)
		.showImageOnLoading(R.drawable.avatar_placeholder)
		.showImageForEmptyUri(R.drawable.avatar_placeholder)
		.showImageOnFail(R.drawable.avatar_placeholder)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.imageScaleType(ImageScaleType.EXACTLY_STRETCHED) 
		.displayer(new RoundedBitmapDisplayer(10))
		.build();
	}
	
	@Override
	public int getCount() {
		return cards.size();
	}

	@Override
	public Object getItem(int arg0) {
		return cards.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return cards.get(arg0).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		CellHolder cell = null;
		if (convertView == null) {
			cell = new CellHolder();
			convertView = inflater.inflate(R.layout.my_card_cell, null);
			cell.avatarImageView = (ImageView) convertView.findViewById(R.id.avatarImageView);
			cell.titleView = (TextView) convertView.findViewById(R.id.title);
			cell.desView = (TextView) convertView.findViewById(R.id.des);
			cell.btnCertified = (Button) convertView.findViewById(R.id.imgCertified);
			convertView.setTag(cell);
		}
		else {
			cell = (CellHolder) convertView.getTag();
		}
		final CardIntroEntity model = cards.get(position);
		cell.titleView.setText(model.realname);
		cell.desView.setText(String.format("%s %s", model.department, model.position));
		this.imageLoader.displayImage(model.avatar, cell.avatarImageView, this.displayOptions);
//		Logger.i(model.certified_state);
		if (StringUtils.notEmpty(model.certified)) {
			cell.btnCertified.setBackgroundResource(model.certified.equals("0")?R.drawable.mycard_uncertified:R.drawable.mycard_certified);
			if ((model.certified.equals("0"))) {
				if (StringUtils.notEmpty(model.certified_state)) {
					if (model.certified_state.equals("1")) {
						cell.btnCertified.setBackgroundResource(R.drawable.mycard_certifying);
					}
				}
			}
		}
        ((MyCard)context).accretionArea(cell.btnCertified);
		cell.btnCertified.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (StringUtils.notEmpty(model.certified)) {
					if ((model.certified.equals("0"))) {
						if (StringUtils.notEmpty(model.certified_state)) {
							if (model.certified_state.equals("1")) {
								return;
							}
							((MyCard)context).startActivityForResult(new Intent(context, JiaV.class).putExtra("code", model.code).putExtra("token", ""), 10);
						}
						else {
							((MyCard)context).startActivityForResult(new Intent(context, JiaV.class).putExtra("code", model.code).putExtra("token", ""), 10);
						}
					}
				}
			}
		});
//		convertView.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View arg0) {
//				((MyCard)context).showCardViewWeb(model);
//			}
//		});
//		convertView.setOnLongClickListener(new OnLongClickListener() {
//			@Override
//			public boolean onLongClick(View arg0) {
//				((MyCard)context).cardSharePre(false, null, model);
//				return false;
//			}
//		});
		return convertView;
	}

}
