package com.traveldiary.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.traveldiary.model.Diary;
import com.traveldiary.model.User;
import com.traveldiary.repository.DiaryRepository;
import com.traveldiary.service.DiaryService;
import com.traveldiary.utils.GzipUtils;
import com.traveldiary.utils.SearchUtils;

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
        // 在保存前压缩日记内容
        compressDiaryContent(diary);
        return diaryRepository.save(diary);
    }

    @Override
    public Optional<Diary> getDiaryById(Long id) {
        Optional<Diary> diaryOpt = diaryRepository.findById(id);
        // 解压缩内容后返回
        return diaryOpt.map(this::decompressDiaryContent);
    }

    @Override
    public List<Diary> getDiariesByUser(User user) {
        List<Diary> diaries = diaryRepository.findByUser(user);
        // 解压缩所有日记内容后返回
        return diaries.stream().map(this::decompressDiaryContent).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<Diary> getAllDiaries() {
        List<Diary> diaries = diaryRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        // 解压缩所有日记内容后返回
        return diaries.stream().map(this::decompressDiaryContent).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<Diary> getDiariesOrderByViews() {
        List<Diary> diaries = diaryRepository.findAll();
        // 先解压缩内容，再排序
        diaries = diaries.stream().map(this::decompressDiaryContent).collect(java.util.stream.Collectors.toList());
        SearchUtils.sortByViewsDesc(diaries);
        return diaries;
    }

    @Override
    public List<Diary> getDiariesOrderByRating() {
        List<Diary> diaries = diaryRepository.findAll();
        // 先解压缩内容，再排序
        diaries = diaries.stream().map(this::decompressDiaryContent).collect(java.util.stream.Collectors.toList());
        SearchUtils.sortByRatingDesc(diaries);
        return diaries;
    }

    @Override
    public List<Diary> getDiariesOrderByTitle(String keyword) {
        List<Diary> diaries = diaryRepository.findAll();
        // 先解压缩内容，再搜索和排序
        diaries = diaries.stream().map(this::decompressDiaryContent).collect(java.util.stream.Collectors.toList());
        List<Diary> result = SearchUtils.searchByTitle(diaries, keyword);
        SearchUtils.sortByTitle(result);
        return result;
    }

    @Override
    public List<Diary> getDiariesByLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Diary> diaries = diaryRepository.findAll();
        // 先解压缩内容
        diaries = diaries.stream().map(this::decompressDiaryContent).collect(java.util.stream.Collectors.toList());
        List<Diary> result = new ArrayList<>();
        
        String lowerLocation = location.toLowerCase();
        for (Diary diary : diaries) {
            String diaryLocation = diary.getLocation();
            if (diaryLocation != null && diaryLocation.toLowerCase().contains(lowerLocation)) {
                result.add(diary);
            }
        }
        
        // 按创建时间降序排序
        SearchUtils.sortByCreatedTimeDesc(result);
        return result;
    }

    @Override
    public List<Diary> searchDiariesByContent(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Diary> diaries = diaryRepository.findAll();
        // 先解压缩内容，再搜索
        diaries = diaries.stream().map(this::decompressDiaryContent).collect(java.util.stream.Collectors.toList());
        List<Diary> result = SearchUtils.searchByContent(diaries, keyword);
        SearchUtils.sortByCreatedTimeDesc(result);
        return result;
    }

    @Override
    @Transactional
    public Diary updateDiary(Diary diary) {
        // 在保存前压缩日记内容
        compressDiaryContent(diary);
        Diary savedDiary = diaryRepository.save(diary);
        // 返回解压缩后的内容给调用者
        return decompressDiaryContent(savedDiary);
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
        Diary savedDiary = diaryRepository.save(diary);
        // 返回解压缩后的内容给调用者
        return decompressDiaryContent(savedDiary);
    }

    @Override
    public Diary compressDiaryContent(Diary diary) {
        if (diary == null) {
            return null;
        }
        
        // 压缩标题
        if (diary.getTitle() != null && !diary.getTitle().isEmpty()) {
            String compressedTitle = GzipUtils.compress(diary.getTitle());
            diary.setTitle(compressedTitle);
        }
        
        // 压缩内容
        if (diary.getContent() != null && !diary.getContent().isEmpty()) {
            String compressedContent = GzipUtils.compress(diary.getContent());
            diary.setContent(compressedContent);
        }
        
        return diary;
    }

    @Override
    public Diary decompressDiaryContent(Diary diary) {
        if (diary == null) {
            return null;
        }
        
        // 解压缩标题
        if (diary.getTitle() != null && !diary.getTitle().isEmpty()) {
            String decompressedTitle = GzipUtils.decompress(diary.getTitle());
            diary.setTitle(decompressedTitle);
        }
        
        // 解压缩内容
        if (diary.getContent() != null && !diary.getContent().isEmpty()) {
            String decompressedContent = GzipUtils.decompress(diary.getContent());
            diary.setContent(decompressedContent);
        }
        
        return diary;
    }
} 