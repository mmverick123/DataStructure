package com.traveldiary.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import com.traveldiary.model.Diary;
import com.traveldiary.model.Media;

public interface MediaService {
    Media saveMedia(MultipartFile file, Diary diary) throws IOException;
    
    /**
     * 保存外部媒体URL（如AI生成的图片URL）
     * 
     * @param media 媒体对象，包含外部URL
     * @return 保存的媒体对象
     */
    Media saveExternalMedia(Media media);
    
    /**
     * 从URL下载图片并保存到本地
     * 
     * @param imageUrl 图片URL
     * @param diary 关联的日记
     * @return 保存的媒体对象
     * @throws IOException 如果下载或保存过程中发生错误
     */
    Media downloadAndSaveImage(String imageUrl, Diary diary) throws IOException;
    
    List<Media> getMediaByDiary(Diary diary);
    
    Optional<Media> getMediaById(Long id);
    
    void deleteMedia(Long id);
    
    String getFileUrl(String fileName);
} 