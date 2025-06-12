package com.traveldiary.model.dto;

/**
 * 附近设施DTO
 */
public class NearbyFacility {
    private String id;           // 设施ID
    private String name;         // 设施名称
    private String type;         // 设施类型
    private String address;      // 设施地址
    private double longitude;    // 经度
    private double latitude;     // 纬度
    private double distance;     // 与中心点的距离（米）
    private String category;     // 设施类别
    private String telephone;    // 联系电话
    
    public NearbyFacility() {
    }
    
    public NearbyFacility(String id, String name, String type, String address, 
                         double longitude, double latitude, double distance,
                         String category, String telephone) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.address = address;
        this.longitude = longitude;
        this.latitude = latitude;
        this.distance = distance;
        this.category = category;
        this.telephone = telephone;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    
    public double getDistance() {
        return distance;
    }
    
    public void setDistance(double distance) {
        this.distance = distance;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getTelephone() {
        return telephone;
    }
    
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
} 