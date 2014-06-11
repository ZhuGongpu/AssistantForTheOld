package common.message;

public class Login extends DataPacket {

    public Login() {
        type = MessageType.LOGIN;
    }

    private static final long serialVersionUID = 4432007765736134602L;
    public String account = null;

}
