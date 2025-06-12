package com.traveldiary.service;

import java.util.Map;

/**
 * 通义万相文生图API服务接口
 */
public interface ImageGenerationService {
    
    /**
     * 根据日记内容生成图片
     * 
     * @param diaryId 日记ID
     * @return 包含图片信息的Map，如：base64编码的图片数据、图片URL等
     * @throws Exception 当API调用失败或处理过程中出现错误时抛出
     */
    Map<String, Object> generateImageFromDiary(Long diaryId) throws Exception;
    
    /**
     * 根据提示词生成图片
     * 
     * @param prompt 提示词
     * @return 包含图片信息的Map，如：base64编码的图片数据、图片URL等
     * @throws Exception 当API调用失败或处理过程中出现错误时抛出
     */
    Map<String, Object> generateImageFromPrompt(String prompt) throws Exception;
} 