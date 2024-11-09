package example.dbchatbot.model;

import lombok.Data;
import java.io.Serializable;
import java.time.Instant;

@Data
public class ChatSession implements Serializable {
    private String sessionId;
    private String userId;
    private String currentSchema;
    private Instant lastAccessTime;
    private String databaseName;

    public ChatSession() {
        this.lastAccessTime = Instant.now();
    }

    public ChatSession(String sessionId) {
        this();
        this.sessionId = sessionId;
        this.userId = sessionId;
    }

    public void updateLastAccessTime() {
        this.lastAccessTime = Instant.now();
    }
}