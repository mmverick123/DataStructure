package com.traveldiary.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.traveldiary.model.Diary;
import com.traveldiary.model.Media;
import com.traveldiary.payload.response.MessageResponse;
import com.traveldiary.security.services.UserDetailsImpl;
import com.traveldiary.service.DiaryService;
import com.traveldiary.service.MediaService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/media")
public class MediaController {
    @Autowired
    private MediaService mediaService;

    @Autowired
    private DiaryService diaryService;

    @PostMapping("/upload/{diaryId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadMedia(
            @PathVariable Long diaryId,
            @RequestParam("file") MultipartFile file) {
        
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
                        .body(new MessageResponse("错误: 无权为他人的日记上传媒体文件!"));
            }
            
            // 上传媒体文件
            Media media = mediaService.saveMedia(file, diary);
            return ResponseEntity.ok(media);
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("错误: 上传媒体文件失败! " + e.getMessage()));
        }
    }

    @GetMapping("/files/{fileName:.+}")
    public ResponseEntity<?> getFile(@PathVariable String fileName) {
        try {
            // 构建文件路径
            Path path = Paths.get(System.getProperty("user.dir") + "/uploads/" + fileName);
            Resource resource = new UrlResource(path.toUri());
            
            if (resource.exists()) {
                // 确定内容类型 - 仅支持JPEG图片和MP4视频
                String contentType;
                if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                    contentType = MediaType.IMAGE_JPEG_VALUE;
                } else if (fileName.endsWith(".mp4")) {
                    contentType = "video/mp4";
                } else {
                    // 不支持的文件类型
                    return ResponseEntity.badRequest().body(new MessageResponse("错误: 不支持的文件类型，仅支持JPEG图片和MP4视频"));
                }
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("错误: 获取文件失败!"));
        }
    }

    @GetMapping("/diary/{diaryId}")
    public ResponseEntity<?> getMediaByDiary(@PathVariable Long diaryId) {
        // 检查日记是否存在
        Optional<Diary> optionalDiary = diaryService.getDiaryById(diaryId);
        if (!optionalDiary.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("错误: 日记不存在!"));
        }
        
        Diary diary = optionalDiary.get();
        List<Media> mediaList = mediaService.getMediaByDiary(diary);
        return ResponseEntity.ok(mediaList);
    }

    @DeleteMapping("/{mediaId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteMedia(@PathVariable Long mediaId) {
        Optional<Media> optionalMedia = mediaService.getMediaById(mediaId);
        if (!optionalMedia.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("错误: 媒体文件不存在!"));
        }
        
        Media media = optionalMedia.get();
        Diary diary = media.getDiary();
        
        // 检查当前用户是否是日记的作者
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        if (!diary.getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity.badRequest().body(new MessageResponse("错误: 无权删除他人的媒体文件!"));
        }
        
        mediaService.deleteMedia(mediaId);
        return ResponseEntity.ok(new MessageResponse("媒体文件删除成功!"));
    }
} 