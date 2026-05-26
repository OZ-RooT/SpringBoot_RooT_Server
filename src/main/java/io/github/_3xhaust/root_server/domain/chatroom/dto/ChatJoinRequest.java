package io.github._3xhaust.root_server.domain.chatroom.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatJoinRequest {
    private Long chatRoomId;
}
