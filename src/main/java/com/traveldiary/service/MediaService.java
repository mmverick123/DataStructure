package com.traveldiary.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import com.traveldiary.model.Diary;
import com.traveldiary.model.Media;

public interface MediaService {
    Media saveMedia(MultipartFile file, Diary diary) throws IOException;
    
    List<Media> getMediaByDiary(Diary diary);
    
    Optional<Media> getMediaById(Long id);
    
    void deleteMedia(Long id);
    
    String getFileUrl(String fileName);
} 