package com.lumina.backend.common.utill;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CookieUtil {

    private CookieUtil() {} // 인스턴스화 방지


    /**
     * 쿠키를 생성합니다.
     *
     * @param key 쿠키 이름
     * @param value 쿠키 값
     * @return 생성된 Cookie 객체
     */
    public static Cookie createCookie(
            String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60 * 60 * 24); // 1일 유지
//        cookie.setSecure(true); // HTTPS에서만 전송 (배포 환경에서는 필수)
        cookie.setHttpOnly(true); // JavaScript에서 접근 불가
        cookie.setPath("/"); // 모든 경로에서 접근 가능

        return cookie;
    }


    /**
     * 특정 이름의 쿠키를 삭제하는 헬퍼 메서드
     *
     * @param response HTTP 응답 객체
     * @param cookieName 삭제할 쿠키의 이름
     */
    public static void deleteCookie(
            HttpServletResponse response, String cookieName) {

        Cookie cookie = new Cookie(cookieName, null); // 쿠키 값을 null로 설정
        cookie.setMaxAge(0); // 쿠키 만료 시간을 0으로 설정 (즉시 삭제)
        cookie.setPath("/"); // 쿠키 경로를 루트로 설정 (애플리케이션 전체에 적용)
        response.addCookie(cookie); // 응답에 삭제할 쿠키 추가
    }


    /**
     * 요청에서 특정 이름의 쿠키 값을 반환합니다.
     *
     * @param request HTTP 요청 객체
     * @param name    쿠키 이름
     * @return        쿠키 값, 없으면 null
     */
    public static String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
