package cotede.interns.project.guessify.webSocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
// usage.
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketSecurityConfig  {

//    @Override
//    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
//        messages
//                .nullDestMatcher().permitAll()
//                .simpSubscribeDestMatchers("/topic/**", "/queue/**").permitAll()
//                .simpDestMatchers("/app/public/**").permitAll()
//                .simpDestMatchers("/app/**").authenticated()
//                .anyMessage().denyAll();
//    }
//
//
//    @Override
//    protected boolean sameOriginDisabled() {
//        return true;
//    }
}
//

