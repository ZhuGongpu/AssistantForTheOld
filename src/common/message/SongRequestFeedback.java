package common.message;

/**
 * Created by Gongpu on 2014/5/22.
 */
public class SongRequestFeedback extends DataPacket {
    public String url = null;
    public SongRequestFeedback()
    {
        this.type = MessageType.SongRequestFeedback;
    }
}
