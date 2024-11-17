package cotede.interns.project.guessify.config;


import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiImageApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class AiConfiguration {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Bean
    public OpenAiApi openAiApi() {
        return new OpenAiApi(apiKey);
    }

    @Bean
    public OpenAiChatOptions chatOptions() {
        return OpenAiChatOptions.builder()
                .withModel("gpt-4")
                .withTemperature(0.7F)
                .withMaxTokens(300)
                .build();
    }
//
    @Bean
    public OpenAiImageApi openAiImageApi() {
        return new OpenAiImageApi(apiKey);
    }

    @Bean
    public OpenAiImageOptions imageOptions() {
        return OpenAiImageOptions.builder()
                .withQuality("medium")
                .withHeight(512)
                .withWidth(512)
                .withN(1)
                .build();
    }

    @Bean
    public ChatModel aiTextModel(OpenAiApi openAiApi, OpenAiChatOptions chatOptions) {
        return new OpenAiChatModel(openAiApi, chatOptions);
    }

    @Bean
    public ImageModel aiImageModel(OpenAiImageApi openAiImageApi, OpenAiImageOptions imageOptions) {
        return new OpenAiImageModel(openAiImageApi, imageOptions, RetryTemplate.defaultInstance());
    }
}


