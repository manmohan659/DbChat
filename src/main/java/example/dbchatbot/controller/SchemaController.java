package example.dbchatbot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import example.dbchatbot.service.SchemaService;
import example.dbchatbot.service.SessionService;


@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/schema")
public class SchemaController {

    private final SchemaService schemaService;
    @Autowired
    private SessionService sessionService;

    public SchemaController(SchemaService schemaService, SessionService sessionService) {
        this.schemaService = schemaService;
    }

// SchemaController.java

    @PostMapping("/upload")
    public ResponseEntity<String> uploadSchema(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-Session-ID") String sessionId) {
        try {
            // Ensure session exists
            sessionService.getSession(sessionId).orElseGet(() -> sessionService.createSession(sessionId));

            schemaService.parseAndStoreSchema(file, sessionId);
            return ResponseEntity.ok("Schema uploaded and associated with session successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to upload schema: " + e.getMessage());
        }
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> schemaExists(@RequestHeader("X-Session-ID") String sessionId) {
        try {
            String schema = schemaService.getSchema(sessionId);
            return ResponseEntity.ok(schema != null);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }
}