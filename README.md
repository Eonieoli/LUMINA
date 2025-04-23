# LUMINA

## 프로젝트 개요
LUMINA는 현대적인 웹 어플리케이션으로 사용자 중심의 서비스를 제공합니다.

## 기술 스택

### 백엔드
- Spring Boot 3.4.4
- Java 21
- Spring Data JPA
- Spring Security
- Redis
- MySQL 8.4
- OAuth2 (Google, Kakao)

### 프론트엔드
- React

### 인프라
- Docker, Docker Compose
- Jenkins (CI/CD)
- Nginx (리버스 프록시)
- Let's Encrypt (SSL)

### 모니터링
- Prometheus
- Grafana
- Node Exporter
- cAdvisor
- Spring Boot Actuator

## 아키텍처

```
[사용자] --> [HTTPS/Nginx] --> [프론트엔드(React)] --> [백엔드(Spring Boot)]
                                                        |
                                                        v
                                 [Redis]  <--  [MySQL 데이터베이스]
```

## 모니터링 시스템

프로젝트는 포괄적인 모니터링 시스템을 포함하고 있습니다:

- **애플리케이션 모니터링**: Spring Boot Actuator와 Prometheus 통합
- **인프라 모니터링**: Node Exporter를 이용한 서버 자원 모니터링
- **컨테이너 모니터링**: cAdvisor를 이용한 Docker 컨테이너 모니터링
- **시각화**: Grafana 대시보드

접근 정보:
- Prometheus: https://picscore.net/prometheus/
- Grafana: https://picscore.net/grafana/
- cAdvisor: https://picscore.net/cadvisor/

자세한 정보는 [모니터링 문서](./infra/monitoring/README.md)를 참조하세요.

## 개발 및 배포

### 로컬 개발 환경 설정

```bash
# 프로젝트 클론
git clone https://github.com/username/lumina.git
cd lumina

# 백엔드 실행
cd backend
./gradlew bootRun

# 프론트엔드 실행 (별도 터미널에서)
cd frontend
npm install
npm start
```

### 배포

배포는 Jenkins CI/CD 파이프라인을 통해 자동화되어 있습니다. develop 브랜치에 변경사항이 푸시되면 자동으로 배포가 시작됩니다.

## 기여자

- 개발팀