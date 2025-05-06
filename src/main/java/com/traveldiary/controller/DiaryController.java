package com.traveldiary.controller;

import com.traveldiary.model.Diary;
import com.traveldiary.model.User;
import com.traveldiary.payload.request.DiaryRequest;
import com.traveldiary.payload.response.MessageResponse;
import com.traveldiary.security.services.UserDetailsImpl;
import com.traveldiary.service.DiaryService;
import com.traveldiary.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/diaries")
public class DiaryController {
    @Autowired
    private DiaryService diaryService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<?> getAllDiaries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String orderType) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<Diary> diaries;

        if (orderType != null) {
            if (orderType.equals("views")) {
                diaries = diaryService.getDiariesOrderByViews(pageable);
            } else if (orderType.equals("rating")) {
                diaries = diaryService.getDiariesOrderByRating(pageable);
            } else {
                diaries = diaryService.getAllDiaries(pageable);
            }
        } else {
            diaries = diaryService.getAllDiaries(pageable);
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
    public ResponseEntity<?> getDiariesByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Optional<User> user = userService.getUserById(userId);
        
        if (user.isPresent()) {
            Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
            
            Page<Diary> diaries = diaryService.getDiariesByUser(user.get(), pageable);
            return ResponseEntity.ok(diaries);
        } else {
            return ResponseEntity.notFound().build();
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