package com.traveldiary.utils;

import java.util.Comparator;
import java.util.List;

import com.traveldiary.model.Diary;

/**
 * 快速排序工具类
 * 用于对日记列表进行高效排序
 */
public class QuickSortUtils {
    
    /**
     * 按浏览量排序（从高到低）
     * @param diaries 日记列表
     */
    public static void sortByViews(List<Diary> diaries) {
        if (diaries == null || diaries.size() <= 1) {
            return;
        }
        quickSortByViews(diaries, 0, diaries.size() - 1);
    }
    
    /**
     * 按评分排序（从高到低）
     * @param diaries 日记列表
     */
    public static void sortByRating(List<Diary> diaries) {
        if (diaries == null || diaries.size() <= 1) {
            return;
        }
        quickSortByRating(diaries, 0, diaries.size() - 1);
    }
    
    /**
     * 快速排序实现（按浏览量）
     */
    private static void quickSortByViews(List<Diary> diaries, int low, int high) {
        if (low < high) {
            int pivotIndex = partitionByViews(diaries, low, high);
            quickSortByViews(diaries, low, pivotIndex - 1);
            quickSortByViews(diaries, pivotIndex + 1, high);
        }
    }
    
    /**
     * 快速排序实现（按评分）
     */
    private static void quickSortByRating(List<Diary> diaries, int low, int high) {
        if (low < high) {
            int pivotIndex = partitionByRating(diaries, low, high);
            quickSortByRating(diaries, low, pivotIndex - 1);
            quickSortByRating(diaries, pivotIndex + 1, high);
        }
    }
    
    /**
     * 分区操作（按浏览量）
     */
    private static int partitionByViews(List<Diary> diaries, int low, int high) {
        // 选择最右边的元素作为基准值
        Diary pivot = diaries.get(high);
        int i = low - 1;
        
        for (int j = low; j < high; j++) {
            // 降序排列（浏览量高的排前面）
            if (diaries.get(j).getViews() >= pivot.getViews()) {
                i++;
                swap(diaries, i, j);
            }
        }
        
        swap(diaries, i + 1, high);
        return i + 1;
    }
    
    /**
     * 分区操作（按评分）
     */
    private static int partitionByRating(List<Diary> diaries, int low, int high) {
        // 选择最右边的元素作为基准值
        Diary pivot = diaries.get(high);
        int i = low - 1;
        
        for (int j = low; j < high; j++) {
            // 降序排列（评分高的排前面）
            if (diaries.get(j).getAverageRating() >= pivot.getAverageRating()) {
                i++;
                swap(diaries, i, j);
            }
        }
        
        swap(diaries, i + 1, high);
        return i + 1;
    }
    
    /**
     * 交换列表中两个元素的位置
     */
    private static void swap(List<Diary> list, int i, int j) {
        Diary temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }
    
    /**
     * 按标题排序（从A到Z）
     * @param diaries 日记列表
     */
    public static void sortByTitle(List<Diary> diaries) {
        if (diaries == null || diaries.size() <= 1) {
            return;
        }
        quickSortByTitle(diaries, 0, diaries.size() - 1);
    }
    
    /**
     * 快速排序实现（按标题）
     */
    private static void quickSortByTitle(List<Diary> diaries, int low, int high) {
        if (low < high) {
            int pivotIndex = partitionByTitle(diaries, low, high);
            quickSortByTitle(diaries, low, pivotIndex - 1);
            quickSortByTitle(diaries, pivotIndex + 1, high);
        }
    }
    
    /**
     * 分区操作（按标题）
     */
    private static int partitionByTitle(List<Diary> diaries, int low, int high) {
        // 选择最右边的元素作为基准值
        Diary pivot = diaries.get(high);
        int i = low - 1;
        
        for (int j = low; j < high; j++) {
            // 升序排列（按字母顺序）
            if (diaries.get(j).getTitle().compareTo(pivot.getTitle()) <= 0) {
                i++;
                swap(diaries, i, j);
            }
        }
        
        swap(diaries, i + 1, high);
        return i + 1;
    }
    
    /**
     * 根据标题关键词搜索并排序
     * @param diaries 日记列表
     * @param keyword 关键词
     * @return 过滤并排序后的列表
     */
    public static List<Diary> searchAndSortByTitle(List<Diary> diaries, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return diaries;
        }
        
        String lowercaseKeyword = keyword.toLowerCase();
        
        // 使用Java 8 Stream API进行过滤和排序
        return diaries.stream()
                .filter(diary -> diary.getTitle().toLowerCase().contains(lowercaseKeyword))
                .sorted(Comparator.comparing(Diary::getTitle))
                .toList();
    }
} 