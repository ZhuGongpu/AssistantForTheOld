package view.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {		
		Intent in = new Intent();
		in.putExtra("content", intent.getStringExtra("content"));
		in.setClass(context, DialogActivity.class);
		in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(in);
	}
}
