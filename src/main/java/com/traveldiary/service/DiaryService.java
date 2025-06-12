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
    
    List<Diary> searchDiariesByContent(String keyword);
    
    Diary updateDiary(Diary diary);
    
    void deleteDiary(Long id);
    
    Diary incrementDiaryViews(Long id);
    
    /**
     * 压缩日记内容
     * @param diary 需要压缩内容的日记
     * @return 压缩后的日记
     */
    Diary compressDiaryContent(Diary diary);
    
    /**
     * 解压缩日记内容
     * @param diary 需要解压内容的日记
     * @return 解压后的日记
     */
    Diary decompressDiaryContent(Diary diary);
} 