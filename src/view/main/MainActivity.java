package view.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.iflytek.cloud.speech.SpeechSynthesizer;
import common.message.DataPacket;
import common.message.VoiceCallRequest;
import info.UserInfo;
import model.network.NetworkIOManager;
import tts.TTS;
import utils.RadioPlayer;
import view.call.CallActivity;
import view.call.ContactActivity;
import view.notification.NotificationActivity;
import view.radio.RadioActivity;
import view.settings.SettingsActivity;

import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private static final int VOICE_CONTROL_REQUEST_CODE = 1221;

    /**
     * UI elements
     */
    private ImageButton callButton = null;
    private ImageButton musicPlayerButton = null;
    private ImageButton notificationButton = null;
    private ImageButton settingsButton = null;
    private ImageButton ttsButton = null;
    private ImageButton voiceControlButton = null;

    /**
     * TTS content
     */
    private String[] supportedModes = new String[]{"播放音乐", "打电话", "信息提醒", "详细设置"};

    private TTS tts = null;

    /**
     * network data handler
     */
    private Handler networkHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.obj instanceof DataPacket) {
                DataPacket dataPacket = (DataPacket) msg.obj;

                switch (dataPacket.type) {
                    case VOICE_CALL_REQUEST:
                        //put extra and jump to call activity
                        VoiceCallRequest voiceCallRequest = (VoiceCallRequest) dataPacket;
                        Intent intent = new Intent();
                        intent.putExtra("name", voiceCallRequest.callerAccount);
                        intent.putExtra("isCallOut", false);
                        intent.setClass(MainActivity.this, CallActivity.class);

                        try {
                            NetworkIOManager.getInstance().setHandler(null);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        startActivity(intent);
                        finish();
                        break;

                    default:
                        break;
                }
            }
            super.handleMessage(msg);
        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUserInfo();

        if (UserInfo.userAccount.length() <= 0)//无法获取本机号码
        {

            final EditText phoneNumberEditText = new EditText(this);

            //提示用户手动输入本机号码
            new AlertDialog.Builder(this)
                    .setTitle("请输入本机号码")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setView(phoneNumberEditText)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            //检查长度
                            String phoneNumber = phoneNumberEditText.getText().toString();

                            if (isPhoneNumberLegal(phoneNumber)) {
                                UserInfo.setUserAccount(phoneNumber);
                                logonAndInitViewsAsync();
                            } else {
                                phoneNumberEditText.setError("手机号码错误");
                            }
                        }

                        /**
                         * 检查号码是否合法
                         * @param phoneNumber
                         * @return
                         */
                        private boolean isPhoneNumberLegal(String phoneNumber) {
                            if (phoneNumber.length() == 11) {
                                for (char number : phoneNumber.toCharArray()) {
                                    if (!(number >= '0' && number <= '9'))
                                        return false;
                                }
                                return true;
                            } else
                                return false;
                        }
                    })
                    .create();
        } else
            logonAndInitViewsAsync();
    }

    /**
     * 设置用户信息，用于登录
     */
    private void setUserInfo() {
        UserInfo.userAccount = ((TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();
    }

    /**
     * 以异步任务的形式登录，登陆成功后加载界面
     */
    private void logonAndInitViewsAsync() {
        new AsyncTask<Void, Void, Void>() {

            ProgressDialog progressBar = null;

            boolean isSucceeded = false;

            /**
             * 显示登录进度条
             */
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressBar = new ProgressDialog(MainActivity.this);
                progressBar.setMessage("请稍后...");
                progressBar.show();

            }

            /**
             * 与服务器建立连接，并发送 login 包，同时设置网络数据包 handler，并初始化 tts
             */
            @Override
            protected Void doInBackground(Void... voids) {

                try {
                    NetworkIOManager.getInstance().setHandler(networkHandler);

                    tts = new TTS(MainActivity.this);
                    tts.init();
                    isSucceeded = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            /**
             * 取消进度条
             * 若与服务器正确建立连接，则开始初始化界面；否则退出应用，提示 检查网络设置
             */
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                progressBar.dismiss();
                if (isSucceeded)
                    initView();
                else {
                    exit();
                    Toast.makeText(MainActivity.this, "请检查网络设置", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();

    }

    /**
     * 初始化界面元素，并设置监听器
     */
    private void initView() {
        setContentView(R.layout.main);

        /**
         * find views
         */
        callButton = (ImageButton) findViewById(R.id.main_call_button);
        musicPlayerButton = (ImageButton) findViewById(R.id.main_music_player_button);
        notificationButton = (ImageButton) findViewById(R.id.main_notification_button);
        settingsButton = (ImageButton) findViewById(R.id.main_settings_button);

        ttsButton = (ImageButton) findViewById(R.id.main_tts_button);
        voiceControlButton = (ImageButton) findViewById(R.id.main_voice_control_button);


        /**
         * set listeners
         */
        callButton.setOnClickListener(this);
        musicPlayerButton.setOnClickListener(this);
        notificationButton.setOnClickListener(this);
        settingsButton.setOnClickListener(this);
        ttsButton.setOnClickListener(this);
        voiceControlButton.setOnClickListener(this);

    }

    /**
     * 处理按钮点击事件,进行界面跳转或启动相应的功能
     */
    @Override
    public void onClick(View view) {
        //todo handle the click events
        switch (view.getId()) {
            case R.id.main_call_button:
                //jump to call activity and show contact
                jumpToActivity(new Intent(this, ContactActivity.class));
                break;
            case R.id.main_music_player_button:
                //jump to music activity
                jumpToActivity(new Intent(this, RadioActivity.class));
                break;
            case R.id.main_notification_button:
                jumpToActivity(new Intent(this, NotificationActivity.class));
                break;
            case R.id.main_settings_button:
                jumpToActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.main_tts_button://读出当前界面
                readCurrentPage();
                break;
            case R.id.main_voice_control_button://激活语音控制
                startVoiceControl();
                break;
            default:
                break;
        }
    }

    private void jumpToActivity(Intent intent) {
        startActivity(intent);
        finish();
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
     * 处理按键事件
     * 当按下返回键时，直接退出应用，并清理资源
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
        }
        return super.onKeyDown(keyCode, event);
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

        if (text.contains("电话")) {//语音通话

            /**
             * 可能出现的情况有：
             * “打电话”：直接跳转到通讯录界面
             * “给xxx打电话”：直接跳转到通话界面
             */

            //TODO 处理 给xxx打电话的情况


            startActivity(new Intent(this, ContactActivity.class));
            finish();

            return true;
        } else if (text.contains("提醒") || text.contains("信息") || text.contains("消息")) {
            /**
             * 可能出现的情况：
             * “进入消息提醒界面”：直接跳转至 NotificationActivity
             * “查看消息提醒”：直接tts所有提醒，需要在 NotificationActivity 中提供接口
             * “添加提醒”：直接进入插入消息界面，需要在 NotificationActivity 中提供接口
             */

            //TODO 很不完善

            jumpToActivity(new Intent(this, NotificationActivity.class));
            return true;
        } else if (text.contains("设置")) {

            /**
             * 可能出现的情况：
             * “进入设置界面”：直接跳转
             * “设置xxx为xxx”：直接修改响应的设置
             */

            //TODO 很不完善

            jumpToActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (text.contains("暂停")) {//暂停音乐
            RadioPlayer.pause();
            return true;
        } else if (text.contains("播放")) {//播放音乐
            try {
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

    /**
     * 关闭网络连接，并退出
     */
    private void exit() {
        try {
            NetworkIOManager.getInstance().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finish();
    }

}
