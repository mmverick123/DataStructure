package com.traveldiary.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GZIP压缩工具类
 * 用于旅游日记文字内容的无损压缩
 */
public class GzipUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(GzipUtils.class);
    
    /**
     * 压缩字符串
     * @param text 待压缩的文本
     * @return 压缩后的Base64编码字符串，如果压缩失败返回原文本
     */
    public static String compress(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        try {
            byte[] data = text.getBytes(StandardCharsets.UTF_8);
            
            // 如果文本太短，压缩可能不会减少大小，直接返回原文本
            if (data.length < 100) {
                return text;
            }
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
                gzipOut.write(data);
                gzipOut.finish();
            }
            
            byte[] compressedData = baos.toByteArray();
            
            // 如果压缩后的大小没有明显减少，返回原文本
            if (compressedData.length >= data.length * 0.9) {
                logger.debug("压缩效果不明显，返回原文本。原大小: {}, 压缩后大小: {}", 
                           data.length, compressedData.length);
                return text;
            }
            
            String compressed = Base64.getEncoder().encodeToString(compressedData);
            logger.debug("文本压缩成功。原大小: {} bytes, 压缩后大小: {} bytes, 压缩率: {:.2f}%", 
                       data.length, compressedData.length, 
                       (1.0 - (double)compressedData.length / data.length) * 100);
            
            // 添加压缩标识前缀
            return "GZIP:" + compressed;
            
        } catch (IOException e) {
            logger.error("文本压缩失败", e);
            return text; // 压缩失败时返回原文本
        }
    }
    
    /**
     * 解压缩字符串
     * @param compressedText 压缩后的文本（可能包含GZIP:前缀）
     * @return 解压缩后的原文本
     */
    public static String decompress(String compressedText) {
        if (compressedText == null || compressedText.isEmpty()) {
            return compressedText;
        }
        
        // 检查是否是压缩过的文本
        if (!compressedText.startsWith("GZIP:")) {
            return compressedText; // 未压缩的文本直接返回
        }
        
        try {
            // 移除压缩标识前缀
            String base64Data = compressedText.substring(5);
            byte[] compressedData = Base64.getDecoder().decode(base64Data);
            
            ByteArrayInputStream bais = new ByteArrayInputStream(compressedData);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            try (GZIPInputStream gzipIn = new GZIPInputStream(bais)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = gzipIn.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
            }
            
            String decompressed = baos.toString(StandardCharsets.UTF_8);
            logger.debug("文本解压缩成功。压缩大小: {} bytes, 解压后大小: {} bytes", 
                       compressedData.length, decompressed.getBytes(StandardCharsets.UTF_8).length);
            
            return decompressed;
            
        } catch (Exception e) {
            logger.error("文本解压缩失败，返回原文本", e);
            return compressedText; // 解压缩失败时返回原文本
        }
    }
    
    /**
     * 检查文本是否已压缩
     * @param text 待检查的文本
     * @return true表示已压缩，false表示未压缩
     */
    public static boolean isCompressed(String text) {
        return text != null && text.startsWith("GZIP:");
    }
    
    /**
     * 获取压缩率
     * @param originalText 原文本
     * @param compressedText 压缩后的文本
     * @return 压缩率（百分比），如果无法计算返回0
     */
    public static double getCompressionRatio(String originalText, String compressedText) {
        if (originalText == null || compressedText == null || !isCompressed(compressedText)) {
            return 0.0;
        }
        
        try {
            int originalSize = originalText.getBytes(StandardCharsets.UTF_8).length;
            String base64Data = compressedText.substring(5);
            int compressedSize = Base64.getDecoder().decode(base64Data).length;
            
            return (1.0 - (double)compressedSize / originalSize) * 100;
        } catch (Exception e) {
            logger.error("计算压缩率失败", e);
            return 0.0;
        }
    }
    
    /**
     * 批量压缩文本数组
     * @param texts 待压缩的文本数组
     * @return 压缩后的文本数组
     */
    public static String[] compressBatch(String... texts) {
        if (texts == null) {
            return null;
        }
        
        String[] compressed = new String[texts.length];
        for (int i = 0; i < texts.length; i++) {
            compressed[i] = compress(texts[i]);
        }
        return compressed;
    }
    
    /**
     * 批量解压缩文本数组
     * @param compressedTexts 压缩后的文本数组
     * @return 解压缩后的文本数组
     */
    public static String[] decompressBatch(String... compressedTexts) {
        if (compressedTexts == null) {
            return null;
        }
        
        String[] decompressed = new String[compressedTexts.length];
        for (int i = 0; i < compressedTexts.length; i++) {
            decompressed[i] = decompress(compressedTexts[i]);
        }
        return decompressed;
    }
} 