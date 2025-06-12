package com.traveldiary.service.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.traveldiary.model.Diary;
import com.traveldiary.service.DiaryService;
import com.traveldiary.service.ImageGenerationService;

@Service
public class ImageGenerationServiceImpl implements ImageGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(ImageGenerationServiceImpl.class);
    
    private final DiaryService diaryService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${dashscope.api.key:}")
    private String apiKey;

    @Value("${dashscope.api.url:https://dashscope.aliyuncs.com/api/v1/services/aigc/text2image/image-synthesis}")
    private String apiUrl;

    @Autowired
    public ImageGenerationServiceImpl(DiaryService diaryService) {
        this.diaryService = diaryService;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Map<String, Object> generateImageFromDiary(Long diaryId) throws Exception {
        // 获取日记信息
        Optional<Diary> diaryOpt = diaryService.getDiaryById(diaryId);
        if (!diaryOpt.isPresent()) {
            throw new IllegalArgumentException("日记不存在: " + diaryId);
        }
        
        Diary diary = diaryOpt.get();
        
        // 从日记内容中提取文本作为生成图片的提示语
        String promptText = generatePromptFromDiary(diary);
        
        // 调用通义万相API生成图片
        return generateImageFromPrompt(promptText);
    }
    
    @Override
    public Map<String, Object> generateImageFromPrompt(String prompt) throws Exception {
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("提示词不能为空");
        }
        
        // 调用通义万相API生成图片
        return callDashscopeApi(prompt);
    }
    
    /**
     * 从日记内容生成图片提示语
     */
    private String generatePromptFromDiary(Diary diary) {
        StringBuilder prompt = new StringBuilder();
        
        // 添加位置信息
        if (diary.getLocation() != null && !diary.getLocation().isEmpty()) {
            prompt.append(diary.getLocation()).append("，");
        }
        
        // 添加标题
        prompt.append(diary.getTitle()).append("，");
        
        // 从内容中提取关键信息
        String content = diary.getContent();
        if (content.length() > 100) {
            // 如果内容太长，只取前100个字符
            content = content.substring(0, 100);
        }
        prompt.append(content);
        
        return prompt.toString();
    }
    
    /**
     * 调用通义万相API
     */
    private Map<String, Object> callDashscopeApi(String prompt) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        // 添加异步调用请求头
        headers.set("X-DashScope-Async", "enable");
        
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> requestInput = new HashMap<>();
        Map<String, Object> parameters = new HashMap<>();
        
        // 使用文生图V2版模型
        requestBody.put("model", "wanx2.1-t2i-turbo");
        
        // 设置输入参数
        requestInput.put("prompt", prompt);
        
        // 设置参数
        parameters.put("size", "1024*1024");
        parameters.put("n", 1);
        parameters.put("prompt_extend", true);
        requestBody.put("parameters", parameters);
        
        // 设置请求体
        requestBody.put("input", requestInput);
        
        // 记录请求体，用于调试
        String requestJson = objectMapper.writeValueAsString(requestBody);
        logger.info("API请求URL: {}", apiUrl);
        logger.info("API请求体: {}", requestJson);
        
        // 创建HTTP请求实体
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        try {
            // 发送请求
            String responseStr = restTemplate.postForObject(apiUrl, request, String.class);
            logger.info("API响应: {}", responseStr);
            
            // 解析响应，获取任务ID
            JsonNode responseJson = objectMapper.readTree(responseStr);
            if (responseJson.has("output") && responseJson.get("output").has("task_id")) {
                // 这是一个异步任务响应，需要轮询结果
                String taskId = responseJson.get("output").get("task_id").asText();
                logger.info("获取到异步任务ID: {}, 开始轮询任务状态", taskId);
                return pollTaskResult(taskId);
            } else if (responseJson.has("code") && !responseJson.get("code").asText().equals("0")) {
                // 处理错误响应
                String errorCode = responseJson.get("code").asText();
                String errorMsg = responseJson.has("message") ? 
                        responseJson.get("message").asText() : "API调用失败";
                
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("error", errorMsg);
                logger.error("API返回错误: {} - {}", errorCode, errorMsg);
                return errorResult;
            }
            
            // 解析响应
            return parseApiResponse(responseStr);
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            
            // 特殊处理不支持同步调用的错误
            String errorMsg = e.getMessage();
            if (errorMsg.contains("does not support synchronous calls")) {
                errorResult.put("error", "当前API账户不支持同步调用，请升级您的API账户或联系服务提供商");
                logger.error("API账户权限问题: {}", errorMsg);
            } else {
                errorResult.put("error", "API调用异常: " + errorMsg);
                logger.error("API调用异常: {}", errorMsg, e);
            }
            
            return errorResult;
        }
    }
    
    /**
     * 轮询异步任务结果
     */
    private Map<String, Object> pollTaskResult(String taskId) throws Exception {
        // 构建任务查询URL
        String taskUrl = "https://dashscope.aliyuncs.com/api/v1/tasks/" + taskId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        
        logger.info("开始轮询任务结果，任务ID: {}, 查询URL: {}", taskId, taskUrl);
        
        HttpEntity<?> request = new HttpEntity<>(headers);
        
        // 最多尝试60次，每次间隔5秒，最长等待5分钟
        for (int i = 0; i < 60; i++) {
            try {
                // 等待5秒
                Thread.sleep(5000);
                
                logger.info("第{}次轮询任务状态，任务ID: {}", i + 1, taskId);
                
                // 查询任务状态
                String responseStr = restTemplate.exchange(
                        taskUrl, 
                        org.springframework.http.HttpMethod.GET, 
                        request, 
                        String.class).getBody();
                
                logger.info("轮询任务结果 #{}: {}", i + 1, responseStr);
                
                // 解析响应
                JsonNode responseJson = objectMapper.readTree(responseStr);
                
                // 检查任务状态
                if (responseJson.has("output") && responseJson.get("output").has("task_status")) {
                    String taskStatus = responseJson.get("output").get("task_status").asText();
                    
                    if ("SUCCEEDED".equals(taskStatus)) {
                        // 任务成功完成
                        logger.info("异步任务完成成功: {}", taskId);
                        return parseAsyncTaskResult(responseStr);
                    } else if ("FAILED".equals(taskStatus)) {
                        // 任务失败
                        Map<String, Object> errorResult = new HashMap<>();
                        errorResult.put("success", false);
                        
                        String errorMsg = "异步任务失败";
                        if (responseJson.has("output") && responseJson.get("output").has("task_message")) {
                            errorMsg = responseJson.get("output").get("task_message").asText();
                        }
                        
                        errorResult.put("error", errorMsg);
                        logger.error("异步任务失败: {}, 错误信息: {}", taskId, errorMsg);
                        return errorResult;
                    } else {
                        // 任务仍在处理中
                        logger.info("异步任务状态: {} (尝试 {}/60), 任务ID: {}", taskStatus, i + 1, taskId);
                    }
                } else {
                    logger.warn("异步任务响应格式异常，无法获取任务状态: {}", responseStr);
                }
            } catch (Exception e) {
                logger.warn("轮询任务状态出错: {}, 任务ID: {}", e.getMessage(), taskId);
                // 继续尝试
            }
        }
        
        // 超过最大尝试次数
        Map<String, Object> timeoutResult = new HashMap<>();
        timeoutResult.put("success", false);
        timeoutResult.put("error", "异步任务超时，请稍后再试");
        logger.error("异步任务超时: {}, 已尝试60次", taskId);
        return timeoutResult;
    }
    
    /**
     * 解析API响应
     */
    private Map<String, Object> parseApiResponse(String responseStr) throws IOException {
        Map<String, Object> result = new HashMap<>();
        
        try {
            JsonNode responseJson = objectMapper.readTree(responseStr);
            
            // 检查是否有错误
            if (responseJson.has("code") && !responseJson.get("code").asText().equals("0")) {
                String errorMsg = responseJson.has("message") ? 
                        responseJson.get("message").asText() : "API调用失败";
                result.put("success", false);
                result.put("error", errorMsg);
                logger.error("API返回错误: {}", errorMsg);
                return result;
            }
            
            // 提取输出内容
            if (responseJson.has("output") && responseJson.get("output").has("results")) {
                JsonNode resultsNode = responseJson.get("output").get("results");
                return processResultsNode(resultsNode, result);
            } else {
                result.put("success", false);
                result.put("error", "API响应格式不正确: " + responseStr);
                logger.error("API响应格式不正确: {}", responseStr);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "解析API响应失败: " + e.getMessage() + ", 原始响应: " + responseStr);
            logger.error("解析API响应失败: {}", e.getMessage(), e);
        }
        
        return result;
    }
    
    /**
     * 解析异步任务结果
     */
    private Map<String, Object> parseAsyncTaskResult(String responseStr) throws IOException {
        Map<String, Object> result = new HashMap<>();
        
        try {
            JsonNode responseJson = objectMapper.readTree(responseStr);
            
            // 检查是否有错误
            if (responseJson.has("code") && !responseJson.get("code").asText().equals("0")) {
                String errorMsg = responseJson.has("message") ? 
                        responseJson.get("message").asText() : "API调用失败";
                result.put("success", false);
                result.put("error", errorMsg);
                logger.error("API返回错误: {}", errorMsg);
                return result;
            }
            
            // 提取输出内容
            if (responseJson.has("output")) {
                JsonNode outputNode = responseJson.get("output");
                
                // 检查任务状态
                if (outputNode.has("task_status") && !"SUCCEEDED".equals(outputNode.get("task_status").asText())) {
                    String taskStatus = outputNode.get("task_status").asText();
                    String errorMsg = "异步任务未成功完成: " + taskStatus;
                    
                    if (outputNode.has("task_message")) {
                        errorMsg = outputNode.get("task_message").asText();
                    }
                    
                    result.put("success", false);
                    result.put("error", errorMsg);
                    logger.error("异步任务未成功完成: {}", errorMsg);
                    return result;
                }
                
                // 提取结果
                if (outputNode.has("results")) {
                    JsonNode resultsNode = outputNode.get("results");
                    if (resultsNode.isArray() && resultsNode.size() > 0) {
                        JsonNode firstResult = resultsNode.get(0);
                        
                        // V2版API返回的是URL而不是base64
                        if (firstResult.has("url")) {
                            String imageUrl = firstResult.get("url").asText();
                            result.put("imageUrl", imageUrl);
                            result.put("success", true);
                            
                            // 记录生成的提示词信息
                            if (firstResult.has("orig_prompt")) {
                                result.put("originalPrompt", firstResult.get("orig_prompt").asText());
                            }
                            
                            if (firstResult.has("actual_prompt")) {
                                result.put("actualPrompt", firstResult.get("actual_prompt").asText());
                            }
                            
                            logger.info("成功获取到图片URL: {}", imageUrl);
                            logger.info("图片URL有效期为24小时，将在前端下载并上传到服务器");
                        } else {
                            result.put("success", false);
                            result.put("error", "返回结果中没有图片URL");
                            logger.error("返回结果中没有图片URL: {}", firstResult.toString());
                        }
                    } else {
                        result.put("success", false);
                        result.put("error", "API返回结果为空");
                        logger.error("API返回结果为空");
                    }
                } else {
                    // 任务成功但不包含结果，记录整个响应用于调试
                    result.put("success", false);
                    result.put("error", "异步任务结果不包含图片数据");
                    logger.error("异步任务结果不包含图片数据，完整响应: {}", responseStr);
                }
            } else {
                result.put("success", false);
                result.put("error", "API响应格式不正确: " + responseStr);
                logger.error("API响应格式不正确: {}", responseStr);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "解析异步任务结果失败: " + e.getMessage());
            logger.error("解析异步任务结果失败: {}, 原始响应: {}", e.getMessage(), responseStr, e);
        }
        
        return result;
    }
    
    /**
     * 处理结果节点
     */
    private Map<String, Object> processResultsNode(JsonNode resultsNode, Map<String, Object> result) {
        if (resultsNode.isArray() && resultsNode.size() > 0) {
            JsonNode firstResult = resultsNode.get(0);
            
            // 获取图片URL
            if (firstResult.has("url")) {
                result.put("imageUrl", firstResult.get("url").asText());
                result.put("success", true);
                
                // 记录生成的提示词信息
                if (firstResult.has("orig_prompt")) {
                    result.put("originalPrompt", firstResult.get("orig_prompt").asText());
                }
                
                if (firstResult.has("actual_prompt")) {
                    result.put("actualPrompt", firstResult.get("actual_prompt").asText());
                }
            }
            // V2版API不再返回base64编码的图片数据
            else {
                result.put("success", false);
                result.put("error", "返回结果中没有图片URL");
            }
        } else {
            result.put("success", false);
            result.put("error", "API返回结果为空");
            logger.error("API返回结果为空");
        }
        
        return result;
    }
} 