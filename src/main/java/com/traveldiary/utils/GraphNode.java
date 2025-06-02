package com.traveldiary.utils;

import java.util.HashMap;
import java.util.Map;

import com.traveldiary.model.Location;

/**
 * 图节点类，用于路径规划算法
 */
public class GraphNode {
    private Location location;                        // 位置信息
    private Map<GraphNode, Edge> neighbors;           // 邻接点和边信息
    
    public GraphNode(Location location) {
        this.location = location;
        this.neighbors = new HashMap<>();
    }
    
    public void addNeighbor(GraphNode neighbor, String roadName, double distance) {
        neighbors.put(neighbor, new Edge(roadName, distance));
    }
    
    public Location getLocation() {
        return location;
    }
    
    public Map<GraphNode, Edge> getNeighbors() {
        return neighbors;
    }
    
    /**
     * 边信息类
     */
    public static class Edge {
        private String roadName;       // 道路名称
        private double distance;       // 距离（米）
        
        public Edge(String roadName, double distance) {
            this.roadName = roadName;
            this.distance = distance;
        }
        
        public String getRoadName() {
            return roadName;
        }
        
        public double getDistance() {
            return distance;
        }
    }
} 