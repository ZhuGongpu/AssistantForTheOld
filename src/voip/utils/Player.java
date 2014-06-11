package voip.utils;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import voip.speex.Speex;


public class Player {

    private int sampleRateInHz = 44100;
    private int inChannelConfig = AudioFormat.CHANNEL_IN_STEREO;
    private int outChannelConfig = AudioFormat.CHANNEL_OUT_STEREO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSize;
    private short[] buffer;
    private Speex speex = null;
    private AudioTrack audioTrack = null;

    public Player() {
        int audioRecordBufferSize = AudioRecord.getMinBufferSize(
                sampleRateInHz, inChannelConfig, audioFormat);
        int audioTrackBufferSize = AudioTrack.getMinBufferSize(sampleRateInHz,
                outChannelConfig, audioFormat);

        bufferSize = Math.max(audioRecordBufferSize, audioTrackBufferSize);
        System.out.println("Player : audioRecordBufferSize " + audioRecordBufferSize);
        System.out.println("Player : audioTrackBufferSize " + audioTrackBufferSize);
        System.out.println("Player : bufferSize " + bufferSize);

        buffer = new short[bufferSize];
        speex = new Speex();
        speex.init();
        System.out.println("speex.getFrameSize() " + speex.getFrameSize());

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRateInHz, outChannelConfig, audioFormat, bufferSize,
                AudioTrack.MODE_STREAM);

        if (audioTrack != null)
            System.out.println("audioTrack inited");

        try {
            audioTrack.play();
            System.out.println("audioTrack.playEncodedData()");
        } catch (Exception e) {
            System.out.println("failed to audioTrack.playEncodedData()");
            e.printStackTrace();
        }
    }

    public void playEncodedData(byte[] encodedBuffer, int length) {

        System.out.println("TODO : decode      length = " + length);
        for (int i = 0; i < length; i++)
            System.out.print(encodedBuffer[i]);
        System.out.println();
//TODO speex.getFrameSize
        buffer = new short[bufferSize];
        System.out.println("before decode");
        int decodeSize = speex.decode(encodedBuffer, buffer, length);
        System.out.println("after decode, before playEncodedData    decodeSize = " + decodeSize);
        for (int i = 0; i < decodeSize; i++) {
            System.out.print(buffer[i]);
        }
        System.out.println();

        if (decodeSize > 0)
            audioTrack.write(buffer, 0, decodeSize);

        System.out.println("played");
    }

    public void play(byte[] data, boolean isEncoded) {
        if (!isEncoded) {
            audioTrack.write(data, 0, data.length);
        } else
            playEncodedData(data, data.length);
    }

    public void stopPlaying() {
        audioTrack.stop();
        audioTrack.release();
        audioTrack = null;
        speex.close();
    }

}
