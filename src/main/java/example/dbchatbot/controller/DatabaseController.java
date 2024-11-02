package example.dbchatbot.controller;

import example.dbchatbot.service.DatabaseService;
import example.dbchatbot.service.SessionService;
import example.dbchatbot.model.DataBaseConnection;
import example.dbchatbot.model.ChatSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/database")
public class DatabaseController {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseController.class);
    private final DatabaseService databaseService;
    private final SessionService sessionService;

    public DatabaseController(DatabaseService databaseService, SessionService sessionService) {
        this.databaseService = databaseService;
        this.sessionService = sessionService;
    }

    @PostMapping("/connect")
    public ResponseEntity<?> connectToDatabase(
            @RequestBody DataBaseConnection dataBaseConnection,
            @RequestHeader("X-Session-ID") String sessionId) {

        logger.info("Received connection request for host: {}, database: {}",
                dataBaseConnection.getHost(), dataBaseConnection.getDatabase());

        try {
            ChatSession session = sessionService.getSession(sessionId)
                    .orElseGet(() -> sessionService.createSession(sessionId));

            databaseService.connectAndFetchSchema(session.getUserId(), dataBaseConnection);
            logger.info("Successfully connected to database for session: {}", sessionId);

            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("message", "Connected to database successfully");
            successResponse.put("sessionId", sessionId);
            return ResponseEntity.ok(successResponse);
        } catch (Exception e) {
            logger.error("Failed to connect to database", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to connect to database: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    @GetMapping("/schema")
    public ResponseEntity<String> getSchema(@RequestHeader("X-Session-ID") String sessionId) {
        try {
            ChatSession session = sessionService.getSession(sessionId)
                .orElseThrow(() -> new RuntimeException("Invalid session"));
                
            String schema = databaseService.getSchema(session.getUserId());
            if (schema == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                   .body("No schema found for this session");
            }
            return ResponseEntity.ok(schema);
        } catch (Exception e) {
            logger.error("Error retrieving schema", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body("Error retrieving schema: " + e.getMessage());
        }
    }
}