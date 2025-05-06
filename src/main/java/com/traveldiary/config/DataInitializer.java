package com.traveldiary.config;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.traveldiary.model.Diary;
import com.traveldiary.model.Media;
import com.traveldiary.model.Media.MediaType;
import com.traveldiary.model.Rating;
import com.traveldiary.model.User;
import com.traveldiary.repository.DiaryRepository;
import com.traveldiary.repository.MediaRepository;
import com.traveldiary.repository.RatingRepository;
import com.traveldiary.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ResourceLoader resourceLoader;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DiaryRepository diaryRepository;
    
    @Autowired
    private MediaRepository mediaRepository;
    
    @Autowired
    private RatingRepository ratingRepository;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 检查数据库是否已有数据
        if (userRepository.count() > 0) {
            System.out.println("数据库已有数据，跳过初始化");
            return;
        }
        
        try {
            Resource resource = resourceLoader.getResource("classpath:static/mock-api/db.json");
            JsonNode rootNode = objectMapper.readTree(resource.getInputStream());
            
            // 创建用户映射，避免重复创建
            Map<Long, User> userMap = new HashMap<>();
            
            // 导入日记数据
            JsonNode diariesNode = rootNode.get("diaries");
            if (diariesNode != null && diariesNode.isArray()) {
                for (JsonNode diaryNode : diariesNode) {
                    // 处理用户
                    JsonNode userNode = diaryNode.get("user");
                    Long userId = userNode.get("id").asLong();
                    User user = userMap.get(userId);
                    
                    if (user == null) {
                        user = new User();
                        user.setId(userId);
                        user.setUsername(userNode.get("username").asText());
                        user.setEmail(userNode.get("email").asText());
                        user.setPassword("password123"); // 设置默认密码
                        user = userRepository.save(user);
                        userMap.put(userId, user);
                    }
                    
                    // 创建日记
                    Diary diary = new Diary();
                    diary.setId(diaryNode.get("id").asLong());
                    diary.setTitle(diaryNode.get("title").asText());
                    diary.setContent(diaryNode.get("content").asText());
                    
                    // 使用反射设置没有setter方法的字段
                    setPrivateField(diary, "createdAt", LocalDateTime.parse(diaryNode.get("createdAt").asText(), FORMATTER));
                    setPrivateField(diary, "updatedAt", LocalDateTime.parse(diaryNode.get("updatedAt").asText(), FORMATTER));
                    setPrivateField(diary, "views", diaryNode.get("views").asInt());
                    setPrivateField(diary, "averageRating", diaryNode.get("averageRating").asDouble());
                    setPrivateField(diary, "ratingCount", diaryNode.get("ratingCount").asInt());
                    
                    // 设置用户
                    diary.setUser(user);
                    
                    // 保存日记
                    diaryRepository.save(diary);
                    
                    // 处理媒体列表
                    JsonNode mediaListNode = diaryNode.get("mediaList");
                    if (mediaListNode != null && mediaListNode.isArray()) {
                        for (JsonNode mediaNode : mediaListNode) {
                            Media media = new Media();
                            media.setId(mediaNode.get("id").asLong());
                            media.setFileName(mediaNode.get("fileName").asText());
                            media.setFileType(mediaNode.get("fileType").asText());
                            media.setFileUrl(mediaNode.get("fileUrl").asText());
                            media.setFileSize(mediaNode.get("fileSize").asLong());
                            media.setMediaType(MediaType.valueOf(mediaNode.get("mediaType").asText()));
                            media.setDiary(diary);
                            mediaRepository.save(media);
                        }
                    }
                    
                    // 处理评分
                    JsonNode ratingsNode = diaryNode.get("ratings");
                    if (ratingsNode != null && ratingsNode.isArray()) {
                        for (JsonNode ratingNode : ratingsNode) {
                            Rating rating = new Rating();
                            rating.setId(ratingNode.get("id").asLong());
                            rating.setScore(ratingNode.get("score").asDouble());
                            
                            // 处理评分用户
                            JsonNode ratingUserNode = ratingNode.get("user");
                            Long ratingUserId = ratingUserNode.get("id").asLong();
                            User ratingUser = userMap.get(ratingUserId);
                            
                            if (ratingUser == null) {
                                ratingUser = new User();
                                ratingUser.setId(ratingUserId);
                                ratingUser.setUsername(ratingUserNode.get("username").asText());
                                // 如果JSON中评分用户没有邮箱，设置一个默认值
                                ratingUser.setEmail(ratingUserNode.has("email") 
                                        ? ratingUserNode.get("email").asText() 
                                        : ratingUserNode.get("username").asText() + "@example.com");
                                ratingUser.setPassword("password123"); // 设置默认密码
                                ratingUser = userRepository.save(ratingUser);
                                userMap.put(ratingUserId, ratingUser);
                            }
                            
                            rating.setUser(ratingUser);
                            rating.setDiary(diary);
                            
                            ratingRepository.save(rating);
                        }
                    }
                }
            }
            
            System.out.println("成功从JSON文件导入初始数据！");
            
        } catch (IOException e) {
            System.err.println("读取初始数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // 使用反射设置私有字段的值
    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
} 