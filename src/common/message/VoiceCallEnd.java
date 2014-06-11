package common.message;

/**
 * Created with IntelliJ IDEA.
 * User: 朱公朴
 * Date: 13-8-13
 * Time: 下午11:40
 * To change this template use File | Settings | File Templates.
 */
public class VoiceCallEnd extends DataPacket {

    public String hostUserAccount = null;
    public String bindUserAccount = null;

    public VoiceCallEnd() {
        type = MessageType.VOICE_CALL_END;
    }

}
