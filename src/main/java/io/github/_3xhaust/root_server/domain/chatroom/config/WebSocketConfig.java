package io.github._3xhaust.root_server.domain.chatroom.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    // STOMP 라우팅 규칙
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라 /app/chat.send  => @MessageMapping("/chat.send")가 받음
        registry.setApplicationDestinationPrefixes("/app");

        // 서버에서 브로드캐스트 /topic/chat-rooms/10 을 구독한 클라이언트에게 서버가 push
        registry.enableSimpleBroker("/topic", "/queue");

        // 특정 사용자 전송
        registry.setUserDestinationPrefix("/user");

    }
}
