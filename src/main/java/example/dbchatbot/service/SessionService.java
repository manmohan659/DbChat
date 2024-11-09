package example.dbchatbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import example.dbchatbot.model.ChatMessage;
import example.dbchatbot.model.ChatSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class SessionService {
    private static final Logger logger = LoggerFactory.getLogger(SessionService.class);
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String SESSIONS_SET_KEY = "sessions:set";


    @Value("${session.timeout.minutes:90}")
    private long sessionTimeoutHours;

    @Autowired
    public SessionService(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public ChatSession createSession(String sessionId) {
        try {
            ChatSession session = new ChatSession(sessionId);
            String sessionJson = objectMapper.writeValueAsString(session);
            String key = getSessionKey(session.getSessionId());

            redisTemplate.opsForValue().set(key, sessionJson, Duration.ofHours(sessionTimeoutHours));

            // Add session ID to the set
            redisTemplate.opsForSet().add(SESSIONS_SET_KEY, sessionId);
            logger.debug("Created new session: {}", session.getSessionId());

            return session;
        } catch (Exception e) {
            logger.error("Error creating session for sessionId {}: {}", sessionId, e.getMessage());
            throw new RuntimeException("Failed to create session", e);
        }
    }

    public Optional<ChatSession> getSession(String sessionId) {
        try {
            String key = getSessionKey(sessionId);
            String sessionJson = redisTemplate.opsForValue().get(key);

            if (sessionJson != null) {
                ChatSession session = objectMapper.readValue(sessionJson, ChatSession.class);
                session.updateLastAccessTime();
                updateSession(session);
                return Optional.of(session);
            }

            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error retrieving session {}: {}", sessionId, e.getMessage());
            return Optional.empty();
        }
    }

    public void updateSession(ChatSession session) {
        try {
            String key = getSessionKey(session.getSessionId());
            String sessionJson = objectMapper.writeValueAsString(session);
            redisTemplate.opsForValue().set(key, sessionJson, Duration.ofHours(sessionTimeoutHours));
        } catch (Exception e) {
            logger.error("Error updating session {}: {}", session.getSessionId(), e.getMessage());
            throw new RuntimeException("Failed to update session", e);
        }
    }


    public void saveSchema(String sessionId, String schema) {
        try {
            ChatSession session = getSession(sessionId)
                    .orElseGet(() -> createSession(sessionId));

            session.setCurrentSchema(schema);
            updateSession(session);
            logger.debug("Schema saved for session: {}", sessionId);
        } catch (Exception e) {
            logger.error("Error saving schema for session {}: {}", sessionId, e.getMessage());
            throw new RuntimeException("Failed to save schema", e);
        }
    }

    public String getSchema(String sessionId) {
        return getSession(sessionId)
            .map(ChatSession::getCurrentSchema)
            .orElse(null);
    }

    public void saveChatMessage(ChatMessage message) {
        try {
            String key = getChatHistoryKey(message.getSessionId());
            String messageJson = objectMapper.writeValueAsString(message);
            redisTemplate.opsForList().rightPush(key, messageJson);
            redisTemplate.expire(key, Duration.ofHours(sessionTimeoutHours));

            logger.debug("Saved chat message for session: {}", message.getSessionId());
        } catch (Exception e) {
            logger.error("Error saving chat message: {}", e.getMessage());
            throw new RuntimeException("Failed to save chat message", e);
        }
    }


    public List<ChatMessage> getChatHistory(String sessionId) {
        try {
            String key = getChatHistoryKey(sessionId);
            List<String> messageJsons = redisTemplate.opsForList().range(key, 0, -1);
            List<ChatMessage> messages = new ArrayList<>();

            if (messageJsons != null) {
                for (String json : messageJsons) {
                    messages.add(objectMapper.readValue(json, ChatMessage.class));
                }
            }

            return messages;
        } catch (Exception e) {
            logger.error("Error retrieving chat history for session {}: {}", sessionId, e.getMessage());
            return new ArrayList<>();
        }
    }
    
    
    private String getSessionKey(String sessionId) {
        return "session:" + sessionId;
    }

    public void deleteSession(String sessionId) {
        try {
            String sessionKey = getSessionKey(sessionId);
            String chatHistoryKey = getChatHistoryKey(sessionId);

            redisTemplate.delete(sessionKey);
            redisTemplate.delete(chatHistoryKey);

            // Remove session ID from the set
            redisTemplate.opsForSet().remove(SESSIONS_SET_KEY, sessionId);

            logger.debug("Deleted session and chat history: {}", sessionId);
        } catch (Exception e) {
            logger.error("Error deleting session {}: {}", sessionId, e.getMessage());
            throw new RuntimeException("Failed to delete session", e);
        }
    }

    public List<ChatMessage> getConversationHistory(String sessionId) {
        return getChatHistory(sessionId);
    }

    private String getChatHistoryKey(String sessionId) {
        return "chat:" + sessionId;
    }

    public List<ChatSession> getAllSessions() {
        try {
            Set<String> sessionIds = redisTemplate.opsForSet().members(SESSIONS_SET_KEY);
            List<ChatSession> sessions = new ArrayList<>();

            if (sessionIds != null) {
                for (String sessionId : sessionIds) {
                    Optional<ChatSession> sessionOpt = getSession(sessionId);
                    if (sessionOpt.isPresent()) {
                        sessions.add(sessionOpt.get());
                    } else {
                        // Session has expired, remove it from the set
                        redisTemplate.opsForSet().remove(SESSIONS_SET_KEY, sessionId);
                    }
                }
            }

            return sessions;
        } catch (Exception e) {
            logger.error("Error retrieving all sessions: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    public void deleteAllSessions() {
        try {
            Set<String> sessionIds = redisTemplate.opsForSet().members(SESSIONS_SET_KEY);
            if (sessionIds != null) {
                for (String sessionId : sessionIds) {
                    deleteSession(sessionId);
                }
            }
            // Clear the sessions set
            redisTemplate.delete(SESSIONS_SET_KEY);
            logger.debug("All sessions have been deleted.");
        } catch (Exception e) {
            logger.error("Error deleting all sessions: {}", e.getMessage());
            throw new RuntimeException("Failed to delete all sessions", e);
        }
    }


}