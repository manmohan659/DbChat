package example.dbchatbot.controller;

import example.dbchatbot.model.ChatSession;
import example.dbchatbot.service.SessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping
    public ResponseEntity<List<ChatSession>> getAllSessions() {
        try {
            List<ChatSession> sessions = sessionService.getAllSessions();
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearAllSessions() {
        try {
            sessionService.deleteAllSessions();
            return ResponseEntity.ok("All sessions have been cleared.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to clear sessions: " + e.getMessage());
        }
    }
}