package example.dbchatbot.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Service
public class SchemaService {

    private String schema;

    public void parseAndStoreSchema(MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new Exception("Uploaded file is empty.");
        }

        String content;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            content = reader.lines().collect(Collectors.joining("\n"));
        }

        // Simple validation to check if content contains CREATE TABLE statements
        if (!content.contains("CREATE TABLE")) {
            throw new Exception("Invalid schema file. No CREATE TABLE statements found.");
        }

        // Store the schema in memory for now
        this.schema = content;
    }

    public String getSchema() throws Exception {
        if (this.schema == null || this.schema.isEmpty()) {
            throw new Exception("Schema is not available.");
        }
        return this.schema;
    }
}
