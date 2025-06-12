package com.traveldiary.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.traveldiary.model.Diary;
import com.traveldiary.model.User;
import com.traveldiary.payload.request.DiaryRequest;
import com.traveldiary.payload.response.MessageResponse;
import com.traveldiary.security.services.UserDetailsImpl;
import com.traveldiary.service.DiaryService;
import com.traveldiary.service.UserService;
import com.traveldiary.utils.QuickSortUtils;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/diaries")
public class DiaryController {
    private static final Logger logger = LoggerFactory.getLogger(DiaryController.class);
    
    @Autowired
    private DiaryService diaryService;

    @Autowired
    private UserService userService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllDiaries(
            @RequestParam(required = false) String orderType) {
        
        List<Diary> diaries;

        if (orderType != null) {
            if (orderType.equals("views")) {
                diaries = diaryService.getDiariesOrderByViews();
            } else if (orderType.equals("rating")) {
                diaries = diaryService.getDiariesOrderByRating();
            } else {
                diaries = diaryService.getAllDiaries();
            }
        } else {
            diaries = diaryService.getAllDiaries();
        }

        return ResponseEntity.ok(diaries);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDiaryById(@PathVariable Long id) {
        Optional<Diary> diary = diaryService.getDiaryById(id);
        
        if (diary.isPresent()) {
            // 增加浏览量
            diaryService.incrementDiaryViews(id);
            return ResponseEntity.ok(diary.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getDiariesByUser(@PathVariable Long userId) {
        Optional<User> user = userService.getUserById(userId);
        
        if (user.isPresent()) {
            List<Diary> diaries = diaryService.getDiariesByUser(user.get());
            return ResponseEntity.ok(diaries);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/diary/search_title/{title}")
    public ResponseEntity<?> getDiariesByTitle(
            @PathVariable String title,
            @RequestParam(required = false) String orderType) {
        
        try {
            logger.debug("搜索标题包含 '{}' 的日记，排序方式: {}", title, orderType);
            List<Diary> diaries = diaryService.getDiariesOrderByTitle(title);
            
            // 检查列表是否为空
            if (diaries != null && !diaries.isEmpty() && orderType != null) {
                if (orderType.equals("views")) {
                    QuickSortUtils.sortByViews(diaries);
                } else if (orderType.equals("rating")) {
                    QuickSortUtils.sortByRating(diaries);
                }
            }
            
            return ResponseEntity.ok(diaries);
        } catch (Exception e) {
            logger.error("按标题搜索日记时出错: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(new MessageResponse("搜索处理过程中发生错误: " + e.getMessage()));
        }
    }
    
    @GetMapping("/location/{location}")
    public ResponseEntity<?> getDiariesByLocation(
            @PathVariable String location,
            @RequestParam(required = false) String orderType) {
        
        try {
            logger.debug("搜索位置包含 '{}' 的日记，排序方式: {}", location, orderType);
            List<Diary> diaries = diaryService.getDiariesByLocation(location);
            
            // 检查列表是否为空
            if (diaries != null && !diaries.isEmpty() && orderType != null) {
                if (orderType.equals("views")) {
                    QuickSortUtils.sortByViews(diaries);
                } else if (orderType.equals("rating")) {
                    QuickSortUtils.sortByRating(diaries);
                }
            }
            
            return ResponseEntity.ok(diaries);
        } catch (Exception e) {
            logger.error("按位置搜索日记时出错: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(new MessageResponse("搜索处理过程中发生错误: " + e.getMessage()));
        }
    }
    
    @GetMapping("/search/content/{keyword}")
    public ResponseEntity<?> searchDiariesByContent(
            @PathVariable String keyword,
            @RequestParam(required = false) String orderType) {
        
        try {
            logger.debug("搜索内容包含 '{}' 的日记，排序方式: {}", keyword, orderType);
            List<Diary> diaries = diaryService.searchDiariesByContent(keyword);
            
            // 检查列表是否为空
            if (diaries != null && !diaries.isEmpty() && orderType != null) {
                if (orderType.equals("views")) {
                    QuickSortUtils.sortByViews(diaries);
                } else if (orderType.equals("rating")) {
                    QuickSortUtils.sortByRating(diaries);
                }
            }
            
            return ResponseEntity.ok(diaries);
        } catch (Exception e) {
            logger.error("按内容搜索日记时出错: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(new MessageResponse("搜索处理过程中发生错误: " + e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createDiary(@Valid @RequestBody DiaryRequest diaryRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        Optional<User> user = userService.getUserById(userDetails.getId());
        
        if (user.isPresent()) {
            Diary diary = new Diary();
            diary.setTitle(diaryRequest.getTitle());
            diary.setContent(diaryRequest.getContent());
            diary.setLocation(diaryRequest.getLocation());
            
            Diary createdDiary = diaryService.createDiary(diary, user.get());
            return ResponseEntity.ok(createdDiary);
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("错误: 用户不存在!"));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateDiary(
            @PathVariable Long id,
            @Valid @RequestBody DiaryRequest diaryRequest) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        Optional<Diary> optionalDiary = diaryService.getDiaryById(id);
        
        if (optionalDiary.isPresent()) {
            Diary diary = optionalDiary.get();
            
            // 检查当前用户是否是日记的作者
            if (!diary.getUser().getId().equals(userDetails.getId())) {
                return ResponseEntity.badRequest().body(new MessageResponse("错误: 无权修改他人的日记!"));
            }
            
            // 更新日记内容
            diary.setTitle(diaryRequest.getTitle());
            diary.setContent(diaryRequest.getContent());
            diary.setLocation(diaryRequest.getLocation());
            
            Diary updatedDiary = diaryService.updateDiary(diary);
            return ResponseEntity.ok(updatedDiary);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteDiary(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        Optional<Diary> optionalDiary = diaryService.getDiaryById(id);
        
        if (optionalDiary.isPresent()) {
            Diary diary = optionalDiary.get();
            
            // 检查当前用户是否是日记的作者
            if (!diary.getUser().getId().equals(userDetails.getId())) {
                return ResponseEntity.badRequest().body(new MessageResponse("错误: 无权删除他人的日记!"));
            }
            
            diaryService.deleteDiary(id);
            return ResponseEntity.ok(new MessageResponse("日记删除成功!"));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
} 