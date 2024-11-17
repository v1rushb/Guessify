package cotede.interns.project.guessify;

import org.springframework.boot.SpringApplication;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// root

@SpringBootApplication(scanBasePackages = "cotede.interns.project.guessify")
@EnableWebSocketMessageBroker
public class GuessifyApplication {
    public static void main(String[] args) {
        SpringApplication.run(GuessifyApplication.class, args);
    }
}