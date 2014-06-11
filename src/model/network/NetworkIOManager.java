package model.network;

import android.os.Handler;
import android.os.Message;
import common.message.*;
import info.UserInfo;
import utils.RadioPlayer;
import voip.utils.Player;

import java.io.*;
import java.net.Socket;

/**
 * Created by Gongpu on 2014/5/19.
 */
public class NetworkIOManager {

    private static NetworkIOManager instance = null;

    private InputManager inputManager = null;
    private OutputManager outputManager = null;
    private String ServerIP = "192.168.1.110";//todo
    private int ServerPort = 50323;
    private Socket socket = null;
    private Handler handler = null;

    private NetworkIOManager() throws IOException {
        socket = new Socket(ServerIP, ServerPort);
        socket.setTcpNoDelay(true);
        socket.setKeepAlive(true);

        //outputManager用于供上层调用,无需启动
        this.outputManager = new OutputManager(socket.getOutputStream());
        this.outputManager.login();

        this.inputManager = new InputManager(socket.getInputStream());
        this.inputManager.start();
    }

    public static NetworkIOManager getInstance() throws IOException {
        if (instance == null) {
            instance = new NetworkIOManager();
        }
        return instance;
    }


    public void close() {
        handler = null;

        try {
            this.getOutputManager().logout();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            inputManager.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outputManager.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (socket != null)
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        instance = null;

    }

    public OutputManager getOutputManager() {
        return outputManager;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    /**
     * 负责所有输入事务
     */
    private class InputManager extends Thread {

        private ObjectInputStream inputStream = null;
        private Player player = new Player();

        private InputManager(InputStream inputStream) throws IOException {
            this.inputStream = new ObjectInputStream(inputStream);
        }

        @Override
        public void run() {
            super.run();

            while (!this.isInterrupted()) {
                if (this.inputStream != null)
                    try {
                        DataPacket dataPacket = (DataPacket) this.inputStream.readObject();

                        this.dataPacketProcessor(dataPacket);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }

        }

        private void dataPacketProcessor(DataPacket dataPacket) {
//todo
            if (dataPacket != null) {
                switch (dataPacket.type) {
                    case VOICE:
                        //直接播放
                        Voice voice = (Voice) dataPacket;
                        player.playEncodedData(voice.data, voice.size);
                        break;
                    case SongRequestFeedback:
                        RadioPlayer.addSongToPlayList(((SongRequestFeedback) dataPacket).url);
                        break;
                    default:
                        if (handler != null) {
                            Message message = handler.obtainMessage();
                            message.obj = dataPacket;
                            message.sendToTarget();
                        }
                        break;
                }
            }
        }

        private void close() throws IOException {

            interrupt();

            player.stopPlaying();

            if (this.inputStream != null) {
                this.inputStream.close();
                this.inputStream = null;
            }
        }
    }

    /**
     * 负责所有输出事务
     */
    public class OutputManager {

        private ObjectOutputStream outputStream = null;

        private OutputManager(OutputStream outputStream) throws IOException {
            this.outputStream = new ObjectOutputStream(outputStream);
        }

        private void writeObject(DataPacket dataPacket) throws IOException {
            this.outputStream.writeObject(dataPacket);
            this.outputStream.flush();
        }

        public void login() throws IOException {

            Login loginPacket = new Login();
            loginPacket.account = UserInfo.userAccount;

            this.writeObject(loginPacket);
        }

        public void logout() throws IOException {
            Logout logoutPacket = new Logout();
            logoutPacket.account = UserInfo.userAccount;
            this.writeObject(logoutPacket);
        }

        public void requestForSong(MusicGenre genre) throws IOException {
            SongRequest request = new SongRequest();
            request.genre = genre;

            this.writeObject(request);
        }

        public void requestForCall(String calleeAccount) throws IOException {
            VoiceCallRequest request = new VoiceCallRequest();
            request.calleeAccount = calleeAccount;
            request.callerAccount = UserInfo.userAccount;
            this.writeObject(request);
        }

        public void sendVoice(Voice voice) throws IOException {
            this.writeObject(voice);
        }

        public void hangup(VoiceCallEnd end) throws IOException {
            this.writeObject(end);
        }

        public void acceptTheCall() throws IOException {
            VoiceCallRequestFeedback feedback = new
                    VoiceCallRequestFeedback();
            feedback.isAccepted = true;
            feedback.isOnline = true;

            this.writeObject(feedback);
        }

        public void rejectTheCall() throws IOException {
            VoiceCallRequestFeedback feedback = new VoiceCallRequestFeedback();
            feedback.isAccepted = false;

            this.writeObject(feedback);
        }

        private void close() throws IOException {

            if (this.outputStream != null) {
                this.outputStream.close();
                this.outputStream = null;
            }
        }
    }

}
