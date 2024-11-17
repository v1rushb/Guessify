package cotede.interns.project.guessify.Security.token;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;


@Service
public class TokenService {

    private Set<String> invalidatedTokens = ConcurrentHashMap.newKeySet();

    public void invalidateToken(String token) {
        invalidatedTokens.add(token);
    }

    public boolean isTokenInvalidated(String token) {
        return invalidatedTokens.contains(token);
    }
}
