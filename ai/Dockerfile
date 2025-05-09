FROM python:3.12-slim

WORKDIR /app

# 필요한 시스템 패키지 설치
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 필요한 Python 패키지 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 환경 변수 설정 - Hugging Face 모델 ID
ENV ABUSE_MODEL_ID="woobae/kcelectra-swear-detector"
ENV GOODNESS_MODEL_ID="woobae/goodness-electra-v3"
ENV FALLBACK_MODEL_ID="monologg/koelectra-base-v3-discriminator"

# Hugging Face 캐시 디렉토리 생성
RUN mkdir -p /root/.cache/huggingface

# 애플리케이션 코드 복사
COPY . .

# FastAPI 서버를 위한 포트 설정
EXPOSE 8000

# 서버 실행 명령
CMD ["uvicorn", "reward:app", "--host", "0.0.0.0", "--port", "8000"]
