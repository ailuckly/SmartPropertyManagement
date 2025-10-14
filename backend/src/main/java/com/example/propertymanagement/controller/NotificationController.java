package com.example.propertymanagement.controller;

import com.example.propertymanagement.dto.common.PageResponse;
import com.example.propertymanagement.dto.notification.NotificationDto;
import com.example.propertymanagement.service.NotificationService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 通知管理控制器
 * 提供通知的查询、标记已读、删除等接口
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * 获取当前用户的通知列表
     *
     * @param pageable 分页参数
     * @param unreadOnly 是否只显示未读通知
     * @return 通知分页结果
     */
    @GetMapping
    public ResponseEntity<PageResponse<NotificationDto>> getMyNotifications(
            @PageableDefault Pageable pageable,
            @RequestParam(required = false, defaultValue = "false") Boolean unreadOnly) {
        return ResponseEntity.ok(notificationService.getMyNotifications(pageable, unreadOnly));
    }

    /**
     * 获取未读通知数量
     *
     * @return 未读通知数量
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        Long count = notificationService.getUnreadCount();
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * 标记单个通知为已读
     *
     * @param id 通知ID
     * @return 更新后的通知
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationDto> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    /**
     * 标记所有通知为已读
     *
     * @return 更新的通知数量
     */
    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Integer>> markAllAsRead() {
        int count = notificationService.markAllAsRead();
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * 删除单个通知
     *
     * @param id 通知ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 删除所有已读通知
     *
     * @return 删除的通知数量
     */
    @DeleteMapping("/read")
    public ResponseEntity<Map<String, Integer>> deleteAllRead() {
        int count = notificationService.deleteAllRead();
        return ResponseEntity.ok(Map.of("count", count));
    }
}
