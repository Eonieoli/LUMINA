# LUMINA 🌟
> **선함을 기록하고, 나누고, 기부로 이어지게 하는 SNS**

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19.0.0-blue.svg)](https://reactjs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.7.2-blue.svg)](https://www.typescriptlang.org/)
[![Python](https://img.shields.io/badge/Python-3.12.6-blue.svg)](https://www.python.org/)
[![FastAPI](https://img.shields.io/badge/FastAPI-0.115.12-green.svg)](https://fastapi.tiangolo.com/)
[![ElizaOS](https://img.shields.io/badge/ElizaOS-0.1.9-purple.svg)](https://github.com/elizaOS/eliza)
[![Docker](https://img.shields.io/badge/Docker-28.1.1-blue.svg)](https://www.docker.com/)
[![AWS](https://img.shields.io/badge/AWS-EC2%20%7C%20S3-orange.svg)](https://aws.amazon.com/)
[![OneStore](https://img.shields.io/badge/OneStore-출시완료-green.svg)](https://onestore.co.kr/)

LUMINA는 자극적인 컨텐츠와 악플로 피로해진 기존 SNS 환경에서 벗어나, **작은 선행이 기록되고 격려받아 기부로까지 이어지는 선순환 생태계**를 만드는 혁신적인 소셜 플랫폼입니다. AI 기반 선행 감지 시스템과 자동화된 기부 연계 서비스를 통해 사용자의 작은 친절이 큰 변화로 이어지도록 설계되었습니다.

**LK브라더스 기업연계 프로젝트**로 개발되어 실제 원스토어에 출시된 상용 서비스입니다.

---

## 📖 목차

- [주요 기능](#-주요-기능)
- [기술 스택](#-기술-스택)
- [시스템 아키텍처](#-시스템-아키텍처)
- [설치 및 실행](#-설치-및-실행)
- [AI 모델 상세](#-ai-모델-상세)
- [ElizaOS 통합](#-elizaos-통합)
- [API 문서](#-api-문서)
- [팀원](#-팀원)
- [라이선스](#-라이선스)

---

## ✨ 주요 기능

### 🤖 LUNA - AI 선행 관리자
ElizaOS 오픈소스를 기반으로 개발된 **지능형 AI 에이전트**로, 사용자의 선한 행동을 감지하고 격려합니다.

**핵심 역할:**
- **실시간 게시글 분석**: 사용자가 작성한 게시글을 실시간으로 분석하여 격려의 댓글을 자동 생성
- **캠페인 정보 수집**: 선한 영향력 캠페인 정보를 자동으로 크롤링하여 주기적으로 게시물 업로드  
- **멀티턴 대화**: 사용자별 맥락을 유지하며 자연스러운 상호작용 제공
- **개인화된 응답**: 사용자의 행동 패턴과 선호도를 학습하여 맞춤형 격려 메시지 생성

**기술적 특징:**
- ElizaOS 프레임워크 기반의 자율적 AI 에이전트
- Local LLM (Gemma) 및 OpenAI, Llama, Gemini 연동 지원
- JSON 기반 캐릭터 페르소나 정의로 일관된 응답 보장
- SQLite 기반 대화 이력 관리로 개인화된 상호작용

### 📊 선행 감지 및 리워드 시스템
**KcELECTRA 기반 AI 분석**을 통해 선한 행동을 정량화하고 자동으로 포인트를 지급합니다.

**분석 과정:**
- **욕설 1차 필터링**: KcELECTRA 모델로 욕설 여부 판단 (정확도 94.2%)
- **감정 점수 산정**: 비욕설 텍스트의 선함/악함 정도를 -1.0~1.0 범위로 정량화
- **실시간 포인트 적립**: 게시물과 댓글의 선한 영향력 강도에 따라 자동 포인트 지급
- **투명한 평가 기준**: 모든 활동에 대한 명확한 평가 근거 제공

### 💝 리워드 기반 기부 연계 서비스
**하이브리드 AI 모델**을 통해 사용자의 관심사를 분석하여 개인화된 기부처를 추천합니다.

**추천 시스템:**
- **1단계**: Okt 형태소 분석 + 동적 키워드 사전으로 빠른 분류
- **2단계**: KoBERT 기반 임베딩으로 맥락적 이해
- **3단계**: Gemma LLM을 통한 최종 분류 및 추천
- **원클릭 기부**: 적립된 리워드 포인트로 즉시 기부 가능
- **기부 내역 추적**: 개인 기부 이력과 사회적 기여도 시각화

### 🏷️ 스마트 카테고리 시스템
**멀티모달 AI 분석**을 통한 정확한 콘텐츠 분류와 개인화된 추천 서비스를 제공합니다.

**분석 기술:**
- **LLaVA 기반 이미지 분석**: 이미지 내용을 텍스트로 변환하여 맥락 파악
- **OCR 텍스트 추출**: Tesseract OCR로 이미지 내 텍스트 인식
- **통합 분류**: 텍스트, 이미지 설명, 해시태그를 종합하여 12개 카테고리로 분류
- **동적 학습**: 새로운 키워드를 자동으로 학습하여 분류 정확도 지속 향상

---

## 🛠 기술 스택

### Frontend
- **Framework**: React 19.0.0, Vite 6.3.1
- **Language**: TypeScript 5.7.2, JavaScript ES2016
- **Styling**: TailwindCSS 4.1.4
- **State Management**: Zustand 5.0.3
- **Animation**: Framer Motion 12.10.1
- **Routing**: React Router DOM 7.5.2
- **HTTP Client**: Axios 1.9.0
- **Analytics**: React GA4 2.1.0
- **Package Manager**: Yarn
- **Build Tool**: Vite 6.3.1
- **Code Quality**: ESLint, Prettier, SonarQube

### Backend
- **Framework**: Spring Boot 3.4.4
- **Language**: Java 21 (OpenJDK 21)
- **Build Tool**: Gradle 8.13.0
- **Database**: MySQL 8.4.5, Redis 7.4.3
- **Authentication**: JWT, OAuth2 (Google, Kakao)
- **Cloud Storage**: AWS S3
- **IDE**: IntelliJ IDEA 2024.3.1.1
- **Testing**: JUnit 5, Spring Boot Test
- **Code Coverage**: Jacoco (최소 50% 커버리지)

### AI/ML
- **Framework**: FastAPI 0.115.12, PyTorch 2.7.0
- **Language**: Python 3.12.6
- **Models**: 
  - **KcELECTRA**: 욕설 감지 및 감정 분류 (정확도 94.2%)
  - **KoBERT**: 텍스트 임베딩 및 분류
  - **LLaVA-1.5**: 멀티모달 이미지 분석
  - **Gemma-2B**: Local LLM 추론
- **NLP**: Transformers 4.51.3, Tokenizers 0.21.1
- **Data Processing**: Pandas 2.2.3, NumPy 2.2.5, scikit-learn 1.6.1
- **OCR**: Tesseract OCR
- **Web Crawling**: Automated scheduling with Python

### AI Agent (LUNA)
- **Platform**: ElizaOS 0.1.9
- **Runtime**: Node.js 23.3.0, TypeScript 5.6.3
- **Package Manager**: pnpm
- **LLM Integration**: 
  - **Local**: Gemma (Primary)
  - **Cloud**: OpenAI GPT, Llama, Gemini (Fallback)
- **Database**: SQLite (대화 이력 관리)
- **Character System**: JSON 기반 페르소나 정의
- **Automation**: Python Schedule 라이브러리

### Mobile
- **Platform**: Android Native + WebView 하이브리드
- **Distribution**: 원스토어 출시 완료
- **IDE**: Android Studio

### DevOps & Infrastructure
- **Development Environment**: GCP VM
- **Production Environment**: AWS EC2
- **Operating System**: Ubuntu 22.04.4 LTS
- **Containerization**: Docker 28.1.1, Docker Compose 2.335.1
- **CI/CD**: Jenkins 2.492.3
- **Deployment Strategy**: Blue-Green 무중단 배포
- **Reverse Proxy**: Nginx 1.26.3
- **Monitoring**: Prometheus 2.53.3, Grafana 11.5.1
- **Code Quality**: SonarQube, Jacoco Test Coverage
- **SSL**: Let's Encrypt 자동 갱신

---

## 🏗 시스템 아키텍처

### 전체 시스템 구조
```
┌─────────────────────────────────────────────────────────────────┐
│                        Client Layer                             │
├─────────────────┬─────────────────┬─────────────────────────────┤
│   React Web     │  Android App    │      Admin Panel            │
│   (React 19)    │   (WebView)     │   (Monitoring)              │
└─────────────────┴─────────────────┴─────────────────────────────┘
          │                │                      │
          └────────────────┼──────────────────────┘
                           │
┌─────────────────────────────────────────────────────────────────┐
│                    API Gateway (Nginx)                          │
│              SSL Termination + Load Balancing                   │
└─────────────────────────┬───────────────────────────────────────┘
                          │
┌─────────────────────────────────────────────────────────-─-───────┐
│                  Application Layer                                │
├─────────────────┬───────────────────┬─────────────────────────────┤
│  Spring Boot    │   LUNA Agent      │    AI Services              │
│   Backend       │   (ElizaOS)       │   (FastAPI)                 │
│                 │                   │                             │
│  ┌───────────┐  │  ┌─────────────┐  │  ┌──────────────────────-┐  │
│  │User       │  │  │Multi-turn   │  │  │KcELECTRA              │  │
│  │Service    │  │  │Conversation │  │  │(Sentiment Analysis)   │  │
│  └───────────┘  │  └─────────────┘  │  └──────────────────────-┘  │
│  ┌───────────┐  │  ┌─────────────┐  │  ┌──────────────────────-┐  │
│  │Post       │  │  │Character    │  │  │KoBERT + Gemma         │  │
│  │Service    │  │  │System       │  │  │(Category Classifier)  │  │
│  └───────────┘  │  └─────────────┘  │  └──────────────────────-┘  │
│  ┌───────────┐  │  ┌─────────────┐  │  ┌──────────────────────-┐  │
│  │Donation   │  │  │News Crawler │  │  │LLaVA + OCR            │  │
│  │Service    │  │  │& Publisher  │  │  │(Multimodal Analysis)  │  │
│  └───────────┘  │  └─────────────┘  │  └──────────────────────-┘  │
└─────────────────┴───────────────────┴─────────────────────────────┘
          │                │                      │
┌────────────────────────────────────────────────────────────────--─┐
│                     Data Layer                                    │
├─────────────────┬───────────────────┬─────────────────────────────┤
│   MySQL         │     Redis         │       AWS S3                │
│ (Primary DB)    │   (Cache &        │   (File Storage)            │
│                 │    Session)       │                             │
│ ┌─────────────┐ │ ┌───────────────┐ │ ┌─────────────────────────┐ │
│ │User Data    │ │ │Session Cache  │ │ │Profile Images           │ │
│ │Post Data    │ │ │Ranking Cache  │ │ │Post Media               │ │
│ │Donation     │ │ │JWT Tokens     │ │ │Static Assets            │ │
│ │Records      │ │ │               │ │ │                         │ │
│ └─────────────┘ │ └───────────────┘ │ └─────────────────────────┘ │
└─────────────────┴───────────────────┴─────────────────────────────┘
          │                │                      │
┌────────────────────────────────────────────────────────────────--─┐
│                Infrastructure Layer                               │
├─────────────────┬───────────────────┬─────────────────────────────┤
│   CI/CD         │   Monitoring      │    Deployment               │
│  (Jenkins)      │ (Prometheus +     │  (Blue-Green)               │
│                 │  Grafana)         │                             │
│ ┌─────────────┐ │ ┌───────────────┐ │ ┌─────────────────────────┐ │
│ │Automated    │ │ │System Metrics │ │ │Blue Environment         │ │
│ │Testing      │ │ │App Metrics    │ │ │Green Environment        │ │
│ │Build & Push │ │ │Log Analysis   │ │ │Zero-Downtime Deploy     │ │
│ │Deploy       │ │ │Alert Manager  │ │ │Health Check             │ │
│ └─────────────┘ │ └───────────────┘ │ └─────────────────────────┘ │
└─────────────────┴───────────────────┴─────────────────────────────┘
```

### 데이터 플로우
```
User Action → Frontend → API Gateway → Backend → AI Analysis → Database
     ↓           ↑         ↑           ↑         ↑            ↑
LUNA Response ← ElizaOS ← AI Result ← Processing ← Query ← Stored Data
```

---

## 🚀 설치 및 실행

### exec 디렉토리의 포팅 매뉴얼 확인

---

## 🧠 AI 모델 상세

### 1. 욕설 감지 및 감정 분류 모델

**KcELECTRA 기반 파인튜닝 모델**로 한국어 텍스트의 욕설 여부와 감정을 정밀하게 분류합니다.

**모델 사양:**
- **베이스 모델**: KcELECTRA (Korean Conversational ELECTRA)
- **입력**: 사용자 작성 텍스트 (게시물, 댓글)
- **출력**: 
  - 욕설 여부: Binary Classification (True/False)
  - 감정 점수: Regression (-1.0 ~ 1.0, 악함 ↔ 선함)
- **훈련 데이터**: 유튜브/기사 댓글 500,000건
- **성능**: 
  - 욕설 감지 정확도: **94.2%**
  - 감정 분류 F1-Score: **91.8%**

**처리 과정:**
```python
# 1단계: 욕설 여부 판단
is_profanity = model.predict_profanity(text)

# 2단계: 비욕설 텍스트의 감정 분석
if not is_profanity:
    sentiment_score = model.predict_sentiment(text)  # -1.0 ~ 1.0
    kindness_level = classify_kindness(sentiment_score)
```

### 2. 카테고리 분류 및 기부처 추천 모델

**하이브리드 접근법**을 사용하여 텍스트를 12개 기부 카테고리로 정확하게 분류합니다.

**3단계 분류 파이프라인:**

**Stage 1: 고속 키워드 매칭**
```python
# Okt 형태소 분석 + 동적 키워드 사전
tokens = okt.morphs(text)
category = keyword_dict.fast_lookup(tokens)
```

**Stage 2: 의미론적 분석**  
```python
# KoBERT 기반 임베딩 분석
if category is None:
    embedding = kobert.encode(text)
    category = similarity_search(embedding, category_embeddings)
```

**Stage 3: LLM 기반 최종 분류**
```python
# Gemma LLM을 통한 최종 분류 및 새 키워드 학습
if confidence < threshold:
    category = gemma.classify(text)
    keyword_dict.update(new_keywords, category)  # 동적 학습
```

### 3. 멀티모달 게시물 분석

**LLaVA + OCR 조합**으로 이미지와 텍스트를 종합 분석하여 게시물의 맥락을 완전히 이해합니다.

**분석 파이프라인:**
```python
# 1단계: 이미지 시각적 분석
image_description = llava_model.analyze(image)
# 출력 예시: "공원에서 강아지와 함께 산책하는 사람들"

# 2단계: 이미지 내 텍스트 추출  
extracted_text = tesseract_ocr.extract(image)
# 출력 예시: "유기견 입양 행사 참여"

# 3단계: 종합 분석
combined_context = {
    "post_text": user_text,
    "image_description": image_description, 
    "extracted_text": extracted_text,
    "hashtags": hashtags
}

final_category = multimodal_classifier(combined_context)
```

**성능 지표:**
- **이미지 분류 정확도**: 89.3%
- **OCR 텍스트 인식률**: 95.7%  
- **종합 분류 정확도**: 92.1%

---

## 🤖 ElizaOS 통합

### LUNA 에이전트 아키텍처

LUNA는 단순한 챗봇이 아닌 **자율적인 AI 에이전트**로, ElizaOS 프레임워크를 기반으로 구축되었습니다.

**핵심 구성 요소:**

**1. Character System (캐릭터 시스템)**
```json
{
  "name": "LUNA",
  "bio": "선한 행동을 감지하고 격려하는 AI 관리자",
  "personality": {
    "traits": ["empathetic", "encouraging", "wise", "supportive"],
    "speaking_style": "warm and motivational",
    "values": ["kindness", "community", "positive_change"]
  },
  "response_examples": [
    {
      "context": "user_performed_good_deed", 
      "response": "정말 따뜻한 마음이 느껴져요! 이런 작은 친절이 세상을 더 밝게 만듭니다 ✨"
    }
  ]
}
```

**2. Multi-turn Conversation (멀티턴 대화)**
```typescript
class LunaConversationManager {
  private userContexts: Map<string, UserContext> = new Map();
  
  async processUserMessage(userId: string, message: string): Promise<string> {
    // 사용자별 대화 이력 로드
    const context = await this.loadUserContext(userId);
    
    // 컨텍스트 기반 응답 생성
    const response = await this.generateContextualResponse(message, context);
    
    // 대화 이력 업데이트
    await this.updateUserContext(userId, message, response);
    
    return response;
  }
}
```

**3. News Crawling & Publishing (뉴스 수집 및 게시)**
```python
import schedule
import time
from news_crawler import collect_positive_news
from content_processor import transform_to_luna_style

def automated_content_publishing():
    # 선한 영향력 관련 뉴스 수집
    news_articles = collect_positive_news([
        "기부", "봉사", "환경보호", "사회공헌", "선행"
    ])
    
    # LUNA 스타일로 콘텐츠 재가공
    for article in news_articles:
        luna_post = transform_to_luna_style(article)
        publish_to_platform(luna_post)

# 매일 오전 9시, 오후 6시 자동 실행
schedule.every().day.at("09:00").do(automated_content_publishing)
schedule.every().day.at("18:00").do(automated_content_publishing)
```

**4. Backend Integration (백엔드 연동)**
```typescript
class BackendConnector {
  async evaluatePost(postData: PostData): Promise<EvaluationResult> {
    const response = await fetch(`${BACKEND_URL}/api/v1/evaluate-post`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(postData)
    });
    
    return response.json();
  }
  
  async generateReplyMention(commentData: CommentData): Promise<string> {
    const response = await fetch(`${BACKEND_URL}/api/v1/reply-mention`, {
      method: 'POST', 
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(commentData)
    });
    
    const result = await response.json();
    return result.reply;
  }
}
```

### LLM 통합 및 Fallback 시스템

LUNA는 **다중 LLM 지원**으로 안정성과 성능을 보장합니다:

```typescript
enum LLMType {
  LOCAL = 'local',      // Gemma (Primary)
  OPENAI = 'openai',    // GPT-4 (Fallback 1)  
  GEMINI = 'gemini',    // Gemini Pro (Fallback 2)
  LLAMA = 'llama'       // Llama 2 (Fallback 3)
}

class LLMManager {
  async generateResponse(prompt: string): Promise<string> {
    try {
      // 1순위: Local Gemma 모델
      if (this.config.USE_LLM_TYPE === LLMType.LOCAL) {
        return await this.localGemma.generate(prompt);
      }
    } catch (error) {
      console.warn('Local LLM failed, trying OpenAI...');
    }
    
    try {
      // 2순위: OpenAI GPT-4
      return await this.openai.generate(prompt);
    } catch (error) {
      console.warn('OpenAI failed, trying Gemini...');
    }
    
    // 3순위: Google Gemini
    return await this.gemini.generate(prompt);
  }
}
```

---

## 📚 API 문서

### 인증 API

**OAuth2 소셜 로그인**
```http
GET /api/v1/auth/oauth2/{provider}
```
- **Providers**: `google`, `kakao`
- **Response**: JWT Access Token + Refresh Token

**토큰 갱신**
```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### 사용자 API

**내 프로필 조회**
```http
GET /api/v1/users/profile
Authorization: Bearer {accessToken}
```

**사용자 팔로우/언팔로우**  
```http
POST /api/v1/users/follow
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "targetUserId": 123,
  "action": "follow"  // "follow" or "unfollow"
}
```

### 게시물 API

**게시물 목록 조회 (페이징)**
```http
GET /api/v1/posts?page=0&size=10&sort=createdDate,desc
Authorization: Bearer {accessToken}
```

**게시물 작성**
```http
POST /api/v1/posts
Authorization: Bearer {accessToken}  
Content-Type: multipart/form-data

{
  "content": "유기견 보호소에 사료를 기부했어요!",
  "hashtags": ["봉사", "유기견"],
  "images": [File, File],
  "category": "동물보호"
}
```

**AI 기반 게시물 평가**
```http
POST /api/v1/lumina/evaluate-post
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "postId": 123,
  "content": "오늘 봉사활동을 다녀왔어요",
  "images": ["image1.jpg", "image2.jpg"]
}

Response:
{
  "kindnessScore": 0.85,
  "category": "봉사활동", 
  "rewardPoints": 150,
  "aiComment": "정말 의미있는 활동이시네요! 많은 분들에게 영감을 주는 모습입니다 ✨"
}
```

### 댓글 API

**댓글 작성**
```http
POST /api/v1/posts/{postId}/comments
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "content": "정말 훌륭한 활동이네요!",
  "parentCommentId": null  // 대댓글인 경우 부모 댓글 ID
}
```

**LUNA 멘션 댓글 (AI 응답 생성)**
```http
POST /api/v1/lumina/reply-mention  
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "commentId": 456,
  "content": "@LUNA 오늘 좋은 일을 했는데 어떻게 생각해?",
  "userId": 123
}

Response:
{
  "reply": "정말 자랑스러운 하루를 보내셨군요! 이런 작은 선행들이 모여 큰 변화를 만들어냅니다 🌟",
  "sentiment": "positive",
  "encouragementLevel": "high"
}
```

### 기부 API

**AI 추천 기부처 조회**
```http
GET /api/v1/donations/recommendations
Authorization: Bearer {accessToken}

Response:
{
  "recommendations": [
    {
      "id": 1,
      "name": "한국동물보호연합",
      "category": "동물보호",
      "description": "유기견 구조 및 입양 지원",
      "matchScore": 0.92,  // AI 추천 점수
      "reason": "최근 동물 관련 게시물 활동이 많으시네요!"
    }
  ]
}
```

**기부 실행**
```http
POST /api/v1/donations/{donationId}/donate
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "amount": 10000,
  "message": "유기견들에게 도움이 되길 바랍니다"
}
```

**내 기부 내역**
```http
GET /api/v1/donations/my-history?page=0&size=10
Authorization: Bearer {accessToken}

Response: 
{
  "content": [
    {
      "donationName": "한국동물보호연합",
      "amount": 10000,
      "donatedAt": "2024-01-15T10:30:00",
      "message": "유기견들에게 도움이 되길 바랍니다",
      "pointsUsed": 10000
    }
  ],
  "totalDonated": 50000,
  "donationCount": 5
}
```

### 리워드 API

**내 포인트 및 리워드 내역**
```http
GET /api/v1/users/rewards
Authorization: Bearer {accessToken}

Response:
{
  "currentPoints": 15000,
  "totalEarned": 25000, 
  "totalDonated": 10000,
  "kindnessRank": 15,
  "recentRewards": [
    {
      "type": "POST_KINDNESS",
      "points": 100,
      "reason": "따뜻한 게시물 작성",
      "earnedAt": "2024-01-15T14:20:00"
    }
  ]
}
```

### 관리자 API

**사용자 관리**
```http
GET /api/v1/admin/users?page=0&size=20
Authorization: Bearer {adminToken}

POST /api/v1/admin/users/{userId}/role
Authorization: Bearer {adminToken}
Content-Type: application/json

{
  "role": "ADMIN"  // "USER", "ADMIN", "BANNED"
}
```

모든 API는 **OpenAPI 3.0 스펙**을 준수하며, 개발 서버 실행 후 `http://localhost:8080/swagger-ui.html`에서 인터랙티브 문서를 확인할 수 있습니다.

---

## 👥 팀원

LUMINA는 각자의 전문 분야에서 뛰어난 역량을 가진 **6명의 개발자**가 협력하여 개발한 프로젝트입니다.

| 역할 | 이름 | GitHub | 담당 업무 |
|------|------|--------|-----------|
| **🏗 인프라 엔지니어** | 김재혁 | [@jaehyeok](https://github.com/Eonieoli) | DevOps, CI/CD 파이프라인 구축, Blue-Green 무중단 배포, 모니터링 시스템 (Prometheus/Grafana), Jenkins 자동화 |
| **🎨 프론트엔드 개발자** | 김민정 | [@minjeong](https://github.com/99minj0731) | React 19 기반 UI/UX 개발, TypeScript 타입 안전성 구현, Zustand 상태 관리, TailwindCSS 디자인 시스템 |
| **🎨 프론트엔드 개발자** | 홍석진 | [@seokjin](https://github.com/seokbangguri) | Vite 빌드 최적화, 웹 성능 튜닝, PWA 구현, Google Analytics 연동, 반응형 디자인 |
| **⚙️ 백엔드 개발자** | 김경환 | [@kyeonghwan](https://github.com/kimkyeonghwan-1) | Spring Boot 3.4 API 개발, MySQL/Redis 데이터베이스 설계, OAuth2/JWT 인증 시스템, AWS S3 연동 |
| **🤖 AI 엔지니어** | 박우담 | [@woodam](https://github.com/parkwoodam) | KcELECTRA/KoBERT 모델 파인튜닝, 멀티모달 분석 (LLaVA+OCR), FastAPI 서버 개발, AI 추론 최적화 |
| **🤖 AI 에이전트 개발자** | 유정현 | [@junghyun](https://github.com/junghyun58) | ElizaOS 기반 LUNA 에이전트 개발, Local LLM (Gemma) 통합, 자동 뉴스 크롤링 시스템, 멀티턴 대화 관리 |

---

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

```
MIT License

Copyright (c) 2024 LUMINA Team

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 📞 연락처 및 지원

- **이메일**: rublin322@gmail.com

**LUMINA**와 함께 더 따뜻한 세상을 만들어가요! 🌟