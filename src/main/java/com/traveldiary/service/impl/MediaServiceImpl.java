package com.traveldiary.service.impl;

import com.traveldiary.model.Diary;
import com.traveldiary.model.Media;
import com.traveldiary.repository.MediaRepository;
import com.traveldiary.service.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;
    
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    public MediaServiceImpl(MediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    @Override
    @Transactional
    public Media saveMedia(MultipartFile file, Diary diary) throws IOException {
        // 创建上传目录（如果不存在）
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 生成唯一文件名
        String fileName = UUID.randomUUID().toString() + "_" +
                StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        // 保存文件到文件系统
        Path targetPath = Paths.get(uploadDir).resolve(fileName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // 判断媒体类型
        Media.MediaType mediaType;
        String contentType = file.getContentType();
        if (contentType != null && contentType.startsWith("image/")) {
            mediaType = Media.MediaType.IMAGE;
        } else if (contentType != null && contentType.startsWith("video/")) {
            mediaType = Media.MediaType.VIDEO;
        } else {
            throw new IOException("不支持的文件类型");
        }

        // 创建媒体记录
        Media media = new Media();
        media.setFileName(fileName);
        media.setFileType(file.getContentType());
        media.setFileSize(file.getSize());
        media.setFileUrl(getFileUrl(fileName));
        media.setMediaType(mediaType);
        media.setDiary(diary);

        return mediaRepository.save(media);
    }

    @Override
    public List<Media> getMediaByDiary(Diary diary) {
        return mediaRepository.findByDiary(diary);
    }

    @Override
    public Optional<Media> getMediaById(Long id) {
        return mediaRepository.findById(id);
    }

    @Override
    @Transactional
    public void deleteMedia(Long id) {
        mediaRepository.findById(id).ifPresent(media -> {
            // 从文件系统中删除文件
            try {
                Path filePath = Paths.get(uploadDir).resolve(media.getFileName());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // 日志记录删除失败
                System.err.println("删除文件失败: " + e.getMessage());
            }
            
            // 从数据库中删除记录
            mediaRepository.deleteById(id);
        });
    }

    @Override
    public String getFileUrl(String fileName) {
        return "/api/media/files/" + fileName;
    }
} 