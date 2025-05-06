package com.lumina.backend.common.utill;

import com.lumina.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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


    /**
     * 패턴에 맞는 모든 키를 반환합니다.
     */
    public Set<String> getKeysByPattern(String pattern) {
        return redisTemplate.keys(pattern);
    }


    /**
     * ZSet에 사용자 누적 기부금액을 추가하거나 갱신합니다.
     * 금액이 기존보다 높을 때만 갱신하며, 점수는 '금액 * 1e13 + timestamp' 형태로 저장되어
     * 동점일 경우 더 과거 기록이 우선 정렬됩니다.
     *
     * @param key      ZSet의 Redis 키
     * @param userId   사용자 식별자
     * @param sumPoint 새로 저장할 누적 기부금
     */
    public void addSumPointToZSetWithTTL(
            String key, String userId, Integer sumPoint) {

        // 현재 저장된 score 가져오기
        Double currentRawSumPoint = redisTemplate.opsForZSet().score(key, userId);
        double currentSumPoint = currentRawSumPoint != null ? currentRawSumPoint : -1;

        long now = System.currentTimeMillis();
        double finalSumPoint = sumPoint * 1e13 + (Long.MAX_VALUE - now);

        // 기존 누적 기부금보다 클 때만 갱신
        if (finalSumPoint > currentSumPoint) {

            redisTemplate.opsForZSet().add(key, userId, finalSumPoint);
        }
    }


    /**
     * ZSet에서 누적 기부금액이 높은 순으로 유저 ID 리스트를 반환합니다.
     * 순위는 start ~ end 범위로 지정할 수 있으며, 누적 기부금액이 높은 순으로 정렬됩니다.
     *
     * @param key   ZSet의 Redis 키
     * @param start 시작 인덱스 (0부터 시작)
     * @param end   종료 인덱스
     * @return 사용자 ID 리스트 (점수 내림차순 정렬)
     */
    public List<String> getTopRankersInOrder(
            String key, int start, int end) {

        Set<ZSetOperations.TypedTuple<Object>> tuples = redisTemplate.opsForZSet()
                .reverseRangeWithScores(key, start, end);

        return tuples.stream()
                .map(t -> t.getValue().toString())
                .collect(Collectors.toList()); // List로 변환 → 순서 보장
    }


    /**
     * 특정 사용자 ID의 현재 랭킹을 반환합니다.
     * 누적 기부금액이 높은 순서대로 랭크되며, 0부터 시작합니다.
     *
     * @param key     ZSet의 Redis 키
     * @param userId  사용자 ID
     * @return 사용자의 랭킹 (0: 1위), 없으면 null
     */
    public Long getUserRank(
            String key, String userId) {

        return redisTemplate.opsForZSet().reverseRank(key, userId);
    }


    /**
     * ZSet에서 특정 사용자 ID를 삭제합니다.
     *
     * @param key    ZSet의 Redis 키
     * @param userId 삭제할 사용자 ID
     * @return 삭제된 요소의 개수(0 또는 1)
     */
    public Long removeUserFromZSet(String key, String userId) {
        return redisTemplate.opsForZSet().remove(key, userId);
    }


    /**
     * User-Agent를 분석하여 기기 유형을 판별합니다.
     *
     * @param userAgent HTTP User-Agent 헤더 값
     * @return "pc" 또는 "mobile"
     */
    public String getDeviceType(
            String userAgent) {

        if (userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone")) {
            return "mobile";
        }

        return "pc";
    }
}