from fastapi import FastAPI
from pydantic import BaseModel
from transformers import AutoTokenizer, AutoModelForSequenceClassification
import torch
import uvicorn
import os
import logging

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI()
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

# 모델 정보 설정 - 기본적으로 사용자의 커스텀 모델을 사용합니다
# 환경 변수로 다른 모델을 지정할 수도 있습니다
ABUSE_MODEL_ID = os.environ.get("ABUSE_MODEL_ID", "woobae/kcelectra-swear-detector")
GOODNESS_MODEL_ID = os.environ.get("GOODNESS_MODEL_ID", "woobae/goodness-electra-v3")

# 대체 모델 - 기본 모델 로드에 실패할 경우 사용합니다
FALLBACK_MODEL_ID = "monologg/koelectra-base-v3-discriminator"

# 모델 로딩 함수 - 첫 요청 시 모델을 로드합니다
abuse_tokenizer = None
abuse_model = None
goodness_tokenizer = None
goodness_model = None

def load_models():
    global abuse_tokenizer, abuse_model, goodness_tokenizer, goodness_model
    
    logger.info(f"Abuse 모델 로딩 시작: {ABUSE_MODEL_ID}")
    try:
        abuse_tokenizer = AutoTokenizer.from_pretrained(ABUSE_MODEL_ID)
        abuse_model = AutoModelForSequenceClassification.from_pretrained(ABUSE_MODEL_ID).to(device)
        abuse_model.eval()
        logger.info("Abuse 모델 로딩 완료")
    except Exception as e:
        logger.error(f"Abuse 모델 로딩 실패: {str(e)}")
        # 대체 모델로 폴백합니다
        logger.info(f"대체 모델로 시도합니다: {FALLBACK_MODEL_ID}")
        try:
            abuse_tokenizer = AutoTokenizer.from_pretrained(FALLBACK_MODEL_ID)
            abuse_model = AutoModelForSequenceClassification.from_pretrained(FALLBACK_MODEL_ID).to(device)
            abuse_model.eval()
            logger.info("대체 모델 로딩 성공")
        except Exception as fallback_error:
            logger.error(f"대체 모델도 로딩 실패: {str(fallback_error)}")
            abuse_tokenizer = None
            abuse_model = None
    
    logger.info(f"Goodness 모델 로딩 시작: {GOODNESS_MODEL_ID}")
    try:
        goodness_tokenizer = AutoTokenizer.from_pretrained(GOODNESS_MODEL_ID)
        goodness_model = AutoModelForSequenceClassification.from_pretrained(GOODNESS_MODEL_ID).to(device)
        goodness_model.eval()
        logger.info("Goodness 모델 로딩 완료")
    except Exception as e:
        logger.error(f"Goodness 모델 로딩 실패: {str(e)}")
        # 대체 모델로 폴백합니다
        logger.info(f"대체 모델로 시도합니다: {FALLBACK_MODEL_ID}")
        try:
            goodness_tokenizer = AutoTokenizer.from_pretrained(FALLBACK_MODEL_ID)
            goodness_model = AutoModelForSequenceClassification.from_pretrained(FALLBACK_MODEL_ID).to(device)
            goodness_model.eval()
            logger.info("대체 모델 로딩 성공")
        except Exception as fallback_error:
            logger.error(f"대체 모델도 로딩 실패: {str(fallback_error)}")
            goodness_tokenizer = None
            goodness_model = None

# 서버 시작 시 모델 로딩 시작 (미리 준비)
@app.on_event("startup")
async def startup_event():
    logger.info("서버 시작 - 모델 로딩을 시작합니다")
    load_models()

# ✅ Threshold 설정
THRESHOLD_DEFAULT = 0.53
THRESHOLD_SHORT = 0.7

