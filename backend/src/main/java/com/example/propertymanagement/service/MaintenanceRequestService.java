package com.example.propertymanagement.service;

import com.example.propertymanagement.dto.common.PageResponse;
import com.example.propertymanagement.dto.maintenance.MaintenanceRequestCreate;
import com.example.propertymanagement.dto.maintenance.MaintenanceRequestDto;
import com.example.propertymanagement.dto.maintenance.MaintenanceStatusUpdate;
import com.example.propertymanagement.exception.ForbiddenException;
import com.example.propertymanagement.exception.ResourceNotFoundException;
import com.example.propertymanagement.mapper.MaintenanceMapper;
import com.example.propertymanagement.model.MaintenanceRequest;
import com.example.propertymanagement.model.MaintenanceStatus;
import com.example.propertymanagement.model.Property;
import com.example.propertymanagement.model.RoleName;
import com.example.propertymanagement.model.User;
import com.example.propertymanagement.repository.MaintenanceRequestRepository;
import com.example.propertymanagement.repository.PropertyRepository;
import com.example.propertymanagement.repository.UserRepository;
import com.example.propertymanagement.security.UserPrincipal;
import com.example.propertymanagement.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * 维修工单业务服务：集中处理工单提报、分页查询、状态更新等逻辑，并在服务层落地角色权限控制。
 */
@Service
public class MaintenanceRequestService {

    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public MaintenanceRequestService(MaintenanceRequestRepository maintenanceRequestRepository,
                                     PropertyRepository propertyRepository,
                                     UserRepository userRepository) {
        this.maintenanceRequestRepository = maintenanceRequestRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    /**
     * 租客提交维修请求。
     *
     * @param requestDto 提交表单
     * @return 新建的工单 DTO
     * @throws ForbiddenException  当前用户不是租客时抛出
     * @throws ResourceNotFoundException 当物业或当前用户不存在时抛出
     */
    @Transactional
    public MaintenanceRequestDto createRequest(MaintenanceRequestCreate requestDto) {
        UserPrincipal principal = getCurrentUser();
        if (!isTenant(principal)) {
            throw new ForbiddenException("只有租户可以提交维修请求");
        }

        Property property = propertyRepository.findById(requestDto.propertyId())
            .orElseThrow(() -> new ResourceNotFoundException("未找到指定物业"));

        User tenant = userRepository.findById(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("未找到当前用户"));

        MaintenanceRequest request = MaintenanceRequest.builder()
            .property(property)
            .tenant(tenant)
            .description(requestDto.description())
            .status(MaintenanceStatus.PENDING)
            .build();

        return MaintenanceMapper.toDto(maintenanceRequestRepository.save(request));
    }

    /**
     * 分页获取工单列表：管理员查看全部，业主查看名下物业，租客查看自己提交的工单。
     *
     * @param pageable 分页参数
     * @return 包含分页元数据的工单列表
     */
    @Transactional(readOnly = true)
    public PageResponse<MaintenanceRequestDto> getRequests(Pageable pageable) {
        UserPrincipal principal = getCurrentUser();
        Page<MaintenanceRequest> page;
        if (isAdmin(principal)) {
            page = maintenanceRequestRepository.findAll(pageable);
        } else if (isOwner(principal)) {
            page = maintenanceRequestRepository.findAllByPropertyOwnerId(principal.getId(), pageable);
        } else {
            page = maintenanceRequestRepository.findAllByTenantId(principal.getId(), pageable);
        }
        return PageResponse.from(page.map(MaintenanceMapper::toDto));
    }

    /**
     * 更新工单状态，仅管理员与业主可执行。
     *
     * @param id      工单 ID
     * @param request 状态更新请求
     * @return 更新后的工单 DTO
     */
    @Transactional
    public MaintenanceRequestDto updateStatus(Long id, MaintenanceStatusUpdate request) {
        UserPrincipal principal = getCurrentUser();
        if (!isAdmin(principal) && !isOwner(principal)) {
            throw new ForbiddenException("只有管理员或业主可以更新维修状态");
        }

        MaintenanceRequest maintenanceRequest = maintenanceRequestRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("未找到维修请求"));

        maintenanceRequest.setStatus(request.status());
        if (request.status() == MaintenanceStatus.COMPLETED) {
            maintenanceRequest.setCompletedAt(java.time.LocalDateTime.now());
        } else {
            maintenanceRequest.setCompletedAt(null);
        }

        return MaintenanceMapper.toDto(maintenanceRequestRepository.save(maintenanceRequest));
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

    private boolean isTenant(UserPrincipal principal) {
        return SecurityUtils.hasRole(principal, RoleName.ROLE_TENANT.name());
    }
}
