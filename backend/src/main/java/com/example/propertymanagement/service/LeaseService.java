package com.example.propertymanagement.service;

import com.example.propertymanagement.dto.common.PageResponse;
import com.example.propertymanagement.dto.lease.LeaseDto;
import com.example.propertymanagement.dto.lease.LeaseRequest;
import com.example.propertymanagement.exception.ForbiddenException;
import com.example.propertymanagement.exception.ResourceNotFoundException;
import com.example.propertymanagement.mapper.LeaseMapper;
import com.example.propertymanagement.model.Lease;
import com.example.propertymanagement.model.LeaseStatus;
import com.example.propertymanagement.model.NotificationType;
import com.example.propertymanagement.model.Property;
import com.example.propertymanagement.model.PropertyStatus;
import com.example.propertymanagement.model.RoleName;
import com.example.propertymanagement.model.User;
import com.example.propertymanagement.repository.LeaseRepository;
import com.example.propertymanagement.repository.PropertyRepository;
import com.example.propertymanagement.repository.UserRepository;
import com.example.propertymanagement.security.UserPrincipal;
import com.example.propertymanagement.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 租约业务层：封装租约的创建、更新、查询、删除等流程，并根据角色限制访问范围。
 */
@Service
public class LeaseService {

    private final LeaseRepository leaseRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public LeaseService(LeaseRepository leaseRepository,
                        PropertyRepository propertyRepository,
                        UserRepository userRepository,
                        NotificationService notificationService) {
        this.leaseRepository = leaseRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    /**
     * 分页返回当前用户可见的租约：管理员查看全部，业主查看自己物业的租约，租客查看个人租约。
     *
     * @param pageable 分页参数
     * @return 租约分页结果
     */
    @Transactional(readOnly = true)
    public PageResponse<LeaseDto> getLeases(Pageable pageable) {
        UserPrincipal principal = getCurrentUser();
        Page<Lease> page;
        if (isAdmin(principal)) {
            page = leaseRepository.findAll(pageable);
        } else if (isOwner(principal)) {
            page = leaseRepository.findAllByOwnerId(principal.getId(), pageable);
        } else {
            page = leaseRepository.findAllByTenantId(principal.getId(), pageable);
        }
        return PageResponse.from(page.map(LeaseMapper::toDto));
    }
    
    /**
     * 搜索租约（支持关键词搜索）
     *
     * @param pageable 分页参数
     * @param keyword 搜索关键词
     * @return 租约分页结果
     */
    @Transactional(readOnly = true)
    public PageResponse<LeaseDto> searchLeases(Pageable pageable, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getLeases(pageable);
        }
        
        Page<Lease> page = leaseRepository.searchByKeyword(keyword.trim(), pageable);
        return PageResponse.from(page.map(LeaseMapper::toDto));
    }

    /**
     * 根据 ID 查询租约并检查可见性。
     *
     * @param id 租约 ID
     * @return 租约 DTO
     * @throws ForbiddenException 若无权查看时抛出
     */
    @Transactional(readOnly = true)
    public LeaseDto getLease(Long id) {
        UserPrincipal principal = getCurrentUser();
        Lease lease = leaseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("未找到租约"));

        if (isAdmin(principal)
            || (isOwner(principal) && lease.getOwnerId().equals(principal.getId()))
            || lease.getTenantId().equals(principal.getId())) {
            return LeaseMapper.toDto(lease);
        }

