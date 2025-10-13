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
 * Handles recording and querying payment transactions tied to leases. Access is restricted to administrators,
 * lease owners and the tenant bound to the lease (for read-only operations).
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
     * Fetches payment history for a lease while ensuring that the caller has permission to inspect it.
     */
    @Transactional(readOnly = true)
    public PageResponse<PaymentDto> getPayments(Long leaseId, Pageable pageable) {
        Lease lease = leaseRepository.findById(leaseId)
            .orElseThrow(() -> new ResourceNotFoundException("未找到租约"));
        UserPrincipal principal = getCurrentUser();
        ensureCanAccessLease(lease, principal);

        Page<com.example.propertymanagement.model.Payment> page =
            paymentRepository.findAllByLeaseId(leaseId, pageable);
        return PageResponse.from(page.map(PaymentMapper::toDto));
    }

    /**
     * Records a payment against a lease. Only admins or the property owner can register the transaction.
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
                .lease(lease)
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
        if (lease.getTenant().getId().equals(principal.getId())) {
            return;
        }
        throw new ForbiddenException("无权查看该租约的支付信息");
    }

    private boolean isOwnerOfLease(Lease lease, UserPrincipal principal) {
        return lease.getProperty().getOwner().getId().equals(principal.getId());
    }

    private UserPrincipal getCurrentUser() {
        return SecurityUtils.getCurrentUserPrincipal()
            .orElseThrow(() -> new ForbiddenException("未登录"));
    }

    private boolean isAdmin(UserPrincipal principal) {
        return SecurityUtils.hasRole(principal, RoleName.ROLE_ADMIN.name());
    }
}
