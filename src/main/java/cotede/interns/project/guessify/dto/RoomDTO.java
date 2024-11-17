package cotede.interns.project.guessify.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@AllArgsConstructor

@Getter
@Setter
public class RoomDTO {
    private Long id;
    private int capacity;
    private List<UserDTO> users;
}