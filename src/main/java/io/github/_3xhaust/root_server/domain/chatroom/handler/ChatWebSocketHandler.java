package io.github._3xhaust.root_server.domain.chatroom.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github._3xhaust.root_server.domain.chatroom.dto.ChatMessageDTO;
import io.github._3xhaust.root_server.domain.chatroom.service.ChatService;
import io.github._3xhaust.root_server.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final ChatService chatService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String PENDING_MESSAGES_PREFIX = "chat:pending:";
    private static final String ACTIVE_SESSIONS_PREFIX = "chat:active:";
    private static final String CHAT_MESSAGES_PREFIX = "chat:messages:";
    private static final String LAST_READ_MESSAGE_PREFIX = "chat:lastRead:";
    private static final Duration MESSAGE_TTL = Duration.ofHours(24);
    private static final Duration SESSION_TTL = Duration.ofHours(1);
    private static final Duration LAST_READ_TTL = Duration.ofDays(30);

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (command == StompCommand.CONNECT) {
            String token = getTokenFromHeader(accessor);
            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                String name = jwtTokenProvider.getNameFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(name);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                accessor.setUser(authentication);
                
                String sessionId = accessor.getSessionId();
                if (sessionId != null) {
                    String key = ACTIVE_SESSIONS_PREFIX + sessionId;
                    redisTemplate.opsForValue().set(key, name, SESSION_TTL);
                }
            }
        } else if (command == StompCommand.DISCONNECT) {
            Principal principal = accessor.getUser();
            if (principal != null) {
                String sessionId = accessor.getSessionId();
                if (sessionId != null) {
                    try {
                        deletePendingMessages(sessionId);
                        deleteActiveSession(sessionId);
                    } catch (Exception e) {
                        log.error("Failed to cleanup on disconnect", e);
                    }
                }
            }
        }

        return message;
    }

    private String getTokenFromHeader(StompHeaderAccessor accessor) {
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String bearerToken = authHeaders.get(0);
            if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7);
            }
        }
        return null;
    }

    public void addPendingMessage(String sessionId, ChatMessageDTO message) {
        try {
            String key = PENDING_MESSAGES_PREFIX + sessionId;
            String messageJson = objectMapper.writeValueAsString(message);
            redisTemplate.opsForList().rightPush(key, messageJson);
            redisTemplate.expire(key, MESSAGE_TTL);
        } catch (Exception e) {
            log.error("Failed to add pending message to Redis", e);
        }
    }

    private List<ChatMessageDTO> getPendingMessages(String sessionId) {
        try {
            String key = PENDING_MESSAGES_PREFIX + sessionId;
            List<String> messageJsonList = redisTemplate.opsForList().range(key, 0, -1);
            if (messageJsonList == null || messageJsonList.isEmpty()) {
                return new ArrayList<>();
            }

            List<ChatMessageDTO> messages = new ArrayList<>();
            for (String messageJson : messageJsonList) {
                ChatMessageDTO message = objectMapper.readValue(messageJson, ChatMessageDTO.class);
                messages.add(message);
            }
            return messages;
        } catch (Exception e) {
            log.error("Failed to get pending messages from Redis", e);
            return new ArrayList<>();
        }
    }

    private void deletePendingMessages(String sessionId) {
        try {
            String key = PENDING_MESSAGES_PREFIX + sessionId;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Failed to delete pending messages from Redis", e);
        }
    }

    private void deleteActiveSession(String sessionId) {
        try {
            String key = ACTIVE_SESSIONS_PREFIX + sessionId;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Failed to delete active session from Redis", e);
        }
    }

    public void saveMessageToRedis(ChatMessageDTO message) {
        try {
            String key = CHAT_MESSAGES_PREFIX + message.getChatRoomId();
            String messageJson = objectMapper.writeValueAsString(message);
            redisTemplate.opsForList().rightPush(key, messageJson);
            redisTemplate.expire(key, MESSAGE_TTL);
            log.debug("Message saved to Redis: chatRoomId={}, messageId={}", message.getChatRoomId(), message.getId());
        } catch (Exception e) {
            log.error("Failed to save message to Redis", e);
        }
    }

    public List<ChatMessageDTO> getMessagesFromRedis(Long chatRoomId, int page, int size) {
        try {
            String key = CHAT_MESSAGES_PREFIX + chatRoomId;
            List<String> messageJsonList = redisTemplate.opsForList().range(key, 0, -1);
            if (messageJsonList == null || messageJsonList.isEmpty()) {
                return new ArrayList<>();
            }

            List<ChatMessageDTO> messages = new ArrayList<>();
            for (String messageJson : messageJsonList) {
                ChatMessageDTO message = objectMapper.readValue(messageJson, ChatMessageDTO.class);
                messages.add(message);
            }

            // 최신 메시지가 뒤에 있으므로 역순으로 정렬
            messages.sort((a, b) -> {
                if (a.getCreatedAt() == null || b.getCreatedAt() == null) {
                    return 0;
                }
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            });

            int start = (page - 1) * size;
            int end = Math.min(start + size, messages.size());
            if (start >= messages.size()) {
                return new ArrayList<>();
            }
            return messages.subList(start, end);
        } catch (Exception e) {
            log.error("Failed to get messages from Redis", e);
            return new ArrayList<>();
        }
    }

    public long getMessageCountFromRedis(Long chatRoomId) {
        try {
            String key = CHAT_MESSAGES_PREFIX + chatRoomId;
            Long count = redisTemplate.opsForList().size(key);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("Failed to get message count from Redis", e);
            return 0;
        }
    }

    public void markAsRead(Long chatRoomId, Long userId, Long messageId) {
        try {
            String key = LAST_READ_MESSAGE_PREFIX + chatRoomId + ":" + userId;
            redisTemplate.opsForValue().set(key, String.valueOf(messageId), LAST_READ_TTL);
            log.debug("Marked message as read: chatRoomId={}, userId={}, messageId={}", chatRoomId, userId, messageId);
        } catch (Exception e) {
            log.error("Failed to mark message as read", e);
        }
    }

    public Long getLastReadMessageId(Long chatRoomId, Long userId) {
        try {
            String key = LAST_READ_MESSAGE_PREFIX + chatRoomId + ":" + userId;
            String messageIdStr = redisTemplate.opsForValue().get(key);
            return messageIdStr != null ? Long.parseLong(messageIdStr) : null;
        } catch (Exception e) {
            log.error("Failed to get last read message ID", e);
            return null;
        }
    }

    public long getUnreadCount(Long chatRoomId, Long userId) {
        try {
            String key = CHAT_MESSAGES_PREFIX + chatRoomId;
            List<String> messageJsonList = redisTemplate.opsForList().range(key, 0, -1);
            if (messageJsonList == null || messageJsonList.isEmpty()) {
                return 0;
            }

            Long lastReadMessageId = getLastReadMessageId(chatRoomId, userId);
            if (lastReadMessageId == null) {
                return messageJsonList.size();
            }

            long unreadCount = 0;
            for (String messageJson : messageJsonList) {
                try {
                    ChatMessageDTO message = objectMapper.readValue(messageJson, ChatMessageDTO.class);
                    if (message.getSenderId() != userId && 
                        (message.getId() == null || message.getId() > lastReadMessageId)) {
                        unreadCount++;
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse message JSON", e);
                }
            }

            return unreadCount;
        } catch (Exception e) {
            log.error("Failed to get unread count", e);
            return 0;
        }
    }
}
