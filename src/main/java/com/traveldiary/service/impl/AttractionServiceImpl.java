package com.traveldiary.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.traveldiary.model.Attraction;
import com.traveldiary.model.Diary;
import com.traveldiary.model.Media;
import com.traveldiary.payload.request.AttractionSearchRequest;
import com.traveldiary.repository.AttractionRepository;
import com.traveldiary.repository.DiaryRepository;
import com.traveldiary.repository.MediaRepository;
import com.traveldiary.service.AttractionService;
import com.traveldiary.utils.AttractionSortUtils;

@Service
@Transactional
public class AttractionServiceImpl implements AttractionService {

    @Autowired
    private AttractionRepository attractionRepository;

    @Autowired
    private DiaryRepository diaryRepository;
    
    @Autowired
    private MediaRepository mediaRepository;

    @Override
    public List<Attraction> getAllAttractions() {
        // 基于现有日记动态生成景点列表
        generateAttractionsFromDiaries();
        List<Attraction> attractions = attractionRepository.findAll();
        // 为所有景点填充图片URLs
        attractions.forEach(this::populateAttractionImages);
        return attractions;
    }

    @Override
    public Optional<Attraction> getAttractionById(Long id) {
        Optional<Attraction> attractionOpt = attractionRepository.findById(id);
        attractionOpt.ifPresent(this::populateAttractionImages);
        return attractionOpt;
    }

    @Override
    public Attraction createAttraction(Attraction attraction) {
        return attractionRepository.save(attraction);
    }

    @Override
    public Attraction updateAttraction(Long id, Attraction attraction) {
        return attractionRepository.findById(id)
                .map(existingAttraction -> {
                    existingAttraction.setName(attraction.getName());
                    existingAttraction.setDescription(attraction.getDescription());
                    existingAttraction.setCategory(attraction.getCategory());
                    existingAttraction.setLocation(attraction.getLocation());
                    existingAttraction.setKeywords(attraction.getKeywords());
                    return attractionRepository.save(existingAttraction);
                })
                .orElseThrow(() -> new RuntimeException("景点不存在，ID: " + id));
    }

    @Override
    public void deleteAttraction(Long id) {
        attractionRepository.deleteById(id);
    }

    @Override
    public List<Attraction> searchAttractions(AttractionSearchRequest request) {
        // 先基于现有日记生成景点
        generateAttractionsFromDiaries();
        
        List<Attraction> attractions = new ArrayList<>();

        // 根据搜索条件获取景点列表
        if (StringUtils.hasText(request.getSearchTerm()) && StringUtils.hasText(request.getCategory())) {
            // 同时按搜索词和类别搜索
            List<Attraction> searchResults = attractionRepository.findBySearchTerm(request.getSearchTerm());
            attractions = searchResults.stream()
                    .filter(a -> a.getCategory().toLowerCase().contains(request.getCategory().toLowerCase()))
                    .toList();
        } else if (StringUtils.hasText(request.getSearchTerm())) {
            // 按搜索词搜索
            attractions = attractionRepository.findBySearchTerm(request.getSearchTerm());
        } else if (StringUtils.hasText(request.getCategory())) {
            // 按类别搜索
            attractions = attractionRepository.findByCategoryContainingIgnoreCase(request.getCategory());
        } else {
            // 获取所有景点
            attractions = attractionRepository.findAll();
        }
        
        // 为所有景点填充图片URLs
        attractions.forEach(this::populateAttractionImages);

        // 根据排序方式和是否使用Top-K算法进行排序
        return sortAttractions(attractions, request);
    }

    @Override
    public List<Attraction> getRecommendationsByViews(int limit) {
        generateAttractionsFromDiaries();
        List<Attraction> allAttractions = attractionRepository.findAll();
        // 为所有景点填充图片URLs
        allAttractions.forEach(this::populateAttractionImages);
        return AttractionSortUtils.getTopKByViews(allAttractions, limit);
    }

    @Override
    public List<Attraction> getRecommendationsByRating(int limit) {
        generateAttractionsFromDiaries();
        List<Attraction> allAttractions = attractionRepository.findAll();
        // 为所有景点填充图片URLs
        allAttractions.forEach(this::populateAttractionImages);
        return AttractionSortUtils.getTopKByRating(allAttractions, limit);
    }

    @Override
    public List<Attraction> getRecommendationsByCompositeScore(int limit, double viewsWeight, double ratingWeight) {
        generateAttractionsFromDiaries();
        List<Attraction> allAttractions = attractionRepository.findAll();
        // 为所有景点填充图片URLs
        allAttractions.forEach(this::populateAttractionImages);
        return AttractionSortUtils.getTopKByCompositeScore(allAttractions, limit, viewsWeight, ratingWeight);
    }

