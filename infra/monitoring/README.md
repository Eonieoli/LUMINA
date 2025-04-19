# LUMINA 모니터링 시스템

## 개요

LUMINA 프로젝트의 모니터링 시스템은 다음 구성 요소로 이루어져 있습니다:

- **Prometheus**: 시계열 데이터베이스 및 모니터링 시스템
- **Grafana**: 데이터 시각화 및 대시보드 도구
- **Node Exporter**: 서버 자체 메트릭 수집기
- **cAdvisor**: 컨테이너 메트릭 수집기

## 기능

- Spring Boot Actuator 지표 수집
- 서버 리소스 모니터링 (CPU, 메모리, 디스크, 네트워크)
- 컨테이너 리소스 모니터링
- 애플리케이션 건강 상태 모니터링
- 시각화된 대시보드

## 설치 및 실행

모니터링 시스템을 시작하려면 다음 명령어를 실행합니다:

```bash
cd /path/to/S12P31S306/infra/monitoring
docker-compose -f monitoring-compose.yml up -d
```

## 접근 정보

- **Prometheus**: https://picscore.net/prometheus/ (사용자: admin, 비밀번호: admin)
- **Grafana**: https://picscore.net/grafana/ (초기 사용자: admin, 비밀번호: admin)
- **cAdvisor**: https://picscore.net/cadvisor/ (사용자: admin, 비밀번호: admin)

## Grafana 대시보드 설정

1. Grafana에 로그인 (초기 비밀번호는 환경 변수 설정 또는 기본값인 'admin'을 사용)
2. 데이터 소스 추가:
   - Connections > Data Sources > Add data source
   - Prometheus 선택
   - URL: http://prometheus:9090
   - Save & Test
3. 대시보드 가져오기:
   - Dashboards > New > Import
   - 다음 대시보드 ID 중 하나를 입력:
     - JVM (Micrometer): 4701
     - Spring Boot 2.1: 10280
     - Node Exporter: 1860
     - MySQL: 7362
     - Redis: 763
     - 컨테이너 모니터링: 893
4. 필요한 경우 데이터 소스를 'Prometheus'로 선택하고 '가져오기' 클릭

## 알림 설정

Grafana에서 알림을 설정하려면:

1. Alerting > Notification channels로 이동
2. New channel 클릭
3. 필요한 정보 입력 (이메일, Slack 등)
4. Test 후 Save

## 모니터링 데이터

- **Spring Boot 애플리케이션**:
  - HTTP 요청 수 및 응답 시간
  - JVM 메모리 사용량
  - 가비지 컬렉션 지표
  - 스레드 상태

- **시스템**:
  - CPU 사용량
  - 메모리 사용량
  - 디스크 I/O
  - 네트워크 트래픽

- **컨테이너**:
  - CPU 사용량
  - 메모리 사용량
  - 네트워크 I/O
  - 디스크 I/O

## 트러블슈팅

- **Prometheus가 연결되지 않는 경우**:
  - 컨테이너가 실행 중인지 확인: `docker ps`
  - Prometheus 로그 확인: `docker logs prometheus`
  - `/actuator/prometheus` 엔드포인트에 직접 접근 가능한지 확인

- **Grafana 대시보드가 데이터를 표시하지 않는 경우**:
  - Prometheus 데이터 소스 연결 확인
  - 올바른 쿼리가 사용되고 있는지 확인
  - 브라우저 개발자 도구에서 오류 확인

- **cAdvisor가 데이터를 수집하지 않는 경우**:
  - 볼륨 마운트가 올바르게 설정되었는지 확인
  - 권한 문제 확인: `docker logs cadvisor`
