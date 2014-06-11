package common.message;

/**
 * Created with IntelliJ IDEA.
 * User: 朱公朴
 * Date: 13-8-14
 * Time: 下午8:03
 * To change this template use File | Settings | File Templates.
 */
public class VoiceCallRequestFeedback extends DataPacket {

    public boolean isAccepted = false;
    public boolean isOnline = true;

    public VoiceCallRequestFeedback() {
        type = MessageType.VOICE_CALL_REQUEST_FEEDBACK;
    }
}
