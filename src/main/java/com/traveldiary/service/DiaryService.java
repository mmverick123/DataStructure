package com.traveldiary.service;

import com.traveldiary.model.Diary;
import com.traveldiary.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface DiaryService {
    Diary createDiary(Diary diary, User user);
    
    Optional<Diary> getDiaryById(Long id);
    
    Page<Diary> getDiariesByUser(User user, Pageable pageable);
    
    Page<Diary> getAllDiaries(Pageable pageable);
    
    Page<Diary> getDiariesOrderByViews(Pageable pageable);
    
    Page<Diary> getDiariesOrderByRating(Pageable pageable);
    
    Diary updateDiary(Diary diary);
    
    void deleteDiary(Long id);
    
    Diary incrementDiaryViews(Long id);
} 