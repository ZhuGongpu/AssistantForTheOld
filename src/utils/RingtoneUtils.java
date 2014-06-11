package utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

/**
 * Created by Gongpu on 2014/4/24.
 */
public class RingtoneUtils {

    private static Ringtone ringtone = null;

    private static Uri getDefaultRingtoneUri(Context context, int type) {
        return RingtoneManager.getActualDefaultRingtoneUri(context, type);
    }

    public static void playRingtone(Context context, int type) {


        ringtone = getDefaultRingtone(context, type);
        ringtone.setStreamType(AudioManager.STREAM_RING);
        ringtone.play();

    }

    public static void stopRingtone() {
        if (ringtone != null && ringtone.isPlaying())
            ringtone.stop();
    }

    private static Ringtone getDefaultRingtone(Context context, int type) {
        return RingtoneManager.getRingtone(context, getDefaultRingtoneUri(context, type));
    }

}
