package io.github._3xhaust.root_server.domain.chatroom.config;

import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SocketIoConfig {

    @Bean
    public SocketIOServer socketIOServer(
            @Value("${socketio.host:0.0.0.0}") String host,
            @Value("${socketio.port:9092}") int port,
            @Value("${socketio.origin:*}") String origin
    ) {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname(host);
        config.setPort(port);
        config.setOrigin(origin);
        return new SocketIOServer(config);
    }

    @Bean
    public SmartLifecycle socketIoLifecycle(SocketIOServer socketIOServer) {
        return new SmartLifecycle() {
            private boolean running;

            @Override
            public void start() {
                socketIOServer.start();
                running = true;
            }

            @Override
            public void stop() {
                socketIOServer.stop();
                running = false;
            }

            @Override
            public boolean isRunning() {
                return running;
            }

            @Override
            public int getPhase() {
                return Integer.MAX_VALUE;
            }
        };
    }
}
