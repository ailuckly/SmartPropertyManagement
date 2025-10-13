package com.example.propertymanagement.dto.property;

import com.example.propertymanagement.model.PropertyStatus;
import com.example.propertymanagement.model.PropertyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record PropertyRequest(
    Long ownerId,

    @NotBlank(message = "详细地址不能为空")
    String address,

    String city,

    String state,

    String zipCode,

    @NotNull(message = "物业类型不能为空")
    PropertyType propertyType,

    @PositiveOrZero(message = "卧室数量不能为负数")
    Integer bedrooms,

    @PositiveOrZero(message = "卫生间数量不能为负数")
    BigDecimal bathrooms,

    @Positive(message = "建筑面积需要为正数")
    Integer squareFootage,

    PropertyStatus status,

    @PositiveOrZero(message = "租金不能为负数")
    BigDecimal rentAmount
) {
}
