package com.traveldiary.service;

import java.util.List;
import java.util.Optional;

import com.traveldiary.model.Diary;
import com.traveldiary.model.User;

public interface DiaryService {
    Diary createDiary(Diary diary, User user);
    
    Optional<Diary> getDiaryById(Long id);
    
    List<Diary> getDiariesByUser(User user);
    
    List<Diary> getAllDiaries();
    
    List<Diary> getDiariesOrderByViews();
    
    List<Diary> getDiariesOrderByRating();

    List<Diary> getDiariesOrderByTitle(String keyword);
    
    List<Diary> getDiariesByLocation(String location);
    
    Diary updateDiary(Diary diary);
    
    void deleteDiary(Long id);
    
    Diary incrementDiaryViews(Long id);
} 