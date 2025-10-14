package com.example.propertymanagement.repository;

import com.example.propertymanagement.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 通知数据访问接口
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 根据接收者ID分页查询通知
     */
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    /**
     * 根据接收者ID和是否已读分页查询通知
     */
    Page<Notification> findByRecipientIdAndIsReadOrderByCreatedAtDesc(Long recipientId, Boolean isRead, Pageable pageable);

    /**
     * 统计用户未读通知数量
     */
    Long countByRecipientIdAndIsRead(Long recipientId, Boolean isRead);

    /**
     * 批量标记用户的所有通知为已读
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.recipient.id = :recipientId AND n.isRead = false")
    int markAllAsReadByRecipient(@Param("recipientId") Long recipientId);

    /**
     * 删除指定用户的所有已读通知
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.recipient.id = :recipientId AND n.isRead = true")
    int deleteAllReadByRecipient(@Param("recipientId") Long recipientId);

    /**
     * 根据关联实体类型和ID查询通知
     */
    Page<Notification> findByRelatedEntityTypeAndRelatedEntityId(String entityType, Long entityId, Pageable pageable);
}
