package tts;

import android.content.Context;
import com.iflytek.cloud.speech.SpeechConstant;
import com.iflytek.cloud.speech.SpeechSynthesizer;
import com.iflytek.cloud.speech.SpeechUser;

/**
 * Created by Gongpu on 2014/5/8.
 */
public class TTS {

    private Context context = null;
    private SpeechSynthesizer synthesizer = null;

    public TTS(Context context) {
        this.context = context;
    init();
    }

    public void init()
    {
        SpeechUser.getUser().login(context, null, null, "appid=51feb3a8", null);
    }

    public SpeechSynthesizer getsSpeechSynthesizer() {
        if (synthesizer == null) {
            synthesizer = SpeechSynthesizer.createSynthesizer(context);

            //设置发音人
            synthesizer.setParameter(SpeechConstant.VOICE_NAME, "xiaoyu");
            //设置语速 0~100
            synthesizer.setParameter(SpeechConstant.SPEED, "50");
            //设置音量
            synthesizer.setParameter(SpeechConstant.VOLUME, "80");
        }

        return synthesizer;
    }

}
