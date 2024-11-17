package cotede.interns.project.guessify.dto;

import jakarta.validation.Valid;
import lombok.*;

//DTO

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileUpdateDTO {
    @Valid
    private ProfileRequestDTO profile;
}
