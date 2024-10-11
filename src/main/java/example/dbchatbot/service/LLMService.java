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

    public String decideAndRespond(String query, String schema) {
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
    public String generateSQL(String query, String results) {
        return sqlGenerator.generateSQL(query, results);
    }

    interface SqlGenerator {
        @SystemMessage("You are a SQL assistant that helps users retrieve data from a database. Your only job is to generate `SELECT` queries for retrieving data. " +
                "You must only return the SQL query without any extra commentary, explanations, or apologies. " +
                "If the user's question asks for data retrieval, return the appropriate `SELECT` query based on the user's request and the provided schema. " +
                "Do not explain the query, and do not say that you cannot execute the query. Only return the SQL query as plain text. If your response has select query just reply with one select query which you find fits the most as per user query do not use words like here is select query")
        @UserMessage("Database Schema:\n\n{{schema}}\n\nUser Query: {{query}}")
        String decideAndRespond(@V("schema")String schema, @V("query")String query);

        @SystemMessage("You are an AI assistant that provides natural language answers based on query results.")
        @UserMessage("User Query: {{query}}\n\nQuery Results:\n{{results}}")
        String generateSQL(@V("query") String query, @V("results") String results);
    }

}