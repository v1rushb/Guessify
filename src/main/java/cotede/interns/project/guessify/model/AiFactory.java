package cotede.interns.project.guessify.model;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AiFactory {

    private final ChatModel chatModel;
    private final ImageModel imageModel;

    @Value("${spring.ai.text-prompt}")
    private String textPrompt;

    @Value("${spring.ai.image-prompt}")
    private String imagePrompt;

    @Autowired
    public AiFactory(ChatModel aiTextModel, ImageModel aiImageModel) {
        this.chatModel = aiTextModel;
        this.imageModel = aiImageModel;
    }


    public String generate(Boolean withImage) {
        return withImage ? generateImage() : generateText();
    }


    public String generateText() {
        try {
            ChatResponse response = chatModel.call(new Prompt(textPrompt));
            String textContent = response.getResult().getOutput().getContent();
            return textContent;

        } catch (Exception e) {
            return "Error generating the question: " + e.getMessage();
        }

    }


    public String generateImage() {
        try {
            ImagePrompt prompt = new ImagePrompt(imagePrompt);
            ImageResponse response = imageModel.call(prompt);
            if (response != null && response.getResult() != null && response.getResult().getOutput() != null) {
                return response.getResult().getOutput().getUrl();
            } else {
                throw new RuntimeException("Image generation failed: No URL returned ! . ");
            }
        } catch (Exception e) {
            return "Error generating the image: " + e.getMessage();
        }
    }
}
