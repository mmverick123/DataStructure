package com.traveldiary.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
import com.traveldiary.model.Media;
import com.traveldiary.model.User;
import com.traveldiary.payload.request.DiaryRequest;
import com.traveldiary.payload.request.ImageUrlRequest;
import com.traveldiary.payload.response.MessageResponse;
import com.traveldiary.security.services.UserDetailsImpl;
import com.traveldiary.service.DiaryService;
import com.traveldiary.service.MediaService;
import com.traveldiary.service.UserService;
import com.traveldiary.utils.SearchUtils;

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

    @Autowired
    private MediaService mediaService;

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
                    SearchUtils.sortByViewsDesc(diaries);
                } else if (orderType.equals("rating")) {
                    SearchUtils.sortByRatingDesc(diaries);
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
                    SearchUtils.sortByViewsDesc(diaries);
                } else if (orderType.equals("rating")) {
                    SearchUtils.sortByRatingDesc(diaries);
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
                    SearchUtils.sortByViewsDesc(diaries);
                } else if (orderType.equals("rating")) {
                    SearchUtils.sortByRatingDesc(diaries);
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

    /**
     * 保存图片URL到日记
     * 
     * @param diaryId 日记ID
     * @param imageUrlRequest 包含图片URL的请求体
     * @return 保存结果
     */
    @PostMapping("/{diaryId}/imageUrl")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> saveImageUrl(
            @PathVariable Long diaryId,
            @Valid @RequestBody ImageUrlRequest imageUrlRequest) {
        
        try {
            // 检查日记是否存在
            Optional<Diary> optionalDiary = diaryService.getDiaryById(diaryId);
            if (!optionalDiary.isPresent()) {
                return ResponseEntity.badRequest().body(new MessageResponse("错误: 日记不存在!"));
            }
            
            Diary diary = optionalDiary.get();
            
            // 检查当前用户是否是日记的作者
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            if (!diary.getUser().getId().equals(userDetails.getId())) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("错误: 无权为他人的日记保存图片URL!"));
            }
            
            // 保存图片URL
            String imageUrl = imageUrlRequest.getImageUrl();
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("错误: 图片URL不能为空!"));
            }
            
            // 从URL下载图片并保存到本地
            logger.info("从URL下载并保存图片: {}", imageUrl);
            Media savedMedia = mediaService.downloadAndSaveImage(imageUrl, diary);
            
            return ResponseEntity.ok(savedMedia);
        } catch (Exception e) {
            logger.error("保存图片URL失败", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("错误: 保存图片URL失败! " + e.getMessage()));
        }
    }

    /**
     * 高级搜索接口 - 同时搜索标题和内容
     */
    @GetMapping("/search/advanced")
    public ResponseEntity<?> advancedSearch(
            @RequestParam String keyword,
            @RequestParam(required = false) String orderType) {
        
        try {
            logger.debug("高级搜索关键词 '{}' 的日记，排序方式: {}", keyword, orderType);
            
            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new MessageResponse("搜索关键词不能为空"));
            }
            
            // 获取所有日记
            List<Diary> allDiaries = diaryService.getAllDiaries();
            List<Diary> result = new ArrayList<>();
            
            // 分别搜索标题和内容
            List<Diary> titleMatches = SearchUtils.searchByTitle(allDiaries, keyword);
            List<Diary> contentMatches = SearchUtils.searchByContent(allDiaries, keyword);
            
            // 合并结果（去重）
            Set<Long> addedIds = new HashSet<>();
            
            // 首先添加标题匹配的（优先级更高）
            for (Diary diary : titleMatches) {
                result.add(diary);
                addedIds.add(diary.getId());
            }
            
            // 然后添加内容匹配但标题未匹配的
            for (Diary diary : contentMatches) {
                if (!addedIds.contains(diary.getId())) {
                    result.add(diary);
                    addedIds.add(diary.getId());
                }
            }
            
            // 应用排序
            if (orderType != null) {
                if (orderType.equals("views")) {
                    SearchUtils.sortByViewsDesc(result);
                } else if (orderType.equals("rating")) {
                    SearchUtils.sortByRatingDesc(result);
                } else {
                    // 默认按创建时间降序排序
                    SearchUtils.sortByCreatedTimeDesc(result);
                }
            } else {
                // 默认按创建时间降序排序
                SearchUtils.sortByCreatedTimeDesc(result);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("高级搜索时出错: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(new MessageResponse("搜索处理过程中发生错误: " + e.getMessage()));
        }
    }
} 