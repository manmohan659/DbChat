package example.dbchatbot.model;

import lombok.Data;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

@Data
public class ChatMessage implements Serializable {
    private String messageId;
    private String sessionId;
    private String message;
    private boolean isUser;
    private Instant timestamp;



    private String generatedSql;
    private MessageType type;
    private Map<String, Object> tableData; // Ensure getters and setters are present
    
    public enum MessageType {
        QUERY,
        RESPONSE,
        ERROR,
        SYSTEM,
        TABLE
    }
    
    public ChatMessage() {
        this.timestamp = Instant.now();
    }
    
    public ChatMessage(String sessionId, String message, boolean isUser) {
        this();
        this.sessionId = sessionId;
        this.message = message;
        this.isUser = isUser;
        this.messageId = generateMessageId();
        this.type = isUser ? MessageType.QUERY : MessageType.RESPONSE;
    }
    
    private String generateMessageId() {
        return sessionId + "-" + timestamp.toEpochMilli();
    }
}