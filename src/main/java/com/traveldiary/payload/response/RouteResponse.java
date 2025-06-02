package com.traveldiary.payload.response;

import java.util.List;

/**
 * 路径规划响应类
 */
public class RouteResponse {
    private List<String> pathNames;      // 路径名称列表
    private double distance;             // 距离（米）
    private int duration;                // 预计耗时（秒）
    private List<PathPoint> pathPoints;  // 路径点坐标列表

    public RouteResponse() {
    }

    public RouteResponse(List<String> pathNames, double distance, int duration) {
        this.pathNames = pathNames;
        this.distance = distance;
        this.duration = duration;
    }
    
    public RouteResponse(List<String> pathNames, double distance, int duration, List<PathPoint> pathPoints) {
        this.pathNames = pathNames;
        this.distance = distance;
        this.duration = duration;
        this.pathPoints = pathPoints;
    }

    public List<String> getPathNames() {
        return pathNames;
    }

    public void setPathNames(List<String> pathNames) {
        this.pathNames = pathNames;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    public List<PathPoint> getPathPoints() {
        return pathPoints;
    }
    
    public void setPathPoints(List<PathPoint> pathPoints) {
        this.pathPoints = pathPoints;
    }
    
    /**
     * 路径点类，表示路径上的一个经纬度点
     */
    public static class PathPoint {
        private double longitude;  // 经度
        private double latitude;   // 纬度
        
        public PathPoint() {
        }
        
        public PathPoint(double longitude, double latitude) {
            this.longitude = longitude;
            this.latitude = latitude;
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
    }
} 