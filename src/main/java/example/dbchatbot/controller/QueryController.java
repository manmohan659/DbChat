package example.dbchatbot.controller;

import example.dbchatbot.service.DatabaseService;
import example.dbchatbot.service.LLMService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

            String sql = llmService.decideAndRespond(userQuery.getQuery(), schema);
            String sqlQuery = extractSQLQuery(sql);
            System.out.println("Generated SQL: " + sql);
            if(!sqlQuery.isEmpty()) {
                List<Map<String, Object>> results = databaseService.executeQuery(sqlQuery.trim());

                String finalResponse = llmService.generateSQL(userQuery.getQuery(), convertResultsString(results));
                return ResponseEntity.ok(finalResponse);
            }else{
                return ResponseEntity.ok(sql);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error processing query: " + e.getMessage());
        }
    }

    private String convertResultsString(List<Map<String, Object>> results){
        StringBuilder sb = new StringBuilder();
        if(results.isEmpty()){
            sb.append("No results found.");
        }else{
            //for column headers
            Map<String, Object> firstRow = results.get(0);
            sb.append(String.join("\t", firstRow.keySet())).append('\n');
            for(Map<String, Object> row : results){
                sb.append(row.values().stream().map(value->value == null ? "NULL" : value.toString())
                        .collect(Collectors.joining("\t"))).append('\n');

            }
        }
        return sb.toString();
    }
    private boolean isQuery(String query){
        String trimmedQuery = query.trim().toUpperCase();
        return trimmedQuery.matches("^(Select)\\s.*");
    }
    private String extractSQLQuery(String llmResponse){
        Pattern sqlPattern = Pattern.compile("(?i)(SELECT\\s+.*?;)", Pattern.DOTALL);
        Matcher matcher = sqlPattern.matcher(llmResponse);

        if(matcher.find()){
            return matcher.group(1).trim();
        } else{
            throw new IllegalArgumentException("");
        }
    }

}
