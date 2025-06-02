package com.traveldiary.payload.request;

import java.util.List;

/**
 * 路径规划请求类
 */
public class RouteRequest {
    private String startLocation;           // 起点名称
    private String endLocation;             // 终点名称
    private List<String> waypoints;         // 途径点名称列表
    private String scenicAreaName;          // 景区名称（景区内部路径规划需要）

    public RouteRequest() {
    }

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(String endLocation) {
        this.endLocation = endLocation;
    }

    public List<String> getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(List<String> waypoints) {
        this.waypoints = waypoints;
    }

    public String getScenicAreaName() {
        return scenicAreaName;
    }

    public void setScenicAreaName(String scenicAreaName) {
        this.scenicAreaName = scenicAreaName;
    }
} 