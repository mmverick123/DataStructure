package com.traveldiary.service;

import java.util.List;
import java.util.Optional;

import com.traveldiary.model.Attraction;
import com.traveldiary.payload.request.AttractionSearchRequest;

public interface AttractionService {
    
    /**
     * 获取所有景点
     */
    List<Attraction> getAllAttractions();
    
    /**
     * 根据ID获取景点
     */
    Optional<Attraction> getAttractionById(Long id);
    
    /**
     * 创建新景点
     */
    Attraction createAttraction(Attraction attraction);
    
    /**
     * 更新景点信息
     */
    Attraction updateAttraction(Long id, Attraction attraction);
    
    /**
     * 删除景点
     */
    void deleteAttraction(Long id);
    
    /**
     * 搜索景点
     */
    List<Attraction> searchAttractions(AttractionSearchRequest request);
    
    /**
     * 获取推荐景点（按热度）
     */
    List<Attraction> getRecommendationsByViews(int limit);
    
    /**
     * 获取推荐景点（按评价）
     */
    List<Attraction> getRecommendationsByRating(int limit);
    
    /**
     * 获取推荐景点（综合评分）
     */
    List<Attraction> getRecommendationsByCompositeScore(int limit, double viewsWeight, double ratingWeight);
    
    /**
     * 更新景点统计数据（基于相关日记）
     */
    void updateAttractionStatistics();
    
    /**
     * 更新单个景点的统计数据
     */
    void updateAttractionStatistics(Long attractionId);
} 