    @Override
    public void updateAttractionStatistics() {
        List<Attraction> attractions = attractionRepository.findAll();
        
        for (Attraction attraction : attractions) {
            updateAttractionStatistics(attraction.getId());
        }
    }

    @Override
    public void updateAttractionStatistics(Long attractionId) {
        Optional<Attraction> attractionOpt = attractionRepository.findById(attractionId);
        if (attractionOpt.isEmpty()) {
            return;
        }

        Attraction attraction = attractionOpt.get();
        
        // 查找与该景点相关的日记（通过location字段匹配）
        List<Diary> relatedDiaries = findRelatedDiaries(attraction);
        
        if (relatedDiaries.isEmpty()) {
            attraction.updateStatistics(0L, 0.0, 0);
        } else {
            // 计算总阅读量
            long totalViews = relatedDiaries.stream()
                    .mapToLong(Diary::getViews)
                    .sum();
            
            // 计算平均评分
            double averageRating = relatedDiaries.stream()
                    .filter(d -> d.getRatingCount() > 0)
                    .mapToDouble(Diary::getAverageRating)
                    .average()
                    .orElse(0.0);
            
            // 更新统计数据
            attraction.updateStatistics(totalViews, averageRating, relatedDiaries.size());
        }
        
        attractionRepository.save(attraction);
    }
    
    /**
     * 为景点填充相关图片URL
     */
    private void populateAttractionImages(Attraction attraction) {
        // 清空当前图片列表，确保没有重复
        attraction.setImageUrls(new ArrayList<>());
        
        // 获取相关日记
        List<Diary> relatedDiaries = findRelatedDiaries(attraction);
        
        // 限制每个景点最多展示10张图片
        int maxImagesPerAttraction = 10;
        int imagesAdded = 0;
        
        // 遍历所有相关日记，收集图片
        for (Diary diary : relatedDiaries) {
            if (imagesAdded >= maxImagesPerAttraction) {
                break;
            }
            
            // 获取日记的媒体文件
            List<Media> mediaList = diary.getMediaList();
            
            // 过滤出只有图片类型的媒体
            List<Media> images = mediaList.stream()
                    .filter(media -> media.getMediaType() == Media.MediaType.IMAGE)
                    .collect(Collectors.toList());
            
            // 为每个景点添加图片URL，同时确保不超过最大限制
            for (Media image : images) {
                if (imagesAdded < maxImagesPerAttraction) {
                    attraction.addImageUrl(image.getFileUrl());
                    imagesAdded++;
                } else {
                    break;
                }
            }
        }
    }

    /**
     * 查找与景点相关的日记
     */
    private List<Diary> findRelatedDiaries(Attraction attraction) {
        List<Diary> allDiaries = diaryRepository.findAll();
        List<Diary> relatedDiaries = new ArrayList<>();
        
        String attractionName = attraction.getName().toLowerCase();
        String attractionLocation = attraction.getLocation() != null ? attraction.getLocation().toLowerCase() : "";
        String[] keywords = attraction.getKeywords() != null ? 
                attraction.getKeywords().toLowerCase().split(",") : new String[0];
        
        for (Diary diary : allDiaries) {
            if (isDiaryRelatedToAttraction(diary, attractionName, attractionLocation, keywords)) {
                relatedDiaries.add(diary);
            }
        }
        
        return relatedDiaries;
    }

