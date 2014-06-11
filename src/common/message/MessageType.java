package common.message;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: 朱公朴
 * Date: 13-8-14
 * Time: 下午7:52
 * To change this template use File | Settings | File Templates.
 */
public enum MessageType implements Serializable{
    LOGIN, LOGIN_FEEDBACK,
    LOGOUT,
    REGISTER, REGISTER_FEEDBACK,
    BIND_REQUEST, BIND_REQUEEST_FEEDBACK,
    UPDATE_USER_INFO,
    TEXT,
    VOICE, VOICE_CALL_REQUEST, VOICE_CALL_END, VOICE_CALL_REQUEST_FEEDBACK,
    VIDEO,
    SongRequest, SongRequestFeedback
}
