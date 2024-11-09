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


    interface SqlGenerator {
        @SystemMessage(
                "You are a MySQL SQL specialist responsible for generating precise and executable SQL 'SELECT' queries. " +
                        "Your task is to interpret the user’s query in context, analyze the schema, and use conversation history to generate a fully-formed 'SELECT' statement. " +
                        "Do not create any SQL commands other than 'SELECT'. Avoid INSERT, UPDATE, DELETE, DROP, or any other non-SELECT operations. " +
                        "Always use the conversation history to understand the context fully, such as prior references to specific customers, orders, or data points. " +
                        "Ensure that all values are included directly in the SQL statements without any placeholders like '?'. " +
                        "If you cannot confidently determine specific details for the query, return a complete 'SELECT' query that suggests the fields and tables, but do not use '?' placeholders. " +
                        "For simple greetings like 'Hi' or 'Hello', reply politely without generating SQL, for example, 'Hi, how can I assist you today?'. " +
                        "Provide only the SQL query in response—no additional commentary, explanations, or extra characters beyond the necessary SQL syntax. " +
                        "If the context is clear, respond with the complete 'SELECT' query. If context is lacking or incomplete, return the closest complete 'SELECT' query possible based on schema."
        )
        @UserMessage("{{prompt}}")
        String decideAndRespond(@V("prompt") String prompt);

        @SystemMessage(
                "You are a MySQL SQL assistant limited to generating 'SELECT' queries based on schema, user queries, and previous conversation context. " +
                        "Only generate 'SELECT' statements; do not generate any other type of SQL (e.g., INSERT, UPDATE, DELETE, DROP). " +
                        "Analyze each user query, schema, and conversation context to provide the most accurate 'SELECT' query. " +
                        "If context is unclear, return a 'SELECT' query with placeholders for user input, such as 'WHERE CustomerID = ?' " +
                        "Be polite for greeting-like queries, saying 'Hi, how can I assist you today?' " +
                        "Provide only the 'SELECT' SQL query in your response—no additional text or characters."
        )
        @UserMessage("User Query: {{query}}\n\nQuery Results:\n{{results}}")
        String generateSQL(@V("query") String query, @V("results") String results);
    }
}
