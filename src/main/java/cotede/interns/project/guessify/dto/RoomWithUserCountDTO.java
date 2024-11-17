package cotede.interns.project.guessify.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RoomWithUserCountDTO {
    private Long roomId;
    private Integer capacity;
    private Integer userCount;
}
