package com.traveldiary.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.traveldiary.model.Location;
import com.traveldiary.utils.GraphNode;

/**
 * 高德地图API服务类
 */
@Service
public class AmapService {

    @Value("${amap.api.key}")
    private String apiKey;

    @Value("${amap.api.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // 缓存查询结果
    private final Map<String, Location> locationCache = new HashMap<>();
    private final Map<String, List<GraphNode>> graphCache = new HashMap<>();

    public AmapService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 根据位置名称获取位置详情
     * @param name 位置名称
     * @return 位置信息
     */
    public Location getLocationByName(String name) {
        if (locationCache.containsKey(name)) {
            return locationCache.get(name);
        }

        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/geocode/geo")
                .queryParam("key", apiKey)
                .queryParam("address", name)
                .queryParam("city", "北京") // 添加城市参数，提高查询精度
                .build()
                .toUriString();

        try {
            System.out.println("正在请求高德API: " + url);
            String response = restTemplate.getForObject(url, String.class);
            System.out.println("高德API响应: " + response);
            
            JsonNode root = objectMapper.readTree(response);

            if (root.path("status").asText().equals("1")) {
                // 如果有结果返回
                if (root.path("count").asInt() > 0) {
                    JsonNode firstResult = root.path("geocodes").get(0);
                    String address = firstResult.path("formatted_address").asText();
                    String[] location = firstResult.path("location").asText().split(",");
                    double longitude = Double.parseDouble(location[0]);
                    double latitude = Double.parseDouble(location[1]);

                    Location result = new Location(name, address, longitude, latitude, "");
                    locationCache.put(name, result);
                    System.out.println("成功解析位置: " + name + " -> " + address + " [" + longitude + "," + latitude + "]");
                    return result;
                } else {
                    System.out.println("位置未找到: " + name + ", 尝试使用POI搜索");
                    // 如果地理编码失败，尝试使用POI搜索
                    return searchByPOI(name);
                }
            } else {
                System.out.println("高德API返回错误: " + root.path("info").asText());
                // 如果地理编码API调用失败，尝试POI搜索
                return searchByPOI(name);
            }
        } catch (Exception e) {
            System.out.println("解析位置时发生异常: " + e.getMessage());
            e.printStackTrace();
            // 异常情况下尝试POI搜索
            return searchByPOI(name);
        }
    }

    /**
     * 使用POI搜索查询位置
     * @param keyword 关键词
     * @return 位置信息
     */
    private Location searchByPOI(String keyword) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/place/text")
                .queryParam("key", apiKey)
                .queryParam("keywords", keyword)
                .queryParam("city", "北京")
                .queryParam("extensions", "base")
                .build()
                .toUriString();

        try {
            System.out.println("尝试POI搜索: " + url);
            String response = restTemplate.getForObject(url, String.class);
            System.out.println("POI搜索响应: " + response);
            
            JsonNode root = objectMapper.readTree(response);

            if (root.path("status").asText().equals("1") && root.path("count").asInt() > 0) {
                JsonNode firstPOI = root.path("pois").get(0);
                String name = firstPOI.path("name").asText();
                String address = firstPOI.path("address").asText();
                String[] location = firstPOI.path("location").asText().split(",");
                double longitude = Double.parseDouble(location[0]);
                double latitude = Double.parseDouble(location[1]);
                String poiId = firstPOI.path("id").asText();

                Location result = new Location(keyword, address, longitude, latitude, poiId);
                locationCache.put(keyword, result);
                System.out.println("POI搜索成功: " + keyword + " -> " + name + " [" + longitude + "," + latitude + "]");
                return result;
            }
        } catch (Exception e) {
            System.out.println("POI搜索异常: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("POI搜索也未找到结果: " + keyword);
        return null;
    }

    /**
     * 获取景区的兴趣点列表
     * @param scenicAreaName 景区名称
     * @return 兴趣点列表
     */
    public List<Location> getScenicAreaPOIs(String scenicAreaName) {
        List<Location> pois = new ArrayList<>();
        Location scenicArea = getLocationByName(scenicAreaName);

        if (scenicArea == null) {
            return pois;
        }

        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/place/around")
                .queryParam("key", apiKey)
                .queryParam("location", scenicArea.getLongitude() + "," + scenicArea.getLatitude())
                .queryParam("keywords", "景点|游览|参观")
                .queryParam("radius", 5000)
                .queryParam("extensions", "all")
                .build()
                .toUriString();

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            if (root.path("status").asText().equals("1")) {
                JsonNode pois_node = root.path("pois");
                for (JsonNode poi : pois_node) {
                    String name = poi.path("name").asText();
                    String address = poi.path("address").asText();
                    String[] location = poi.path("location").asText().split(",");
                    double longitude = Double.parseDouble(location[0]);
                    double latitude = Double.parseDouble(location[1]);
                    String poiId = poi.path("id").asText();

                    pois.add(new Location(name, address, longitude, latitude, poiId));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pois;
    }

    /**
     * 构建两点间路网图
     * @param start 起点
     * @param end 终点
     * @return 图节点列表
     */
    public List<GraphNode> buildRouteGraph(Location start, Location end) {
        String cacheKey = start.getName() + "-" + end.getName();
        if (graphCache.containsKey(cacheKey)) {
            return graphCache.get(cacheKey);
        }

        List<GraphNode> graph = new ArrayList<>();
        Map<String, GraphNode> nodeMap = new HashMap<>();

        // 创建起点和终点节点
        GraphNode startNode = new GraphNode(start);
        GraphNode endNode = new GraphNode(end);
        nodeMap.put(start.getName(), startNode);
        nodeMap.put(end.getName(), endNode);
        graph.add(startNode);
        graph.add(endNode);

        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/direction/driving")
                .queryParam("key", apiKey)
                .queryParam("origin", start.getLongitude() + "," + start.getLatitude())
                .queryParam("destination", end.getLongitude() + "," + end.getLatitude())
                .queryParam("extensions", "all")
                .build()
                .toUriString();

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            if (root.path("status").asText().equals("1")) {
                JsonNode route = root.path("route");
                JsonNode paths = route.path("paths").get(0);
                JsonNode steps = paths.path("steps");

                GraphNode prevNode = startNode;

                for (JsonNode step : steps) {
                    String roadName = step.path("road").asText();
                    if (roadName.isEmpty()) {
                        roadName = "未命名道路";
                    }
                    double distance = step.path("distance").asDouble();
                    String instruction = step.path("instruction").asText();

                    // 每个路段的终点创建一个中间节点
                    JsonNode polyline = step.path("polyline");
                    if (polyline.isTextual() && !polyline.asText().isEmpty()) {
                        String[] points = polyline.asText().split(";");
                        if (points.length > 0) {
                            String[] lastPoint = points[points.length - 1].split(",");
                            double lon = Double.parseDouble(lastPoint[0]);
                            double lat = Double.parseDouble(lastPoint[1]);

                            // 如果是最后一步，直接连接到终点
                            if (step.equals(steps.get(steps.size() - 1))) {
                                prevNode.addNeighbor(endNode, roadName, distance);
                            } else {
                                String nodeName = "Node_" + lon + "_" + lat;
                                GraphNode stepNode;
                                if (nodeMap.containsKey(nodeName)) {
                                    stepNode = nodeMap.get(nodeName);
                                } else {
                                    Location stepLocation = new Location(
                                            nodeName, instruction, lon, lat, ""
                                    );
                                    stepNode = new GraphNode(stepLocation);
                                    nodeMap.put(nodeName, stepNode);
                                    graph.add(stepNode);
                                }

                                prevNode.addNeighbor(stepNode, roadName, distance);
                                prevNode = stepNode;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        graphCache.put(cacheKey, graph);
        return graph;
    }

    /**
     * 构建景区内部路网图
     * @param scenicAreaName 景区名称
     * @param poiList 兴趣点列表
     * @return 图节点列表
     */
    public List<GraphNode> buildScenicAreaGraph(String scenicAreaName, List<Location> poiList) {
        if (graphCache.containsKey(scenicAreaName)) {
            return graphCache.get(scenicAreaName);
        }

        List<GraphNode> graph = new ArrayList<>();
        Map<String, GraphNode> nodeMap = new HashMap<>();

        // 创建所有POI节点
        for (Location poi : poiList) {
            GraphNode node = new GraphNode(poi);
            nodeMap.put(poi.getName(), node);
            graph.add(node);
        }

        // 构建完全连通图
        for (int i = 0; i < poiList.size(); i++) {
            for (int j = i + 1; j < poiList.size(); j++) {
                Location poiA = poiList.get(i);
                Location poiB = poiList.get(j);
                GraphNode nodeA = nodeMap.get(poiA.getName());
                GraphNode nodeB = nodeMap.get(poiB.getName());

                // 计算两点间直线距离（米）
                double distance = calculateDistance(
                        poiA.getLatitude(), poiA.getLongitude(),
                        poiB.getLatitude(), poiB.getLongitude()
                );

                // 模拟道路名称
                String roadName = poiA.getName() + " 到 " + poiB.getName() + " 的游览路径";

                // 双向连接
                nodeA.addNeighbor(nodeB, roadName, distance);
                nodeB.addNeighbor(nodeA, roadName, distance);
            }
        }

        graphCache.put(scenicAreaName, graph);
        return graph;
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