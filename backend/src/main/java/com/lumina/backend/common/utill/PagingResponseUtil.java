package com.lumina.backend.common.utill;

import org.springframework.data.domain.Page;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PagingResponseUtil {

    private PagingResponseUtil() {} // 인스턴스화 방지

    public static <T> Map<String, Object> toPagingResult(
            Page<?> page, int currentPage, String contentKey, List<T> content) {
        Map<String, Object> result = new HashMap<>();
        result.put("totalPages", page.getTotalPages());
        result.put("currentPage", currentPage);
        result.put(contentKey, content);
        return result;
    }

    public static <T> Map<String, Object> toPagingResult(
            int totalPages, int currentPage, String contentKey, List<T> content) {
        Map<String, Object> result = new HashMap<>();
        result.put("totalPages", totalPages);
        result.put("currentPage", currentPage);
        result.put(contentKey, content);
        return result;
    }
}

