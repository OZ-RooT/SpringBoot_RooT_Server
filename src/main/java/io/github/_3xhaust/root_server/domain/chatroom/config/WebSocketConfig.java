package io.github._3xhaust.root_server.domain.chatroom.config;

import io.github._3xhaust.root_server.domain.chatroom.handler.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 일반 WebSocket 지원 (Flutter 등 네이티브 클라이언트용)
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*");

        // SockJS 지원 (웹 브라우저용)
        registry.addEndpoint("/ws/chat/sockjs")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(chatWebSocketHandler);
    }
}
