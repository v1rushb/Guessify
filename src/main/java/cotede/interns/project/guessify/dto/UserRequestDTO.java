package cotede.interns.project.guessify.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
@Getter
@Setter
@AllArgsConstructor
public class UserRequestDTO {
    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-={}:;\"'<>?,./]).*$",
            message = "Password must contain at least one uppercase letter and one special character ! ")
    private String password;

    @Pattern(regexp = "ADMIN|USER", message = "Role must be either ADMIN or USER ! ")
    private String role;
    @Min(value = 0, message = "Score must be 0 or higher")
    private Long score;
    @Valid
    private ProfileRequestDTO profile;

    public UserRequestDTO() {
        this.role = "USER";
    }

}