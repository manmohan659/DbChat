package example.dbchatbot.service;

import dev.langchain4j.LLMClient;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.Value;

public class LLMService {

    private final LLMClient llmClient ;

    public LLMService(@Value("${}") String key) throws Exception{
        this.llmClient = OpenAiChatModel.builder().apiKey(key).modelName("gpt-3.5-turbo").temperature(0.0).maxTokens(500).build();
    }

    public String generateSQLQuery(String query, String schema){
        String prompt = buildPrompt(userQuery, schema);
        ChatMessage systemMessage = SystemMessage.of("You are an AI assistant that converts natural language queries into SQL queries.");
        ChatMessage userMessage = UserMessage.of(prompt);

        String response = llmClient.chat(systemMessage, userMessage).getContent();

        return response.trim();

    }
    private String buildPrompt(String query, String schema){
        return "Given the following database schema:\n\n"
                + schema + "\n\n"
                + "\n\nConvert the following natural language query into an SQL query:\n\""
                + query
                + "\"\n\nSQLQuery";
    }

}
