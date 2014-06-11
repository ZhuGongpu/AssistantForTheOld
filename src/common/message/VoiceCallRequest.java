package common.message;

public class VoiceCallRequest extends DataPacket {

    private static final long serialVersionUID = 2647240329845347024L;
    //    public Type type = Type.VOICE_CALL_REQUEST;
    public String callerAccount = null;
    public String calleeAccount = null;

    public VoiceCallRequest() {
        type = MessageType.VOICE_CALL_REQUEST;
    }
}
