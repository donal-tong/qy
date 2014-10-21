package ui;

import tools.AppManager;

import com.vikaa.contactactivityassitant.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

public class CreateActivityTimeDialog extends AppActivity {
	private TextView timeTV;
	private DatePicker datePicker;
	private TimePicker timePicker;
	
	private int year;
	private int month;
	private int date;
	private int hour;
	private int minute;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_activity_time_dialog);
		year = getIntent().getExtras().getInt("year");
		month = getIntent().getExtras().getInt("month");
		date = getIntent().getExtras().getInt("date");
		hour = getIntent().getExtras().getInt("hour");
		minute = getIntent().getExtras().getInt("minute");
		initUI();
		String time = year+"-"+(month+1)+"-"+date+" " +hour+":"+minute;
		timeTV.setText("选择的时间: "+time);
	}
	
	private void initUI() {
		timeTV = (TextView) findViewById(R.id.timeTV);
		datePicker = (DatePicker) findViewById(R.id.datePicker);
		timePicker = (TimePicker) findViewById(R.id.timePicker);
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
				timeTV.setText("选择的时间: "+time);
			}
		});
		timePicker.setCurrentHour(hour);
		timePicker.setCurrentMinute(minute);
		timePicker.setOnTimeChangedListener(new OnTimeChangedListener() {
			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				String time = year+"-"+(month+1)+"-"+date+" " +hourOfDay+":"+minute;
				timeTV.setText("选择的时间: "+time);
			}
		});
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.submit:
			year = datePicker.getYear();
			month = datePicker.getMonth();
			date = datePicker.getDayOfMonth();
			hour = timePicker.getCurrentHour();
			minute = timePicker.getCurrentMinute();
			Intent intent = new Intent();
			intent.putExtra("year", year);
			intent.putExtra("month", month);
			intent.putExtra("date", date);
			intent.putExtra("hour", hour);
			intent.putExtra("minute", minute);
			setResult(RESULT_OK, intent);
			AppManager.getAppManager().finishActivity(this);
			break;

		default:
			break;
		}
	}
	
}
