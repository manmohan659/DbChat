package example.dbchatbot.service;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.V;
import example.dbchatbot.model.ChatMessage;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class LLMService {

    private final SqlGenerator sqlGenerator;

    public LLMService(OpenAiChatModel openAiChatModel) {
        this.sqlGenerator = AiServices.create(SqlGenerator.class, openAiChatModel);
    }

//    public String decideAndRespond(String query, String schema, List<ChatMessage> history) {
//        // Build the conversation history into the prompt
//        String conversation = history.stream()
//                .map(msg -> (msg.isUser() ? "User: " : "Assistant: ") + msg.getMessage())
//                .collect(Collectors.joining("\n"));
//
//        // Construct the prompt
//        String prompt = "Conversation History:\n" + conversation + "\n\n"
//                + "Database Schema:\n" + schema + "\n\n"
//                + "User Query: " + query;
//
//        // Use the prompt with the AI model
//        try {
//            String result = sqlGenerator.decideAndRespond(prompt);
//            return result;
//        } catch (Exception e) {
//            return "Error generating response: " + e.getMessage();
//        }
//    }

    public String decideAndRespond(String query, String schema, List<ChatMessage> history) {
        // Build the conversation history into the prompt
        String conversation = history.stream()
                .map(msg -> (msg.isUser() ? "User: " : "Assistant: ") + msg.getMessage())
                .collect(Collectors.joining("\n"));

        // Construct the prompt
        String prompt = "Conversation History:\n" + conversation + "\n\n"
                + "Database Schema:\n" + schema + "\n\n"
                + "User Query: " + query;

        // Use the prompt with the AI model
        try {
            String result = sqlGenerator.decideAndRespond(prompt);
            return result;
        } catch (Exception e) {
            return "Error generating response: " + e.getMessage();
        }
    }
    public String generateSQL(String query, String results) {
        return sqlGenerator.generateSQL(query, results);
    }



    // LLMService.java

    interface SqlGenerator {
        @SystemMessage("You are a helpful assistant that only assists with database-related queries. If the user greets you with 'Hi' or 'Hello', respond politely with a greeting like 'Hi, how can I assist you today?'. For any non-database-related queries, politely inform the user that you are specialized in database assistance.You are a helpful assistant that generates complete, executable SQL queries based on the conversation context and database schema. Ensure that all values are included directly in the SQL statements without any placeholders like '?'. Do not assume any parameters; the SQL should be ready to execute as-is.")
        @UserMessage("{{prompt}}")
        String decideAndRespond(@V("prompt") String prompt);

        @SystemMessage("You are an AI assistant that provides natural language answers based on query results.")
        @UserMessage("User Query: {{query}}\n\nQuery Results:\n{{results}}")
        String generateSQL(@V("query") String query, @V("results") String results);
    }
}
