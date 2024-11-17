package cotede.interns.project.guessify.service;

import cotede.interns.project.guessify.model.AiFactory;
import groovy.util.logging.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
@Slf4j
public class AiService {

    @Autowired
    private final AiFactory aiFactory;

    public AiService(AiFactory aiFactory) {
        this.aiFactory = aiFactory;
    }


    public String generateContent(Boolean withImage) {
        String question = aiFactory.generate(withImage);
        question = question.replaceAll("$", "");
        return question;
    }
}
