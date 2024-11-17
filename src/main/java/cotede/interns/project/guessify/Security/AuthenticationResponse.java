package cotede.interns.project.guessify.Security;

import lombok.Data;
import lombok.AllArgsConstructor;


@Data
@AllArgsConstructor
public class AuthenticationResponse {
    private String jwt;
}
