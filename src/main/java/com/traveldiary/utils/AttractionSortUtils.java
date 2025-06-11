package com.traveldiary.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import com.traveldiary.model.Attraction;

/**
 * 景点排序工具类
 * 实现Top-K排序算法，用于高效获取前K个最佳景点
 */
public class AttractionSortUtils {
    
    /**
     * 按热度获取前K个景点（使用最小堆实现Top-K算法）
     * 时间复杂度：O(n log k)，空间复杂度：O(k)
     * 
     * @param attractions 景点列表
     * @param k 需要获取的前K个数量
     * @return 按热度排序的前K个景点
     */
    public static List<Attraction> getTopKByViews(List<Attraction> attractions, int k) {
        if (attractions == null || attractions.isEmpty() || k <= 0) {
            return new ArrayList<>();
        }
        
        // 使用最小堆维护前K个最大值
        PriorityQueue<Attraction> minHeap = new PriorityQueue<>(
            Comparator.comparingLong(Attraction::getTotalViews)
        );
        
        for (Attraction attraction : attractions) {
            if (minHeap.size() < k) {
                minHeap.offer(attraction);
            } else if (attraction.getTotalViews() > minHeap.peek().getTotalViews()) {
                minHeap.poll();
                minHeap.offer(attraction);
            }
        }
        
        // 将堆中元素转换为列表并按降序排列
        List<Attraction> result = new ArrayList<>(minHeap);
        result.sort((a, b) -> Long.compare(b.getTotalViews(), a.getTotalViews()));
        
        return result;
    }
    
    /**
     * 按评价获取前K个景点（使用最小堆实现Top-K算法）
     * 时间复杂度：O(n log k)，空间复杂度：O(k)
     * 
     * @param attractions 景点列表
     * @param k 需要获取的前K个数量
     * @return 按评价排序的前K个景点
     */
    public static List<Attraction> getTopKByRating(List<Attraction> attractions, int k) {
        if (attractions == null || attractions.isEmpty() || k <= 0) {
            return new ArrayList<>();
        }
        
        // 使用最小堆维护前K个最大值
        PriorityQueue<Attraction> minHeap = new PriorityQueue<>(
            Comparator.comparingDouble(Attraction::getAverageRating)
        );
        
        for (Attraction attraction : attractions) {
            if (minHeap.size() < k) {
                minHeap.offer(attraction);
            } else if (attraction.getAverageRating() > minHeap.peek().getAverageRating()) {
                minHeap.poll();
                minHeap.offer(attraction);
            }
        }
        
        // 将堆中元素转换为列表并按降序排列
        List<Attraction> result = new ArrayList<>(minHeap);
        result.sort((a, b) -> Double.compare(b.getAverageRating(), a.getAverageRating()));
        
        return result;
    }
    
    /**
     * 按综合评分获取前K个景点（热度和评价的加权平均）
     * 时间复杂度：O(n log k)，空间复杂度：O(k)
     * 
     * @param attractions 景点列表
     * @param k 需要获取的前K个数量
     * @param viewsWeight 热度权重（0-1之间）
     * @param ratingWeight 评价权重（0-1之间）
     * @return 按综合评分排序的前K个景点
     */
    public static List<Attraction> getTopKByCompositeScore(List<Attraction> attractions, int k, 
                                                          double viewsWeight, double ratingWeight) {
        if (attractions == null || attractions.isEmpty() || k <= 0) {
            return new ArrayList<>();
        }
        
        // 标准化权重
        double totalWeight = viewsWeight + ratingWeight;
        if (totalWeight > 0) {
            viewsWeight /= totalWeight;
            ratingWeight /= totalWeight;
        } else {
            viewsWeight = 0.5;
            ratingWeight = 0.5;
        }
        
        // 计算最大值用于标准化
        long maxViews = attractions.stream().mapToLong(Attraction::getTotalViews).max().orElse(1L);
        double maxRating = attractions.stream().mapToDouble(Attraction::getAverageRating).max().orElse(1.0);
        
        final double finalViewsWeight = viewsWeight;
        final double finalRatingWeight = ratingWeight;
        
        // 使用最小堆维护前K个最大值
        PriorityQueue<Attraction> minHeap = new PriorityQueue<>((a, b) -> {
            double scoreA = calculateCompositeScore(a, maxViews, maxRating, finalViewsWeight, finalRatingWeight);
            double scoreB = calculateCompositeScore(b, maxViews, maxRating, finalViewsWeight, finalRatingWeight);
            return Double.compare(scoreA, scoreB);
        });
        
        for (Attraction attraction : attractions) {
            if (minHeap.size() < k) {
                minHeap.offer(attraction);
            } else {
                double currentScore = calculateCompositeScore(attraction, maxViews, maxRating, 
                                                            finalViewsWeight, finalRatingWeight);
                double minScore = calculateCompositeScore(minHeap.peek(), maxViews, maxRating, 
                                                        finalViewsWeight, finalRatingWeight);
                if (currentScore > minScore) {
                    minHeap.poll();
                    minHeap.offer(attraction);
                }
            }
        }
        
        // 将堆中元素转换为列表并按降序排列
        List<Attraction> result = new ArrayList<>(minHeap);
        result.sort((a, b) -> {
            double scoreA = calculateCompositeScore(a, maxViews, maxRating, finalViewsWeight, finalRatingWeight);
            double scoreB = calculateCompositeScore(b, maxViews, maxRating, finalViewsWeight, finalRatingWeight);
            return Double.compare(scoreB, scoreA);
        });
        
        return result;
    }
    
