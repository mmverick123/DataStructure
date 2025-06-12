package com.traveldiary.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.traveldiary.model.Diary;
import com.traveldiary.model.Media;
import com.traveldiary.repository.MediaRepository;
import com.traveldiary.service.MediaService;

@Service
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;
    private final Logger logger = LoggerFactory.getLogger(MediaServiceImpl.class);
    
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

        // 判断媒体类型 - 简化为只支持一种图片和一种视频格式
        Media.MediaType mediaType;
        String contentType = file.getContentType();
        if (contentType != null && contentType.equals("image/jpeg")) {
            mediaType = Media.MediaType.IMAGE;
        } else if (contentType != null && contentType.equals("video/mp4")) {
            mediaType = Media.MediaType.VIDEO;
        } else {
            throw new IOException("不支持的文件类型，仅支持JPEG图片和MP4视频");
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
    @Transactional
    public Media saveExternalMedia(Media media) {
        // 直接保存媒体记录到数据库
        return mediaRepository.save(media);
    }

    @Override
    public List<Media> getMediaByDiary(Diary diary) {
        // 获取日记的所有媒体文件
        List<Media> mediaList = mediaRepository.findByDiary(diary);
        
        // 按类型排序：图片在前，视频在后
        return mediaList.stream()
                .sorted(Comparator.comparing(media -> {
                    // 图片优先级高（排在前面），视频优先级低（排在后面）
                    return media.getMediaType() == Media.MediaType.IMAGE ? 0 : 1;
                }))
                .collect(Collectors.toList());
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

    @Override
    @Transactional
    public Media downloadAndSaveImage(String imageUrl, Diary diary) throws IOException {
        logger.info("开始从URL下载图片: {}", imageUrl);
        
        // 创建上传目录（如果不存在）
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        // 生成唯一文件名
        String fileName = "ai_" + UUID.randomUUID().toString() + ".jpg";
        Path targetPath = Paths.get(uploadDir).resolve(fileName);
        
        // 打开URL连接
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);
        
        // 设置一些必要的请求头，防止被服务器拒绝
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        
        try (InputStream inputStream = connection.getInputStream()) {
            // 保存图片到文件系统
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("图片已下载并保存到: {}", targetPath);
            
            // 创建媒体记录
            Media media = new Media();
            media.setFileName(fileName);
            media.setFileType("image/jpeg");
            media.setFileSize(Files.size(targetPath));
            media.setFileUrl(getFileUrl(fileName));
            media.setMediaType(Media.MediaType.IMAGE);
            media.setDiary(diary);
            
            Media savedMedia = mediaRepository.save(media);
            logger.info("已创建媒体记录: {}", savedMedia.getId());
            return savedMedia;
        } catch (IOException e) {
            logger.error("下载图片失败: {}", e.getMessage(), e);
            throw new IOException("下载图片失败: " + e.getMessage(), e);
        } finally {
            connection.disconnect();
        }
    }
} 