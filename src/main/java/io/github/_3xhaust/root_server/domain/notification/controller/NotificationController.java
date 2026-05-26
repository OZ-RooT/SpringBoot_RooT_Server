package io.github._3xhaust.root_server.domain.notification.controller;

import io.github._3xhaust.root_server.domain.notification.dto.res.NotificationResponse;
import io.github._3xhaust.root_server.global.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    @GetMapping
    public ApiResponse<List<NotificationResponse>> getNotifications() {
        return ApiResponse.ok(List.of(
                new NotificationResponse(1L, "trade", "kevin_k", "liked your product", "Yoga Class!!!", "Now", true),
                new NotificationResponse(2L, "trade", "Jay", "liked your product", "Wired Earphones", "Now", true),
                new NotificationResponse(3L, "garage", "Mike", "liked your event", "BIG SALE OPEN !!!", "Now", true),
                new NotificationResponse(4L, "garage", "LuN1ia", "liked your event", "My first Sale!", "2 min ago", false),
                new NotificationResponse(5L, "trade", "Blue Day", "liked your product", "Fresh Apple", "6 min ago", false),
                new NotificationResponse(6L, "trade", "Mia", "liked your product", "Wrist watch", "12 min ago", false),
                new NotificationResponse(7L, "garage", "Mr. Nuget", "liked your event", "This weeeeeek", "33 min ago", false),
                new NotificationResponse(8L, "trade", "Emily Wilson", "liked your product", "Suit Case", "46 min ago", false),
                new NotificationResponse(9L, "community", "11_ia", "liked your post", "I took a walk wi ...", "1h ago", false),
                new NotificationResponse(10L, "community_comment", "COLL", "replied to your post", "FOR REAL", "2h ago", false),
                new NotificationResponse(11L, "community_comment", "Ben", "replied to your post", "Wowwww", "2h ago", false),
                new NotificationResponse(12L, "community", "moRi", "liked your post", "Have you guys e ...", "6h ago", false)
        ));
    }
}
