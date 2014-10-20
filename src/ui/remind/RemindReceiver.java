package ui.remind;

import tools.Logger;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class RemindReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		Logger.i("闹钟时间到:"+intent.getAction());
		Toast.makeText(context, "闹钟时间到:"+intent.getAction(), Toast.LENGTH_LONG).show();  
	}

}
