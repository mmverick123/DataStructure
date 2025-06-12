package com.traveldiary.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.traveldiary.model.Diary;
import com.traveldiary.model.User;
import com.traveldiary.repository.DiaryRepository;
import com.traveldiary.service.DiaryService;
import com.traveldiary.utils.QuickSortUtils;

@Service
public class DiaryServiceImpl implements DiaryService {

    private final DiaryRepository diaryRepository;

    @Autowired
    public DiaryServiceImpl(DiaryRepository diaryRepository) {
        this.diaryRepository = diaryRepository;
    }

    @Override
    @Transactional
    public Diary createDiary(Diary diary, User user) {
        diary.setUser(user);
        return diaryRepository.save(diary);
    }

    @Override
    public Optional<Diary> getDiaryById(Long id) {
        return diaryRepository.findById(id);
    }

    @Override
    public List<Diary> getDiariesByUser(User user) {
        return diaryRepository.findByUser(user);
    }

    @Override
    public List<Diary> getAllDiaries() {
        return diaryRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Override
    public List<Diary> getDiariesOrderByViews() {
        List<Diary> diaries = diaryRepository.findAll();
        QuickSortUtils.sortByViews(diaries);
        return diaries;
    }

    @Override
    public List<Diary> getDiariesOrderByRating() {
        List<Diary> diaries = diaryRepository.findAll();
        QuickSortUtils.sortByRating(diaries);
        return diaries;
    }

    @Override
    public List<Diary> getDiariesOrderByTitle(String keyword) {
        List<Diary> diaries = diaryRepository.findAll();
        return QuickSortUtils.searchAndSortByTitle(diaries, keyword);
    }

    @Override
    public List<Diary> getDiariesByLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Diary> diaries = diaryRepository.findAll();
        return diaries.stream()
                .filter(diary -> {
                    String diaryLocation = diary.getLocation();
                    return diaryLocation != null && diaryLocation.toLowerCase().contains(location.toLowerCase());
                })
                .sorted((d1, d2) -> d2.getCreatedAt().compareTo(d1.getCreatedAt()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<Diary> searchDiariesByContent(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Diary> diaries = diaryRepository.findAll();
        return QuickSortUtils.searchAndSortByContent(diaries, keyword);
    }

    @Override
    @Transactional
    public Diary updateDiary(Diary diary) {
        return diaryRepository.save(diary);
    }

    @Override
    @Transactional
    public void deleteDiary(Long id) {
        diaryRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Diary incrementDiaryViews(Long id) {
        Diary diary = diaryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Diary not found with id: " + id));
        diary.incrementViews();
        return diaryRepository.save(diary);
    }
} 