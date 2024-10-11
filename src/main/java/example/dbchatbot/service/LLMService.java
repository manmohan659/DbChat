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
            String result = sqlGenerator.decideAndRespond(schema, query);
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
        @SystemMessage("\"You are a SQL assistant that helps users retrieve data from a database. You should only generate `SELECT` queries when asked to retrieve data, such as table rows or specific columns. \" +\n" +
                "                   \"If the user query involves anything that requires modifying the database, such as `UPDATE`, `DELETE`, `DROP`, or `INSERT`, you should not generate a query. \" +\n" +
                "                   \"Only generate `SELECT` queries and avoid any other SQL commands. \" +\n" +
                "                   \"This is very important : Use reasoning to determine if the user's question can be answered by looking at the schema or requires executing a `SELECT` query. \" +\n" +
                "                    \"If user requests anything else which is not related to database. Please reply with I am database assistant, Need help with queries lets chat. Baically do not entertain anything else. Users may type greeting messages like Hi, hello greet and reply them as a database assistant \" +\n" +
                "                   \"If a `SELECT` query is needed, generate only that, no additional commentary.\"")
        @UserMessage("Database Schema:\n\n{{schema}}\n\nUser Query: {{query}}")
        String decideAndRespond(@V("schema")String schema, @V("query")String query);

        @SystemMessage("You are an AI assistant that provides natural language answers based on query results.")
        @UserMessage("User Query: {{query}}\n\nQuery Results:\n{{results}}")
        String generateSQL(@V("query") String query, @V("results") String results);
    }

}