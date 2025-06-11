package com.traveldiary.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.traveldiary.model.Attraction;

@Repository
public interface AttractionRepository extends JpaRepository<Attraction, Long> {
    
    // 按名称查找景点
    List<Attraction> findByNameContainingIgnoreCase(String name);
    
    // 按类别查找景点
    List<Attraction> findByCategoryContainingIgnoreCase(String category);
    
    // 按关键词查找景点
    List<Attraction> findByKeywordsContainingIgnoreCase(String keywords);
    
    // 综合搜索：按名称、类别或关键词查找
    @Query("SELECT a FROM Attraction a WHERE " +
           "LOWER(a.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.category) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.keywords) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Attraction> findBySearchTerm(@Param("searchTerm") String searchTerm);
    
    // 按热度排序（总阅读量降序）
    List<Attraction> findAllByOrderByTotalViewsDesc();
    
    // 按评价排序（平均评分降序）
    List<Attraction> findAllByOrderByAverageRatingDesc();
    
    // 分页查询按热度排序
    Page<Attraction> findAllByOrderByTotalViewsDesc(Pageable pageable);
    
    // 分页查询按评价排序
    Page<Attraction> findAllByOrderByAverageRatingDesc(Pageable pageable);
    
    // 按类别和热度排序
    List<Attraction> findByCategoryContainingIgnoreCaseOrderByTotalViewsDesc(String category);
    
    // 按类别和评价排序
    List<Attraction> findByCategoryContainingIgnoreCaseOrderByAverageRatingDesc(String category);
} 