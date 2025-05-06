package com.traveldiary.service.impl;

import com.traveldiary.model.Diary;
import com.traveldiary.model.Rating;
import com.traveldiary.model.User;
import com.traveldiary.repository.DiaryRepository;
import com.traveldiary.repository.RatingRepository;
import com.traveldiary.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final DiaryRepository diaryRepository;

    @Autowired
    public RatingServiceImpl(RatingRepository ratingRepository, DiaryRepository diaryRepository) {
        this.ratingRepository = ratingRepository;
        this.diaryRepository = diaryRepository;
    }

    @Override
    @Transactional
    public Rating rateDiary(Diary diary, User user, double score) {
        Optional<Rating> existingRating = getRatingByUserAndDiary(user, diary);

        if (existingRating.isPresent()) {
            Rating rating = existingRating.get();
            double oldScore = rating.getScore();
            
            // 更新评分
            rating.setScore(score);
            
            // 从日记平均评分中移除旧评分，然后添加新评分
            diary.removeRating(oldScore);
            diary.updateRating(score);
            
            // 保存日记和评分
            diaryRepository.save(diary);
            return ratingRepository.save(rating);
        } else {
            // 创建新评分
            Rating rating = new Rating();
            rating.setUser(user);
            rating.setDiary(diary);
            rating.setScore(score);
            
            // 更新日记的平均评分
            diary.updateRating(score);
            
            // 保存日记和评分
            diaryRepository.save(diary);
            return ratingRepository.save(rating);
        }
    }

    @Override
    public Optional<Rating> getRatingByUserAndDiary(User user, Diary diary) {
        return ratingRepository.findByUserAndDiary(user, diary);
    }

    @Override
    @Transactional
    public void deleteRating(Long id) {
        Optional<Rating> optionalRating = ratingRepository.findById(id);
        
        if (optionalRating.isPresent()) {
            Rating rating = optionalRating.get();
            Diary diary = rating.getDiary();
            
            // 从日记平均评分中移除该评分
            diary.removeRating(rating.getScore());
            
            // 保存日记并删除评分
            diaryRepository.save(diary);
            ratingRepository.deleteById(id);
        }
    }
} 