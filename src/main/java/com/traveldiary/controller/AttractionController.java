package com.traveldiary.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.traveldiary.model.Attraction;
import com.traveldiary.payload.request.AttractionSearchRequest;
import com.traveldiary.service.AttractionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/attractions")
public class AttractionController {

    @Autowired
    private AttractionService attractionService;

    /**
     * 获取所有景点
     */
    @GetMapping
    public ResponseEntity<List<Attraction>> getAllAttractions() {
        List<Attraction> attractions = attractionService.getAllAttractions();
        return ResponseEntity.ok(attractions);
    }

    /**
     * 根据ID获取景点
     */
    @GetMapping("/{id}")
    public ResponseEntity<Attraction> getAttractionById(@PathVariable Long id) {
        return attractionService.getAttractionById(id)
                .map(attraction -> ResponseEntity.ok(attraction))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 创建新景点
     */
    @PostMapping
    public ResponseEntity<Attraction> createAttraction(@Valid @RequestBody Attraction attraction) {
        Attraction createdAttraction = attractionService.createAttraction(attraction);
        return ResponseEntity.ok(createdAttraction);
    }

    /**
     * 更新景点信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<Attraction> updateAttraction(@PathVariable Long id, 
                                                     @Valid @RequestBody Attraction attraction) {
        try {
            Attraction updatedAttraction = attractionService.updateAttraction(id, attraction);
            return ResponseEntity.ok(updatedAttraction);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 删除景点
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttraction(@PathVariable Long id) {
        attractionService.deleteAttraction(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 搜索景点
     */
    @PostMapping("/search")
    public ResponseEntity<List<Attraction>> searchAttractions(@Valid @RequestBody AttractionSearchRequest request) {
        List<Attraction> attractions = attractionService.searchAttractions(request);
        return ResponseEntity.ok(attractions);
    }

    /**
     * 简单搜索景点（GET方式）
     */
    @GetMapping("/search")
    public ResponseEntity<List<Attraction>> searchAttractionsSimple(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "views") String sortBy,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "true") boolean useTopK) {
        
        AttractionSearchRequest request = new AttractionSearchRequest();
        request.setSearchTerm(searchTerm);
        request.setCategory(category);
        request.setSortBy(sortBy);
        request.setLimit(limit);
        request.setUseTopK(useTopK);
        
        List<Attraction> attractions = attractionService.searchAttractions(request);
        return ResponseEntity.ok(attractions);
    }

    /**
     * 获取热门推荐景点（按热度）
     */
    @GetMapping("/recommendations/popular")
    public ResponseEntity<List<Attraction>> getPopularRecommendations(
            @RequestParam(defaultValue = "10") int limit) {
        List<Attraction> attractions = attractionService.getRecommendationsByViews(limit);
        return ResponseEntity.ok(attractions);
    }

    /**
     * 获取高评分推荐景点（按评价）
     */
    @GetMapping("/recommendations/top-rated")
    public ResponseEntity<List<Attraction>> getTopRatedRecommendations(
            @RequestParam(defaultValue = "10") int limit) {
        List<Attraction> attractions = attractionService.getRecommendationsByRating(limit);
        return ResponseEntity.ok(attractions);
    }

    /**
     * 获取综合推荐景点（综合评分）
     */
    @GetMapping("/recommendations/composite")
    public ResponseEntity<List<Attraction>> getCompositeRecommendations(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0.6") double viewsWeight,
            @RequestParam(defaultValue = "0.4") double ratingWeight) {
        List<Attraction> attractions = attractionService.getRecommendationsByCompositeScore(
                limit, viewsWeight, ratingWeight);
        return ResponseEntity.ok(attractions);
    }

    /**
     * 更新所有景点统计数据
     */
    @PostMapping("/update-statistics")
    public ResponseEntity<String> updateAllStatistics() {
        attractionService.updateAttractionStatistics();
        return ResponseEntity.ok("景点统计数据更新完成");
    }

    /**
     * 更新单个景点统计数据
     */
    @PostMapping("/{id}/update-statistics")
    public ResponseEntity<String> updateAttractionStatistics(@PathVariable Long id) {
        attractionService.updateAttractionStatistics(id);
        return ResponseEntity.ok("景点统计数据更新完成");
    }
} 