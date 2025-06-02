package com.traveldiary.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DiaryRequest {
    @NotBlank
    @Size(min = 3, max = 100)
    private String title;

    @NotBlank
    private String content;
    
    private String location;
    
    public DiaryRequest() {
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
} 