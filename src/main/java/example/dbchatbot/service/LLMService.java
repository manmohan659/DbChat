package example.dbchatbot.service;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.V;
import org.springframework.stereotype.Service;

@Service
public class LLMService {

    private final SqlGenerator sqlGenerator;

    public LLMService(OpenAiChatModel openAiChatModel) {
        this.sqlGenerator = AiServices.create(SqlGenerator.class, openAiChatModel);
    }

    public String generateSQLQuery(String query, String schema) {
        System.out.println("Generating SQL query for: " + query);
        System.out.println("Using schema: " + schema + "\n-----------------------------------");
        
        try {
            String result = sqlGenerator.generateSQL(schema, query);
            System.out.println("Generated SQL: " + result);
            return result;
        } catch (Exception e) {
            System.err.println("Error generating SQL: " + e.getMessage());
            e.printStackTrace();
            return "Error generating SQL: " + e.getMessage();
        }
    }

    interface SqlGenerator {
        @SystemMessage("You are an AI assistant that converts natural language queries into SQL queries. " +
                       "Analyze the given database schema thoroughly and generate an appropriate SQL query based on the user's question. " +
                       "Be smart with your answer if you feel like user query can be answered without SQL query, in the response mention the answer preciesly, if not than only return the SQL query, without any additional explanation. ")
        @UserMessage("Database Schema:\n\n{{schema}}\n\nUser Query: {{query}}")
        String generateSQL(@V("schema") String schema, @V("query") String query);
    }
}