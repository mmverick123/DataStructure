package com.traveldiary.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.traveldiary.model.Diary;
import com.traveldiary.repository.DiaryRepository;
import com.traveldiary.service.CompressionService;
import com.traveldiary.utils.GzipUtils;

/**
 * 压缩服务实现类
 */
@Service
public class CompressionServiceImpl implements CompressionService {
    
    private static final Logger logger = LoggerFactory.getLogger(CompressionServiceImpl.class);
    
    private final DiaryRepository diaryRepository;
    
    @Autowired
    public CompressionServiceImpl(DiaryRepository diaryRepository) {
        this.diaryRepository = diaryRepository;
    }
    
    @Override
    @Transactional
    public Map<String, Object> compressAllDiaries() {
        logger.info("开始批量压缩所有日记");
        
        List<Diary> allDiaries = diaryRepository.findAll();
        int totalCount = allDiaries.size();
        int compressedCount = 0;
        int alreadyCompressedCount = 0;
        long totalOriginalSize = 0;
        long totalCompressedSize = 0;
        
        for (Diary diary : allDiaries) {
            boolean titleCompressed = GzipUtils.isCompressed(diary.getTitle());
            boolean contentCompressed = GzipUtils.isCompressed(diary.getContent());
            
            if (titleCompressed && contentCompressed) {
                alreadyCompressedCount++;
                continue;
            }
            
            // 计算原始大小
            int originalTitleSize = diary.getTitle() != null ? diary.getTitle().getBytes().length : 0;
            int originalContentSize = diary.getContent() != null ? diary.getContent().getBytes().length : 0;
            totalOriginalSize += originalTitleSize + originalContentSize;
            
            // 压缩标题
            if (!titleCompressed && diary.getTitle() != null) {
                String compressedTitle = GzipUtils.compress(diary.getTitle());
                diary.setTitle(compressedTitle);
            }
            
            // 压缩内容
            if (!contentCompressed && diary.getContent() != null) {
                String compressedContent = GzipUtils.compress(diary.getContent());
                diary.setContent(compressedContent);
            }
            
            // 计算压缩后大小
            int compressedTitleSize = diary.getTitle() != null ? diary.getTitle().getBytes().length : 0;
            int compressedContentSize = diary.getContent() != null ? diary.getContent().getBytes().length : 0;
            totalCompressedSize += compressedTitleSize + compressedContentSize;
            
            diaryRepository.save(diary);
            compressedCount++;
        }
        
        double compressionRatio = totalOriginalSize > 0 ? 
            (1.0 - (double)totalCompressedSize / totalOriginalSize) * 100 : 0.0;
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "批量压缩完成");
        result.put("totalCount", totalCount);
        result.put("compressedCount", compressedCount);
        result.put("alreadyCompressedCount", alreadyCompressedCount);
        result.put("originalSize", totalOriginalSize);
        result.put("compressedSize", totalCompressedSize);
        result.put("compressionRatio", String.format("%.2f%%", compressionRatio));
        result.put("spaceSaved", totalOriginalSize - totalCompressedSize);
        
        logger.info("批量压缩完成。总数: {}, 新压缩: {}, 已压缩: {}, 压缩率: {:.2f}%", 
                   totalCount, compressedCount, alreadyCompressedCount, compressionRatio);
        
