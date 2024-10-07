package example.dbchatbot.model;

public class ChatMessage {
    private String message;

    // Constructors
    public ChatMessage() {}

    public ChatMessage(String message) {
        this.message = message;
    }

    // Getter and Setter
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}