        throw new ForbiddenException("无权查看该租约详情");
    }

    /**
     * 创建租约。业主仅能为自己名下物业创建，管理员不受限制。
     *
     * @param request 创建表单
     * @return 创建后的租约 DTO
     */
    @Transactional
    public LeaseDto createLease(LeaseRequest request) {
        UserPrincipal principal = getCurrentUser();
        if (!isAdmin(principal) && !isOwner(principal)) {
            throw new ForbiddenException("只有管理员或业主可以创建租约");
        }

        Property property = propertyRepository.findById(request.propertyId())
            .orElseThrow(() -> new ResourceNotFoundException("未找到物业"));

        if (!isAdmin(principal) && !property.getOwnerId().equals(principal.getId())) {
            throw new ForbiddenException("只能为自己拥有的物业创建租约");
        }

        User tenant = userRepository.findById(request.tenantId())
            .orElseThrow(() -> new ResourceNotFoundException("未找到租户"));

        LeaseStatus status = request.status() != null
            ? request.status()
            : LeaseStatus.ACTIVE;

        Lease lease = Lease.builder()
            .propertyId(property.getId())
            .propertyAddress(property.getAddress())
            .ownerId(property.getOwnerId())
            .ownerUsername(property.getOwnerUsername())
            .tenantId(tenant.getId())
            .tenantUsername(tenant.getUsername())
            .startDate(request.startDate())
            .endDate(request.endDate())
            .rentAmount(request.rentAmount())
            .status(status)
            .build();

        property.setStatus(PropertyStatus.LEASED);

        Lease savedLease = leaseRepository.save(lease);
        
        // 发送通知给租户
        try {
            notificationService.createNotification(
                NotificationType.LEASE_CREATED,
                "租约已创建",
                String.format("您的租约已创建成功，物业地址：%s，租期：%s 至 %s",
                    property.getAddress(),
                    request.startDate(),
                    request.endDate()),
                tenant.getId(),
                "lease",
                savedLease.getId()
            );
        } catch (Exception e) {
            // 通知发送失败不影响主流程
        }

        return LeaseMapper.toDto(savedLease);
    }

    /**
     * 更新租约信息，同样遵守所有权与角色的约束。
     *
     * @param id      租约 ID
     * @param request 更新内容
     * @return 更新后的租约 DTO
     */
    @Transactional
    public LeaseDto updateLease(Long id, LeaseRequest request) {
        UserPrincipal principal = getCurrentUser();
        Lease lease = leaseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("未找到租约"));

        if (!isAdmin(principal)
            && !lease.getOwnerId().equals(principal.getId())) {
            throw new ForbiddenException("无权更新该租约");
        }

        Property property = propertyRepository.findById(request.propertyId())
            .orElseThrow(() -> new ResourceNotFoundException("未找到物业"));

        if (!isAdmin(principal) && !property.getOwnerId().equals(principal.getId())) {
            throw new ForbiddenException("只能将租约关联到自己拥有的物业");
        }

        User tenant = userRepository.findById(request.tenantId())
            .orElseThrow(() -> new ResourceNotFoundException("未找到租户"));

        lease.setPropertyId(property.getId());
        lease.setPropertyAddress(property.getAddress());
        lease.setOwnerId(property.getOwnerId());
        lease.setOwnerUsername(property.getOwnerUsername());
        lease.setTenantId(tenant.getId());
        lease.setTenantUsername(tenant.getUsername());
        lease.setStartDate(request.startDate());
        lease.setEndDate(request.endDate());
        lease.setRentAmount(request.rentAmount());
        if (request.status() != null) {
            lease.setStatus(request.status());
            if (request.status() == LeaseStatus.TERMINATED || request.status() == LeaseStatus.EXPIRED) {
                property.setStatus(PropertyStatus.AVAILABLE);
            }
        }

        return LeaseMapper.toDto(leaseRepository.save(lease));
    }

    /**
     * 删除租约，并把关联物业的状态恢复为 {@code AVAILABLE}。
     *
     * @param id 租约 ID
     */
    @Transactional
    public void deleteLease(Long id) {
        UserPrincipal principal = getCurrentUser();
        Lease lease = leaseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("未找到租约"));

        if (!isAdmin(principal)
            && !lease.getOwnerId().equals(principal.getId())) {
            throw new ForbiddenException("无权删除该租约");
        }

        Property property = propertyRepository.findById(lease.getPropertyId())
            .orElseThrow(() -> new ResourceNotFoundException("未找到物业"));
        property.setStatus(PropertyStatus.AVAILABLE);
        
        leaseRepository.delete(lease);
    }

    private UserPrincipal getCurrentUser() {
        return SecurityUtils.getCurrentUserPrincipal()
            .orElseThrow(() -> new ForbiddenException("未登录"));
    }

    private boolean isAdmin(UserPrincipal principal) {
        return SecurityUtils.hasRole(principal, RoleName.ROLE_ADMIN.name());
    }

    private boolean isOwner(UserPrincipal principal) {
        return SecurityUtils.hasRole(principal, RoleName.ROLE_OWNER.name());
    }
}
