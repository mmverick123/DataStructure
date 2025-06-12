package com.traveldiary.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.traveldiary.model.Diary;

/**
 * 日记搜索工具类
 * 提供自定义的搜索和排序算法，不依赖外部API
 */
public class SearchUtils {
    
    /**
     * 根据标题关键词搜索日记
     * 
     * @param diaries 日记列表
     * @param keyword 关键词
     * @return 匹配的日记列表
     */
    public static List<Diary> searchByTitle(List<Diary> diaries, String keyword) {
        if (keyword == null || keyword.trim().isEmpty() || diaries == null || diaries.isEmpty()) {
            return new ArrayList<>(diaries != null ? diaries : new ArrayList<>());
        }
        
        String lowerKeyword = keyword.toLowerCase();
        List<Diary> result = new ArrayList<>();
        
        for (Diary diary : diaries) {
            if (diary.getTitle() != null && diary.getTitle().toLowerCase().contains(lowerKeyword)) {
                result.add(diary);
            }
        }
        
        return result;
    }
    
    /**
     * 根据内容关键词搜索日记
     * 
     * @param diaries 日记列表
     * @param keyword 关键词
     * @return 匹配的日记列表
     */
    public static List<Diary> searchByContent(List<Diary> diaries, String keyword) {
        if (keyword == null || keyword.trim().isEmpty() || diaries == null || diaries.isEmpty()) {
            return new ArrayList<>(diaries != null ? diaries : new ArrayList<>());
        }
        
        String lowerKeyword = keyword.toLowerCase();
        List<Diary> result = new ArrayList<>();
        
        for (Diary diary : diaries) {
            if (diary.getContent() != null && diary.getContent().toLowerCase().contains(lowerKeyword)) {
                result.add(diary);
            }
        }
        
        return result;
    }
    
    /**
     * 按标题字母顺序排序
     * 
     * @param diaries 日记列表
     */
    public static void sortByTitle(List<Diary> diaries) {
        if (diaries == null || diaries.size() <= 1) {
            return;
        }
        
        mergeSort(diaries, 0, diaries.size() - 1, (d1, d2) -> {
            if (d1.getTitle() == null) return (d2.getTitle() == null) ? 0 : -1;
            if (d2.getTitle() == null) return 1;
            return d1.getTitle().compareToIgnoreCase(d2.getTitle());
        });
    }
    
    /**
     * 按创建时间降序排序（最新的排在前面）
     * 
     * @param diaries 日记列表
     */
    public static void sortByCreatedTimeDesc(List<Diary> diaries) {
        if (diaries == null || diaries.size() <= 1) {
            return;
        }
        
        mergeSort(diaries, 0, diaries.size() - 1, (d1, d2) -> {
            if (d1.getCreatedAt() == null) return (d2.getCreatedAt() == null) ? 0 : 1;
            if (d2.getCreatedAt() == null) return -1;
            return d2.getCreatedAt().compareTo(d1.getCreatedAt());
        });
    }
    
    /**
     * 按浏览量降序排序
     * 
     * @param diaries 日记列表
     */
    public static void sortByViewsDesc(List<Diary> diaries) {
        if (diaries == null || diaries.size() <= 1) {
            return;
        }
        
        mergeSort(diaries, 0, diaries.size() - 1, (d1, d2) -> Integer.compare(d2.getViews(), d1.getViews()));
    }
    
    /**
     * 按评分降序排序
     * 
     * @param diaries 日记列表
     */
    public static void sortByRatingDesc(List<Diary> diaries) {
        if (diaries == null || diaries.size() <= 1) {
            return;
        }
        
        mergeSort(diaries, 0, diaries.size() - 1, (d1, d2) -> Double.compare(d2.getAverageRating(), d1.getAverageRating()));
    }
    
    /**
     * 通用归并排序算法
     * 
     * @param <T> 列表元素类型
     * @param list 要排序的列表
     * @param start 起始索引
     * @param end 结束索引
     * @param comparator 比较器
     */
    private static <T> void mergeSort(List<T> list, int start, int end, Comparator<T> comparator) {
        if (start < end) {
            int mid = start + (end - start) / 2;
            mergeSort(list, start, mid, comparator);
            mergeSort(list, mid + 1, end, comparator);
            merge(list, start, mid, end, comparator);
        }
    }
    
    /**
     * 归并排序的合并操作
     * 
     * @param <T> 列表元素类型
     * @param list 要合并的列表
     * @param start 起始索引
     * @param mid 中间索引
     * @param end 结束索引
     * @param comparator 比较器
     */
    @SuppressWarnings("unchecked")
    private static <T> void merge(List<T> list, int start, int mid, int end, Comparator<T> comparator) {
        // 创建临时数组
        Object[] temp = new Object[end - start + 1];
        
        int i = start;      // 左半部分起始索引
        int j = mid + 1;    // 右半部分起始索引
        int k = 0;          // 临时数组索引
        
        // 合并两个有序部分
        while (i <= mid && j <= end) {
            if (comparator.compare(list.get(i), list.get(j)) <= 0) {
                temp[k++] = list.get(i++);
            } else {
                temp[k++] = list.get(j++);
            }
        }
        
        // 复制左半部分剩余元素
        while (i <= mid) {
            temp[k++] = list.get(i++);
        }
        
        // 复制右半部分剩余元素
        while (j <= end) {
            temp[k++] = list.get(j++);
        }
        
        // 将临时数组中的元素复制回原数组
        for (i = 0; i < temp.length; i++) {
            list.set(start + i, (T) temp[i]);
        }
    }
    
    /**
     * KMP算法实现字符串匹配
     * 比简单的contains方法更高效，尤其是对于长文本
     * 
     * @param text 文本
     * @param pattern 模式串
     * @return 是否匹配
     */
    public static boolean kmpSearch(String text, String pattern) {
        if (text == null || pattern == null) {
            return false;
        }
        
        if (pattern.isEmpty()) {
            return true;
        }
        
        if (text.isEmpty()) {
            return false;
        }
        
        char[] textArray = text.toLowerCase().toCharArray();
        char[] patternArray = pattern.toLowerCase().toCharArray();
        
        // 计算部分匹配表
        int[] lps = computeLPSArray(patternArray);
        
        int i = 0; // 文本指针
        int j = 0; // 模式串指针
        
        while (i < textArray.length) {
            if (patternArray[j] == textArray[i]) {
                j++;
                i++;
            }
            
            if (j == patternArray.length) {
                // 找到匹配
                return true;
            } else if (i < textArray.length && patternArray[j] != textArray[i]) {
                // 不匹配
                if (j != 0) {
                    j = lps[j - 1];
                } else {
                    i++;
                }
            }
        }
        
        return false;
    }
    
    /**
     * 计算KMP算法的部分匹配表
     * 
     * @param pattern 模式串
     * @return 部分匹配表
     */
    private static int[] computeLPSArray(char[] pattern) {
        int[] lps = new int[pattern.length];
        int length = 0;
        int i = 1;
        
        while (i < pattern.length) {
            if (pattern[i] == pattern[length]) {
                length++;
                lps[i] = length;
                i++;
            } else {
                if (length != 0) {
                    length = lps[length - 1];
                } else {
                    lps[i] = 0;
                    i++;
                }
            }
        }
        
        return lps;
    }
} 