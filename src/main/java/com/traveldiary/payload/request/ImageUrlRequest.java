package com.traveldiary.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 图片URL请求对象
 */
public class ImageUrlRequest {
    
    @NotBlank(message = "图片URL不能为空")
    @Size(max = 1000, message = "图片URL长度不能超过1000个字符")
    private String imageUrl;
    
    public ImageUrlRequest() {
    }
    
    public ImageUrlRequest(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
} 