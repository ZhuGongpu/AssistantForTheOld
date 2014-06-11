package common.message;

public class Logout extends DataPacket {

    private static final long serialVersionUID = 6883096295902922961L;

    public Logout() {
        type = MessageType.LOGOUT;
    }

    public String account = null;
}
