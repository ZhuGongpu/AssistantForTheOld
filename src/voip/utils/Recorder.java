package voip.utils;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import voip.speex.Speex;


public class Recorder {

    // AudioManager audioManager =
    // (AudioManager)getSystemService(Context.AUDIO_SERVICE);

    private AudioRecord audioRecord = null;
    private int bufferSize;
    private int sampleRateInHz = 41000;
    private int inChannelConfig = AudioFormat.CHANNEL_IN_STEREO;
    private int outChannelConfig = AudioFormat.CHANNEL_OUT_STEREO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int readSize;
    private Speex speex = null;

    public Recorder() {

        int audioRecordBufferSize = AudioRecord.getMinBufferSize(
                sampleRateInHz, inChannelConfig, audioFormat);
        int audioTrackBufferSize = AudioTrack.getMinBufferSize(sampleRateInHz,
                outChannelConfig, audioFormat);

        bufferSize = Math.max(audioRecordBufferSize, audioTrackBufferSize);
        System.out.println("Recorder : audioRecordBufferSize " + audioRecordBufferSize);
        System.out.println("Recorder : audioTrackBufferSize " + audioTrackBufferSize);
        System.out.println("Recorder : bufferSize " + bufferSize);

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                sampleRateInHz, inChannelConfig, audioFormat, bufferSize);

        speex = new Speex();
        speex.init();
        System.out.println("speex.getFrameSize() = " + speex.getFrameSize());

        if (audioRecord != null)
            System.out.println("audioRecord inited");
        try {
            audioRecord.startRecording();
            System.out.println("startRecording");
        } catch (Exception e) {
            System.out.println("failed to startRecording");
            e.printStackTrace();
        }
    }

    public int record(byte[] buffer) {
//TODO speex.getFrameSize()
        short[] tempInShort = new short[bufferSize];
        byte[] tempInByte = new byte[bufferSize];
        System.out.println("Recorder : record : TODO read");
        readSize = audioRecord.read(tempInShort, 0, speex.getFrameSize());
        System.out.println("Recorder : record : after read before encode       readSize in short = " + readSize);

//        for (int i = 0; i < readSize; i++)
//            System.out.print(tempInShort[i]);
//        System.out.println();

        int size = speex.encode(tempInShort, 0, tempInByte, readSize);
        System.out.println("Recorder : record : encode done");

        buffer = new byte[size];
        System.arraycopy(tempInByte, 0, buffer, 0, size);

        for (int i = 0; i < size; i++)
            System.out.print(buffer[i]);
        System.out.println();
        System.out.println("size = " + size);
        return size;
    }

    public void stopRecording() {
        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;
        System.out.println("stopRecording");
    }
}
