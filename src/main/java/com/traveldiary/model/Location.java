package com.traveldiary.model;

/**
 * 位置信息类
 */
public class Location {
    private String name;        // 位置名称
    private String address;     // 详细地址
    private double longitude;   // 经度
    private double latitude;    // 纬度
    private String poiId;       // 高德POI ID

    public Location() {
    }

    public Location(String name, String address, double longitude, double latitude, String poiId) {
        this.name = name;
        this.address = address;
        this.longitude = longitude;
        this.latitude = latitude;
        this.poiId = poiId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getPoiId() {
        return poiId;
    }

    public void setPoiId(String poiId) {
        this.poiId = poiId;
    }
} 