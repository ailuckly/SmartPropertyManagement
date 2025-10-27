package com.example.propertymanagement.service;

import com.example.propertymanagement.dto.ai.MaintenanceAnalysisResult;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * 维修工单业务服务：集中处理工单提报、分页查询、状态更新等逻辑，并在服务层落地角色权限控制。
 */
@Slf4j
@Service
public class MaintenanceRequestService {

    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    
    @Autowired(required = false)
    private AIService aiService;

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
            .propertyId(property.getId())
            .propertyAddress(property.getAddress())
            .tenantId(tenant.getId())
            .tenantUsername(tenant.getUsername())
            .description(requestDto.description())
            .status(MaintenanceStatus.PENDING)
            .build();
        
        // AI智能分析
        try {
            if (aiService != null) {
                MaintenanceAnalysisResult analysis = aiService.analyzeMaintenanceRequest(
                    requestDto.description(),
                    property.getAddress()
                );
                if (analysis.isSuccess()) {
                    request.setAiCategory(analysis.getCategory());
                    request.setAiUrgencyLevel(analysis.getUrgencyLevel());
                    request.setAiSolution(analysis.getSolution());
                    request.setAiEstimatedCost(analysis.getEstimatedCost());
                }
            }
        } catch (Exception e) {
            // AI失败不影响业务流程
            log.warn("维修工单AI分析失败", e);
        }

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
     * 分页获取工单列表（支持状态和物业ID筛选）
     *
     * @param pageable 分页参数
     * @param status 状态筛选（可选）
     * @param propertyId 物业ID筛选（可选）
     * @return 包含分页元数据的工单列表
     */
    @Transactional(readOnly = true)
    public PageResponse<MaintenanceRequestDto> getRequestsWithFilters(Pageable pageable, 
                                                                       MaintenanceStatus status, 
                                                                       Long propertyId) {
        UserPrincipal principal = getCurrentUser();
        Page<MaintenanceRequest> page;
        
        // 根据角色和筛选条件执行不同的查询
        if (isAdmin(principal)) {
            if (status != null && propertyId != null) {
                page = maintenanceRequestRepository.findAllByStatusAndPropertyId(status, propertyId, pageable);
            } else if (status != null) {
                page = maintenanceRequestRepository.findAllByStatus(status, pageable);
            } else if (propertyId != null) {
                page = maintenanceRequestRepository.findAllByPropertyId(propertyId, pageable);
            } else {
                page = maintenanceRequestRepository.findAll(pageable);
            }
        } else if (isOwner(principal)) {
            Long ownerId = principal.getId();
            if (status != null && propertyId != null) {
                page = maintenanceRequestRepository.findAllByPropertyOwnerIdAndStatusAndPropertyId(ownerId, status, propertyId, pageable);
            } else if (status != null) {
                page = maintenanceRequestRepository.findAllByPropertyOwnerIdAndStatus(ownerId, status, pageable);
            } else if (propertyId != null) {
                page = maintenanceRequestRepository.findAllByPropertyOwnerIdAndPropertyId(ownerId, propertyId, pageable);
            } else {
                page = maintenanceRequestRepository.findAllByPropertyOwnerId(ownerId, pageable);
            }
        } else {
            Long tenantId = principal.getId();
            if (status != null && propertyId != null) {
                page = maintenanceRequestRepository.findAllByTenantIdAndStatusAndPropertyId(tenantId, status, propertyId, pageable);
            } else if (status != null) {
                page = maintenanceRequestRepository.findAllByTenantIdAndStatus(tenantId, status, pageable);
            } else if (propertyId != null) {
                page = maintenanceRequestRepository.findAllByTenantIdAndPropertyId(tenantId, propertyId, pageable);
            } else {
                page = maintenanceRequestRepository.findAllByTenantId(tenantId, pageable);
            }
        }
        
        return PageResponse.from(page.map(MaintenanceMapper::toDto));
    }
    
    /**
     * 搜索维修请求（支持关键词搜索）
     *
     * @param pageable 分页参数
     * @param keyword 搜索关键词
     * @return 维修请求分页结果
     */
    @Transactional(readOnly = true)
    public PageResponse<MaintenanceRequestDto> searchRequests(Pageable pageable, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getRequests(pageable);
        }
        
        Page<MaintenanceRequest> page = maintenanceRequestRepository.searchByKeyword(keyword.trim(), pageable);
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
    
    private String getStatusText(MaintenanceStatus status) {
        return switch (status) {
            case PENDING -> "待处理";
            case IN_PROGRESS -> "处理中";
            case COMPLETED -> "已完成";
            case CANCELLED -> "已取消";
        };
    }
}
