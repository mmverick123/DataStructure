package com.traveldiary.service.impl;

import com.traveldiary.model.Diary;
import com.traveldiary.model.User;
import com.traveldiary.repository.DiaryRepository;
import com.traveldiary.service.DiaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
        return diaryRepository.findAllByOrderByViewsDesc();
    }

    @Override
    public List<Diary> getDiariesOrderByRating() {
        return diaryRepository.findAllByOrderByAverageRatingDesc();
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