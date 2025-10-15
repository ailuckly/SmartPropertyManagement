package com.example.propertymanagement.mapper;

import com.example.propertymanagement.dto.notification.NotificationDto;
import com.example.propertymanagement.model.Notification;

/**
 * 通知实体与 DTO 转换器
 */
public class NotificationMapper {

    /**
     * 将通知实体转换为 DTO
     */
    public static NotificationDto toDto(Notification notification) {
        if (notification == null) {
            return null;
        }

        return new NotificationDto(
            notification.getId(),
            notification.getType(),
            notification.getTitle(),
            notification.getContent(),
            notification.getRecipientId(),
            notification.getRecipientUsername(),
            notification.getIsRead(),
            notification.getRelatedEntityType(),
            notification.getRelatedEntityId(),
            notification.getCreatedAt(),
            notification.getReadAt()
        );
    }
}
