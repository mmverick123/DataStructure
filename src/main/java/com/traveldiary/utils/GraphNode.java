package com.traveldiary.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.traveldiary.model.Location;

/**
 * 图节点类，用于路径规划
 */
public class GraphNode {
    private Location location;
    private Map<GraphNode, Edge> neighbors;
    private double distance;  // 从起点到该节点的距离
    private GraphNode previous;  // 最短路径中的前一个节点

    public GraphNode(Location location) {
        this.location = location;
        this.neighbors = new HashMap<>();
        this.distance = Double.MAX_VALUE;
        this.previous = null;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public GraphNode getPrevious() {
        return previous;
    }

    public void setPrevious(GraphNode previous) {
        this.previous = previous;
    }

    public void addNeighbor(GraphNode node, String roadName, double distance) {
        neighbors.put(node, new Edge(roadName, distance));
    }

    public List<Neighbor> getNeighbors() {
        List<Neighbor> neighborList = new ArrayList<>();
        for (Map.Entry<GraphNode, Edge> entry : neighbors.entrySet()) {
            neighborList.add(new Neighbor(entry.getKey(), entry.getValue()));
        }
        return neighborList;
    }

    public Edge getEdgeTo(GraphNode node) {
        return neighbors.get(node);
    }

    /**
     * 边类，表示两个节点之间的连接
     */
    public static class Edge {
        private String roadName;
        private double distance;

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

    /**
     * 邻居节点类，包含节点和连接边的信息
     */
    public static class Neighbor {
        private GraphNode node;
        private Edge edge;

        public Neighbor(GraphNode node, Edge edge) {
            this.node = node;
            this.edge = edge;
        }

        public GraphNode getNode() {
            return node;
        }

        public String getRoadName() {
            return edge.getRoadName();
        }

        public double getDistance() {
            return edge.getDistance();
        }
    }
} 