    /**
     * 判断日记是否与景点相关
     */
    private boolean isDiaryRelatedToAttraction(Diary diary, String attractionName, 
                                             String attractionLocation, String[] keywords) {
        String diaryTitle = diary.getTitle().toLowerCase();
        String diaryContent = diary.getContent().toLowerCase();
        String diaryLocation = diary.getLocation() != null ? diary.getLocation().toLowerCase() : "";
        
        // 检查景点名称
        if (diaryTitle.contains(attractionName) || diaryContent.contains(attractionName) || 
            diaryLocation.contains(attractionName)) {
            return true;
        }
        
        // 检查位置信息
        if (!attractionLocation.isEmpty() && 
            (diaryTitle.contains(attractionLocation) || diaryContent.contains(attractionLocation) || 
             diaryLocation.contains(attractionLocation))) {
            return true;
        }
        
        // 检查关键词
        for (String keyword : keywords) {
            keyword = keyword.trim();
            if (!keyword.isEmpty() && 
                (diaryTitle.contains(keyword) || diaryContent.contains(keyword))) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 基于现有日记动态生成景点
     */
    private void generateAttractionsFromDiaries() {
        List<Diary> allDiaries = diaryRepository.findAll();
        
        // 按location分组，统计每个景点的数据
        Map<String, List<Diary>> locationGroups = new HashMap<>();
        
        for (Diary diary : allDiaries) {
            String location = diary.getLocation();
            if (location != null && !location.trim().isEmpty()) {
                location = location.trim();
                locationGroups.computeIfAbsent(location, k -> new ArrayList<>()).add(diary);
            }
        }
        
        // 为每个景点创建或更新Attraction记录
        for (Map.Entry<String, List<Diary>> entry : locationGroups.entrySet()) {
            String locationName = entry.getKey();
            List<Diary> diaries = entry.getValue();
            
            // 检查是否已存在该景点
            List<Attraction> existingAttractions = attractionRepository.findByNameContainingIgnoreCase(locationName);
            Attraction attraction;
            
            if (existingAttractions.isEmpty()) {
                // 创建新景点
                attraction = new Attraction();
                attraction.setName(locationName);
                attraction.setDescription("基于旅游日记生成的景点");
                attraction.setCategory(inferCategory(locationName, diaries));
                attraction.setLocation(locationName);
                attraction.setKeywords(generateKeywords(locationName, diaries));
            } else {
                // 使用现有景点
                attraction = existingAttractions.get(0);
            }
            
            // 计算统计数据
            long totalViews = diaries.stream().mapToLong(Diary::getViews).sum();
            double averageRating = diaries.stream()
                    .filter(d -> d.getRatingCount() > 0)
                    .mapToDouble(Diary::getAverageRating)
                    .average()
                    .orElse(0.0);
            int diaryCount = diaries.size();
            
            attraction.updateStatistics(totalViews, averageRating, diaryCount);
            attractionRepository.save(attraction);
        }
    }
    
    /**
     * 推断景点类别
     */
    private String inferCategory(String locationName, List<Diary> diaries) {
        String name = locationName.toLowerCase();
        
        // 基于名称关键词推断类别
        if (name.contains("博物馆") || name.contains("故宫") || name.contains("寺") || name.contains("庙") || 
            name.contains("古城") || name.contains("古镇") || name.contains("遗址")) {
            return "历史文化";
        } else if (name.contains("山") || name.contains("湖") || name.contains("海") || name.contains("河") || 
                  name.contains("森林") || name.contains("公园") || name.contains("峡谷")) {
            return "自然风光";
        } else if (name.contains("园") || name.contains("花园")) {
            return "园林景观";
        } else if (name.contains("大学") || name.contains("学校") || name.contains("学院")) {
            return "教育机构";
        } else if (name.contains("商场") || name.contains("购物") || name.contains("街")) {
            return "商业区域";
        } else {
            return "其他景点";
        }
    }
    
    /**
     * 生成关键词
     */
    private String generateKeywords(String locationName, List<Diary> diaries) {
        Set<String> keywords = new HashSet<>();
        keywords.add(locationName);
        
        // 从日记标题和内容中提取关键词
        for (Diary diary : diaries) {
            String title = diary.getTitle().toLowerCase();
            String content = diary.getContent().toLowerCase();
            
            // 简单的关键词提取（可以根据需要优化）
            String[] titleWords = title.split("[\\s,，。！？；：]+");
            String[] contentWords = content.split("[\\s,，。！？；：]+");
            
            for (String word : titleWords) {
                if (word.length() >= 2 && word.length() <= 10) {
                    keywords.add(word);
                }
            }
            
            for (String word : contentWords) {
                if (word.length() >= 2 && word.length() <= 10) {
                    keywords.add(word);
                }
            }
        }
        
        // 限制关键词数量，取前10个
        return keywords.stream()
                .limit(10)
                .collect(Collectors.joining(","));
    }

    /**
     * 根据请求参数对景点进行排序
     */
    private List<Attraction> sortAttractions(List<Attraction> attractions, AttractionSearchRequest request) {
        if (attractions.isEmpty()) {
            return attractions;
        }

        String sortBy = request.getSortBy();
        int limit = Math.min(request.getLimit(), attractions.size());
        
        if (request.isUseTopK()) {
            // 使用Top-K算法
            switch (sortBy.toLowerCase()) {
                case "views":
                    return AttractionSortUtils.getTopKByViews(attractions, limit);
                case "rating":
                    return AttractionSortUtils.getTopKByRating(attractions, limit);
                case "composite":
                    return AttractionSortUtils.getTopKByCompositeScore(attractions, limit, 
                            request.getViewsWeight(), request.getRatingWeight());
                default:
                    return AttractionSortUtils.getTopKByViews(attractions, limit);
            }
        } else {
            // 使用完整排序
            List<Attraction> sortedAttractions = AttractionSortUtils.quickSort(attractions, sortBy);
            return sortedAttractions.subList(0, Math.min(limit, sortedAttractions.size()));
        }
    }
} 