package com.example.propertymanagement.service;

import com.example.propertymanagement.dto.common.PageResponse;
import com.example.propertymanagement.dto.property.PropertyDto;
import com.example.propertymanagement.dto.property.PropertyFilterRequest;
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

/**
 * 物业模块业务门面，负责：
 * <ul>
 *   <li>提供 {@link com.example.propertymanagement.model.Property} 的增删改查能力；</li>
 *   <li>在服务层执行角色/业主校验，避免控制器重复判断；</li>
 *   <li>统一封装分页与异常处理逻辑。</li>
 * </ul>
 */
@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public PropertyService(PropertyRepository propertyRepository, UserRepository userRepository) {
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    /**
     * 查询物业列表，当提供 ownerId 时只返回指定业主的数据。
     *
     * @param pageable 分页参数
     * @param ownerId  业主 ID，可为 null
     * @return 包含分页元数据的 {@link PageResponse}
     */
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
    
    /**
     * 搜索物业列表（支持关键词搜索）
     *
     * @param pageable 分页参数
     * @param ownerId  业主 ID，可为 null
     * @param keyword  搜索关键词
     * @return 包含分页元数据的 {@link PageResponse}
     */
    @Transactional(readOnly = true)
    public PageResponse<PropertyDto> searchProperties(Pageable pageable, Long ownerId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getProperties(pageable, ownerId);
        }
        
        Page<Property> page;
        if (ownerId != null) {
            page = propertyRepository.searchByOwnerIdAndKeyword(ownerId, keyword.trim(), pageable);
        } else {
            page = propertyRepository.searchByKeyword(keyword.trim(), pageable);
        }
        return PageResponse.from(page.map(PropertyMapper::toDto));
    }
    
    /**
     * 使用高级筛选条件查询物业列表
     *
     * @param pageable 分页参数
     * @param filterRequest 筛选条件
     * @return 包含分页元数据的 {@link PageResponse}
     */
    @Transactional(readOnly = true)
    public PageResponse<PropertyDto> getPropertiesWithFilters(Pageable pageable, PropertyFilterRequest filterRequest) {
        // 如果有关键词搜索，优先使用搜索功能
        if (filterRequest.hasKeyword()) {
            return searchProperties(pageable, filterRequest.getOwnerId(), filterRequest.getKeyword());
        }
        
        // 否则使用高级筛选
        Page<Property> page;
        if (filterRequest.hasFilters()) {
            page = propertyRepository.findPropertiesWithFilters(
                filterRequest.getOwnerId(),
                filterRequest.getStatus(),
                filterRequest.getPropertyType(),
                filterRequest.getMinRent(),
                filterRequest.getMaxRent(),
                filterRequest.getMinBedrooms(),
                filterRequest.getMaxBedrooms(),
                filterRequest.getCity(),
                pageable
            );
        } else {
            // 无筛选条件，使用基础查询
            return getProperties(pageable, filterRequest.getOwnerId());
        }
        
        return PageResponse.from(page.map(PropertyMapper::toDto));
    }

    /**
     * 根据 ID 获取物业详情。
     *
     * @param id 物业 ID
     * @return 物业 DTO
     * @throws ResourceNotFoundException 未找到目标数据时抛出
     */
    @Transactional(readOnly = true)
    public PropertyDto getProperty(Long id) {
        Property property = findPropertyOrThrow(id);
        return PropertyMapper.toDto(property);
    }

    /**
     * 创建物业。管理员可指定任意业主；业主本人只能为自己创建物业。
     *
     * @param request 请求体
     * @return 新建的物业 DTO
     */
    @Transactional
    public PropertyDto createProperty(PropertyRequest request) {
        UserPrincipal principal = getAuthenticatedUser();

        if (!isAdmin(principal) && !isOwner(principal)) {
            throw new ForbiddenException("仅物业管理员或业主可创建物业信息");
        }

        User owner = determineOwner(principal, request.ownerId());

        Property property = Property.builder()
            .ownerId(owner.getId())
            .ownerUsername(owner.getUsername())
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

    /**
     * 更新物业，权限规则与 {@link #createProperty(PropertyRequest)} 相同。
     *
     * @param id      物业 ID
     * @param request 新数据
     * @return 更新后的 DTO
     */
    @Transactional
    public PropertyDto updateProperty(Long id, PropertyRequest request) {
        UserPrincipal principal = getAuthenticatedUser();
        Property property = findPropertyOrThrow(id);

        if (!isAdmin(principal) && !property.getOwnerId().equals(principal.getId())) {
            throw new ForbiddenException("仅物业管理员或该物业所有者可以更新信息");
        }

        if (request.ownerId() != null && isAdmin(principal)) {
            User newOwner = userRepository.findById(request.ownerId())
                .orElseThrow(() -> new ResourceNotFoundException("未找到指定业主"));
            property.setOwnerId(newOwner.getId());
            property.setOwnerUsername(newOwner.getUsername());
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

    /**
     * 删除物业。只有管理员或物业所有者可执行。
     *
     * @param id 物业 ID
     */
    @Transactional
    public void deleteProperty(Long id) {
        UserPrincipal principal = getAuthenticatedUser();
        Property property = findPropertyOrThrow(id);

        if (!isAdmin(principal) && !property.getOwnerId().equals(principal.getId())) {
            throw new ForbiddenException("仅物业管理员或该物业所有者可以删除信息");
        }

        propertyRepository.delete(property);
    }

    /**
     * 批量删除物业。只有管理员或物业所有者可执行。
     *
     * @param ids 物业ID列表
     */
    @Transactional
    public void batchDeleteProperties(java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        
        UserPrincipal principal = getAuthenticatedUser();
        
        for (Long id : ids) {
            Property property = findPropertyOrThrow(id);
            
            if (!isAdmin(principal) && !property.getOwnerId().equals(principal.getId())) {
                throw new ForbiddenException("仅物业管理员或该物业所有者可以删除信息：物业ID=" + id);
            }
        }
        
        // 所有权限检查通过后，批量删除
        propertyRepository.deleteAllByIdIn(ids);
    }

    /**
     * 批量更新物业状态。只有管理员或物业所有者可执行。
     *
     * @param ids 物业ID列表
     * @param status 新状态
     */
    @Transactional
    public void batchUpdateStatus(java.util.List<Long> ids, PropertyStatus status) {
        if (ids == null || ids.isEmpty() || status == null) {
            return;
        }
        
        UserPrincipal principal = getAuthenticatedUser();
        
        for (Long id : ids) {
            Property property = findPropertyOrThrow(id);
            
            if (!isAdmin(principal) && !property.getOwnerId().equals(principal.getId())) {
                throw new ForbiddenException("仅物业管理员或该物业所有者可以更新状态：物业ID=" + id);
            }
            
            property.setStatus(status);
        }
        
        // 无需显式调用save，@Transactional会自动保存更改
    }

    /**
     * 查询物业实体，若不存在则抛出 404 异常。
     */
    private Property findPropertyOrThrow(Long id) {
        return propertyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("未找到指定物业"));
    }

    /**
     * 校验并返回物业所属业主。
     *
     * @param principal         当前登录用户
     * @param ownerIdFromRequest 请求体中的业主 ID
     * @return 实际的业主实体
     */
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

    /**
     * 获取当前登录用户，若未登录则抛出异常。
     */
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
