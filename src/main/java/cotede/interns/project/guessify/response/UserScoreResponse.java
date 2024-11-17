package cotede.interns.project.guessify.response;

import lombok.Data;
import lombok.AllArgsConstructor;


@Data
@AllArgsConstructor
public class UserScoreResponse {
    private Long userId;
    private String name;
    private Integer score;
}
