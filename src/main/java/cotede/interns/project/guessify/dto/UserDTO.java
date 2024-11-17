package cotede.interns.project.guessify.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String name;
    private Integer score;
    private String password;
    public UserDTO(Long id, String name, Integer score) {
        this.id = id;
        this.name = name;
        this.score = score;
    }
}
