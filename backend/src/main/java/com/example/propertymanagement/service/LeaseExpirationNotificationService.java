package com.example.propertymanagement.service;

import com.example.propertymanagement.model.Lease;
import com.example.propertymanagement.model.LeaseStatus;
import com.example.propertymanagement.model.NotificationType;
import com.example.propertymanagement.repository.LeaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 租约到期通知定时任务服务
 * 每天检查即将到期的租约并发送通知
 */
@Service
public class LeaseExpirationNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(LeaseExpirationNotificationService.class);

    private final LeaseRepository leaseRepository;
    private final NotificationService notificationService;

    public LeaseExpirationNotificationService(LeaseRepository leaseRepository,
                                              NotificationService notificationService) {
        this.leaseRepository = leaseRepository;
        this.notificationService = notificationService;
    }

    /**
     * 每天凌晨2点检查租约到期情况
     * 提前30天和7天发送提醒
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void checkLeaseExpiration() {
        logger.info("开始检查租约到期情况");

        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysLater = today.plusDays(30);
        LocalDate sevenDaysLater = today.plusDays(7);

        // 检查30天后到期的租约
        checkAndNotify(today, thirtyDaysLater, 30);

        // 检查7天后到期的租约
        checkAndNotify(today, sevenDaysLater, 7);

        logger.info("租约到期检查完成");
    }

    /**
     * 检查并通知即将到期的租约
     */
    private void checkAndNotify(LocalDate startDate, LocalDate endDate, int daysUntilExpiry) {
        List<Lease> expiringLeases = leaseRepository.findByEndDateBetweenAndStatus(
            startDate, endDate, LeaseStatus.ACTIVE);

        logger.info("发现 {} 个租约将在 {} 天后到期", expiringLeases.size(), daysUntilExpiry);

        for (Lease lease : expiringLeases) {
            try {
                // 通知业主
                notificationService.createNotification(
                    NotificationType.LEASE_EXPIRING_SOON,
                    "租约即将到期提醒",
                    String.format("您的物业 %s 的租约将在 %d 天后到期（到期日期：%s）。请及时与租户沟通续约事宜。",
                        lease.getProperty().getAddress(),
                        daysUntilExpiry,
                        lease.getEndDate()),
                    lease.getProperty().getOwner().getId(),
                    "lease",
                    lease.getId()
                );

                // 通知租户
                notificationService.createNotification(
                    NotificationType.LEASE_EXPIRING_SOON,
                    "租约即将到期提醒",
                    String.format("您租赁的物业 %s 的租约将在 %d 天后到期（到期日期：%s）。如需续租，请及时联系业主。",
                        lease.getProperty().getAddress(),
                        daysUntilExpiry,
                        lease.getEndDate()),
                    lease.getTenant().getId(),
                    "lease",
                    lease.getId()
                );

                logger.debug("已为租约 {} 发送到期提醒通知", lease.getId());
            } catch (Exception e) {
                logger.error("发送租约到期通知失败，租约ID: {}", lease.getId(), e);
            }
        }
    }
}
