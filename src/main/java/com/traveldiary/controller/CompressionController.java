package com.traveldiary.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.traveldiary.service.CompressionService;

/**
 * 压缩管理控制器
 * 提供旅游日记压缩相关的API接口
 */
@RestController
@RequestMapping("/api/compression")
public class CompressionController {
    
    private final CompressionService compressionService;
    
    @Autowired
    public CompressionController(CompressionService compressionService) {
        this.compressionService = compressionService;
    }
    
    /**
     * 获取压缩统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getCompressionStatistics() {
        Map<String, Object> statistics = compressionService.getCompressionStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * 批量压缩所有日记
     */
    @PostMapping("/compress-all")
    public ResponseEntity<Map<String, Object>> compressAllDiaries() {
        Map<String, Object> result = compressionService.compressAllDiaries();
        return ResponseEntity.ok(result);
    }
    
    /**
     * 批量解压缩所有日记
     */
    @PostMapping("/decompress-all")
    public ResponseEntity<Map<String, Object>> decompressAllDiaries() {
        Map<String, Object> result = compressionService.decompressAllDiaries();
        return ResponseEntity.ok(result);
    }
    
    /**
     * 检查指定日记的压缩状态
     */
    @GetMapping("/diary/{id}/status")
    public ResponseEntity<Map<String, Object>> checkDiaryCompressionStatus(@PathVariable Long id) {
        Map<String, Object> status = compressionService.checkDiaryCompressionStatus(id);
        return ResponseEntity.ok(status);
    }
    
    /**
     * 压缩指定日记
     */
    @PostMapping("/diary/{id}/compress")
    public ResponseEntity<Map<String, Object>> compressDiary(@PathVariable Long id) {
        Map<String, Object> result = compressionService.compressDiary(id);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 解压缩指定日记
     */
    @PostMapping("/diary/{id}/decompress")
    public ResponseEntity<Map<String, Object>> decompressDiary(@PathVariable Long id) {
        Map<String, Object> result = compressionService.decompressDiary(id);
        return ResponseEntity.ok(result);
    }
} 