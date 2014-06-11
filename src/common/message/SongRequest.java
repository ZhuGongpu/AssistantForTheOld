package common.message;

/**
 * Created by Gongpu on 2014/5/22.
 */
public class SongRequest extends DataPacket {
    public MusicGenre genre = null;

    public SongRequest() {
        this.type = MessageType.SongRequest;
    }
}
