package com.traveldiary.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.traveldiary.model.dto.NearbyFacilityByLocationRequest;
import com.traveldiary.model.dto.NearbyFacilityResponse;
import com.traveldiary.service.FacilityService;

import jakarta.validation.Valid;

/**
 * 设施查询控制器
 */
@RestController
@RequestMapping("/api/facilities")
public class FacilityController {
    
    private final FacilityService facilityService;
    
    @Autowired
    public FacilityController(FacilityService facilityService) {
        this.facilityService = facilityService;
    }
    
    /**
     * 根据经纬度查询附近设施
     */
    @PostMapping("/nearby/search")
    public ResponseEntity<NearbyFacilityResponse> searchNearbyFacilities(
            @Valid @RequestBody NearbyFacilityByLocationRequest request) {
        
        NearbyFacilityResponse response = facilityService.findNearbyFacilitiesByLocation(request);
        return ResponseEntity.ok(response);
    }
} 