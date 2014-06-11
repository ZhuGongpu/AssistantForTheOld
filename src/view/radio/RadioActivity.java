package view.radio;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import com.iflytek.cloud.speech.SpeechSynthesizer;
import common.message.MusicGenre;
import tts.TTS;
import utils.RadioPlayer;
import view.main.R;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by zhugongpu on 14-6-7.
 * TODO 待完成
 */
public class RadioActivity extends Activity implements View.OnClickListener {

    private static final int VOICE_CONTROL_REQUEST_CODE = 1221;
    private static String TAG = "RadioActivity";
    /**
     * UI elements
     */
    private ImageButton ttsButton = null;
    private ImageButton voiceControlButton = null;
    private ImageButton classicMusicButton = null;
    private ImageButton teleplayMusicButton = null;
    private ImageButton erhuButton = null;
    private ImageButton guzhengButton = null;
    private ImageButton redSongButton = null;
    private ImageButton traditionalOperaButton = null;

    /**
     * TTS content
     */
    private String[] supportedModes = new String[]{"古典音乐", "电视剧插曲", "二胡", "古筝", "红歌", "戏曲"};

    private TTS tts = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.radio_activity_layout);

        tts = new TTS(this);
        tts.init();

        initViews();
    }

    /**
     * 初始化界面
     */
    private void initViews() {

        ttsButton = (ImageButton) findViewById(R.id.radio_tts_button);
        ttsButton.setOnClickListener(this);
        voiceControlButton = (ImageButton) findViewById(R.id.radio_voice_control_button);
        voiceControlButton.setOnClickListener(this);

        classicMusicButton = (ImageButton) findViewById(R.id.radio_class_song);
        classicMusicButton.setOnClickListener(this);
        teleplayMusicButton = (ImageButton) findViewById(R.id.radio_teleplay_song);
        teleplayMusicButton.setOnClickListener(this);
        erhuButton = (ImageButton) findViewById(R.id.radio_erhu);
        erhuButton.setOnClickListener(this);
        guzhengButton = (ImageButton) findViewById(R.id.radio_guzheng);
        guzhengButton.setOnClickListener(this);
        redSongButton = (ImageButton) findViewById(R.id.radio_red_song);
        redSongButton.setOnClickListener(this);
        traditionalOperaButton = (ImageButton) findViewById(R.id.radio_traditional_opera);
        traditionalOperaButton.setOnClickListener(this);
    }

    /**
     * 每次点击播放时，先将所有图标重置，然后在设置单击的图标
     */
    private void resetIcons() {
        //全部设置为默认图标
        classicMusicButton.setImageResource(R.drawable.music_classic_button_normal);
        teleplayMusicButton.setImageResource(R.drawable.music_teleplay_button_normal);
        erhuButton.setImageResource(R.drawable.music_erhu_button_normal);
        guzhengButton.setImageResource(R.drawable.music_guzhen_button_normal);
        redSongButton.setImageResource(R.drawable.music_redsong_button_normal);
        traditionalOperaButton.setImageResource(R.drawable.music_traditional_opera_button_normal);

    }

    /**
     * 点击按钮时，先将所有图标全部重置，然后设置点击的图标，然后设置music genre,然后播放
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.radio_tts_button:
                readCurrentPage();
                break;
            case R.id.radio_voice_control_button:
                startVoiceControl();
                break;
            case R.id.radio_class_song:
                try {
                    RadioPlayer.setMusicGenre(MusicGenre.ClassicMusic);
                    resetIcons();

                    classicMusicButton.setImageResource(R.drawable.music_classic_button_playing);
                    RadioPlayer.play(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.radio_teleplay_song:

                try {
                    RadioPlayer.setMusicGenre(MusicGenre.TeleplayMusic);
                    resetIcons();

                    teleplayMusicButton.setImageResource(R.drawable.music_teleplay_button_playing);
                    RadioPlayer.play(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.radio_erhu:
                try {
                    RadioPlayer.setMusicGenre(MusicGenre.Erhu);
                    resetIcons();

                    erhuButton.setImageResource(R.drawable.music_erhu_button_playing);
                    RadioPlayer.play(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.radio_guzheng:
                try {
                    RadioPlayer.setMusicGenre(MusicGenre.Guzheng);
                    resetIcons();

                    guzhengButton.setImageResource(R.drawable.music_guzhen_button_playing);
                    RadioPlayer.play(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.radio_red_song:
                try {
                    RadioPlayer.setMusicGenre(MusicGenre.RedSong);
                    resetIcons();

                    redSongButton.setImageResource(R.drawable.music_redsong_button_playing);
                    RadioPlayer.play(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.radio_traditional_opera:
                try {
                    RadioPlayer.setMusicGenre(MusicGenre.TraditionalOpera);
                    resetIcons();

                    traditionalOperaButton.setImageResource(R.drawable.music_traditional_opera_button_playing);
                    RadioPlayer.play(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            default:
                break;
        }
    }

    /**
     * 读出当前界面
     */
    private void readCurrentPage() {
        SpeechSynthesizer synthesizer = tts.getsSpeechSynthesizer();
        String content = "当前支持的模式有：";

        for (String item : supportedModes) {
            content += item + ",";
        }

        Log.e(TAG, "Content : " + content);
        synthesizer.startSpeaking(content, null);
    }

    /**
     * 开始语音控制，开启相应的Intent
     */
    public void startVoiceControl() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, VOICE_CONTROL_REQUEST_CODE);
    }

    /**
     * 用于处理语音识别返回的结果
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == VOICE_CONTROL_REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            for (String match : matches.subList(0, Math.min(5, matches.size()))) {
                Log.e(TAG, "语音识别结果：" + match);
                if (textUnderstander(match))
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 将文本转化为相应的命令并执行
     *
     * @param text 需要处理的命令
     * @return 若不支持输入的命令，返回false
     */
    private boolean textUnderstander(String text) {

        if (text.contains("暂停")) {//暂停音乐
            RadioPlayer.pause();
            return true;
        } else if (text.contains("播放")) {//播放音乐

            MusicGenre genre = MusicGenre.ClassicMusic;
            if (text.contains("红歌"))
                genre = MusicGenre.RedSong;
            else if (text.contains("电视"))
                genre = MusicGenre.TeleplayMusic;
            else if (text.contains("二胡"))
                genre = MusicGenre.Erhu;
            else if (text.contains("古筝"))
                genre = MusicGenre.Guzheng;
            else if (text.contains("戏曲"))
                genre = MusicGenre.TraditionalOpera;

            try {
                RadioPlayer.setMusicGenre(genre);
                RadioPlayer.play(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        } else if (text.contains("下一")) {
            try {
                RadioPlayer.playNext(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (text.contains("上一")) {
            try {
                RadioPlayer.playLast(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;//未能处理输入的指令
    }
}
