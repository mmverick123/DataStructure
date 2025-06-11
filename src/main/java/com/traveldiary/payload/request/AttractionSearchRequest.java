package com.traveldiary.payload.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class AttractionSearchRequest {
    private String searchTerm; // 搜索关键词
    private String category; // 景点类别
    private String sortBy = "views"; // 排序方式：views（热度）或 rating（评价）
    
    @Min(1)
    @Max(50)
    private int limit = 10; // 返回结果数量限制，默认10个
    
    private boolean useTopK = true; // 是否使用Top-K算法，默认true
    
    // 综合评分权重（当sortBy为composite时使用）
    @Min(0)
    @Max(1)
    private double viewsWeight = 0.6; // 热度权重，默认0.6
    
    @Min(0)
    @Max(1)
    private double ratingWeight = 0.4; // 评价权重，默认0.4

    public AttractionSearchRequest() {
    }

    public AttractionSearchRequest(String searchTerm, String sortBy, int limit) {
        this.searchTerm = searchTerm;
        this.sortBy = sortBy;
        this.limit = limit;
    }

    // Getters and Setters
    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public boolean isUseTopK() {
        return useTopK;
    }

    public void setUseTopK(boolean useTopK) {
        this.useTopK = useTopK;
    }

    public double getViewsWeight() {
        return viewsWeight;
    }

    public void setViewsWeight(double viewsWeight) {
        this.viewsWeight = viewsWeight;
    }

    public double getRatingWeight() {
        return ratingWeight;
    }

    public void setRatingWeight(double ratingWeight) {
        this.ratingWeight = ratingWeight;
    }
} 