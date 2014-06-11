package view.call;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import common.message.DataPacket;
import common.message.Voice;
import common.message.VoiceCallEnd;
import common.message.VoiceCallRequestFeedback;
import info.UserInfo;
import model.network.NetworkIOManager;
import tts.TTS;
import view.main.MainActivity;
import view.main.R;
import voip.utils.Recorder;

import java.io.IOException;

/**
 * 跳转时，需要传入是否为call out 和 name 和 number
 * Created by Gongpu on 2014/5/22.
 */
public class CallActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "CallActivity";

    /**
     * UI elements
     */
    private Button acceptButton = null;
    private Button rejectButton = null;
    private Button hangupButton = null;
    private TextView calleeNameTextView = null;

    /**
     * network handler
     */
    private Handler networkHandler
            = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            //todo
            if (msg.obj instanceof DataPacket) {
                DataPacket dataPacket = (DataPacket) msg.obj;

                switch (dataPacket.type) {
                    case VOICE_CALL_REQUEST_FEEDBACK:
                        VoiceCallRequestFeedback voiceCallRequestFeedback = (VoiceCallRequestFeedback) dataPacket;
                        if (voiceCallRequestFeedback.isOnline) {
                            if (voiceCallRequestFeedback.isAccepted) {
                                //开始通话

                                TTS tts = new TTS(CallActivity.this);
                                tts.getsSpeechSynthesizer().startSpeaking("开始通话", null);

                                if (rejectButton != null)
                                    rejectButton.setVisibility(View.GONE);
                                if (acceptButton != null)
                                    acceptButton.setVisibility(View.GONE);
                                if (hangupButton == null) {
                                    findViewById(R.id.call_out_layout).setVisibility(View.VISIBLE);
                                    hangupButton = (Button) findViewById(R.id.hangUpButton);
                                }

                                //start recording and output
                                recordThread.start();

                            } else {
                                //语音提示：您所拨打的号码正在通话中  然后跳转到main
                                new TTS(CallActivity.this).getsSpeechSynthesizer()
                                        .startSpeaking("您所拨打的号码正在通话中...", null);
                                Intent intent = new Intent();
                                intent.setClass(CallActivity.this, MainActivity.class);
                                jumpToOtherActivity(intent);

                            }
                        } else {
                            //直接跳转到拨号界面 and exit
                            String calleeAccount = getIntent().getStringExtra("name");
                            if (calleeAccount != null) {

                                Uri uri = Uri.parse("tel:" + calleeAccount);
                                Intent intent = new Intent(Intent.ACTION_DIAL, uri);
                                jumpToOtherActivity(intent);
                                try {
                                    NetworkIOManager.getInstance().close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        break;
                    case VOICE_CALL_END: {

                        //给出提示音
                        TTS tts = new TTS(CallActivity.this);
                        tts.getsSpeechSynthesizer().startSpeaking("通话已结束", null);

                        Intent intent = new Intent();
                        intent.setClass(CallActivity.this, MainActivity.class);
                        jumpToOtherActivity(intent);
                    }
                    break;
                    default:
                        break;
                }
            }
        }
    };


    /**
     * record thread
     */
    private Thread recordThread = new Thread() {
        @Override
        public void run() {
            super.run();

            Recorder recorder = new Recorder();

            byte[] buffer = new byte[1024];

            while (!this.isInterrupted()) {
                int length = recorder.record(buffer);
                Voice voice = new Voice();
                voice.data = buffer;
                voice.size = length;

                try {
                    NetworkIOManager.getInstance().getOutputManager().sendVoice(voice);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            recorder.stopRecording();
        }
    };

    private String name = "";
    private String number = "";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        number = getIntent().getStringExtra("number");

        initView();

        new AsyncTask<Void,Void,Void>()
        {

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    NetworkIOManager.getInstance().setHandler(networkHandler);
                    NetworkIOManager.getInstance().getOutputManager().requestForCall(number);
                } catch (IOException e) {
                    e.printStackTrace();
                    finish();
                }
                return null;
            }
        }.execute();

    }

    private void initView() {
        setContentView(R.layout.call_activity_layout);

        if (getIntent().getBooleanExtra("isCallOut", true)) {
            findViewById(R.id.call_out_layout).setVisibility(View.VISIBLE);
            hangupButton = (Button) findViewById(R.id.hangUpButton);

            hangupButton.setOnClickListener(this);

        } else {
            findViewById(R.id.call_in_layout).setVisibility(View.VISIBLE);
            acceptButton = (Button) findViewById(R.id.acceptCallButton);

            acceptButton.setOnClickListener(this);

            rejectButton = (Button) findViewById(R.id.rejectCallButton);
            rejectButton.setOnClickListener(this);
        }

        calleeNameTextView = (TextView) findViewById(R.id.calleeName);
        name = getIntent().getStringExtra("name");
        calleeNameTextView.setText(name);
    }

    private void jumpToOtherActivity(Intent intent) {
        try {
            NetworkIOManager.getInstance().setHandler(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.acceptCallButton: {
                try {
                    NetworkIOManager.getInstance().getOutputManager().acceptTheCall();

                    recordThread.start();

                    acceptButton.setVisibility(View.GONE);
                    rejectButton.setVisibility(View.GONE);

                    hangupButton = (Button) findViewById(R.id.hangUpButton);
                    hangupButton.setOnClickListener(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            break;
            case R.id.rejectCallButton: {
                try {
                    NetworkIOManager.getInstance().getOutputManager().rejectTheCall();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent();
                intent.setClass(this, MainActivity.class);
                jumpToOtherActivity(intent);
            }
            break;
            case R.id.hangUpButton: {
                recordThread.interrupt();

                VoiceCallEnd end = new VoiceCallEnd();

                end.bindUserAccount = name;
                end.hostUserAccount = UserInfo.userAccount;
                try {
                    NetworkIOManager.getInstance().getOutputManager().hangup(end);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent();
                intent.setClass(CallActivity.this, MainActivity.class);
                jumpToOtherActivity(intent);
            }

            break;
            default:
                break;
        }
    }
}
