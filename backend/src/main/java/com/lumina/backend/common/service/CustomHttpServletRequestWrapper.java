package com.lumina.backend.common.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.*;

public class CustomHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private final Map<String, String> customHeaders = new HashMap<>();
    private Cookie[] customCookies;


    /**
     * HttpServletRequest를 감싸서 커스텀 헤더와 쿠키를 추가/수정할 수 있는 래퍼 클래스입니다.
     *
     * @param request 원본 HttpServletRequest
     */
    public CustomHttpServletRequestWrapper(HttpServletRequest request) {

        super(request);
        this.customCookies = request.getCookies(); // 기존 쿠키 복사
    }


    /**
     * 커스텀 헤더 추가 메서드
     *
     * @param name  헤더 이름
     * @param value 헤더 값
     */
    public void addHeader(String name, String value) {

        customHeaders.put(name, value);
    }


    /**
     * 쿠키 값을 업데이트합니다.
     * 동일한 이름의 쿠키가 있으면 값을 변경하고, 없으면 새로 추가합니다.
     *
     * @param name  쿠키 이름
     * @param value 새로운 값
     */
    public void updateCookie(String name, String value) {

        List<Cookie> updatedCookies = new ArrayList<>();

        boolean found = false;
        if (customCookies != null) {
            for (Cookie cookie : customCookies) {
                if (cookie.getName().equals(name)) {
                    cookie.setValue(value);
                    found = true;
                }
                updatedCookies.add(cookie);
            }
        }

        if (!found) {
            updatedCookies.add(new Cookie(name, value));
        }

        customCookies = updatedCookies.toArray(new Cookie[0]);
    }


    @Override
    public String getHeader(String name) {

        // 커스텀 헤더가 있으면 반환, 없으면 원본 헤더 반환
        return customHeaders.getOrDefault(name, super.getHeader(name));
    }


    /**
     * 기존 헤더 이름과 커스텀 헤더 이름을 모두 반환합니다.
     *
     * @return 헤더 이름 목록
     */
    @Override
    public Enumeration<String> getHeaderNames() {

        List<String> headerNames = new ArrayList<>(customHeaders.keySet());
        Enumeration<String> originalHeaderNames = super.getHeaderNames();

        while (originalHeaderNames.hasMoreElements()) {
            headerNames.add(originalHeaderNames.nextElement());
        }

        return Collections.enumeration(headerNames);
    }


    /**
     * 현재 설정된 쿠키 배열을 반환합니다.
     *
     * @return 쿠키 배열
     */
    @Override
    public Cookie[] getCookies() {
        return customCookies;
    }
}

