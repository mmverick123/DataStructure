package com.traveldiary.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import com.traveldiary.model.Location;
import com.traveldiary.model.RouteResult;

/**
 * 路径规划工具类
 */
public class RouteUtils {
    
    /**
     * Dijkstra算法实现最短路径规划
     * @param graph 图节点列表
     * @param start 起点
     * @param end 终点
     * @return 路径规划结果
     */
    public static RouteResult findShortestPath(List<GraphNode> graph, GraphNode start, GraphNode end) {
        Map<GraphNode, Double> distances = new HashMap<>();
        Map<GraphNode, GraphNode> previousNodes = new HashMap<>();
        Map<GraphNode, String> previousRoads = new HashMap<>();
        PriorityQueue<GraphNode> queue = new PriorityQueue<>(Comparator.comparing(distances::get));
        Set<GraphNode> visited = new HashSet<>();
        
        // 初始化距离
        for (GraphNode node : graph) {
            distances.put(node, Double.MAX_VALUE);
        }
        distances.put(start, 0.0);
        queue.add(start);
        
        while (!queue.isEmpty()) {
            GraphNode current = queue.poll();
            
            if (current.equals(end)) {
                break; // 找到终点
            }
            
            if (visited.contains(current)) {
                continue;
            }
            
            visited.add(current);
            
            for (Map.Entry<GraphNode, GraphNode.Edge> entry : current.getNeighbors().entrySet()) {
                GraphNode neighbor = entry.getKey();
                GraphNode.Edge edge = entry.getValue();
                
                if (visited.contains(neighbor)) {
                    continue;
                }
                
                double newDistance = distances.get(current) + edge.getDistance();
                
                if (newDistance < distances.get(neighbor)) {
                    distances.put(neighbor, newDistance);
                    previousNodes.put(neighbor, current);
                    previousRoads.put(neighbor, edge.getRoadName());
                    
                    // 更新队列
                    queue.remove(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        
        // 构建结果路径
        List<String> pathNames = new ArrayList<>();
        List<Location> waypoints = new ArrayList<>();
        GraphNode current = end;
        
        if (previousNodes.get(current) == null) {
            return null; // 没有找到路径
        }
        
        while (current != start) {
            GraphNode previous = previousNodes.get(current);
            pathNames.add(0, previousRoads.get(current));
            waypoints.add(0, previous.getLocation());
            current = previous;
        }
        
        waypoints.add(end.getLocation());
        
        return new RouteResult(
            start.getLocation(),
            end.getLocation(),
            pathNames,
            waypoints,
            distances.get(end),
            (int)(distances.get(end) / 50) // 假设平均速度50米/秒计算时间
        );
    }
    
    /**
     * 旅行商问题（TSP）算法实现多点路径规划
     * 采用最近邻算法（Nearest Neighbor）的贪心策略
     * @param graph 图节点列表
     * @param start 起点（也是终点）
     * @param waypoints 必须经过的途径点
     * @return 路径规划结果
     */
    public static RouteResult findMultiPointPath(List<GraphNode> graph, GraphNode start, List<GraphNode> waypoints) {
        List<GraphNode> allPoints = new ArrayList<>(waypoints);
        if (!allPoints.contains(start)) {
            allPoints.add(start);
        }
        
        List<String> pathNames = new ArrayList<>();
        List<Location> visitedLocations = new ArrayList<>();
        visitedLocations.add(start.getLocation());
        
        double totalDistance = 0;
        GraphNode current = start;
        Set<GraphNode> unvisited = new HashSet<>(waypoints);
        
        // 使用贪心策略找到路径
        while (!unvisited.isEmpty()) {
            GraphNode nearest = null;
            double minDistance = Double.MAX_VALUE;
            RouteResult tempResult = null;
            
            for (GraphNode next : unvisited) {
                RouteResult result = findShortestPath(graph, current, next);
                
                if (result != null && result.getDistance() < minDistance) {
                    minDistance = result.getDistance();
                    nearest = next;
                    tempResult = result;
                }
            }
            
            if (nearest == null) {
                // 无法到达任何未访问点
                break;
            }
            
            // 更新路径
            pathNames.addAll(tempResult.getPathNames());
            visitedLocations.addAll(tempResult.getWaypoints());
            totalDistance += minDistance;
            
            current = nearest;
            unvisited.remove(nearest);
        }
        
        // 最后返回起点
        if (current != start) {
            RouteResult finalLeg = findShortestPath(graph, current, start);
            if (finalLeg != null) {
                pathNames.addAll(finalLeg.getPathNames());
                visitedLocations.addAll(finalLeg.getWaypoints());
                totalDistance += finalLeg.getDistance();
            }
        }
        
        return new RouteResult(
            start.getLocation(),
            start.getLocation(),
            pathNames,
            visitedLocations,
            totalDistance,
            (int)(totalDistance / 50) // 假设平均速度50米/秒计算时间
        );
    }
} 