package com.example.propertymanagement.service;

import com.example.propertymanagement.dto.common.PageResponse;
import com.example.propertymanagement.dto.notification.NotificationDto;
import com.example.propertymanagement.exception.ForbiddenException;
import com.example.propertymanagement.exception.ResourceNotFoundException;
import com.example.propertymanagement.mapper.NotificationMapper;
import com.example.propertymanagement.model.Notification;
import com.example.propertymanagement.model.NotificationType;
import com.example.propertymanagement.model.User;
import com.example.propertymanagement.repository.NotificationRepository;
import com.example.propertymanagement.repository.UserRepository;
import com.example.propertymanagement.security.UserPrincipal;
import com.example.propertymanagement.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 通知服务
 * 处理通知的创建、查询、标记已读等业务逻辑
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    /**
     * 创建通知
     *
     * @param type 通知类型
     * @param title 标题
     * @param content 内容
     * @param recipientId 接收者ID
     * @param relatedEntityType 关联实体类型
     * @param relatedEntityId 关联实体ID
     * @return 创建的通知
     */
    @Transactional
    public NotificationDto createNotification(NotificationType type,
                                              String title,
                                              String content,
                                              Long recipientId,
                                              String relatedEntityType,
                                              Long relatedEntityId) {
        User recipient = userRepository.findById(recipientId)
            .orElseThrow(() -> new ResourceNotFoundException("接收者不存在"));

        Notification notification = Notification.builder()
            .type(type)
            .title(title)
            .content(content)
            .recipient(recipient)
            .isRead(false)
            .relatedEntityType(relatedEntityType)
            .relatedEntityId(relatedEntityId)
            .build();

        return NotificationMapper.toDto(notificationRepository.save(notification));
    }

    /**
     * 获取当前用户的通知列表
     *
     * @param pageable 分页参数
     * @param unreadOnly 是否只显示未读通知
     * @return 通知分页结果
     */
    @Transactional(readOnly = true)
    public PageResponse<NotificationDto> getMyNotifications(Pageable pageable, Boolean unreadOnly) {
        UserPrincipal principal = getCurrentUser();
        Page<Notification> page;

        if (Boolean.TRUE.equals(unreadOnly)) {
            page = notificationRepository.findByRecipientIdAndIsReadOrderByCreatedAtDesc(
                principal.getId(), false, pageable);
        } else {
            page = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(
                principal.getId(), pageable);
        }

        return PageResponse.from(page.map(NotificationMapper::toDto));
    }

    /**
     * 获取当前用户的未读通知数量
     *
     * @return 未读通知数量
     */
    @Transactional(readOnly = true)
    public Long getUnreadCount() {
        UserPrincipal principal = getCurrentUser();
        return notificationRepository.countByRecipientIdAndIsRead(principal.getId(), false);
    }

    /**
     * 标记通知为已读
     *
     * @param notificationId 通知ID
     * @return 更新后的通知
     */
    @Transactional
    public NotificationDto markAsRead(Long notificationId) {
        UserPrincipal principal = getCurrentUser();
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new ResourceNotFoundException("通知不存在"));

        // 确保只能标记自己的通知
        if (!notification.getRecipient().getId().equals(principal.getId())) {
            throw new ForbiddenException("无权操作此通知");
        }

        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
        }

        return NotificationMapper.toDto(notification);
    }

    /**
     * 标记所有通知为已读
     *
     * @return 更新的通知数量
     */
    @Transactional
    public int markAllAsRead() {
        UserPrincipal principal = getCurrentUser();
        return notificationRepository.markAllAsReadByRecipient(principal.getId());
    }

    /**
     * 删除通知
     *
     * @param notificationId 通知ID
     */
    @Transactional
    public void deleteNotification(Long notificationId) {
        UserPrincipal principal = getCurrentUser();
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new ResourceNotFoundException("通知不存在"));

        // 确保只能删除自己的通知
        if (!notification.getRecipient().getId().equals(principal.getId())) {
            throw new ForbiddenException("无权删除此通知");
        }

        notificationRepository.delete(notification);
    }

    /**
     * 删除所有已读通知
     *
     * @return 删除的通知数量
     */
    @Transactional
    public int deleteAllRead() {
        UserPrincipal principal = getCurrentUser();
        return notificationRepository.deleteAllReadByRecipient(principal.getId());
    }

    private UserPrincipal getCurrentUser() {
        return SecurityUtils.getCurrentUserPrincipal()
            .orElseThrow(() -> new ForbiddenException("未登录"));
    }
}
