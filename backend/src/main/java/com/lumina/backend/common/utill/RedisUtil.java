package com.lumina.backend.common.utill;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;


    /**
     * 키-값 쌍을 저장하고 만료 시간을 설정합니다. (SETEX 기능)
     *
     * @param key     저장할 키
     * @param value   저장할 값
     * @param seconds 만료 시간(초)
     */
    public void setex(
            String key, Object value, long seconds) {

        redisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
    }


    /**
     * 키의 남은 만료 시간을 조회합니다.
     *
     * @param key 조회할 키
     * @return 남은 만료 시간(초), 키가 없거나 만료 시간이 설정되지 않은 경우 null
     */
    public Long getTtl(
            String key) {

        return redisTemplate.getExpire(key);
    }


    /**
     * 키에 해당하는 값을 조회합니다.
     *
     * @param key 조회할 키
     * @return 저장된 값, 키가 없는 경우 null
     */
    public Object get(
            String key) {

        return redisTemplate.opsForValue().get(key);
    }


    /**
     * 키의 존재 여부를 확인합니다.
     *
     * @param key 확인할 키
     * @return 키가 존재하면 true, 그렇지 않으면 false
     */
    public Boolean exists(
            String key) {

        return redisTemplate.hasKey(key);
    }


    /**
     * 지정된 키를 삭제합니다.
     *
     * @param key 삭제할 키
     * @return 키가 성공적으로 삭제되면 true, 키가 존재하지 않으면 false
     */
    public Boolean delete(
            String key) {

        return redisTemplate.delete(key);
    }
}