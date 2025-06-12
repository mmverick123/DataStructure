package com.traveldiary.service;

import java.util.Optional;

import com.traveldiary.model.User;

public interface UserService {
    User registerUser(User user);
    
    Optional<User> getUserById(Long id);
    
    Optional<User> getUserByUsername(String username);
    
    Optional<User> getUserByEmail(String email);
    
    User updateUser(User user);
    
    void deleteUser(Long id);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    boolean changePassword(Long userId, String currentPassword, String newPassword);
} 