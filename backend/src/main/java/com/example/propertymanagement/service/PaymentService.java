package com.example.propertymanagement.service;

import com.example.propertymanagement.dto.common.PageResponse;
import com.example.propertymanagement.dto.payment.PaymentDto;
import com.example.propertymanagement.dto.payment.PaymentRequest;
import com.example.propertymanagement.exception.ForbiddenException;
import com.example.propertymanagement.exception.ResourceNotFoundException;
import com.example.propertymanagement.mapper.PaymentMapper;
import com.example.propertymanagement.model.Lease;
import com.example.propertymanagement.model.RoleName;
import com.example.propertymanagement.model.User;
import com.example.propertymanagement.repository.LeaseRepository;
import com.example.propertymanagement.repository.PaymentRepository;
import com.example.propertymanagement.repository.UserRepository;
import com.example.propertymanagement.security.UserPrincipal;
import com.example.propertymanagement.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 租金收支服务：处理租金记录的写入与查询。
 * <p>
 * - 写入权限：管理员 / 物业业主；
 * - 查询权限：管理员 / 物业业主 / 租客（仅可查看本人租约）。
 * </p>
 */
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final LeaseRepository leaseRepository;
    public PaymentService(PaymentRepository paymentRepository,
                          LeaseRepository leaseRepository) {
        this.paymentRepository = paymentRepository;
        this.leaseRepository = leaseRepository;
    }

    /**
     * 查询所有可见的支付记录（根据用户角色）。
     *
     * @param pageable 分页参数
     * @return 支付记录分页结果
     */
    @Transactional(readOnly = true)
    public PageResponse<PaymentDto> getAllPayments(Pageable pageable) {
        UserPrincipal principal = getCurrentUser();
        Page<com.example.propertymanagement.model.Payment> page;

        if (isAdmin(principal)) {
            // 管理员可以查看所有支付记录
            page = paymentRepository.findAll(pageable);
        } else if (isOwner(principal)) {
            // 业主可以查看自己物业的支付记录
            page = paymentRepository.findAllByLeasePropertyOwnerId(principal.getId(), pageable);
        } else {
            // 租客只能查看自己租约的支付记录
            page = paymentRepository.findAllByTenantId(principal.getId(), pageable);
        }

        return PageResponse.from(page.map(PaymentMapper::toDto));
    }

    /**
     * 查询特定租约对应的支付记录，并在服务层完成权限校验。
     *
     * @param leaseId  租约 ID
     * @param pageable 分页参数
     * @return 支付记录分页结果
     */
    @Transactional(readOnly = true)
    public PageResponse<PaymentDto> getPaymentsByLease(Long leaseId, Pageable pageable) {
        Lease lease = leaseRepository.findById(leaseId)
            .orElseThrow(() -> new ResourceNotFoundException("未找到租约"));
        UserPrincipal principal = getCurrentUser();
        ensureCanAccessLease(lease, principal);

        Page<com.example.propertymanagement.model.Payment> page =
            paymentRepository.findAllByLeaseId(leaseId, pageable);
        return PageResponse.from(page.map(PaymentMapper::toDto));
    }

    /**
     * 记录支付信息，仅管理员或物业业主可执行。
     *
     * @param request 支付表单
     * @return 保存后的支付记录
     */
    @Transactional
    public PaymentDto recordPayment(PaymentRequest request) {
        Lease lease = leaseRepository.findById(request.leaseId())
            .orElseThrow(() -> new ResourceNotFoundException("未找到租约"));
        UserPrincipal principal = getCurrentUser();

        if (!isAdmin(principal) && !isOwnerOfLease(lease, principal)) {
            throw new ForbiddenException("只有管理员或业主可以记录支付信息");
        }

        com.example.propertymanagement.model.Payment payment =
            com.example.propertymanagement.model.Payment.builder()
                .leaseId(lease.getId())
                .tenantId(lease.getTenantId())
                .tenantUsername(lease.getTenantUsername())
                .propertyId(lease.getPropertyId())
                .propertyAddress(lease.getPropertyAddress())
                .amount(request.amount())
                .paymentDate(request.paymentDate())
                .paymentMethod(request.paymentMethod())
                .build();

        return PaymentMapper.toDto(paymentRepository.save(payment));
    }

    private void ensureCanAccessLease(Lease lease, UserPrincipal principal) {
        if (isAdmin(principal)) {
            return;
        }
        if (isOwnerOfLease(lease, principal)) {
            return;
        }
        if (lease.getTenantId().equals(principal.getId())) {
            return;
        }
        throw new ForbiddenException("无权查看该租约的支付信息");
    }

    private boolean isOwnerOfLease(Lease lease, UserPrincipal principal) {
        return lease.getOwnerId().equals(principal.getId());
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
