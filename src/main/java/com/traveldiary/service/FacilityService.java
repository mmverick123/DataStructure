package com.traveldiary.service;

import com.traveldiary.model.dto.NearbyFacilityByLocationRequest;
import com.traveldiary.model.dto.NearbyFacilityResponse;

/**
 * 设施查询服务接口
 */
public interface FacilityService {
    
    /**
     * 根据经纬度查询附近设施
     * @param request 查询请求
     * @return 设施查询结果
     */
    NearbyFacilityResponse findNearbyFacilitiesByLocation(NearbyFacilityByLocationRequest request);
} 