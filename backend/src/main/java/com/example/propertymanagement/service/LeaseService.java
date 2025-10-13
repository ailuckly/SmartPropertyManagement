package com.example.propertymanagement.service;

import com.example.propertymanagement.dto.common.PageResponse;
import com.example.propertymanagement.dto.lease.LeaseDto;
import com.example.propertymanagement.dto.lease.LeaseRequest;
import com.example.propertymanagement.exception.ForbiddenException;
import com.example.propertymanagement.exception.ResourceNotFoundException;
import com.example.propertymanagement.mapper.LeaseMapper;
import com.example.propertymanagement.model.Lease;
import com.example.propertymanagement.model.LeaseStatus;
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

@Service
public class LeaseService {

    private final LeaseRepository leaseRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public LeaseService(LeaseRepository leaseRepository,
                        PropertyRepository propertyRepository,
                        UserRepository userRepository) {
        this.leaseRepository = leaseRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<LeaseDto> getLeases(Pageable pageable) {
        UserPrincipal principal = getCurrentUser();
        Page<Lease> page;
        if (isAdmin(principal)) {
            page = leaseRepository.findAll(pageable);
        } else if (isOwner(principal)) {
            page = leaseRepository.findAllByPropertyOwnerId(principal.getId(), pageable);
        } else {
            page = leaseRepository.findAllByTenantId(principal.getId(), pageable);
        }
        return PageResponse.from(page.map(LeaseMapper::toDto));
    }

    @Transactional(readOnly = true)
    public LeaseDto getLease(Long id) {
        UserPrincipal principal = getCurrentUser();
        Lease lease = leaseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("未找到租约"));

        if (isAdmin(principal)
            || (isOwner(principal) && lease.getProperty().getOwner().getId().equals(principal.getId()))
            || lease.getTenant().getId().equals(principal.getId())) {
            return LeaseMapper.toDto(lease);
        }

        throw new ForbiddenException("无权查看该租约详情");
    }

    @Transactional
    public LeaseDto createLease(LeaseRequest request) {
        UserPrincipal principal = getCurrentUser();
        if (!isAdmin(principal) && !isOwner(principal)) {
            throw new ForbiddenException("只有管理员或业主可以创建租约");
        }

        Property property = propertyRepository.findById(request.propertyId())
            .orElseThrow(() -> new ResourceNotFoundException("未找到物业"));

        if (!isAdmin(principal) && !property.getOwner().getId().equals(principal.getId())) {
            throw new ForbiddenException("只能为自己拥有的物业创建租约");
        }

        User tenant = userRepository.findById(request.tenantId())
            .orElseThrow(() -> new ResourceNotFoundException("未找到租户"));

        LeaseStatus status = request.status() != null
            ? request.status()
            : LeaseStatus.ACTIVE;

        Lease lease = Lease.builder()
            .property(property)
            .tenant(tenant)
            .startDate(request.startDate())
            .endDate(request.endDate())
            .rentAmount(request.rentAmount())
            .status(status)
            .build();

        property.setStatus(PropertyStatus.LEASED);

        return LeaseMapper.toDto(leaseRepository.save(lease));
    }

    @Transactional
    public LeaseDto updateLease(Long id, LeaseRequest request) {
        UserPrincipal principal = getCurrentUser();
        Lease lease = leaseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("未找到租约"));

        if (!isAdmin(principal)
            && !lease.getProperty().getOwner().getId().equals(principal.getId())) {
            throw new ForbiddenException("无权更新该租约");
        }

        Property property = propertyRepository.findById(request.propertyId())
            .orElseThrow(() -> new ResourceNotFoundException("未找到物业"));

        if (!isAdmin(principal) && !property.getOwner().getId().equals(principal.getId())) {
            throw new ForbiddenException("只能将租约关联到自己拥有的物业");
        }

        User tenant = userRepository.findById(request.tenantId())
            .orElseThrow(() -> new ResourceNotFoundException("未找到租户"));

        lease.setProperty(property);
        lease.setTenant(tenant);
        lease.setStartDate(request.startDate());
        lease.setEndDate(request.endDate());
        lease.setRentAmount(request.rentAmount());
        if (request.status() != null) {
            lease.setStatus(request.status());
            if (request.status() == LeaseStatus.TERMINATED || request.status() == LeaseStatus.EXPIRED) {
                lease.getProperty().setStatus(PropertyStatus.AVAILABLE);
            }
        }

        return LeaseMapper.toDto(leaseRepository.save(lease));
    }

    @Transactional
    public void deleteLease(Long id) {
        UserPrincipal principal = getCurrentUser();
        Lease lease = leaseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("未找到租约"));

        if (!isAdmin(principal)
            && !lease.getProperty().getOwner().getId().equals(principal.getId())) {
            throw new ForbiddenException("无权删除该租约");
        }

        lease.getProperty().setStatus(PropertyStatus.AVAILABLE);
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
