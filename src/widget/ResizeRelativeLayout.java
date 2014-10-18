package widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class ResizeRelativeLayout extends RelativeLayout {
	
	private OnSizeChangedListener onSizeChangedListener;
	
	public ResizeRelativeLayout(Context context) {
		super(context);
	}
	
	public ResizeRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public ResizeRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void setOnSizeChangedListener(OnSizeChangedListener onSizeChangedListener) {
		this.onSizeChangedListener = onSizeChangedListener;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (onSizeChangedListener != null) {
			onSizeChangedListener.onSizeChanged(w, h, oldw, oldh);
		}
		super.onSizeChanged(w, h, oldw, oldh);
	}
	
	public interface OnSizeChangedListener
	{
		public void onSizeChanged(int w, int h, int oldw, int oldh);
	}
}
