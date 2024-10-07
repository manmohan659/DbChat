package example.dbchatbot.controller;

import example.dbchatbot.service.DatabaseService;
import example.dbchatbot.model.DataBaseConnection;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/database")
public class DatabaseController {

    private final DatabaseService databaseService;
    public DatabaseController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @PostMapping("/connect")
    public ResponseEntity<String> connectToDatabase(@RequestBody DataBaseConnection dataBaseConnection) {
        try{
            databaseService.connectAndFetchSchema(connection);
            return ResponseEntity.ok("Connected to database successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to connect to database: " + e.getMessage());
        }
    }

}
