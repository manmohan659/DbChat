package example.dbchatbot.controller;

import example.dbchatbot.service.DatabaseService;
import example.dbchatbot.model.DataBaseConnection;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/database")
public class DatabaseController {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseController.class);
    private final DatabaseService databaseService;

    public DatabaseController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @PostMapping("/connect")
    public ResponseEntity<String> connectToDatabase(@RequestBody DataBaseConnection dataBaseConnection) {
        logger.info("Received connection request for host: {}, database: {}", dataBaseConnection.getHost(), dataBaseConnection.getDatabase());
        try {
            databaseService.connectAndFetchSchema(dataBaseConnection);
            logger.info("Successfully connected to database");
            return ResponseEntity.ok("Connected to database successfully");
        } catch (Exception e) {
            logger.error("Failed to connect to database", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Failed to connect to database: " + e.getMessage());
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> testConnection() {
        logger.info("Received test connection request");
        return ResponseEntity.ok("Backend is reachable");
    }
}
