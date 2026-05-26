package io.github._3xhaust.root_server.domain.chatroom.handler;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import io.github._3xhaust.root_server.domain.chatroom.dto.ChatJoinRequest;
import io.github._3xhaust.root_server.domain.chatroom.dto.ChatMessageDTO;
import io.github._3xhaust.root_server.domain.chatroom.dto.ChatSendRequest;
import io.github._3xhaust.root_server.domain.chatroom.service.ChatService;
import io.github._3xhaust.root_server.domain.user.entity.User;
import io.github._3xhaust.root_server.domain.user.repository.UserRepository;
import io.github._3xhaust.root_server.global.security.jwt.JwtTokenProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatSocketIoHandler {

    private static final String USER_ID_KEY = "userId";
    private static final String USER_NAME_KEY = "userName";
    private static final String EVENT_CONNECTED = "chat:connected";
    private static final String EVENT_JOIN = "chat:join";
    private static final String EVENT_LEAVE = "chat:leave";
    private static final String EVENT_SEND = "chat:send";
    private static final String EVENT_MESSAGE = "chat:message";
    private static final String EVENT_ERROR = "chat:error";

    private final SocketIOServer socketIOServer;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final ChatService chatService;
    private final ChatWebSocketHandler chatWebSocketHandler;

    @PostConstruct
    public void registerListeners() {
        socketIOServer.addConnectListener(this::connect);
        socketIOServer.addDisconnectListener(this::disconnect);
        socketIOServer.addEventListener(EVENT_JOIN, ChatJoinRequest.class, this::joinRoom);
        socketIOServer.addEventListener(EVENT_LEAVE, ChatJoinRequest.class, this::leaveRoom);
        socketIOServer.addEventListener(EVENT_SEND, ChatSendRequest.class, this::sendMessage);

        socketIOServer.addEventListener("joinRoom", ChatJoinRequest.class, this::joinRoom);
        socketIOServer.addEventListener("leaveRoom", ChatJoinRequest.class, this::leaveRoom);
        socketIOServer.addEventListener("sendMessage", ChatSendRequest.class, this::sendMessage);
    }

    private void connect(SocketIOClient client) {
        String token = extractToken(client.getHandshakeData());
        if (!StringUtils.hasText(token) || !jwtTokenProvider.validateToken(token)) {
            sendError(client, "UNAUTHORIZED", "인증이 필요합니다.");
            client.disconnect();
            return;
        }

        String userName = jwtTokenProvider.getNameFromToken(token);
        User user = userRepository.findByName(userName)
                .orElse(null);
        if (user == null) {
            sendError(client, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다.");
            client.disconnect();
            return;
        }

        client.set(USER_ID_KEY, user.getId());
        client.set(USER_NAME_KEY, user.getName());
        client.sendEvent(EVENT_CONNECTED, Map.of(
                "userId", user.getId(),
                "userName", user.getName()
        ));
        log.info("Socket.IO chat connected: sessionId={}, userId={}", client.getSessionId(), user.getId());
    }

    private void disconnect(SocketIOClient client) {
        Long userId = client.get(USER_ID_KEY);
        log.info("Socket.IO chat disconnected: sessionId={}, userId={}", client.getSessionId(), userId);
    }

    private void joinRoom(SocketIOClient client, ChatJoinRequest request, AckRequest ackRequest) {
        try {
            Long userId = currentUserId(client);
            Long chatRoomId = requireChatRoomId(request);
            if (!chatService.isParticipant(chatRoomId, userId)) {
                sendError(client, "FORBIDDEN", "채팅방 접근 권한이 없습니다.");
                return;
            }

            client.joinRoom(roomName(chatRoomId));
            client.sendEvent("chat:joined", Map.of("chatRoomId", chatRoomId));
            if (ackRequest.isAckRequested()) {
                ackRequest.sendAckData(Map.of("success", true, "chatRoomId", chatRoomId));
            }
        } catch (Exception e) {
            log.warn("Failed to join chat room", e);
            sendError(client, "JOIN_FAILED", e.getMessage());
        }
    }

    private void leaveRoom(SocketIOClient client, ChatJoinRequest request, AckRequest ackRequest) {
        try {
            Long chatRoomId = requireChatRoomId(request);
            client.leaveRoom(roomName(chatRoomId));
            client.sendEvent("chat:left", Map.of("chatRoomId", chatRoomId));
            if (ackRequest.isAckRequested()) {
                ackRequest.sendAckData(Map.of("success", true, "chatRoomId", chatRoomId));
            }
        } catch (Exception e) {
            log.warn("Failed to leave chat room", e);
            sendError(client, "LEAVE_FAILED", e.getMessage());
        }
    }

    private void sendMessage(SocketIOClient client, ChatSendRequest request, AckRequest ackRequest) {
        try {
            Long senderId = currentUserId(client);
            Long chatRoomId = requireChatRoomId(request);
            String message = request.getMessage() == null ? "" : request.getMessage().trim();
            if (!StringUtils.hasText(message)) {
                sendError(client, "EMPTY_MESSAGE", "메시지를 입력해주세요.");
                return;
            }

            if (!chatService.isParticipant(chatRoomId, senderId)) {
                sendError(client, "FORBIDDEN", "채팅방 접근 권한이 없습니다.");
                return;
            }

            ChatMessageDTO chatMessage = chatService.saveMessageDto(chatRoomId, senderId, message);
            chatWebSocketHandler.saveMessageToRedis(chatMessage);

            String roomName = roomName(chatRoomId);
            client.joinRoom(roomName);
            socketIOServer.getRoomOperations(roomName).sendEvent(EVENT_MESSAGE, chatMessage);
            if (ackRequest.isAckRequested()) {
                ackRequest.sendAckData(chatMessage);
            }
        } catch (Exception e) {
            log.warn("Failed to send chat message", e);
            sendError(client, "SEND_FAILED", e.getMessage());
        }
    }

    private Long currentUserId(SocketIOClient client) {
        Long userId = client.get(USER_ID_KEY);
        if (userId == null) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }
        return userId;
    }

    private Long requireChatRoomId(ChatJoinRequest request) {
        if (request == null || request.getChatRoomId() == null) {
            throw new IllegalArgumentException("chatRoomId가 필요합니다.");
        }
        return request.getChatRoomId();
    }

    private Long requireChatRoomId(ChatSendRequest request) {
        if (request == null || request.getChatRoomId() == null) {
            throw new IllegalArgumentException("chatRoomId가 필요합니다.");
        }
        return request.getChatRoomId();
    }

    private String roomName(Long chatRoomId) {
        return "chat:" + chatRoomId;
    }

    private void sendError(SocketIOClient client, String code, String message) {
        client.sendEvent(EVENT_ERROR, Map.of(
                "code", code,
                "message", message == null ? "채팅 처리 중 오류가 발생했습니다." : message
        ));
    }

    private String extractToken(HandshakeData handshakeData) {
        String token = handshakeData.getSingleUrlParam("token");
        if (!StringUtils.hasText(token)) {
            token = handshakeData.getHttpHeaders().get("Authorization");
        }
        if (!StringUtils.hasText(token)) {
            return null;
        }
        return token.startsWith("Bearer ") ? token.substring(7) : token;
    }
}
