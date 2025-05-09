package com.lumina.backend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    /**
     * 비동기/논블로킹 방식의 HTTP 요청을 지원하는 Spring의 HTTP 클라이언트
     *
     * @return WebClient 인스턴스
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}
