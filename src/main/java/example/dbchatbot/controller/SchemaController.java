package example.dbchatbot.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import example.dbchatbot.service.SchemaService;

@RestController
@RequestMapping("/schema")
public class SchemaController {

    private final SchemaService schemaService;

    public SchemaController(SchemaService schemaService) {
        this.schemaService = schemaService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadSchema(@RequestParam("file") MultipartFile file) {
        try {
            schemaService.parseAndStoreSchema(file);
            return ResponseEntity.ok("Schema uploaded and parsed successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to upload schema: " + e.getMessage());
        }
    }
}