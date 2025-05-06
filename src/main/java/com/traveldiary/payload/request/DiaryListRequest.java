package com.traveldiary.payload.request;

public class DiaryListRequest {
    private String orderType;

    public DiaryListRequest() {
    }

    public DiaryListRequest(String orderType) {
        this.orderType = orderType;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }
} 