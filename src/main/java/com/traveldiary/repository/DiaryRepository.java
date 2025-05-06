package com.traveldiary.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.traveldiary.model.Diary;
import com.traveldiary.model.User;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
    Page<Diary> findByUser(User user, Pageable pageable);
    List<Diary> findByUser(User user);
    
    Page<Diary> findAllByOrderByViewsDesc(Pageable pageable);
    
    @Query("SELECT d FROM Diary d LEFT JOIN FETCH d.user LEFT JOIN FETCH d.mediaList LEFT JOIN FETCH d.ratings ORDER BY d.views DESC")
    List<Diary> findAllByOrderByViewsDesc();
    
    Page<Diary> findAllByOrderByAverageRatingDesc(Pageable pageable);
    
    @Query("SELECT d FROM Diary d LEFT JOIN FETCH d.user LEFT JOIN FETCH d.mediaList LEFT JOIN FETCH d.ratings ORDER BY d.averageRating DESC")
    List<Diary> findAllByOrderByAverageRatingDesc();
    
    @Override
    @EntityGraph(attributePaths = {"user", "mediaList", "ratings"})
    List<Diary> findAll();
    
    @Override
    @EntityGraph(attributePaths = {"user", "mediaList", "ratings"})
    Optional<Diary> findById(Long id);
} 