package com.example.propertymanagement.service;

import com.example.propertymanagement.dto.common.PageResponse;
import com.example.propertymanagement.dto.property.PropertyDto;
import com.example.propertymanagement.dto.property.PropertyRequest;
import com.example.propertymanagement.exception.ForbiddenException;
import com.example.propertymanagement.exception.ResourceNotFoundException;
import com.example.propertymanagement.mapper.PropertyMapper;
import com.example.propertymanagement.model.Property;
import com.example.propertymanagement.model.PropertyStatus;
import com.example.propertymanagement.model.RoleName;
import com.example.propertymanagement.model.User;
import com.example.propertymanagement.repository.PropertyRepository;
import com.example.propertymanagement.repository.UserRepository;
import com.example.propertymanagement.security.UserPrincipal;
import com.example.propertymanagement.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public PropertyService(PropertyRepository propertyRepository, UserRepository userRepository) {
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<PropertyDto> getProperties(Pageable pageable, Long ownerId) {
        Page<Property> page;
        if (ownerId != null) {
            page = propertyRepository.findAllByOwnerId(ownerId, pageable);
        } else {
            page = propertyRepository.findAll(pageable);
        }
        return PageResponse.from(page.map(PropertyMapper::toDto));
    }

    @Transactional(readOnly = true)
    public PropertyDto getProperty(Long id) {
        Property property = findPropertyOrThrow(id);
        return PropertyMapper.toDto(property);
    }

    @Transactional
    public PropertyDto createProperty(PropertyRequest request) {
        UserPrincipal principal = getAuthenticatedUser();

        if (!isAdmin(principal) && !isOwner(principal)) {
            throw new ForbiddenException("仅物业管理员或业主可创建物业信息");
        }

        User owner = determineOwner(principal, request.ownerId());

        Property property = Property.builder()
            .owner(owner)
            .address(request.address())
            .city(request.city())
            .state(request.state())
            .zipCode(request.zipCode())
            .propertyType(request.propertyType())
            .bedrooms(request.bedrooms())
            .bathrooms(request.bathrooms())
            .squareFootage(request.squareFootage())
            .status(request.status() != null ? request.status() : PropertyStatus.AVAILABLE)
            .rentAmount(request.rentAmount())
            .build();

        Property saved = propertyRepository.save(property);
        return PropertyMapper.toDto(saved);
    }

    @Transactional
    public PropertyDto updateProperty(Long id, PropertyRequest request) {
        UserPrincipal principal = getAuthenticatedUser();
        Property property = findPropertyOrThrow(id);

        if (!isAdmin(principal) && !property.getOwner().getId().equals(principal.getId())) {
            throw new ForbiddenException("仅物业管理员或该物业所有者可以更新信息");
        }

        if (request.ownerId() != null && isAdmin(principal)) {
            User newOwner = userRepository.findById(request.ownerId())
                .orElseThrow(() -> new ResourceNotFoundException("未找到指定业主"));
            property.setOwner(newOwner);
        }

        property.setAddress(request.address());
        property.setCity(request.city());
        property.setState(request.state());
        property.setZipCode(request.zipCode());
        property.setPropertyType(request.propertyType());
        property.setBedrooms(request.bedrooms());
        property.setBathrooms(request.bathrooms());
        property.setSquareFootage(request.squareFootage());
        if (request.status() != null) {
            property.setStatus(request.status());
        }
        property.setRentAmount(request.rentAmount());

        return PropertyMapper.toDto(propertyRepository.save(property));
    }

    @Transactional
    public void deleteProperty(Long id) {
        UserPrincipal principal = getAuthenticatedUser();
        Property property = findPropertyOrThrow(id);

        if (!isAdmin(principal) && !property.getOwner().getId().equals(principal.getId())) {
            throw new ForbiddenException("仅物业管理员或该物业所有者可以删除信息");
        }

        propertyRepository.delete(property);
    }

    private Property findPropertyOrThrow(Long id) {
        return propertyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("未找到指定物业"));
    }

    private User determineOwner(UserPrincipal principal, Long ownerIdFromRequest) {
        if (isAdmin(principal)) {
            if (ownerIdFromRequest == null) {
                throw new ForbiddenException("管理员创建物业时需指定业主");
            }
            return userRepository.findById(ownerIdFromRequest)
                .orElseThrow(() -> new ResourceNotFoundException("未找到指定业主"));
        }

        if (ownerIdFromRequest != null && !ownerIdFromRequest.equals(principal.getId())) {
            throw new ForbiddenException("不能为其他用户创建物业");
        }

        return userRepository.findById(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("未找到当前用户"));
    }

    private UserPrincipal getAuthenticatedUser() {
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