        return result;
    }
    
    @Override
    @Transactional
    public Map<String, Object> decompressAllDiaries() {
        logger.info("开始批量解压缩所有日记");
        
        List<Diary> allDiaries = diaryRepository.findAll();
        int totalCount = allDiaries.size();
        int decompressedCount = 0;
        int notCompressedCount = 0;
        
        for (Diary diary : allDiaries) {
            boolean titleCompressed = GzipUtils.isCompressed(diary.getTitle());
            boolean contentCompressed = GzipUtils.isCompressed(diary.getContent());
            
            if (!titleCompressed && !contentCompressed) {
                notCompressedCount++;
                continue;
            }
            
            // 解压缩标题
            if (titleCompressed) {
                String decompressedTitle = GzipUtils.decompress(diary.getTitle());
                diary.setTitle(decompressedTitle);
            }
            
            // 解压缩内容
            if (contentCompressed) {
                String decompressedContent = GzipUtils.decompress(diary.getContent());
                diary.setContent(decompressedContent);
            }
            
            diaryRepository.save(diary);
            decompressedCount++;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "批量解压缩完成");
        result.put("totalCount", totalCount);
        result.put("decompressedCount", decompressedCount);
        result.put("notCompressedCount", notCompressedCount);
        
        logger.info("批量解压缩完成。总数: {}, 解压缩: {}, 未压缩: {}", 
                   totalCount, decompressedCount, notCompressedCount);
        
        return result;
    }
    
    @Override
    public Map<String, Object> getCompressionStatistics() {
        List<Diary> allDiaries = diaryRepository.findAll();
        int totalCount = allDiaries.size();
        int compressedCount = 0;
        int partiallyCompressedCount = 0;
        int uncompressedCount = 0;
        long totalOriginalSize = 0;
        long totalCurrentSize = 0;
        
        for (Diary diary : allDiaries) {
            boolean titleCompressed = GzipUtils.isCompressed(diary.getTitle());
            boolean contentCompressed = GzipUtils.isCompressed(diary.getContent());
            
            if (titleCompressed && contentCompressed) {
                compressedCount++;
            } else if (titleCompressed || contentCompressed) {
                partiallyCompressedCount++;
            } else {
                uncompressedCount++;
            }
            
            // 计算大小
            String originalTitle = titleCompressed ? GzipUtils.decompress(diary.getTitle()) : diary.getTitle();
            String originalContent = contentCompressed ? GzipUtils.decompress(diary.getContent()) : diary.getContent();
            
            int originalTitleSize = originalTitle != null ? originalTitle.getBytes().length : 0;
            int originalContentSize = originalContent != null ? originalContent.getBytes().length : 0;
            int currentTitleSize = diary.getTitle() != null ? diary.getTitle().getBytes().length : 0;
            int currentContentSize = diary.getContent() != null ? diary.getContent().getBytes().length : 0;
            
            totalOriginalSize += originalTitleSize + originalContentSize;
            totalCurrentSize += currentTitleSize + currentContentSize;
        }
        
        double compressionRatio = totalOriginalSize > 0 ? 
            (1.0 - (double)totalCurrentSize / totalOriginalSize) * 100 : 0.0;
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", totalCount);
        result.put("compressedCount", compressedCount);
        result.put("partiallyCompressedCount", partiallyCompressedCount);
        result.put("uncompressedCount", uncompressedCount);
        result.put("compressionPercentage", totalCount > 0 ? (double)compressedCount / totalCount * 100 : 0.0);
        result.put("originalSize", totalOriginalSize);
        result.put("currentSize", totalCurrentSize);
        result.put("compressionRatio", compressionRatio);
        result.put("spaceSaved", totalOriginalSize - totalCurrentSize);
        
        return result;
    }
    
    @Override
    public Map<String, Object> checkDiaryCompressionStatus(Long diaryId) {
        Optional<Diary> diaryOpt = diaryRepository.findById(diaryId);
        
        if (!diaryOpt.isPresent()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "日记不存在");
            return result;
        }
        
        Diary diary = diaryOpt.get();
        boolean titleCompressed = GzipUtils.isCompressed(diary.getTitle());
        boolean contentCompressed = GzipUtils.isCompressed(diary.getContent());
        
        // 计算大小和压缩率
        String originalTitle = titleCompressed ? GzipUtils.decompress(diary.getTitle()) : diary.getTitle();
        String originalContent = contentCompressed ? GzipUtils.decompress(diary.getContent()) : diary.getContent();
        
        int originalTitleSize = originalTitle != null ? originalTitle.getBytes().length : 0;
        int originalContentSize = originalContent != null ? originalContent.getBytes().length : 0;
        int currentTitleSize = diary.getTitle() != null ? diary.getTitle().getBytes().length : 0;
        int currentContentSize = diary.getContent() != null ? diary.getContent().getBytes().length : 0;
        
        int totalOriginalSize = originalTitleSize + originalContentSize;
        int totalCurrentSize = currentTitleSize + currentContentSize;
        
        double titleCompressionRatio = titleCompressed && originalTitleSize > 0 ? 
            GzipUtils.getCompressionRatio(originalTitle, diary.getTitle()) : 0.0;
        double contentCompressionRatio = contentCompressed && originalContentSize > 0 ? 
            GzipUtils.getCompressionRatio(originalContent, diary.getContent()) : 0.0;
        double overallCompressionRatio = totalOriginalSize > 0 ? 
            (1.0 - (double)totalCurrentSize / totalOriginalSize) * 100 : 0.0;
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("diaryId", diaryId);
        result.put("titleCompressed", titleCompressed);
        result.put("contentCompressed", contentCompressed);
        result.put("fullyCompressed", titleCompressed && contentCompressed);
        result.put("originalTitleSize", originalTitleSize);
        result.put("originalContentSize", originalContentSize);
        result.put("currentTitleSize", currentTitleSize);
        result.put("currentContentSize", currentContentSize);
        result.put("totalOriginalSize", totalOriginalSize);
        result.put("totalCurrentSize", totalCurrentSize);
        result.put("titleCompressionRatio", titleCompressionRatio);
        result.put("contentCompressionRatio", contentCompressionRatio);
        result.put("overallCompressionRatio", overallCompressionRatio);
        result.put("spaceSaved", totalOriginalSize - totalCurrentSize);
        
        return result;
    }
    
    @Override
    @Transactional
    public Map<String, Object> compressDiary(Long diaryId) {
        Optional<Diary> diaryOpt = diaryRepository.findById(diaryId);
        
        if (!diaryOpt.isPresent()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "日记不存在");
            return result;
        }
        
        Diary diary = diaryOpt.get();
        
        // 记录原始大小
        int originalTitleSize = diary.getTitle() != null ? diary.getTitle().getBytes().length : 0;
        int originalContentSize = diary.getContent() != null ? diary.getContent().getBytes().length : 0;
        
        // 强制压缩（即使已经压缩过）
        if (diary.getTitle() != null) {
            String originalTitle = GzipUtils.isCompressed(diary.getTitle()) ? 
                GzipUtils.decompress(diary.getTitle()) : diary.getTitle();
            diary.setTitle(GzipUtils.compress(originalTitle));
        }
        
        if (diary.getContent() != null) {
            String originalContent = GzipUtils.isCompressed(diary.getContent()) ? 
                GzipUtils.decompress(diary.getContent()) : diary.getContent();
            diary.setContent(GzipUtils.compress(originalContent));
        }
        
        // 记录压缩后大小
        int compressedTitleSize = diary.getTitle() != null ? diary.getTitle().getBytes().length : 0;
        int compressedContentSize = diary.getContent() != null ? diary.getContent().getBytes().length : 0;
        
        diaryRepository.save(diary);
        
        int totalOriginalSize = originalTitleSize + originalContentSize;
        int totalCompressedSize = compressedTitleSize + compressedContentSize;
        double compressionRatio = totalOriginalSize > 0 ? 
            (1.0 - (double)totalCompressedSize / totalOriginalSize) * 100 : 0.0;
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "日记压缩成功");
        result.put("diaryId", diaryId);
        result.put("originalSize", totalOriginalSize);
        result.put("compressedSize", totalCompressedSize);
        result.put("compressionRatio", compressionRatio);
        result.put("spaceSaved", totalOriginalSize - totalCompressedSize);
        
        logger.info("日记 {} 压缩成功，压缩率: {:.2f}%", diaryId, compressionRatio);
        
        return result;
    }
    
    @Override
    @Transactional
    public Map<String, Object> decompressDiary(Long diaryId) {
        Optional<Diary> diaryOpt = diaryRepository.findById(diaryId);
        
        if (!diaryOpt.isPresent()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "日记不存在");
            return result;
        }
        
        Diary diary = diaryOpt.get();
        
        boolean titleWasCompressed = GzipUtils.isCompressed(diary.getTitle());
        boolean contentWasCompressed = GzipUtils.isCompressed(diary.getContent());
        
        // 记录压缩前大小
        int compressedTitleSize = diary.getTitle() != null ? diary.getTitle().getBytes().length : 0;
        int compressedContentSize = diary.getContent() != null ? diary.getContent().getBytes().length : 0;
        
        // 解压缩
        if (titleWasCompressed) {
            diary.setTitle(GzipUtils.decompress(diary.getTitle()));
        }
        
        if (contentWasCompressed) {
            diary.setContent(GzipUtils.decompress(diary.getContent()));
        }
        
        // 记录解压缩后大小
        int decompressedTitleSize = diary.getTitle() != null ? diary.getTitle().getBytes().length : 0;
        int decompressedContentSize = diary.getContent() != null ? diary.getContent().getBytes().length : 0;
        
        diaryRepository.save(diary);
        
        int totalCompressedSize = compressedTitleSize + compressedContentSize;
        int totalDecompressedSize = decompressedTitleSize + decompressedContentSize;
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "日记解压缩成功");
        result.put("diaryId", diaryId);
        result.put("titleWasCompressed", titleWasCompressed);
        result.put("contentWasCompressed", contentWasCompressed);
        result.put("compressedSize", totalCompressedSize);
        result.put("decompressedSize", totalDecompressedSize);
        result.put("sizeIncrease", totalDecompressedSize - totalCompressedSize);
        
        logger.info("日记 {} 解压缩成功", diaryId);
        
        return result;
    }
} 