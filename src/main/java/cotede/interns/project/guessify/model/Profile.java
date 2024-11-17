package cotede.interns.project.guessify.model;

import lombok.*;
import jakarta.persistence.Embeddable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
@Data
public class Profile {
    private String bio;
    private Integer  age ;
}
