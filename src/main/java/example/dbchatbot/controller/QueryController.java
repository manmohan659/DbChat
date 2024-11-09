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

import java.util.ArrayList;
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
            boolean hasDatabaseConnection = databaseService.hasConnection(session.getUserId());
            // Get schema
            String schema = sessionService.getSchema(sessionId);
            if (schema == null || schema.isEmpty()) {
                return createErrorResponse(sessionId, "No schema available. Please upload a schema file or connect to a database.");
            }
            System.out.println("UserQuery: " + userQuery.getQuery());
            System.out.println(schema);
            List<ChatMessage> history = sessionService.getConversationHistory(sessionId);
            String llmResponse = llmService.decideAndRespond(userQuery.getQuery(), schema, history);

            // Generate SQL
            String sqlQuery = extractSQLQuery(llmResponse);
            System.out.println("SQLQuerY " +  sqlQuery);
            System.out.println("LLM Response " +  llmResponse);
            logger.debug("LLM Response: {}", llmResponse);
            logger.debug("Extracted SQL Query: {}", sqlQuery);

            // Initialize AI message
            ChatMessage aiMessage = new ChatMessage(sessionId, llmResponse, false);
            aiMessage.setType(ChatMessage.MessageType.RESPONSE);
            aiMessage.setGeneratedSql(sqlQuery);

            // Check if there is a database connection

            if (!hasDatabaseConnection) {
                // No database connection, return the SQL query
                aiMessage.setMessage("Generated SQL query based on your input.");
                aiMessage.setGeneratedSql(sqlQuery);
                sessionService.saveChatMessage(aiMessage);
                return ResponseEntity.ok(aiMessage);
            }

            // Proceed to execute the query if connected to a database
            if (sqlQuery == null || !sqlQuery.trim().toLowerCase().startsWith("select")) {
                // No valid SELECT SQL generated
                aiMessage.setMessage("I'm sorry, I couldn't generate a valid SQL query for that request.");
                sessionService.saveChatMessage(aiMessage);
                return ResponseEntity.ok(aiMessage);
            }

            if (sqlQuery.contains("?")) {
                aiMessage.setMessage("The generated SQL query contains unresolved parameters. Please provide more details.");
                sessionService.saveChatMessage(aiMessage);
                return ResponseEntity.ok(aiMessage);
            }

            // Execute query
            List<Map<String, Object>> results = databaseService.executeQuery(session.getUserId(), sqlQuery.trim());

            // Generate response
            String naturalResponse;
            if (shouldDisplayAsTable(results)) {
                Map<String, Object> tableData = convertResultsToMap(results);
                aiMessage.setTableData(tableData);
                naturalResponse = "Displaying results as a table.";
            } else {
                naturalResponse = llmService.generateSQL(userQuery.getQuery(), convertResultsToString(results));
                aiMessage.setMessage(naturalResponse);
            }

            aiMessage.setMessage(naturalResponse);
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

    // Method to determine if data should be displayed in a table
    private boolean shouldDisplayAsTable(List<Map<String, Object>> results) {
        return results != null && !results.isEmpty();
    }   

    private ResponseEntity<ChatMessage> createErrorResponse(String sessionId, String errorMessage) {
        ChatMessage errorResponse = new ChatMessage(sessionId, errorMessage, false);
        errorResponse.setType(ChatMessage.MessageType.ERROR);
        sessionService.saveChatMessage(errorResponse);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }



    private String extractSQLQuery(String llmResponse) {
        Pattern sqlPattern = Pattern.compile("(?is)(SELECT\\s+.*?;)");
        Matcher matcher = sqlPattern.matcher(llmResponse);
        if (matcher.find()) {
            String sql = matcher.group(1).trim();
            // Remove any trailing or leading characters that are not part of the SQL
            sql = sql.replaceAll("^.*?(SELECT\\s)", "SELECT ").replaceAll(";.*$", ";");
            return sql;
        }
        return null;
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

    // In QueryController.java
    private Map<String, Object> convertResultsToMap(List<Map<String, Object>> results) {
        Map<String, Object> data = new HashMap<>();
        if (results == null || results.isEmpty()) {
            data.put("headers", new ArrayList<>());
            data.put("rows", new ArrayList<>());
            return data;
        }
    
        List<String> headers = new ArrayList<>(results.get(0).keySet());
        List<List<Object>> rows = new ArrayList<>();
    
        for (Map<String, Object> row : results) {
            List<Object> rowData = new ArrayList<>();
            for (String header : headers) {
                rowData.add(row.get(header));
            }
            rows.add(rowData);
        }
    
        data.put("headers", headers);
        data.put("rows", rows);
        return data;
    }


}