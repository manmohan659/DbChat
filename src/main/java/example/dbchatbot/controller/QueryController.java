package example.dbchatbot.controller;

import example.dbchatbot.service.DatabaseService;
import example.dbchatbot.service.LLMService;
import example.dbchatbot.service.SchemaService;
import example.dbchatbot.service.UserQuery;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMappin("/api/query")
public class QueryController {

    private final LLMService llmService;
    private final SchemaService schemaService;
    private final DatabaseService databaseService;

    public QueryController(LLMService llmService, SchemaService schemaService, DatabaseService databaseService) {
        this.llmService = llmService;
        this.schemaService = schemaService;
        this.databaseService = databaseService;

    }
    @PostMapping("/nl-to-sql")
    public ResponseEntity<String> query(@RequestBody UserQuery userQuery) {
        try{
            String schema = getAvailableSchema();
            String sqlQuery = llmService.generateSQLQuery(userQuery.getQuery(), schema);
            return ResponseEntity.ok(sqlQuery);
        }catch(Excpetion e){
            return ResponseEntity.badRequest().body("Error processing query: " + e.getMessage());

        }
    }
    private String getAvailableSchema() {
        try{
          return schemaService.getSchema();
        }catch(Excpetion e){
            // If schema is not available in SchemaService, check DatabaseService
            return databaseService.getSchema();
        }

    }
}
