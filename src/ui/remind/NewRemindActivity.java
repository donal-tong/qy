package ui.remind;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import bean.RemindEntity;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.vikaa.contactactivityassitant.R;

import db.manager.RemindDBManager;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ActionBar.LayoutParams;
import android.content.Intent;
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
import tools.AppManager;
import tools.ImageUtils;
import tools.StringUtils;
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
	private Calendar calendar;
	private int pickerWidth;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_remind_add);
		ViewUtils.inject(this);
		pickerWidth = (screeWidth-ImageUtils.dip2px(this, 20)*2)/6;
		resizeDatePikcer(datePicker);
		resizePikcer(timePicker);
		calendar=Calendar.getInstance();
		year = calendar.get(Calendar.YEAR);
		month = calendar.get(Calendar.MONTH);
		date = calendar.get(Calendar.DAY_OF_MONTH);;
		hour = calendar.get(Calendar.HOUR_OF_DAY);
		minute = calendar.get(Calendar.MINUTE);
		String time = year+"-"+(month+1)+"-"+date+" " +hour+":"+minute;
		tvTime.setText(time);
		initTimeLayout();
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
		case R.id.tvTime:
			closeInput();
			timeLayout.setVisibility(View.VISIBLE);
			break;
		case R.id.btnTimeOK:
			timeLayout.setVisibility(View.INVISIBLE);
			break;
		case R.id.leftBarButton:
			AppManager.getAppManager().finishActivity(this);
			break;
		case R.id.rightBarButton:
			saveRemind();
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
			public void onTimeChanged(TimePicker view, int hourOfDay, int minuteOfHour) {
				hour = hourOfDay;
				minute = minuteOfHour;
				String time = year+"-"+(month+1)+"-"+date+" " +hour+":"+minute;
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
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(pickerWidth, LayoutParams.WRAP_CONTENT);
		params.setMargins(5, 0, 5, 0);
		np.setLayoutParams(params);
	}
	
	private void resizeNumberPicker(NumberPicker np){
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(pickerWidth, LayoutParams.WRAP_CONTENT);
		params.setMargins(5, 0, 5, 0);
		np.setLayoutParams(params);
	}
	
	private void saveRemind() {
		String name = tvName.getText().toString();
		if (StringUtils.empty(name)) {
			WarningDialog("请输入名称");
			return;
		}
		if (year < calendar.get(Calendar.YEAR) || month<calendar.get(Calendar.MONTH) || date<calendar.get(Calendar.DAY_OF_MONTH)
				|| hour<calendar.get(Calendar.HOUR_OF_DAY) || minute<calendar.get(Calendar.MINUTE)) {
			WarningDialog("请选择正确的时间");
			return;
		}
		RemindEntity remind = new RemindEntity();
		remind.remindName = name;
		remind.year = year;
		remind.month = month;
		remind.day = date;
		remind.hour = hour;
		remind.minute = minute;
		remind.repeat = "" + 60*1000;
		long id = RemindDBManager.getInstance(this).saveRemind(remind);
		Calendar remindCalendar = Calendar.getInstance();
		remindCalendar.set(year, month, date, hour, minute, 0);
		Intent intent = new Intent(this, RemindReceiver.class);
		// 设置intent的动作,识别当前设置的是哪一个闹铃,有利于管理闹铃的关闭
		intent.setAction(""+id);
		// 用广播管理闹铃
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);
		// 获取闹铃管理
		AlarmManager am = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
		// 设置闹钟
		am.set(AlarmManager.RTC_WAKEUP, remindCalendar.getTimeInMillis(), pi);
		// 设置闹钟重复时间
		am.setRepeating(AlarmManager.RTC_WAKEUP,
				remindCalendar.getTimeInMillis(), 60 * 1000, pi);
		AppManager.getAppManager().finishActivity(this);
	}
}
