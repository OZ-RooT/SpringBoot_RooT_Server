package io.github._3xhaust.root_server.domain.chatroom.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat-rooms")
@RequiredArgsConstructor
public class ChatroomController {
//    private final ChatroomService chatroomService;

    /**
     * 중고거래 게시글에서 "채팅하기" 클릭:
     * (itemId, buyerId) 기준으로 방이 있으면 반환, 없으면 생성
     */
    @PostMapping("/items/{itemId}")
    public ResponseEntity<ChatRoomCreateResponse> getOrCreate(@PathVariable Long itemId) {
        // TODO: 인증 붙이면 buyerId는 SecurityContext에서 가져오는 게 정석
        Long buyerId = 1L; // 예시
        return ResponseEntity.ok(chatroomService.getOrCreate(itemId, buyerId));
    }


    /**
     * 채팅방 상단 메타 (상품/판매자 정보 포함)
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<ChatRoomDetailResponse> getRoomDetail(@PathVariable Long roomId) {
        // TODO: assertMember(roomId, userId) 권장
        return ResponseEntity.ok(chatroomService.getRoomDetail(roomId));
    }

    /**
     * 메시지 히스토리(페이징)
     */
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(
            @PathVariable Long roomId,
            @RequestParam(required = false) Long cursorMessageId,
            @RequestParam(defaultValue = "30") int size
    ) {
        // TODO: assertMember(roomId, userId) 권장
        return ResponseEntity.ok(chatroomService.getMessages(roomId, cursorMessageId, size));
    }
}
