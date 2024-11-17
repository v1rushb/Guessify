package cotede.interns.project.guessify.service;


import cotede.interns.project.guessify.model.Answer;
import cotede.interns.project.guessify.model.GameSession;
import cotede.interns.project.guessify.model.Round;
import cotede.interns.project.guessify.repository.GameSessionRepository;
import cotede.interns.project.guessify.repository.RoundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;


@Service
public class RoundService {
    @Autowired
    private RoundRepository roundRepository;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private AiService aiService;


    public Round startRound(GameSession gameSession) {
        String content = aiService.generateContent(false);
        Round round = new Round();
        String correctAnswer = getCorrectAnswer(content);

        List<String> hints = extractHints(content);
        content = removeHintsFromContent(content);

        if (!hints.isEmpty()) {
            round.setHints(hints);
        }

        if (!correctAnswer.isEmpty()) {
            String[] temp = correctAnswer.split(" ", 2);
            String answerNumberStr = temp[0].replace("$", "").replace(".", "").trim();
            Integer answerNumber = Integer.parseInt(answerNumberStr);
            correctAnswer = temp[1].trim();

            content = content.replace("$", "");
            round.setContent(content);
            round.setCorrectAnswer(correctAnswer);
            round.setCorrectAnswerNumber(answerNumber);
        }

        round.setGameSession(gameSession);
        roundRepository.save(round);

        gameSession.getRounds().add(round);
        gameSessionRepository.save(gameSession);

        return round;
    }

    private List<String> extractHints(String content) {
        List<String> hints = new ArrayList<>();
        for (String line : content.split("\n")) {
            if (line.startsWith("Hint")) {
                hints.add(line.trim());
            }
        }
        return hints;
    }

    private String removeHintsFromContent(String content) {
        StringBuilder cleanedContent = new StringBuilder();
        for (String line : content.split("\n")) {
            if (!line.startsWith("Hint")) {
                cleanedContent.append(line).append("\n");
            }
        }
        return cleanedContent.toString().trim();
    }
    private String getCorrectAnswer(String content) {
        for (String line : content.split("\n")) {
            if (line.contains("$")) {
                return line.trim();
            }
        }
        return "";
    }

    public void addPlayerAnswer(Round round, Answer answer) {
        round.getAnswers().add(answer);
        answer.setRound(round);
        roundRepository.save(round);

    }

}
