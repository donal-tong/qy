package ui.remind;

import java.util.Calendar;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.vikaa.mycontact.R;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_remind_add);
		ViewUtils.inject(this);
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
			break;

		default:
			break;
		}
	}

	
}
