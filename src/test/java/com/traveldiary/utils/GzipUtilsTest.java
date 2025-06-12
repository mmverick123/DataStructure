package com.traveldiary.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * GzipUtils工具类测试
 */
public class GzipUtilsTest {
    
    @Test
    public void testCompressAndDecompress() {
        String originalText = "这是一个很长的旅游日记内容，包含了很多重复的文字和描述。" +
                             "今天我们去了北京的故宫，故宫真的很大很壮观。" +
                             "我们在故宫里面走了很久，看到了很多古代的建筑和文物。" +
                             "故宫的红墙黄瓦在阳光下显得格外美丽，让人印象深刻。" +
                             "这次旅行真的很有意义，我会永远记住这美好的一天。";
        
        // 测试压缩
        String compressed = GzipUtils.compress(originalText);
        assertNotNull(compressed);
        assertTrue(compressed.startsWith("GZIP:"));
        
        // 测试解压缩
        String decompressed = GzipUtils.decompress(compressed);
        assertEquals(originalText, decompressed);
    }
    
    @Test
    public void testShortTextNotCompressed() {
        String shortText = "短文本";
        String result = GzipUtils.compress(shortText);
        // 短文本应该不被压缩
        assertEquals(shortText, result);
    }
    
    @Test
    public void testNullAndEmptyText() {
        // 测试null
        assertNull(GzipUtils.compress(null));
        assertNull(GzipUtils.decompress(null));
        
        // 测试空字符串
        assertEquals("", GzipUtils.compress(""));
        assertEquals("", GzipUtils.decompress(""));
    }
    
    @Test
    public void testIsCompressed() {
        String originalText = "这是一个测试文本，用来验证压缩功能是否正常工作。" +
                             "这个文本足够长，应该能够被成功压缩。";
        
        // 原始文本不应该被识别为已压缩
        assertFalse(GzipUtils.isCompressed(originalText));
        
        // 压缩后的文本应该被识别为已压缩
        String compressed = GzipUtils.compress(originalText);
        if (compressed.startsWith("GZIP:")) {
            assertTrue(GzipUtils.isCompressed(compressed));
        }
    }
    
    @Test
    public void testDecompressUncompressedText() {
        String normalText = "这是一个普通的文本";
        // 解压缩未压缩的文本应该返回原文本
        String result = GzipUtils.decompress(normalText);
        assertEquals(normalText, result);
    }
    
    @Test
    public void testBatchCompress() {
        String[] texts = {
            "第一个日记内容，包含了很多详细的描述和感受，记录了美好的旅行时光。",
            "第二个日记内容，描述了另一次精彩的旅行经历，充满了回忆和感动。",
            "第三个日记内容，记录了与朋友们一起旅行的快乐时光，非常有意义。"
        };
        
        String[] compressed = GzipUtils.compressBatch(texts);
        assertNotNull(compressed);
        assertEquals(texts.length, compressed.length);
        
        String[] decompressed = GzipUtils.decompressBatch(compressed);
        assertNotNull(decompressed);
        assertEquals(texts.length, decompressed.length);
        
        for (int i = 0; i < texts.length; i++) {
            assertEquals(texts[i], decompressed[i]);
        }
    }
    
    @Test
    public void testCompressionRatio() {
        String originalText = "这是一个重复的文本。这是一个重复的文本。这是一个重复的文本。" +
                             "这是一个重复的文本。这是一个重复的文本。这是一个重复的文本。";
        
        String compressed = GzipUtils.compress(originalText);
        
        if (GzipUtils.isCompressed(compressed)) {
            double ratio = GzipUtils.getCompressionRatio(originalText, compressed);
            assertTrue(ratio > 0, "压缩率应该大于0");
            assertTrue(ratio <= 100, "压缩率应该小于等于100");
        }
    }
} 