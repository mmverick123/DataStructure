package com.traveldiary.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 图片生成提示词请求对象
 */
public class PromptRequest {
    
    @NotBlank(message = "提示词不能为空")
    @Size(max = 1000, message = "提示词长度不能超过1000个字符")
    private String prompt;
    
    public PromptRequest() {
    }
    
    public PromptRequest(String prompt) {
        this.prompt = prompt;
    }
    
    public String getPrompt() {
        return prompt;
    }
    
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
} 