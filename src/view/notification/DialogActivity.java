package view.notification;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Vibrator;
import utils.RingtoneUtils;
import view.main.R;

public class DialogActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notify_dialog);
			
		showAlertDialog(this, "提醒", this.getIntent().getStringExtra("content"));
	}

    public static void showAlertDialog(final Activity context, String title, String message) {
        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        final Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        vibrator.vibrate(999999999900000L);

        final int audioType = AudioManager.STREAM_RING;

        final int currentVolume = audioManager.getStreamVolume(audioType);
        audioManager.setStreamVolume(audioType, 100, AudioManager.FLAG_ALLOW_RINGER_MODES);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

        RingtoneUtils.playRingtone(context, RingtoneManager.TYPE_RINGTONE);

        new AlertDialog.Builder(context).setTitle(title).setMessage(message)
                .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RingtoneUtils.stopRingtone();
                        vibrator.cancel();
                        audioManager.setStreamVolume(audioType, currentVolume, AudioManager.FLAG_ALLOW_RINGER_MODES);
                        context.finish();
                    }
                }).create().show();
    }
}
