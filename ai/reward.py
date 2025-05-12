from fastapi import FastAPI
from pydantic import BaseModel
from transformers import AutoTokenizer, AutoModelForSequenceClassification
import torch
import uvicorn
import os
import logging
import re


# ✅ 정규화 딕셔너리 통합
from normalization_dict import NORMALIZATION_DICT

# 정규화된 욕설 단어 리스트 자동 생성
abuse_words = sorted(
    set(
        v
        for v in NORMALIZATION_DICT.values()
        if len(v) >= 2 and v != "욕설"  # 예: 너무 짧거나 일반적인 단어 제거
    )
)
# non_abuse_words: 비욕설 확정 단어
non_abuse_words = sorted(
    set(
        [
            "ㅋㅋ",
            "ㅋㅋㅋ",
            "ㅎㅎ",
            "ㅎㅎㅎ",
            "하하",
            "하하하",
            "호호",
            "헤헤",
            "히히",
            "히히히",
            "으휴",
            "하아",
            "아이고",
            "허허",
            "음",
            "좋아요",
            "멋져요",
            "고마워요",
            "감사합니다",
            "재밌다",
        ]
    )
)


def is_laughter_pattern(text: str) -> bool:
    # ㅋ, ㅎ, 하, 호, 헤, 히 등 반복되는 감탄사만 있는 경우
    return bool(re.fullmatch(r"[ㅋㅎ하호헤히]{3,}", text))


def normalize_text(text: str) -> str:
    for variant, normalized in NORMALIZATION_DICT.items():
        text = text.replace(variant, normalized)
    return text


# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI()
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

ABUSE_MODEL_ID = os.environ.get("ABUSE_MODEL_ID", "woobae/kcelectra-swear-detector")
GOODNESS_MODEL_ID = os.environ.get("GOODNESS_MODEL_ID", "woobae/goodness-electra-v3")
FALLBACK_MODEL_ID = "monologg/koelectra-base-v3-discriminator"

abuse_tokenizer = None
abuse_model = None
goodness_tokenizer = None
goodness_model = None


def load_models():
    global abuse_tokenizer, abuse_model, goodness_tokenizer, goodness_model
    logger.info(f"Abuse 모델 로딩 시작: {ABUSE_MODEL_ID}")
    try:
        abuse_tokenizer = AutoTokenizer.from_pretrained(ABUSE_MODEL_ID)
        abuse_model = AutoModelForSequenceClassification.from_pretrained(
            ABUSE_MODEL_ID
        ).to(device)
        abuse_model.eval()
        logger.info("Abuse 모델 로딩 완료")
    except Exception as e:
        logger.error(f"Abuse 모델 로딩 실패: {str(e)}")
        logger.info(f"대체 모델로 시도합니다: {FALLBACK_MODEL_ID}")
        try:
            abuse_tokenizer = AutoTokenizer.from_pretrained(FALLBACK_MODEL_ID)
            abuse_model = AutoModelForSequenceClassification.from_pretrained(
                FALLBACK_MODEL_ID
            ).to(device)
            abuse_model.eval()
            logger.info("대체 모델 로딩 성공")
        except Exception as fallback_error:
            logger.error(f"대체 모델도 로딩 실패: {str(fallback_error)}")
            abuse_tokenizer = None
            abuse_model = None

    logger.info(f"Goodness 모델 로딩 시작: {GOODNESS_MODEL_ID}")
    try:
        goodness_tokenizer = AutoTokenizer.from_pretrained(GOODNESS_MODEL_ID)
        goodness_model = AutoModelForSequenceClassification.from_pretrained(
            GOODNESS_MODEL_ID
        ).to(device)
        goodness_model.eval()
        logger.info("Goodness 모델 로딩 완료")
    except Exception as e:
        logger.error(f"Goodness 모델 로딩 실패: {str(e)}")
        logger.info(f"대체 모델로 시도합니다: {FALLBACK_MODEL_ID}")
        try:
            goodness_tokenizer = AutoTokenizer.from_pretrained(FALLBACK_MODEL_ID)
            goodness_model = AutoModelForSequenceClassification.from_pretrained(
                FALLBACK_MODEL_ID
            ).to(device)
            goodness_model.eval()
            logger.info("대체 모델 로딩 성공")
        except Exception as fallback_error:
            logger.error(f"대체 모델도 로딩 실패: {str(fallback_error)}")
            goodness_tokenizer = None
            goodness_model = None


