package cotede.interns.project.guessify.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;



@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoundResponse {
    private Integer roundId;
    private String content;
    private Integer roundNumber;
}
