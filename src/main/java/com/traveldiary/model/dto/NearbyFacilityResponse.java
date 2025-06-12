package com.traveldiary.model.dto;

import java.util.List;

/**
 * 附近设施查询响应DTO
 */
public class NearbyFacilityResponse {
    private double centerLongitude;    // 中心点经度
    private double centerLatitude;     // 中心点纬度
    private String facilityType;       // 设施类型
    private int totalCount;            // 设施总数
    private int radius;                // 搜索半径（米）
    private List<NearbyFacility> facilities; // 设施列表
    
    public NearbyFacilityResponse() {
    }
    
    public NearbyFacilityResponse(double centerLongitude, double centerLatitude,
                                String facilityType, int totalCount, int radius,
                                List<NearbyFacility> facilities) {
        this.centerLongitude = centerLongitude;
        this.centerLatitude = centerLatitude;
        this.facilityType = facilityType;
        this.totalCount = totalCount;
        this.radius = radius;
        this.facilities = facilities;
    }
    
    // Getters and Setters
    public double getCenterLongitude() {
        return centerLongitude;
    }
    
    public void setCenterLongitude(double centerLongitude) {
        this.centerLongitude = centerLongitude;
    }
    
    public double getCenterLatitude() {
        return centerLatitude;
    }
    
    public void setCenterLatitude(double centerLatitude) {
        this.centerLatitude = centerLatitude;
    }
    
    public String getFacilityType() {
        return facilityType;
    }
    
    public void setFacilityType(String facilityType) {
        this.facilityType = facilityType;
    }
    
    public int getTotalCount() {
        return totalCount;
    }
    
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
    
    public int getRadius() {
        return radius;
    }
    
    public void setRadius(int radius) {
        this.radius = radius;
    }
    
    public List<NearbyFacility> getFacilities() {
        return facilities;
    }
    
    public void setFacilities(List<NearbyFacility> facilities) {
        this.facilities = facilities;
    }
} 