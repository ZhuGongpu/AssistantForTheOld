package common.message;

public class Voice extends DataPacket {
    private static final long serialVersionUID = 2603326424171173846L;

    public Voice() {
        type = MessageType.VOICE;
    }

    public byte[] data = null;
    public int size;
}
