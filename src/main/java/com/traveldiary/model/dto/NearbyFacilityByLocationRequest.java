package com.traveldiary.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 按经纬度查询附近设施请求DTO
 */
public class NearbyFacilityByLocationRequest {
    @NotNull(message = "经度不能为空")
    private Double longitude;      // 经度
    
    @NotNull(message = "纬度不能为空")
    private Double latitude;       // 纬度
    
    private String facilityType;   // 设施类型，例如：超市、卫生间、餐厅等
    
    @Min(value = 1, message = "查询数量最小为1")
    @Max(value = 50, message = "查询数量最大为50")
    private int limit = 20;        // 查询数量限制，默认20
    
    @Min(value = 500, message = "搜索半径最小为500米")
    @Max(value = 5000, message = "搜索半径最大为5000米")
    private int radius = 2000;     // 搜索半径（米），默认2000米
    
    public NearbyFacilityByLocationRequest() {
    }
    
    public NearbyFacilityByLocationRequest(Double longitude, Double latitude, 
                                         String facilityType, int limit, int radius) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.facilityType = facilityType;
        this.limit = limit;
        this.radius = radius;
    }
    
    // Getters and Setters
    public Double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    
    public Double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    
    public String getFacilityType() {
        return facilityType;
    }
    
    public void setFacilityType(String facilityType) {
        this.facilityType = facilityType;
    }
    
    public int getLimit() {
        return limit;
    }
    
    public void setLimit(int limit) {
        this.limit = limit;
    }
    
    public int getRadius() {
        return radius;
    }
    
    public void setRadius(int radius) {
        this.radius = radius;
    }
} 