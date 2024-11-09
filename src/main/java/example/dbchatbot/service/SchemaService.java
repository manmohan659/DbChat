package example.dbchatbot.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Service
public class SchemaService {

    private final SessionService sessionService;

    @Autowired
    public SchemaService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    public void parseAndStoreSchema(MultipartFile file, String sessionId) throws Exception {
        if (file.isEmpty()) {
            throw new Exception("Uploaded file is empty.");
        }

        String content;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            content = reader.lines().collect(Collectors.joining("\n"));
        }

        // Simple validation to check if content contains CREATE TABLE statements
//        if (!content.contains("CREATE TABLE")) {
//            throw new Exception("Invalid schema file. No CREATE TABLE statements found.");
//        }

        // Store the schema in Redis via SessionService
        sessionService.saveSchema(sessionId, content);
    }

    public String getSchema(String sessionId) throws Exception {
        String schema = sessionService.getSchema(sessionId);
        if (schema == null || schema.isEmpty()) {
            throw new Exception("Schema is not available for session: " + sessionId);
        }
        return schema;
    }
}