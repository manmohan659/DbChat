package example.dbchatbot.controller;

import example.dbchatbot.model.ChatMessage;
import example.dbchatbot.model.ChatSession;
import example.dbchatbot.service.DatabaseService;
import example.dbchatbot.service.LLMService;
import example.dbchatbot.service.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/query")
public class QueryController {
    private static final Logger logger = LoggerFactory.getLogger(QueryController.class);
    private final LLMService llmService;
    private final DatabaseService databaseService;
    private final SessionService sessionService;

    public QueryController(LLMService llmService, DatabaseService databaseService, SessionService sessionService) {
        this.llmService = llmService;
        this.databaseService = databaseService;
        this.sessionService = sessionService;
    }

    @PostMapping("/nl-to-sql")
    public ResponseEntity<?> query(@RequestBody UserQuery userQuery, @RequestHeader("X-Session-ID") String sessionId) {
        logger.debug("Received query request for session: {}", sessionId);
        try {
            Optional<ChatSession> sessionOpt = sessionService.getSession(sessionId);
            if (sessionOpt.isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid or expired session");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            ChatSession session = sessionOpt.get();

            // Save user message
            ChatMessage userMessage = new ChatMessage(sessionId, userQuery.getQuery(), true);
            sessionService.saveChatMessage(userMessage);

            // Get schema
            String schema = sessionService.getSchema(sessionId);
            if (schema == null || schema.isEmpty()) {
                return createErrorResponse(sessionId, "No schema available. Please connect to a database first.");
            }

            // Generate SQL
            String llmResponse = llmService.decideAndRespond(userQuery.getQuery(), schema);
            String sqlQuery = extractSQLQuery(llmResponse);
            
            if (sqlQuery == null || !sqlQuery.trim().toLowerCase().startsWith("select")) {
                ChatMessage aiMessage = new ChatMessage(sessionId, llmResponse, false);
                aiMessage.setType(ChatMessage.MessageType.RESPONSE);
                sessionService.saveChatMessage(aiMessage);
                return ResponseEntity.ok(aiMessage);
            }

            // Execute query with user ID from session
            List<Map<String, Object>> results = databaseService.executeQuery(session.getUserId(), sqlQuery.trim());
            
            // Generate natural language response
            String naturalResponse = llmService.generateSQL(userQuery.getQuery(), convertResultsToString(results));
            
            // Save AI response
            ChatMessage aiMessage = new ChatMessage(sessionId, naturalResponse, false);
            aiMessage.setGeneratedSql(sqlQuery);
            aiMessage.setType(ChatMessage.MessageType.RESPONSE);
            sessionService.saveChatMessage(aiMessage);

            return ResponseEntity.ok(aiMessage);

        } catch (Exception e) {
            logger.error("Error processing query: {}", e.getMessage(), e);
            return createErrorResponse(sessionId, "Error processing query: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getChatHistory(@RequestHeader("X-Session-ID") String sessionId) {
        try {
            List<ChatMessage> history = sessionService.getChatHistory(sessionId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            logger.error("Error retrieving chat history: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving chat history: " + e.getMessage());
        }
    }

    private ResponseEntity<ChatMessage> createErrorResponse(String sessionId, String errorMessage) {
        ChatMessage errorResponse = new ChatMessage(sessionId, errorMessage, false);
        errorResponse.setType(ChatMessage.MessageType.ERROR);
        sessionService.saveChatMessage(errorResponse);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    private String extractSQLQuery(String llmResponse) {
        Pattern sqlPattern = Pattern.compile("(?i)(SELECT\\s+.*?;)", Pattern.DOTALL);
        Matcher matcher = sqlPattern.matcher(llmResponse);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    private String convertResultsToString(List<Map<String, Object>> results) {
        if (results == null || results.isEmpty()) {
            return "No results found.";
        }

        StringBuilder sb = new StringBuilder();
        Map<String, Object> firstRow = results.get(0);
        
        // Add headers
        firstRow.keySet().forEach(header -> sb.append(header).append("\t"));
        sb.append("\n");

        // Add data rows
        results.forEach(row -> {
            row.values().forEach(value -> sb.append(value != null ? value : "NULL").append("\t"));
            sb.append("\n");
        });

        return sb.toString();
    }
}