@app.on_event("startup")
async def startup_event():
    logger.info("서버 시작 - 모델 로딩을 시작합니다")
    load_models()


THRESHOLD_DEFAULT = 0.53
THRESHOLD_SHORT = 0.7


class Comment(BaseModel):
    text: str


# ✅ 욕설 여부 판단 (정규화 포함)
def is_abusive(text: str) -> bool:
    if abuse_tokenizer is None or abuse_model is None:
        logger.warning("모델이 로드되지 않았습니다. 로딩을 시도합니다.")
        load_models()
        if abuse_tokenizer is None or abuse_model is None:
            logger.error("모델 로드 실패, 기본값으로 대체합니다")
            return False

    original_text = text.strip()
    normalized_text = normalize_text(original_text)

    logger.info(f"[정규화 전] {original_text}")
    logger.info(f"[정규화 후] {normalized_text}")

    if len(text) <= 1:
        return False
    # 1. 반복 감탄사 필터
    if is_laughter_pattern(text):
        logger.info("[반복 감탄사로 판단됨]")
        return False
    elif any(word in normalized_text.split() for word in non_abuse_words):
        logger.info("[비욕설 단어가 포함된 문장입니다]")
        return False
    elif any(bad_word in normalized_text for bad_word in abuse_words):
        logger.info("[욕설 리스트에 포함된 단어 감지됨]")
        return True
    else:
        inputs = abuse_tokenizer(
            normalized_text,
            return_tensors="pt",
            truncation=True,
            padding="max_length",
            max_length=128,
        ).to(device)
        with torch.no_grad():
            outputs = abuse_model(**inputs)
            probs = torch.softmax(outputs.logits, dim=-1)
            prob_abuse = probs[0][1].item() if outputs.logits.size(1) > 1 else 0.0
            logger.info(f"[모델 예측 확률] 욕설일 확률: {prob_abuse:.4f}")
            threshold = (
                THRESHOLD_SHORT if 2 <= len(normalized_text) <= 3 else THRESHOLD_DEFAULT
            )
            result = prob_abuse >= threshold
            logger.info(f"[최종 판단] 욕설 여부: {result}")
            return result


def get_goodness_level(text: str) -> int:
    if goodness_tokenizer is None or goodness_model is None:
        logger.warning("모델이 로드되지 않았습니다. 로드를 시도합니다.")
        load_models()
        if goodness_tokenizer is None or goodness_model is None:
            logger.error("모델 로드 실패, 기본값으로 대체합니다")
            return 2

    text = normalize_text(text.strip())
    inputs = goodness_tokenizer(
        text, return_tensors="pt", truncation=True, padding="max_length", max_length=128
    ).to(device)
    with torch.no_grad():
        outputs = goodness_model(**inputs)
        logits = outputs.logits
        if logits.size(1) <= 1:
            logger.warning("모델 출력이 예상과 다릅니다. 기본값 사용.")
            return 2
        prediction = torch.argmax(logits, dim=1).item()
        return prediction


@app.get("/health")
async def health_check():
    models_loaded = (
        abuse_tokenizer is not None
        and abuse_model is not None
        and goodness_tokenizer is not None
        and goodness_model is not None
    )
    status = "healthy" if models_loaded else "models loading"
    return {"status": status}


@app.post("/analyze")
async def analyze(comment: Comment):
    if (
        abuse_tokenizer is None
        or abuse_model is None
        or goodness_tokenizer is None
        or goodness_model is None
    ):
        logger.warning("모델이 준비되지 않았습니다. 로딩을 시도합니다.")
        load_models()

    normalized_text = normalize_text(comment.text)
    if is_abusive(normalized_text):
        return {"reward": -10}
    else:
        level = get_goodness_level(normalized_text)
        reward_score = {0: 10, 1: 3, 2: 0, 3: -5}[level]
        return {"reward": reward_score}


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
