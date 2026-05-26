package io.github._3xhaust.root_server.domain.notification.dto.res;

public record NotificationResponse(
        Long id,
        String type,
        String actor,
        String action,
        String target,
        String timeAgo,
        Boolean unread
) {
}
