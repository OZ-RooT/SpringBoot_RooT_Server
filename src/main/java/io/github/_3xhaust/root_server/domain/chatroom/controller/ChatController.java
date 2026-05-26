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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ChatWebSocketHandler chatWebSocketHandler;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomDTO> createOrGetChatRoom(
            @RequestParam Long productId,
            Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User buyer = userRepository.findByName(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        User seller = product.getSeller();

        if (seller.getId().equals(buyer.getId())) {
            throw new IllegalArgumentException("Cannot create chat room with yourself");
        }

        var chatRoom = chatService.getOrCreateChatRoom(productId, seller.getId(), buyer.getId());

        ChatRoomDTO response = ChatRoomDTO.builder()
                .id(chatRoom.getId())
                .productId(product.getId())
                .productTitle(product.getTitle())
                .productPrice(product.getPrice())
                .sellerId(seller.getId())
                .sellerName(seller.getName())
                .buyerId(buyer.getId())
                .buyerName(buyer.getName())
                .createdAt(chatRoom.getCreatedAt())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomDTO>> getChatRooms(
            @RequestParam(required = false, defaultValue = "all") String filter,
            Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByName(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<ChatRoomDTO> chatRooms = chatService.getChatRoomsByUserId(user.getId());
        
        List<ChatRoomDTO> chatRoomsWithUnread = chatRooms.stream()
                .map(chatRoom -> {
                    Long lastReadMessageId = chatWebSocketHandler.getLastReadMessageId(chatRoom.getId(), user.getId());
                    long unreadCount = chatService.getUnreadCount(chatRoom.getId(), user.getId(), lastReadMessageId);
                    return ChatRoomDTO.builder()
                            .id(chatRoom.getId())
                            .productId(chatRoom.getProductId())
                            .productTitle(chatRoom.getProductTitle())
                            .productPrice(chatRoom.getProductPrice())
                            .sellerId(chatRoom.getSellerId())
                            .sellerName(chatRoom.getSellerName())
                            .buyerId(chatRoom.getBuyerId())
                            .buyerName(chatRoom.getBuyerName())
                            .createdAt(chatRoom.getCreatedAt())
                            .lastMessage(chatRoom.getLastMessage())
                            .lastMessageTime(chatRoom.getLastMessageTime())
                            .unreadCount(unreadCount)
                            .build();
                })
                .filter(chatRoom -> {
                    try {
                        io.github._3xhaust.root_server.domain.chatroom.enums.ChatFilter chatFilter = 
                            io.github._3xhaust.root_server.domain.chatroom.enums.ChatFilter.valueOf(filter.toLowerCase());
                        
                        switch (chatFilter) {
                            case all:
                                return true;
                            case unread:
                                return chatRoom.getUnreadCount() != null && chatRoom.getUnreadCount() > 0;
                            case buying:
                                return chatRoom.getBuyerId().equals(user.getId());
                            case selling:
                                return chatRoom.getSellerId().equals(user.getId());
                            case reserved:
                                Product product = productRepository.findById(chatRoom.getProductId()).orElse(null);
                                return product != null && isReserved(product);
                            case completed:
                                Product product2 = productRepository.findById(chatRoom.getProductId()).orElse(null);
                                return product2 != null && isCompleted(product2);
                            default:
                                return true;
                        }
                    } catch (IllegalArgumentException e) {
                        return true;
                    }
                })
                .collect(java.util.stream.Collectors.toList());
        
        return ResponseEntity.ok(chatRoomsWithUnread);
    }

    private boolean isReserved(Product product) {
        return false;
    }

    private boolean isCompleted(Product product) {
        return false;
    }

    @GetMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<Page<ChatMessageDTO>> getMessages(
            @PathVariable Long chatRoomId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByName(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!chatService.isParticipant(chatRoomId, user.getId())) {
            throw new IllegalArgumentException("ChatRoom access denied: " + chatRoomId);
        }

        Page<ChatMessageDTO> messagePage = chatService.getMessages(chatRoomId, page, size);

        if (!messagePage.isEmpty()) {
            Long lastMessageId = messagePage.getContent().get(0).getId();
            if (lastMessageId != null) {
                chatWebSocketHandler.markAsRead(chatRoomId, user.getId(), lastMessageId);
            }
        }

        return ResponseEntity.ok(messagePage);
    }

    @PostMapping("/rooms/{chatRoomId}/read")
    public ResponseEntity<Void> markChatRoomAsRead(
            @PathVariable Long chatRoomId,
            Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByName(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!chatService.isParticipant(chatRoomId, user.getId())) {
            throw new IllegalArgumentException("ChatRoom access denied: " + chatRoomId);
        }

        Long lastMessageId = chatService.getLatestMessageId(chatRoomId);
        if (lastMessageId != null) {
            chatWebSocketHandler.markAsRead(chatRoomId, user.getId(), lastMessageId);
        }
        
        return ResponseEntity.ok().build();
    }
}
