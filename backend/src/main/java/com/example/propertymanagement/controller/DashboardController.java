package com.example.propertymanagement.controller;

import com.example.propertymanagement.model.*;
import com.example.propertymanagement.repository.*;
import com.example.propertymanagement.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 仪表盘统计数据控制器
 * 提供各种可视化图表所需的统计数据
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private LeaseRepository leaseRepository;

    @Autowired
    private MaintenanceRequestRepository maintenanceRequestRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 获取基本统计概览
     */
    @GetMapping("/overview")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getOverview(@AuthenticationPrincipal UserPrincipal currentUser) {
        Map<String, Object> overview = new HashMap<>();
        
        Long userId = currentUser.getId();
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_OWNER"));

        if (isAdmin) {
            // 管理员看所有数据
            overview.put("totalProperties", propertyRepository.count());
            overview.put("totalLeases", leaseRepository.count());
            overview.put("totalUsers", userRepository.count());
            overview.put("pendingMaintenances", maintenanceRequestRepository.countByStatus(MaintenanceStatus.PENDING));
        } else if (isOwner) {
            // 业主看自己的物业相关数据
            overview.put("totalProperties", propertyRepository.countByOwnerId(userId));
            overview.put("totalLeases", leaseRepository.countByProperty_OwnerId(userId));
            overview.put("pendingMaintenances", maintenanceRequestRepository.countByProperty_OwnerIdAndStatus(userId, MaintenanceStatus.PENDING));
        } else {
            // 租户看自己相关数据
            overview.put("totalLeases", leaseRepository.countByTenantId(userId));
            overview.put("pendingMaintenances", maintenanceRequestRepository.countByTenantId(userId));
        }

        return ResponseEntity.ok(overview);
    }

    /**
     * 物业状态分布统计
     */
    @GetMapping("/property-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Map<String, Long>> getPropertyStatusDistribution(@AuthenticationPrincipal UserPrincipal currentUser) {
        List<Property> properties;
        
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            properties = propertyRepository.findAll();
        } else {
            properties = propertyRepository.findByOwnerId(currentUser.getId());
        }

        Map<String, Long> distribution = properties.stream()
                .collect(Collectors.groupingBy(
                    p -> p.getStatus() != null ? p.getStatus().name() : "UNKNOWN",
                    Collectors.counting()
                ));

        return ResponseEntity.ok(distribution);
    }

    /**
     * 物业类型分布统计
     */
    @GetMapping("/property-type")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Map<String, Long>> getPropertyTypeDistribution(@AuthenticationPrincipal UserPrincipal currentUser) {
        List<Property> properties;
        
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            properties = propertyRepository.findAll();
        } else {
            properties = propertyRepository.findByOwnerId(currentUser.getId());
        }

        Map<String, Long> distribution = properties.stream()
                .collect(Collectors.groupingBy(
                    p -> p.getPropertyType() != null ? p.getPropertyType().name() : "UNKNOWN",
                    Collectors.counting()
                ));

        return ResponseEntity.ok(distribution);
    }

    /**
     * 维修请求状态分布统计
     */
    @GetMapping("/maintenance-status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Long>> getMaintenanceStatusDistribution(@AuthenticationPrincipal UserPrincipal currentUser) {
        List<MaintenanceRequest> requests;
        
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_OWNER"));

        if (isAdmin) {
            requests = maintenanceRequestRepository.findAll();
        } else if (isOwner) {
            requests = maintenanceRequestRepository.findByProperty_OwnerId(currentUser.getId());
        } else {
            requests = maintenanceRequestRepository.findByTenantId(currentUser.getId());
        }

        Map<String, Long> distribution = requests.stream()
                .collect(Collectors.groupingBy(
                    r -> r.getStatus() != null ? r.getStatus().name() : "UNKNOWN",
                    Collectors.counting()
                ));

        return ResponseEntity.ok(distribution);
    }

    /**
     * 月度收支趋势（近6个月）
     */
    @GetMapping("/payment-trend")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Map<String, Object>> getPaymentTrend(@AuthenticationPrincipal UserPrincipal currentUser) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(5).withDayOfMonth(1);
        
        List<Payment> payments;
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            payments = paymentRepository.findByPaymentDateBetween(startDate, endDate);
        } else {
            payments = paymentRepository.findByLease_Property_OwnerIdAndPaymentDateBetween(
                currentUser.getId(), startDate, endDate);
        }

        // 按月份分组统计（所有支付视为租金收入）
        Map<YearMonth, BigDecimal> incomeByMonth = new TreeMap<>();
        
        // 初始化所有月份为0
        for (int i = 0; i < 6; i++) {
            YearMonth month = YearMonth.from(startDate.plusMonths(i));
            incomeByMonth.put(month, BigDecimal.ZERO);
        }

        // 累加租金收入
        for (Payment payment : payments) {
            YearMonth month = YearMonth.from(payment.getPaymentDate());
            incomeByMonth.merge(month, payment.getAmount(), BigDecimal::add);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("months", incomeByMonth.keySet().stream()
                .map(ym -> ym.getYear() + "-" + String.format("%02d", ym.getMonthValue()))
                .collect(Collectors.toList()));
        result.put("income", incomeByMonth.values());
        // 暂时不追踪支出，返回空列表
        result.put("expense", incomeByMonth.keySet().stream()
                .map(m -> BigDecimal.ZERO)
                .collect(Collectors.toList()));

        return ResponseEntity.ok(result);
    }

    /**
     * 租约到期统计（未来3个月）
     */
    @GetMapping("/lease-expiring")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Map<String, Object>> getLeaseExpiring(@AuthenticationPrincipal UserPrincipal currentUser) {
        LocalDate today = LocalDate.now();
        LocalDate threeMonthsLater = today.plusMonths(3);
        
        List<Lease> leases;
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            leases = leaseRepository.findByEndDateBetweenAndStatus(today, threeMonthsLater, LeaseStatus.ACTIVE);
        } else {
            leases = leaseRepository.findByProperty_OwnerIdAndEndDateBetweenAndStatus(
                currentUser.getId(), today, threeMonthsLater, LeaseStatus.ACTIVE);
        }

        // 按月份分组
        Map<YearMonth, Long> expiringByMonth = new TreeMap<>();
        for (int i = 0; i < 3; i++) {
            expiringByMonth.put(YearMonth.from(today.plusMonths(i)), 0L);
        }

        for (Lease lease : leases) {
            YearMonth month = YearMonth.from(lease.getEndDate());
            expiringByMonth.merge(month, 1L, Long::sum);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("months", expiringByMonth.keySet().stream()
                .map(ym -> ym.getYear() + "-" + String.format("%02d", ym.getMonthValue()))
                .collect(Collectors.toList()));
        result.put("counts", expiringByMonth.values());

        return ResponseEntity.ok(result);
    }

    /**
     * 近期活动记录（最新10条）
     */
    @GetMapping("/recent-activities")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Map<String, Object>>> getRecentActivities(@AuthenticationPrincipal UserPrincipal currentUser) {
        List<Map<String, Object>> activities = new ArrayList<>();
        
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_OWNER"));

        // 获取最新租约
        List<Lease> recentLeases;
        if (isAdmin) {
            recentLeases = leaseRepository.findTop5ByOrderByIdDesc();
        } else if (isOwner) {
            recentLeases = leaseRepository.findTop5ByProperty_OwnerIdOrderByIdDesc(currentUser.getId());
        } else {
            recentLeases = leaseRepository.findTop5ByTenantIdOrderByIdDesc(currentUser.getId());
        }

        for (Lease lease : recentLeases) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("type", "lease");
            activity.put("title", "新租约创建");
            activity.put("description", "物业: " + (lease.getProperty() != null ? lease.getProperty().getAddress() : "N/A"));
            activity.put("date", lease.getStartDate());
            activities.add(activity);
        }

        // 获取最新维修请求
        List<MaintenanceRequest> recentMaintenance;
        if (isAdmin) {
            recentMaintenance = maintenanceRequestRepository.findTop5ByOrderByIdDesc();
        } else if (isOwner) {
            recentMaintenance = maintenanceRequestRepository.findTop5ByProperty_OwnerIdOrderByIdDesc(currentUser.getId());
        } else {
            recentMaintenance = maintenanceRequestRepository.findTop5ByTenantIdOrderByIdDesc(currentUser.getId());
        }

        for (MaintenanceRequest request : recentMaintenance) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("type", "maintenance");
            activity.put("title", "维修请求");
            activity.put("description", request.getDescription());
            activity.put("date", request.getReportedAt() != null ? request.getReportedAt().toLocalDate() : LocalDate.now());
            activities.add(activity);
        }

        // 按日期排序并限制数量
        activities.sort((a, b) -> {
            LocalDate dateA = (LocalDate) a.get("date");
            LocalDate dateB = (LocalDate) b.get("date");
            return dateB.compareTo(dateA);
        });

        return ResponseEntity.ok(activities.stream().limit(10).collect(Collectors.toList()));
    }
}
