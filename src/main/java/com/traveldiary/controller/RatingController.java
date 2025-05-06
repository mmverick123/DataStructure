package com.traveldiary.controller;

import com.traveldiary.model.Diary;
import com.traveldiary.model.Rating;
import com.traveldiary.model.User;
import com.traveldiary.payload.request.RatingRequest;
import com.traveldiary.payload.response.MessageResponse;
import com.traveldiary.security.services.UserDetailsImpl;
import com.traveldiary.service.DiaryService;
import com.traveldiary.service.RatingService;
import com.traveldiary.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/ratings")
public class RatingController {
    @Autowired
    private RatingService ratingService;

    @Autowired
    private DiaryService diaryService;

    @Autowired
    private UserService userService;

    @PostMapping("/{diaryId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> rateDiary(
            @PathVariable Long diaryId,
            @Valid @RequestBody RatingRequest ratingRequest) {
        
        // 检查日记是否存在
        Optional<Diary> optionalDiary = diaryService.getDiaryById(diaryId);
        if (!optionalDiary.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("错误: 日记不存在!"));
        }
        
        Diary diary = optionalDiary.get();
        
        // 获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        Optional<User> optionalUser = userService.getUserById(userDetails.getId());
        if (!optionalUser.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("错误: 用户不存在!"));
        }
        
        User user = optionalUser.get();
        
        // 用户不能为自己的日记评分
        if (diary.getUser().getId().equals(user.getId())) {
            return ResponseEntity.badRequest().body(new MessageResponse("错误: 不能为自己的日记评分!"));
        }
        
        // 添加评分
        Rating rating = ratingService.rateDiary(diary, user, ratingRequest.getScore());
        return ResponseEntity.ok(rating);
    }

    @DeleteMapping("/{ratingId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteRating(@PathVariable Long ratingId) {
        // 获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // 查找评分
        Optional<Rating> optionalRating = ratingService.getRatingByUserAndDiary(
                userService.getUserById(userDetails.getId()).orElse(null),
                diaryService.getDiaryById(ratingId).orElse(null));
        
        if (!optionalRating.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("错误: 评分不存在!"));
        }
        
        ratingService.deleteRating(ratingId);
        return ResponseEntity.ok(new MessageResponse("评分删除成功!"));
    }
} 