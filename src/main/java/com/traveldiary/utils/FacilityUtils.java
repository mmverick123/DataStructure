package com.traveldiary.utils;

import java.util.ArrayList;
import java.util.List;

import com.traveldiary.model.dto.NearbyFacility;

/**
 * 设施工具类
 */
public class FacilityUtils {
    
    /**
     * 按距离排序（从近到远）
     * @param facilities 设施列表
     * @return 排序后的设施列表
     */
    public static List<NearbyFacility> sortByDistance(List<NearbyFacility> facilities) {
        if (facilities == null || facilities.size() <= 1) {
            return facilities;
        }
        
        List<NearbyFacility> result = new ArrayList<>(facilities);
        quickSortByDistance(result, 0, result.size() - 1);
        return result;
    }
    
    /**
     * 快速排序实现（按距离排序）
     * @param facilities 设施列表
     * @param low 低位索引
     * @param high 高位索引
     */
    private static void quickSortByDistance(List<NearbyFacility> facilities, int low, int high) {
        if (low < high) {
            int pivotIndex = partitionByDistance(facilities, low, high);
            quickSortByDistance(facilities, low, pivotIndex - 1);
            quickSortByDistance(facilities, pivotIndex + 1, high);
        }
    }
    
    /**
     * 分区操作（按距离）
     */
    private static int partitionByDistance(List<NearbyFacility> facilities, int low, int high) {
        // 选择最右边的元素作为基准值
        NearbyFacility pivot = facilities.get(high);
        int i = low - 1;
        
        for (int j = low; j < high; j++) {
            // 升序排列（距离近的排前面）
            if (facilities.get(j).getDistance() <= pivot.getDistance()) {
                i++;
                swap(facilities, i, j);
            }
        }
        
        swap(facilities, i + 1, high);
        return i + 1;
    }
    
    /**
     * 交换列表中两个元素的位置
     */
    private static void swap(List<NearbyFacility> list, int i, int j) {
        NearbyFacility temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }
    
    /**
     * 计算两点之间的距离（米）
     * 使用Haversine公式计算球面距离
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 地球半径（千米）
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // 转换为米

        return Math.round(distance * 10) / 10.0; // 保留一位小数
    }
} 