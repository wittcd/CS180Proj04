import java.io.Serializable;

/**
 * ChatMessage class, represents a message sent by a user
 * @author Colin Witt, Justin Chen
 * @version 1.0.0
 * 
 */
final class ChatMessage implements Serializable {
    private static final long serialVersionUID = 6898543889087L;

    // Here is where you should implement the chat message object.
    // Variables, Constructors, Methods, etc.

    private String message;
    private int type;

    public ChatMessage (String message, int type) {
        this.message = message;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public int getType() {
        return type;
    }
}
