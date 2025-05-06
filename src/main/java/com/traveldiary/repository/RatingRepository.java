package com.traveldiary.repository;

import com.traveldiary.model.Diary;
import com.traveldiary.model.Rating;
import com.traveldiary.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByUserAndDiary(User user, Diary diary);
} 