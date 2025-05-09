package com.lumina.backend.common.utill;

import org.springframework.data.domain.Page;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PagingResponseUtil {

    private PagingResponseUtil() {} // 인스턴스화 방지


    /**
     * Page 객체를 기반으로 페이징 응답 Map을 생성합니다.
     *
     * @param page       Page 객체
     * @param currentPage 현재 페이지 번호
     * @param contentKey  콘텐츠를 담을 키 이름
     * @param content     콘텐츠 리스트
     * @return 페이징 정보를 담은 Map
     */
    public static <T> Map<String, Object> toPagingResult(
            Page<?> page, int currentPage, String contentKey, List<T> content) {
        Map<String, Object> result = new HashMap<>();
        result.put("totalPages", page.getTotalPages());
        result.put("currentPage", currentPage);
        result.put(contentKey, content);
        return result;
    }


    /**
     * 직접 totalPages 값을 받아 페이징 응답 Map을 생성합니다.
     *
     * @param totalPages  전체 페이지 수
     * @param currentPage 현재 페이지 번호
     * @param contentKey  콘텐츠를 담을 키 이름
     * @param content     콘텐츠 리스트
     * @return 페이징 정보를 담은 Map
     */
    public static <T> Map<String, Object> toPagingResult(
            int totalPages, int currentPage, String contentKey, List<T> content) {
        Map<String, Object> result = new HashMap<>();
        result.put("totalPages", totalPages);
        result.put("currentPage", currentPage);
        result.put(contentKey, content);
        return result;
    }
}

