package com.traveldiary.service;

import java.util.Map;

/**
 * 压缩服务接口
 * 提供旅游日记压缩相关的管理功能
 */
public interface CompressionService {
    
    /**
     * 批量压缩所有未压缩的日记
     * @return 压缩统计信息
     */
    Map<String, Object> compressAllDiaries();
    
    /**
     * 批量解压缩所有已压缩的日记
     * @return 解压缩统计信息
     */
    Map<String, Object> decompressAllDiaries();
    
    /**
     * 获取压缩统计信息
     * @return 包含压缩率、压缩数量等信息的Map
     */
    Map<String, Object> getCompressionStatistics();
    
    /**
     * 检查单个日记的压缩状态
     * @param diaryId 日记ID
     * @return 压缩状态信息
     */
    Map<String, Object> checkDiaryCompressionStatus(Long diaryId);
    
    /**
     * 强制压缩指定日记
     * @param diaryId 日记ID
     * @return 压缩结果
     */
    Map<String, Object> compressDiary(Long diaryId);
    
    /**
     * 强制解压缩指定日记
     * @param diaryId 日记ID
     * @return 解压缩结果
     */
    Map<String, Object> decompressDiary(Long diaryId);
} 