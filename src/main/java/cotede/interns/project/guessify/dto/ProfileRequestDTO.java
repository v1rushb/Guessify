package cotede.interns.project.guessify.dto;
import jakarta.validation.constraints.*;
import lombok.*;



@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProfileRequestDTO {
    @Size(max = 250, message = "Bio must be less than 250 characters ! ")
    private String bio;
    @Min(value = 0, message = "Age must be a positive number ! ")
    @Max(value = 150, message = "Age must be less than 150 ! ")
    private Integer  age;
}