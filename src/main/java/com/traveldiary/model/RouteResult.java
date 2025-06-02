package com.traveldiary.model;

import java.util.List;

/**
 * 路径规划结果类
 */
public class RouteResult {
    private Location start;                // 起点
    private Location end;                  // 终点
    private List<String> pathNames;        // 路径名称列表
    private List<Location> waypoints;      // 途径点
    private double distance;               // 距离（米）
    private int duration;                  // 预计耗时（秒）

    public RouteResult() {
    }

    public RouteResult(Location start, Location end, List<String> pathNames, List<Location> waypoints, double distance, int duration) {
        this.start = start;
        this.end = end;
        this.pathNames = pathNames;
        this.waypoints = waypoints;
        this.distance = distance;
        this.duration = duration;
    }

    public Location getStart() {
        return start;
    }

    public void setStart(Location start) {
        this.start = start;
    }

    public Location getEnd() {
        return end;
    }

    public void setEnd(Location end) {
        this.end = end;
    }

    public List<String> getPathNames() {
        return pathNames;
    }

    public void setPathNames(List<String> pathNames) {
        this.pathNames = pathNames;
    }

    public List<Location> getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(List<Location> waypoints) {
        this.waypoints = waypoints;
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
} 