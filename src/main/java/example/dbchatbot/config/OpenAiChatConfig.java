package example.dbchatbot.config;

import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiChatConfig {

    @Value("${langchain4j.openai.api-key}")
    private String apiKey;

    @Bean
    public OpenAiChatModel openAiChatModel() {
        System.out.println("The API key is  : " +apiKey);
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gpt-4-0613")
                .build();
    }
}