package com.traveldiary.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.traveldiary.model.dto.NearbyFacility;
import com.traveldiary.model.dto.NearbyFacilityByLocationRequest;
import com.traveldiary.model.dto.NearbyFacilityResponse;
import com.traveldiary.service.FacilityService;
import com.traveldiary.utils.FacilityUtils;

/**
 * 设施查询服务实现类
 */
@Service
public class FacilityServiceImpl implements FacilityService {
    
    private static final Logger logger = LoggerFactory.getLogger(FacilityServiceImpl.class);
    
    @Value("${amap.api.key}")
    private String apiKey;

    @Value("${amap.api.base-url}")
    private String baseUrl;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public FacilityServiceImpl() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public NearbyFacilityResponse findNearbyFacilitiesByLocation(NearbyFacilityByLocationRequest request) {
        // 设置设施类型关键词
        String keywords = request.getFacilityType();
        if (keywords == null || keywords.trim().isEmpty()) {
            keywords = "超市|便利店|卫生间|洗手间|厕所";
        }
        
        // 查询附近设施
        List<NearbyFacility> facilities = searchNearbyFacilities(
            request.getLongitude(), 
            request.getLatitude(), 
            keywords, 
            request.getLimit(),
            request.getRadius()
        );
        
        // 根据距离排序（使用本地排序算法）
        List<NearbyFacility> sortedFacilities = FacilityUtils.sortByDistance(facilities);
        
        // 构建响应结果
        return new NearbyFacilityResponse(
            request.getLongitude(),
            request.getLatitude(),
            request.getFacilityType(),
            sortedFacilities.size(),
            request.getRadius(),
            sortedFacilities
        );
    }
    
    /**
     * 通过高德API查询附近设施
     * @param longitude 经度
     * @param latitude 纬度
     * @param keywords 设施类型关键词
     * @param limit 数量限制
     * @param radius 搜索半径
     * @return 设施列表
     */
    private List<NearbyFacility> searchNearbyFacilities(double longitude, double latitude, 
                                                      String keywords, int limit, int radius) {
        List<NearbyFacility> facilities = new ArrayList<>();
        
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/place/around")
                .queryParam("key", apiKey)
                .queryParam("location", longitude + "," + latitude)
                .queryParam("keywords", keywords)
                .queryParam("radius", radius)  // 搜索半径（米）
                .queryParam("extensions", "all")
                .queryParam("sortrule", "weight")  // 按权重排序，之后在本地根据距离重新排序
                .queryParam("offset", limit)  // 返回的POI数量
                .build()
                .toUriString();
        
        try {
            logger.info("请求高德API查询附近设施: {}", url);
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            
            if (root.path("status").asText().equals("1")) {
                JsonNode pois = root.path("pois");
                for (JsonNode poi : pois) {
                    String id = poi.path("id").asText();
                    String name = poi.path("name").asText();
                    String type = poi.path("type").asText();
                    String address = poi.path("address").asText();
                    String[] location = poi.path("location").asText().split(",");
                    double poiLongitude = Double.parseDouble(location[0]);
                    double poiLatitude = Double.parseDouble(location[1]);
                    double distance = poi.path("distance").asDouble();
                    String category = getFirstCategory(poi.path("type").asText());
                    String telephone = poi.path("tel").asText();
                    
                    NearbyFacility facility = new NearbyFacility(
                        id, name, type, address, 
                        poiLongitude, poiLatitude, distance,
                        category, telephone
                    );
                    facilities.add(facility);
                }
            } else {
                logger.error("高德API返回错误: {}", root.path("info").asText());
            }
        } catch (Exception e) {
            logger.error("查询附近设施时发生异常", e);
        }
        
        return facilities;
    }
    
    /**
     * 从类型字符串中提取第一个分类
     * @param type 类型字符串，例如："餐饮服务;中餐厅;川菜"
     * @return 第一个分类，例如："餐饮服务"
     */
    private String getFirstCategory(String type) {
        if (type == null || type.isEmpty()) {
            return "";
        }
        
        String[] parts = type.split(";");
        return parts.length > 0 ? parts[0] : type;
    }
} 