    /**
     * 计算综合评分
     */
    private static double calculateCompositeScore(Attraction attraction, long maxViews, double maxRating,
                                                double viewsWeight, double ratingWeight) {
        double normalizedViews = maxViews > 0 ? (double) attraction.getTotalViews() / maxViews : 0;
        double normalizedRating = maxRating > 0 ? attraction.getAverageRating() / maxRating : 0;
        
        return normalizedViews * viewsWeight + normalizedRating * ratingWeight;
    }
    
    /**
     * 快速排序实现（完整排序）
     * 时间复杂度：平均O(n log n)，最坏O(n²)
     * 
     * @param attractions 景点列表
     * @param sortBy 排序方式："views" 或 "rating"
     * @return 排序后的景点列表
     */
    public static List<Attraction> quickSort(List<Attraction> attractions, String sortBy) {
        if (attractions == null || attractions.size() <= 1) {
            return new ArrayList<>(attractions != null ? attractions : new ArrayList<>());
        }
        
        List<Attraction> result = new ArrayList<>(attractions);
        
        if ("views".equalsIgnoreCase(sortBy)) {
            quickSortByViews(result, 0, result.size() - 1);
        } else if ("rating".equalsIgnoreCase(sortBy)) {
            quickSortByRating(result, 0, result.size() - 1);
        }
        
        return result;
    }
    
    /**
     * 按热度快速排序（降序）
     */
    private static void quickSortByViews(List<Attraction> attractions, int low, int high) {
        if (low < high) {
            int pivotIndex = partitionByViews(attractions, low, high);
            quickSortByViews(attractions, low, pivotIndex - 1);
            quickSortByViews(attractions, pivotIndex + 1, high);
        }
    }
    
    /**
     * 按评价快速排序（降序）
     */
    private static void quickSortByRating(List<Attraction> attractions, int low, int high) {
        if (low < high) {
            int pivotIndex = partitionByRating(attractions, low, high);
            quickSortByRating(attractions, low, pivotIndex - 1);
            quickSortByRating(attractions, pivotIndex + 1, high);
        }
    }
    
    /**
     * 按热度分区
     */
    private static int partitionByViews(List<Attraction> attractions, int low, int high) {
        long pivot = attractions.get(high).getTotalViews();
        int i = low - 1;
        
        for (int j = low; j < high; j++) {
            if (attractions.get(j).getTotalViews() >= pivot) { // 降序
                i++;
                swap(attractions, i, j);
            }
        }
        
        swap(attractions, i + 1, high);
        return i + 1;
    }
    
    /**
     * 按评价分区
     */
    private static int partitionByRating(List<Attraction> attractions, int low, int high) {
        double pivot = attractions.get(high).getAverageRating();
        int i = low - 1;
        
        for (int j = low; j < high; j++) {
            if (attractions.get(j).getAverageRating() >= pivot) { // 降序
                i++;
                swap(attractions, i, j);
            }
        }
        
        swap(attractions, i + 1, high);
        return i + 1;
    }
    
    /**
     * 交换列表中两个元素的位置
     */
    private static void swap(List<Attraction> attractions, int i, int j) {
        Attraction temp = attractions.get(i);
        attractions.set(i, attractions.get(j));
        attractions.set(j, temp);
    }
} 