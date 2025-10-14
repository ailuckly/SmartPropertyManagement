package com.example.propertymanagement.dto.notification;

import com.example.propertymanagement.model.NotificationType;

import java.time.LocalDateTime;

/**
 * 通知数据传输对象
 */
public record NotificationDto(
    Long id,
    NotificationType type,
    String title,
    String content,
    Long recipientId,
    String recipientUsername,
    Boolean isRead,
    String relatedEntityType,
    Long relatedEntityId,
    LocalDateTime createdAt,
    LocalDateTime readAt
) {
}
