package cotede.interns.project.guessify.webSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.security.access.AccessDeniedException;

@Controller
public class WebSocketExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandshakeInterceptor.class);

    @MessageExceptionHandler(AccessDeniedException.class)
    @SendToUser("/queue/errors")
    public String handleAccessDeniedException(AccessDeniedException e) {
        return e.getMessage();
    }
}
