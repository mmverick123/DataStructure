package com.traveldiary.controller;

import java.util.ArrayList;
import java.util.List;

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
     * 景区内部的多点路径规划
     * @param routeRequest 路径规划请求
     * @return 路径规划结果
     */
    @PostMapping("/within-scenic")
    public ResponseEntity<?> withinScenicRoute(@RequestBody RouteRequest routeRequest) {
        String scenicAreaName = routeRequest.getScenicAreaName();
        List<String> waypointNames = routeRequest.getWaypoints();
        
        if (scenicAreaName == null || waypointNames == null || waypointNames.isEmpty()) {
            return ResponseEntity.badRequest().body("景区名称或途径点不能为空");
        }
        
        // 获取景区内所有兴趣点
        List<Location> poiList = amapService.getScenicAreaPOIs(scenicAreaName);
        
        if (poiList.isEmpty()) {
            return ResponseEntity.badRequest().body("无法获取景区信息");
        }
        
        // 构建景区内路网图
        List<GraphNode> graph = amapService.buildScenicAreaGraph(scenicAreaName, poiList);
        
        // 查找起点（当前位置，假设为第一个兴趣点）和所有途径点
        Location currentLocation = poiList.get(0);
        
        GraphNode currentNode = graph.stream()
                .filter(node -> node.getLocation().getName().equals(currentLocation.getName()))
                .findFirst()
                .orElse(null);
        
        List<GraphNode> waypointNodes = new ArrayList<>();
        for (String waypointName : waypointNames) {
            GraphNode node = graph.stream()
                    .filter(n -> n.getLocation().getName().contains(waypointName))
                    .findFirst()
                    .orElse(null);
            
            if (node != null) {
                waypointNodes.add(node);
            }
        }
        
        if (currentNode == null || waypointNodes.isEmpty()) {
            return ResponseEntity.badRequest().body("无法匹配途径点");
        }
        
        // 规划多点路径
        RouteResult result = RouteUtils.findMultiPointPath(graph, currentNode, waypointNodes);
        
        if (result == null) {
            return ResponseEntity.badRequest().body("无法规划有效路径");
        }
        
        // 提取路径点信息
        List<RouteResponse.PathPoint> pathPoints = new ArrayList<>();
        List<String> scenicSpotNames = new ArrayList<>();
        
        // 添加起点
        pathPoints.add(new RouteResponse.PathPoint(currentLocation.getLongitude(), currentLocation.getLatitude()));
        scenicSpotNames.add(currentLocation.getName());
        
        // 处理途经点
        for (Location location : result.getWaypoints()) {
            pathPoints.add(new RouteResponse.PathPoint(location.getLongitude(), location.getLatitude()));
            
            // 排除非景点的中间路径点
            if (!location.getName().startsWith("Node_") && !scenicSpotNames.contains(location.getName())) {
                scenicSpotNames.add(location.getName());
            }
        }
        
        // 构建响应
        RouteResponse response = new RouteResponse(
                scenicSpotNames,
                result.getDistance(),
                result.getDuration(),
                pathPoints
        );
        
        return ResponseEntity.ok(response);
    }
} 