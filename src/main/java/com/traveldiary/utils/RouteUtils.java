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
            
            for (GraphNode.Neighbor neighbor : current.getNeighbors()) {
                GraphNode neighborNode = neighbor.getNode();
                
                if (visited.contains(neighborNode)) {
                    continue;
                }
                
                double newDistance = distances.get(current) + neighbor.getDistance();
                
                if (newDistance < distances.get(neighborNode)) {
                    distances.put(neighborNode, newDistance);
                    previousNodes.put(neighborNode, current);
                    previousRoads.put(neighborNode, neighbor.getRoadName());
                    
                    // 更新队列
                    queue.remove(neighborNode);
                    queue.add(neighborNode);
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
        // 深度拷贝途径点列表，避免修改原始列表
        List<GraphNode> unvisited = new ArrayList<>(waypoints);
        
        List<String> pathNames = new ArrayList<>();
        List<Location> visitedLocations = new ArrayList<>();
        
        // 添加起点
        visitedLocations.add(start.getLocation());
        pathNames.add(start.getLocation().getName());
        
        double totalDistance = 0;
        GraphNode current = start;
        
        System.out.println("开始多点路径规划，起点: " + start.getLocation().getName());
        System.out.println("途径点: " + unvisited.stream().map(n -> n.getLocation().getName()).toList());
        
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
                // 无法到达任何未访问点，中断循环
                System.out.println("无法到达任何未访问点，中断循环");
                break;
            }
            
            System.out.println("找到最近点: " + nearest.getLocation().getName() + ", 距离: " + minDistance);
            
            // 添加路径名称（如果有）
            if (tempResult.getPathNames() != null && !tempResult.getPathNames().isEmpty()) {
                pathNames.addAll(tempResult.getPathNames());
            }
            
            // 添加途径点（但不包括起点，因为已经添加过了）
            if (tempResult.getWaypoints() != null && !tempResult.getWaypoints().isEmpty()) {
                // 跳过第一个点，因为它是当前点，已经在路径中了
                for (int i = 1; i < tempResult.getWaypoints().size(); i++) {
                    visitedLocations.add(tempResult.getWaypoints().get(i));
                }
            }
            
            // 添加目标点名称
            pathNames.add(nearest.getLocation().getName());
            
            // 累计距离
            totalDistance += minDistance;
            
            // 更新当前点
            current = nearest;
            // 从未访问列表中移除
            unvisited.remove(nearest);
        }
        
        // 返回起点的路径
        if (!current.equals(start)) {
            System.out.println("添加返回起点的路径");
            RouteResult finalLeg = findShortestPath(graph, current, start);
            if (finalLeg != null) {
                // 添加路径名称
                if (finalLeg.getPathNames() != null && !finalLeg.getPathNames().isEmpty()) {
                    pathNames.addAll(finalLeg.getPathNames());
                }
                
                // 添加途径点（但不包括起点，因为已经添加过了）
                if (finalLeg.getWaypoints() != null && !finalLeg.getWaypoints().isEmpty()) {
                    // 跳过第一个点，因为它是当前点，已经在路径中了
                    for (int i = 1; i < finalLeg.getWaypoints().size(); i++) {
                        visitedLocations.add(finalLeg.getWaypoints().get(i));
                    }
                }
                
                // 添加起点名称（作为终点）
                pathNames.add(start.getLocation().getName());
                
                // 累计距离
                totalDistance += finalLeg.getDistance();
            }
        }
        
        // 打印最终路径
        System.out.println("最终路径: " + pathNames);
        System.out.println("总距离: " + totalDistance + "米");
        
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