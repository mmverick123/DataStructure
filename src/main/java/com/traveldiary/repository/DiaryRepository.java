package com.traveldiary.repository;

import com.traveldiary.model.Diary;
import com.traveldiary.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
    Page<Diary> findByUser(User user, Pageable pageable);
    
    Page<Diary> findAllByOrderByViewsDesc(Pageable pageable);
    
    Page<Diary> findAllByOrderByAverageRatingDesc(Pageable pageable);
} 