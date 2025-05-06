package com.traveldiary.service;

import com.traveldiary.model.Diary;
import com.traveldiary.model.Rating;
import com.traveldiary.model.User;

import java.util.Optional;

public interface RatingService {
    Rating rateDiary(Diary diary, User user, double score);
    
    Optional<Rating> getRatingByUserAndDiary(User user, Diary diary);
    
    void deleteRating(Long id);
} 