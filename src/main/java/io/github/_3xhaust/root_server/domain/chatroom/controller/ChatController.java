package io.github._3xhaust.root_server.domain.chatroom.controller;

import io.github._3xhaust.root_server.domain.chatroom.dto.ChatMessageDTO;
import io.github._3xhaust.root_server.domain.chatroom.dto.ChatRoomDTO;
import io.github._3xhaust.root_server.domain.chatroom.handler.ChatWebSocketHandler;
import io.github._3xhaust.root_server.domain.chatroom.service.ChatService;
import io.github._3xhaust.root_server.domain.product.entity.Product;
import io.github._3xhaust.root_server.domain.product.repository.ProductRepository;
import io.github._3xhaust.root_server.domain.user.entity.User;
import io.github._3xhaust.root_server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ChatWebSocketHandler chatWebSocketHandler;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/send")
    public void sendMessage(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        Principal principal = headerAccessor.getUser();
        if (principal == null) {
            throw new IllegalStateException("User not authenticated");
        }

        UserDetails userDetails;
        if (principal instanceof org.springframework.security.authentication.UsernamePasswordAuthenticationToken) {
            userDetails = (UserDetails) ((org.springframework.security.authentication.UsernamePasswordAuthenticationToken) principal).getPrincipal();
        } else if (principal instanceof UserDetails) {
            userDetails = (UserDetails) principal;
        } else {
            throw new IllegalStateException("Unexpected principal type: " + principal.getClass());
        }

        User sender = userRepository.findByName(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Long chatRoomId = Long.valueOf(payload.get("chatRoomId").toString());
        String message = payload.get("message").toString();

        log.info("Sending message: chatRoomId={}, sender={}, message={}", chatRoomId, sender.getName(), message);

        Long messageId = System.currentTimeMillis();
        java.time.Instant createdAt = java.time.Instant.now();

        ChatMessageDTO chatMessage = ChatMessageDTO.builder()
                .id(messageId)
                .chatRoomId(chatRoomId)
                .senderId(sender.getId())
                .senderName(sender.getName())
                .message(message)
                .createdAt(createdAt)
                .build();

        chatWebSocketHandler.saveMessageToRedis(chatMessage);

        messagingTemplate.convertAndSend("/topic/chat/" + chatRoomId, chatMessage);
        
        log.info("Message sent and saved to Redis: chatRoomId={}, messageId={}, senderId={}, message={}", 
                chatRoomId, messageId, sender.getId(), message);
    }

    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomDTO> createOrGetChatRoom(
            @RequestParam Long productId,
            Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User seller = userRepository.findByName(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        User buyer = product.getSeller();

        var chatRoom = chatService.getOrCreateChatRoom(productId, seller.getId(), buyer.getId());

        ChatRoomDTO response = ChatRoomDTO.builder()
                .id(chatRoom.getId())
                .productId(product.getId())
                .productTitle(product.getTitle())
                .sellerId(seller.getId())
                .sellerName(seller.getName())
                .buyerId(buyer.getId())
                .buyerName(buyer.getName())
                .createdAt(chatRoom.getCreatedAt())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomDTO>> getChatRooms(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByName(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<ChatRoomDTO> chatRooms = chatService.getChatRoomsByUserId(user.getId());
        return ResponseEntity.ok(chatRooms);
    }

    @GetMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<Page<ChatMessageDTO>> getMessages(
            @PathVariable Long chatRoomId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        List<ChatMessageDTO> messages = chatWebSocketHandler.getMessagesFromRedis(chatRoomId, page, size);
        long total = chatWebSocketHandler.getMessageCountFromRedis(chatRoomId);
        
        Page<ChatMessageDTO> messagePage = new PageImpl<>(
                messages,
                PageRequest.of(page - 1, size),
                total
        );
        
        return ResponseEntity.ok(messagePage);
    }
}
