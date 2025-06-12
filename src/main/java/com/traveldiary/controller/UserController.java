package com.traveldiary.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.traveldiary.model.User;
import com.traveldiary.payload.request.ChangePasswordRequest;
import com.traveldiary.payload.response.MessageResponse;
import com.traveldiary.payload.response.UserInfoResponse;
import com.traveldiary.security.services.UserDetailsImpl;
import com.traveldiary.service.UserService;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        Optional<User> userOpt = userService.getUserById(userDetails.getId());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return ResponseEntity.ok(new UserInfoResponse(
                user.getId(), 
                user.getUsername(), 
                user.getEmail()
            ));
        }
        
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // 验证新密码和确认密码是否一致
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(new MessageResponse("错误: 新密码和确认密码不一致!"));
        }
        
        // 修改密码
        boolean success = userService.changePassword(
            userDetails.getId(), 
            request.getCurrentPassword(), 
            request.getNewPassword()
        );
        
        if (success) {
            return ResponseEntity.ok(new MessageResponse("密码修改成功!"));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("错误: 当前密码不正确!"));
        }
    }
} 