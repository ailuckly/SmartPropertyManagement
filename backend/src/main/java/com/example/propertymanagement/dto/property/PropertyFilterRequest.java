package com.example.propertymanagement.dto.property;

/**
 * Request DTO for advanced property filtering.
 */
public class PropertyFilterRequest {
    
    private Long ownerId;
    private String keyword;
    private String status;
    private String propertyType;
    private Double minRent;
    private Double maxRent;
    private Integer minBedrooms;
    private Integer maxBedrooms;
    private String city;
    
    // Private constructor for builder
    private PropertyFilterRequest(Builder builder) {
        this.ownerId = builder.ownerId;
        this.keyword = builder.keyword;
        this.status = builder.status;
        this.propertyType = builder.propertyType;
        this.minRent = builder.minRent;
        this.maxRent = builder.maxRent;
        this.minBedrooms = builder.minBedrooms;
        this.maxBedrooms = builder.maxBedrooms;
        this.city = builder.city;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Builder class
    public static class Builder {
        private Long ownerId;
        private String keyword;
        private String status;
        private String propertyType;
        private Double minRent;
        private Double maxRent;
        private Integer minBedrooms;
        private Integer maxBedrooms;
        private String city;
        
        public Builder ownerId(Long ownerId) {
            this.ownerId = ownerId;
            return this;
        }
        
        public Builder keyword(String keyword) {
            this.keyword = keyword;
            return this;
        }
        
        public Builder status(String status) {
            this.status = status;
            return this;
        }
        
        public Builder propertyType(String propertyType) {
            this.propertyType = propertyType;
            return this;
        }
        
        public Builder minRent(Double minRent) {
            this.minRent = minRent;
            return this;
        }
        
        public Builder maxRent(Double maxRent) {
            this.maxRent = maxRent;
            return this;
        }
        
        public Builder minBedrooms(Integer minBedrooms) {
            this.minBedrooms = minBedrooms;
            return this;
        }
        
        public Builder maxBedrooms(Integer maxBedrooms) {
            this.maxBedrooms = maxBedrooms;
            return this;
        }
        
        public Builder city(String city) {
            this.city = city;
            return this;
        }
        
        public PropertyFilterRequest build() {
            return new PropertyFilterRequest(this);
        }
    }
    
    // Getters
    public Long getOwnerId() { return ownerId; }
    public String getKeyword() { return keyword; }
    public String getStatus() { return status; }
    public String getPropertyType() { return propertyType; }
    public Double getMinRent() { return minRent; }
    public Double getMaxRent() { return maxRent; }
    public Integer getMinBedrooms() { return minBedrooms; }
    public Integer getMaxBedrooms() { return maxBedrooms; }
    public String getCity() { return city; }
    
    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }
    
    public boolean hasFilters() {
        return status != null || propertyType != null || 
               minRent != null || maxRent != null ||
               minBedrooms != null || maxBedrooms != null ||
               (city != null && !city.trim().isEmpty());
    }
}