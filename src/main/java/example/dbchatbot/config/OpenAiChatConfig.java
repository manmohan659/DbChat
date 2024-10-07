package example.dbchatbot.config;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain.model.chat.ChatOpenAI;
import dev.langchain.model.openai.OpenAILanguageModel;

public class OpenAiChatConfig {
    static class Simple_prompt{
        public static void main(String[] args) {

            ChatLanguageModel model = OpenAiChatModel.builder().apiKey("sk-proj-9543454c34543543543543543543543543543543543543543543543543543543").modelName("gpt-3.5-turbo").build();
            String response = model.generate("What is the capital of the moon?");
            System.out.println(response);
        }
    }
}
