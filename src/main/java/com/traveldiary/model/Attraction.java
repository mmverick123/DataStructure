package com.traveldiary.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "attractions")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Attraction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank
    @Size(max = 50)
    private String category;

    @Size(max = 200)
    private String location;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "keywords")
    private String keywords; // 用逗号分隔的关键词

    @Column(name = "total_views")
    private Long totalViews = 0L; // 相关日记的总阅读量

    @Column(name = "average_rating")
    private Double averageRating = 0.0; // 相关日记的平均评分

    @Column(name = "diary_count")
    private Integer diaryCount = 0; // 相关日记数量

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Transient // 不保存到数据库中，仅用于传输数据给前端
    private List<String> imageUrls = new ArrayList<>();

    public Attraction() {
    }

    public Attraction(String name, String description, String category, String location, String keywords) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.location = location;
        this.keywords = keywords;
    }
    
    public Attraction(String name, String description, String category, String location, 
                     Double longitude, Double latitude, String keywords) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.location = location;
        this.longitude = longitude;
        this.latitude = latitude;
        this.keywords = keywords;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 更新统计数据
    public void updateStatistics(Long totalViews, Double averageRating, Integer diaryCount) {
        this.totalViews = totalViews;
        this.averageRating = averageRating;
        this.diaryCount = diaryCount;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
    
    public Double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    
    public Double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public Long getTotalViews() {
        return totalViews;
    }

    public void setTotalViews(Long totalViews) {
        this.totalViews = totalViews;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getDiaryCount() {
        return diaryCount;
    }

    public void setDiaryCount(Integer diaryCount) {
        this.diaryCount = diaryCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public List<String> getImageUrls() {
        return imageUrls;
    }
    
    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
    
    public void addImageUrl(String imageUrl) {
        if (this.imageUrls == null) {
            this.imageUrls = new ArrayList<>();
        }
        this.imageUrls.add(imageUrl);
    }
} 