package com.traveldiary.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.traveldiary.model.dto.PromptRequest;
import com.traveldiary.service.ImageGenerationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/image-generation")
public class ImageGenerationController {

    private final ImageGenerationService imageGenerationService;

    @Autowired
    public ImageGenerationController(ImageGenerationService imageGenerationService) {
        this.imageGenerationService = imageGenerationService;
    }

    /**
     * 根据提示词生成图片
     * 
     * @param promptRequest 包含提示词的请求体
     * @return 包含图片URL的响应
     */
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> generateImageFromPrompt(@Valid @RequestBody PromptRequest promptRequest) {
        try {
            Map<String, Object> result = imageGenerationService.generateImageFromPrompt(promptRequest.getPrompt());
            
            if (result.containsKey("success") && !(boolean)result.get("success")) {
                // 如果API调用失败
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", (String)result.getOrDefault("error", "图片生成失败"));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
            
            // 成功生成图片
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            // 提示词为空
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            // 其他错误
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "图片生成过程中发生错误: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 根据提示词生成图片并直接返回图片数据
     * 
     * @param promptRequest 包含提示词的请求体
     * @return 图片数据的二进制流或重定向到图片URL
     */
    @PostMapping(value = "/image", produces = MediaType.IMAGE_JPEG_VALUE)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getGeneratedImageFromPrompt(@Valid @RequestBody PromptRequest promptRequest) {
        try {
            Map<String, Object> result = imageGenerationService.generateImageFromPrompt(promptRequest.getPrompt());
            
            if (result.containsKey("imageUrl")) {
                // 重定向到图片URL
                return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
                        .header("Location", (String)result.get("imageUrl"))
                        .build();
            } else {
                // 生成失败
                String errorMsg = (String)result.getOrDefault("error", "图片生成失败");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.TEXT_PLAIN)
                        .body(errorMsg.getBytes());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(("图片生成错误: " + e.getMessage()).getBytes());
        }
    }

    /**
     * 根据日记ID生成图片
     * 
     * @param diaryId 日记ID
     * @return 包含图片URL或Base64编码的响应
     */
    @GetMapping("/diary/{diaryId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> generateImageFromDiary(@PathVariable Long diaryId) {
        try {
            Map<String, Object> result = imageGenerationService.generateImageFromDiary(diaryId);
            
            if (result.containsKey("success") && !(boolean)result.get("success")) {
                // 如果API调用失败
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", (String)result.getOrDefault("error", "图片生成失败"));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
            
            // 成功生成图片
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            // 日记不存在
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            // 其他错误
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "图片生成过程中发生错误: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 根据日记ID生成图片并直接返回图片数据
     * 
     * @param diaryId 日记ID
     * @return 图片数据的二进制流
     */
    @GetMapping(value = "/diary/{diaryId}/image", produces = MediaType.IMAGE_JPEG_VALUE)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getGeneratedImage(@PathVariable Long diaryId) {
        try {
            Map<String, Object> result = imageGenerationService.generateImageFromDiary(diaryId);
            
            if (result.containsKey("imageUrl")) {
                // 重定向到图片URL
                return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
                        .header("Location", (String)result.get("imageUrl"))
                        .build();
            } else {
                // 生成失败
                String errorMsg = (String)result.getOrDefault("error", "图片生成失败");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.TEXT_PLAIN)
                        .body(errorMsg.getBytes());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(("图片生成错误: " + e.getMessage()).getBytes());
        }
    }
} 