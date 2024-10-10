package example.dbchatbot.controller;

import example.dbchatbot.service.DatabaseService;
import example.dbchatbot.service.LLMService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/query")
public class QueryController {

    private final LLMService llmService;
    private final DatabaseService databaseService;

    public QueryController(LLMService llmService, DatabaseService databaseService) {
        this.llmService = llmService;
        this.databaseService = databaseService;
    }

    @PostMapping("/nl-to-sql")
    public ResponseEntity<String> query(@RequestBody UserQuery userQuery) {
        try {
            // Fetch the schema when the query is submitted
            String schema = databaseService.getSchema();
            if (schema == null || schema.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                     .body("No schema available. Please connect to a database first.");
            }

            System.out.println("Fetched schema: " + schema);
            System.out.println("User Query: " + userQuery.getQuery());

            String sql = llmService.generateSQLQuery(userQuery.getQuery(), schema);
            System.out.println("Generated SQL: " + sql);
            return ResponseEntity.ok(sql);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error processing query: " + e.getMessage());
        }
    }
}
