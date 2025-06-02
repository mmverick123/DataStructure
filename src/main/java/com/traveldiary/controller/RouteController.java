package com.traveldiary.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.traveldiary.model.Location;
import com.traveldiary.model.RouteResult;
import com.traveldiary.payload.request.RouteRequest;
import com.traveldiary.payload.response.RouteResponse;
import com.traveldiary.service.AmapService;
import com.traveldiary.utils.GraphNode;
import com.traveldiary.utils.RouteUtils;

/**
 * 路径规划控制器
 */
@RestController
@RequestMapping("/api/routes")
public class RouteController {
    
    @Autowired
    private AmapService amapService;
    
    /**
     * 景区之间的路径规划
     * @param routeRequest 路径规划请求
     * @return 路径规划结果
     */
    @PostMapping("/between-scenic")
    public ResponseEntity<?> betweenScenicRoute(@RequestBody RouteRequest routeRequest) {
        System.out.println("收到路径规划请求: " + routeRequest.getStartLocation() + " 到 " + routeRequest.getEndLocation());
        
        // 根据名称获取位置信息
        Location startLocation = amapService.getLocationByName(routeRequest.getStartLocation());
        Location endLocation = amapService.getLocationByName(routeRequest.getEndLocation());
        
        if (startLocation == null && endLocation == null) {
            return ResponseEntity.badRequest().body("无法解析起点和终点位置，请检查名称是否正确");
        } else if (startLocation == null) {
            return ResponseEntity.badRequest().body("无法解析起点位置: " + routeRequest.getStartLocation());
        } else if (endLocation == null) {
            return ResponseEntity.badRequest().body("无法解析终点位置: " + routeRequest.getEndLocation());
        }
        
        System.out.println("成功获取位置 - 起点: " + startLocation.getName() + "[" + startLocation.getLongitude() + "," + startLocation.getLatitude() + "], " +
                          "终点: " + endLocation.getName() + "[" + endLocation.getLongitude() + "," + endLocation.getLatitude() + "]");
        
        // 构建路网图
        List<GraphNode> graph = amapService.buildRouteGraph(startLocation, endLocation);
        
        // 查找起点和终点节点
        GraphNode startNode = graph.stream()
                .filter(node -> node.getLocation().getName().equals(startLocation.getName()))
                .findFirst()
                .orElse(null);
        
        GraphNode endNode = graph.stream()
                .filter(node -> node.getLocation().getName().equals(endLocation.getName()))
                .findFirst()
                .orElse(null);
        
        if (startNode == null || endNode == null) {
            return ResponseEntity.badRequest().body("路网构建失败，无法找到路径节点");
        }
        
        // 规划最短路径
        RouteResult result = RouteUtils.findShortestPath(graph, startNode, endNode);
        
        if (result == null) {
            return ResponseEntity.badRequest().body("无法找到从 " + startLocation.getName() + " 到 " + endLocation.getName() + " 的有效路径");
        }
        
        // 提取路径上的所有点的经纬度
        List<RouteResponse.PathPoint> pathPoints = new ArrayList<>();
        
        // 添加起点
        pathPoints.add(new RouteResponse.PathPoint(startLocation.getLongitude(), startLocation.getLatitude()));
        
        // 添加中间点
        for (Location location : result.getWaypoints()) {
            pathPoints.add(new RouteResponse.PathPoint(location.getLongitude(), location.getLatitude()));
        }
        
        // 确保终点也添加进去
        if (!result.getWaypoints().isEmpty() && !result.getWaypoints().get(result.getWaypoints().size() - 1).equals(endLocation)) {
            pathPoints.add(new RouteResponse.PathPoint(endLocation.getLongitude(), endLocation.getLatitude()));
        }
        
        // 构建响应
        RouteResponse response = new RouteResponse(
                result.getPathNames(),
                result.getDistance(),
                result.getDuration(),
                pathPoints
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 多点路径规划
     * @param routeRequest 路径规划请求
     * @return 路径规划结果
     */
    @PostMapping("/within-scenic")
    public ResponseEntity<?> multiPointRoute(@RequestBody RouteRequest routeRequest) {
        String startLocationName = routeRequest.getStartLocation();
        List<String> waypointNames = routeRequest.getWaypoints();
        
        if (startLocationName == null || waypointNames == null || waypointNames.isEmpty()) {
            return ResponseEntity.badRequest().body("起点位置或途径点不能为空");
        }
        
        System.out.println("收到多点路径规划请求 - 起点: " + startLocationName + ", 途径点: " + waypointNames);
        
        // 获取起点位置信息
        Location startLocation = amapService.getLocationByName(startLocationName);
        if (startLocation == null) {
            return ResponseEntity.badRequest().body("无法解析起点位置: " + startLocationName);
        }
        
        // 获取所有途径点的位置信息
        List<Location> waypointLocations = new ArrayList<>();
        for (String waypointName : waypointNames) {
            Location location = amapService.getLocationByName(waypointName);
            if (location == null) {
                return ResponseEntity.badRequest().body("无法解析途径点位置: " + waypointName);
            }
            waypointLocations.add(location);
        }
        
        System.out.println("成功获取所有位置信息");
        
        // 构建完整的路线图
        List<GraphNode> allNodes = new ArrayList<>();
        Map<String, GraphNode> nodeMap = new HashMap<>();
        
        // 创建起点和途径点节点
        GraphNode startNode = new GraphNode(startLocation);
        nodeMap.put(startLocation.getName(), startNode);
        allNodes.add(startNode);
        
        List<GraphNode> waypointNodes = new ArrayList<>();
        for (Location location : waypointLocations) {
            GraphNode node = new GraphNode(location);
            nodeMap.put(location.getName(), node);
            allNodes.add(node);
            waypointNodes.add(node);
        }
        
        // 使用RouteUtils.findMultiPointPath规划多点路径的访问顺序
        // 注意：此时节点之间没有边，只是用来确定访问顺序
        List<GraphNode> initialGraph = new ArrayList<>(allNodes);
        for (int i = 0; i < initialGraph.size(); i++) {
            for (int j = i + 1; j < initialGraph.size(); j++) {
                GraphNode node1 = initialGraph.get(i);
                GraphNode node2 = initialGraph.get(j);
                
                // 计算直线距离
                Location loc1 = node1.getLocation();
                Location loc2 = node2.getLocation();
                double distance = calculateDistance(
                    loc1.getLatitude(), loc1.getLongitude(),
                    loc2.getLatitude(), loc2.getLongitude()
                );
                
                // 添加临时边用于路径规划
                String roadName = loc1.getName() + " 到 " + loc2.getName();
                node1.addNeighbor(node2, roadName, distance);
                node2.addNeighbor(node1, roadName, distance);
            }
        }
        
        // 确定经过哪些点以及顺序
        RouteResult orderResult = RouteUtils.findMultiPointPath(initialGraph, startNode, waypointNodes);
        if (orderResult == null) {
            return ResponseEntity.badRequest().body("无法规划多点路径顺序");
        }
        
        // 获取最终的访问顺序
        List<Location> orderedLocations = new ArrayList<>();
        orderedLocations.add(startLocation); // 添加起点
        
        for (Location location : orderResult.getWaypoints()) {
            if (!location.equals(startLocation)) { // 避免重复添加起点
                orderedLocations.add(location);
            }
        }
        
        // 如果最后一个点不是起点，添加起点作为终点
        if (!orderedLocations.get(orderedLocations.size() - 1).equals(startLocation)) {
            orderedLocations.add(startLocation);
        }
        
        System.out.println("确定了访问顺序: " + orderedLocations.stream().map(Location::getName).toList());
        
        // 使用实际路网数据，通过高德地图API分段规划路径
        List<String> allPathNames = new ArrayList<>();
        List<RouteResponse.PathPoint> allPathPoints = new ArrayList<>();
        double totalDistance = 0;
        int totalDuration = 0;
        
        // 添加起点
        allPathPoints.add(new RouteResponse.PathPoint(startLocation.getLongitude(), startLocation.getLatitude()));
        
        // 分段规划路径
        for (int i = 0; i < orderedLocations.size() - 1; i++) {
            Location currentLocation = orderedLocations.get(i);
            Location nextLocation = orderedLocations.get(i + 1);
            
            System.out.println("规划路段: " + currentLocation.getName() + " -> " + nextLocation.getName());
            
            // 使用高德地图API获取实际路网数据
            List<GraphNode> segmentGraph = amapService.buildRouteGraph(currentLocation, nextLocation);
            
            // 查找当前段的起点和终点节点
            GraphNode segmentStart = segmentGraph.stream()
                    .filter(node -> node.getLocation().getName().equals(currentLocation.getName()))
                    .findFirst()
                    .orElse(null);
            
            GraphNode segmentEnd = segmentGraph.stream()
                    .filter(node -> node.getLocation().getName().equals(nextLocation.getName()))
                    .findFirst()
                    .orElse(null);
            
            if (segmentStart == null || segmentEnd == null) {
                System.out.println("警告: 无法在路网中找到起点或终点，使用直线连接");
                // 如果无法获取路网数据，使用直线连接
                String directRoadName = currentLocation.getName() + " 到 " + nextLocation.getName();
                allPathNames.add(directRoadName);
                allPathPoints.add(new RouteResponse.PathPoint(nextLocation.getLongitude(), nextLocation.getLatitude()));
                
                // 计算直线距离
                double directDistance = calculateDistance(
                    currentLocation.getLatitude(), currentLocation.getLongitude(),
                    nextLocation.getLatitude(), nextLocation.getLongitude()
                );
                totalDistance += directDistance;
                totalDuration += (int)(directDistance / 50); // 假设平均速度50米/秒
                continue;
            }
            
            // 使用Dijkstra算法规划该段路径
            RouteResult segmentResult = RouteUtils.findShortestPath(segmentGraph, segmentStart, segmentEnd);
            
            if (segmentResult == null) {
                System.out.println("警告: 无法规划路段，使用直线连接");
                // 如果无法规划路径，使用直线连接
                String directRoadName = currentLocation.getName() + " 到 " + nextLocation.getName();
                allPathNames.add(directRoadName);
                allPathPoints.add(new RouteResponse.PathPoint(nextLocation.getLongitude(), nextLocation.getLatitude()));
                
                // 计算直线距离
                double directDistance = calculateDistance(
                    currentLocation.getLatitude(), currentLocation.getLongitude(),
                    nextLocation.getLatitude(), nextLocation.getLongitude()
                );
                totalDistance += directDistance;
                totalDuration += (int)(directDistance / 50); // 假设平均速度50米/秒
            } else {
                // 添加路径名称
                allPathNames.addAll(segmentResult.getPathNames());
                
                // 添加路径点
                for (Location waypoint : segmentResult.getWaypoints()) {
                    allPathPoints.add(new RouteResponse.PathPoint(waypoint.getLongitude(), waypoint.getLatitude()));
                }
                
                // 累计距离和时间
                totalDistance += segmentResult.getDistance();
                totalDuration += segmentResult.getDuration();
                
                System.out.println("成功规划路段，距离: " + segmentResult.getDistance() + "米");
            }
        }
        
        // 构建最终响应
        RouteResponse response = new RouteResponse(
                allPathNames,
                totalDistance,
                totalDuration,
                allPathPoints
        );
        
        System.out.println("多点路径规划完成，总距离: " + totalDistance + "米，总时间: " + totalDuration + "秒");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 计算两点间的距离（米）
     * 使用Haversine公式计算球面距离
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 地球半径（千米）
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // 转换为米

        return distance;
    }
} 