# ✅ 필터용 단어 리스트
non_abuse_words = [
    "으휴",
    "하아",
    "아이고",
    "허허",
    "음",
    "히히",
    "히히히",
    "하하",
    "하하하",
    "호호",
    "헤헤",
    "ㅋㅋ",
    "ㅋㅋㅋ",
    "ㅎㅎ",
    "ㅎㅎㅎ",
    "제발",
]
abuse_words = [
    "시발",
    "씨발",
    "ㅅㅂ",
    "ㅆㅂ",
    "좆",
    "존나",
    "병신",
    "지랄",
    "개새끼",
    "꺼져",
    "죽어라",
    "뒤져",
    "걸레",
    "미친놈",
    "개같",
    "쌍년",
    "호로",
    "쓰레기",
    "한남충",
    "김치녀",
    "ㅂㅅ",
    "븅신",
    "ㅈ같",
    "ㅁㅊ",
    "급식충",
    "틀딱",
    "관종",
    "지잡대",
    "후래자식",
    "듣보잡",
    "허접",
    "조또",
    "좇",
    "좆같",
    "좆같다",
    "ㅂㅅ이다",
    "ㅄ",
]


class Comment(BaseModel):
    text: str


# ✅ 욕설 여부 판단
def is_abusive(text: str) -> bool:
    # 모델이 로드되지 않은 경우 먼저 로드합니다
    if abuse_tokenizer is None or abuse_model is None:
        logger.warning("모델이 로드되지 않았습니다. 로드를 시도합니다.")
        load_models()
        if abuse_tokenizer is None or abuse_model is None:
            logger.error("모델 로드 실패, 기본값으로 대체합니다")
            return False
            
    text = text.strip()
    length = len(text)

    if length <= 1:
        return False
    elif text in non_abuse_words:
        return False
    elif any(bad_word in text for bad_word in abuse_words):
        return True
    else:
        inputs = abuse_tokenizer(
            text,
            return_tensors="pt",
            truncation=True,
            padding="max_length",
            max_length=128,
        ).to(device)
        with torch.no_grad():
            outputs = abuse_model(**inputs)
            probs = torch.softmax(outputs.logits, dim=-1)
            prob_abuse = probs[0][1].item() if outputs.logits.size(1) > 1 else 0.0
            threshold = THRESHOLD_SHORT if 2 <= length <= 3 else THRESHOLD_DEFAULT
            return prob_abuse >= threshold


# ✅ 선플 등급 판정 (0~3)
def get_goodness_level(text: str) -> int:
    # 모델이 로드되지 않은 경우 먼저 로드합니다
    if goodness_tokenizer is None or goodness_model is None:
        logger.warning("모델이 로드되지 않았습니다. 로드를 시도합니다.")
        load_models()
        if goodness_tokenizer is None or goodness_model is None:
            logger.error("모델 로드 실패, 기본값으로 대체합니다")
            return 2  # 중립적인 등급으로 기본 반환
            
    inputs = goodness_tokenizer(
        text, return_tensors="pt", truncation=True, padding="max_length", max_length=128
    ).to(device)
    with torch.no_grad():
        outputs = goodness_model(**inputs)
        logits = outputs.logits
        # 모델 출력 형태에 따라 처리
        if logits.size(1) <= 1:
            logger.warning("모델 출력이 예상과 다릅니다. 기본값 사용.")
            return 2  # 중립적인 등급으로 기본 반환
        prediction = torch.argmax(logits, dim=1).item()
        return prediction


@app.post("/analyze")
async def analyze(comment: Comment):
    # 모델 준비 확인
    if abuse_tokenizer is None or abuse_model is None or goodness_tokenizer is None or goodness_model is None:
        logger.warning("모델이 준비되지 않았습니다. 로딩을 시도합니다.")
        load_models()
    
    if is_abusive(comment.text):
        return {"reward": -10}
    else:
        level = get_goodness_level(comment.text)
        reward_score = {0: 10, 1: 3, 2: 0, 3: -5}[level]
        return {"reward": reward_score}


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
