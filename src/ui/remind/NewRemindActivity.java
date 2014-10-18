package ui.remind;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.vikaa.mycontact.R;

import android.app.Dialog;
import android.app.ActionBar.LayoutParams;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;
import ui.AppActivity;

public class NewRemindActivity extends AppActivity implements OnClickListener{
	
	@ViewInject(R.id.tvName)
	private TextView tvName;
	
	@ViewInject(R.id.tvTime)
	private TextView tvTime;
	
	@ViewInject(R.id.hideRemindTogBtn)
	private ToggleButton hideRemindTogBtn;
	
	private EditText edtName;
	
	@ViewInject(R.id.timeLayout)
	private RelativeLayout timeLayout;
	@ViewInject(R.id.datePicker)
	private DatePicker datePicker;
	@ViewInject(R.id.timePicker)
	private TimePicker timePicker;
	private int year;
	private int month;
	private int date;
	private int hour;
	private int minute;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_remind_add);
		ViewUtils.inject(this);
		resizeDatePikcer(datePicker);
		resizePikcer(timePicker);
		Calendar calendar=Calendar.getInstance();
		year = calendar.get(Calendar.YEAR);
		month = calendar.get(Calendar.MONTH);
		date = calendar.get(Calendar.DAY_OF_MONTH);;
		hour = calendar.get(Calendar.HOUR_OF_DAY);
		minute = calendar.get(Calendar.MINUTE);
		String time = year+"-"+(month+1)+"-"+date+" " +hour+":"+minute;
		tvTime.setText(time);
		initTimeLayout();
		tvName.setOnClickListener(this);
		tvTime.setOnClickListener(this);
		hideRemindTogBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean checked) {
				
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tvName:
			break;
		case R.id.tvTime:
			timeLayout.setVisibility(View.VISIBLE);
			break;
		case R.id.btnTimeOK:
			timeLayout.setVisibility(View.INVISIBLE);
			break;
		default:
			break;
		}
	}

	private void initTimeLayout() {
		timePicker.setIs24HourView(true);
		if (year == 0) {
			return;
		}
		datePicker.init(year, month, date, new OnDateChangedListener() {
			@Override
			public void onDateChanged(DatePicker view, int currentYear, int monthOfYear,
					int dayOfMonth) {
				year = currentYear;
				month = monthOfYear;
				date = dayOfMonth;
				String time = year+"-"+(month+1)+"-"+date+" " +hour+":"+minute;
				tvTime.setText(time);
			}
		});
		timePicker.setCurrentHour(hour);
		timePicker.setCurrentMinute(minute);
		timePicker.setOnTimeChangedListener(new OnTimeChangedListener() {
			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				String time = year+"-"+(month+1)+"-"+date+" " +hourOfDay+":"+minute;
				tvTime.setText(time);
			}
		});
	}
	
	private void resizePikcer(FrameLayout tp){
		List<NumberPicker> npList = findNumberPicker(tp);
		for(NumberPicker np:npList){
			resizeNumberPicker(np);
		}
	}
	
	private void resizeDatePikcer(FrameLayout tp){
		List<NumberPicker> npList = findNumberPicker(tp);
		for(NumberPicker np:npList){
			resizeDateNumberPicker(np);
		}
	}
	
	private List<NumberPicker> findNumberPicker(ViewGroup viewGroup){
		List<NumberPicker> npList = new ArrayList<NumberPicker>();
		View child = null;
		if(null != viewGroup){
			for(int i = 0;i<viewGroup.getChildCount();i++){
				child = viewGroup.getChildAt(i);
				if(child instanceof NumberPicker){
					npList.add((NumberPicker)child);
				}
				else if(child instanceof LinearLayout){
					List<NumberPicker> result = findNumberPicker((ViewGroup)child);
					if(result.size()>0){
						return result;
					}
				}
			}
		}
		return npList;
	}
	
	private void resizeDateNumberPicker(NumberPicker np){
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(150, LayoutParams.WRAP_CONTENT);
		params.setMargins(10, 0, 10, 0);
		np.setLayoutParams(params);
	}
	
	private void resizeNumberPicker(NumberPicker np){
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, LayoutParams.WRAP_CONTENT);
		params.setMargins(10, 0, 10, 0);
		np.setLayoutParams(params);
	}
}
