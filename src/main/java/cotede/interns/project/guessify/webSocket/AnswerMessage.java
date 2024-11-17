package cotede.interns.project.guessify.webSocket;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AnswerMessage {
    private Long userId;
    private Long roomId;
    private Long sessionId;
    private Integer roundId;
    private Integer answerNumber;
}

