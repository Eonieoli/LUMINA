from fastapi import FastAPI
from pydantic import BaseModel
from transformers import AutoTokenizer, AutoModelForSequenceClassification
import torch
import logging
import re
import os

# ✅ 정규화 딕셔너리
from normalization_dict import NORMALIZATION_DICT


def is_laughter_pattern(text: str) -> bool:
    return bool(re.fullmatch(r"[ㅋㅎ하호헤히]{3,}", text))


def normalize_text(text: str) -> str:
    for variant, normalized in NORMALIZATION_DICT.items():
        text = text.replace(variant, normalized)
    return text


# ✅ 욕설 필터 클래스
class BadWordFilter:
    def __init__(self, replacement="*"):
        self.bad_words = set()
        self.replacement = replacement

    def load_badwords(self, filepath):
        with open(filepath, "r", encoding="utf-8") as f:
            content = f.read()
            words = [word.strip() for word in content.split(",") if word.strip()]
            self.bad_words.update(words)

    def check(self, text):
        for word in self.bad_words:
            if re.search(rf"{re.escape(word)}", text, re.IGNORECASE):
                return True
        return False

    def blank_check(self, text):
        normalized = re.sub(r"\s+", "", text)
        for word in self.bad_words:
            if re.search(rf"{re.escape(word)}", normalized, re.IGNORECASE):
                return True
        return False

    def change(self, text):
        result = text
        for word in self.bad_words:
            result = re.sub(
                rf"{re.escape(word)}",
                self.replacement * len(word),
                result,
                flags=re.IGNORECASE,
            )
        return result


# ✅ FastAPI 및 로깅
app = FastAPI()
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)
filter = BadWordFilter()

# ✅ Goodness 모델 준비
GOODNESS_MODEL_ID = os.environ.get("GOODNESS_MODEL_ID", "woobae/goodness-electra-v3")
FALLBACK_MODEL_ID = "monologg/koelectra-base-v3-discriminator"
goodness_tokenizer = None
goodness_model = None
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")


@app.on_event("startup")
async def startup_event():
    global goodness_tokenizer, goodness_model

    logger.info("서버 시작 - 욕설 리스트 및 모델 로딩")
    filter.load_badwords("C:/Users/SSAFY/Desktop/lumina/S12P31S306/ai/badwords.txt")

    # 정규화 딕셔너리 값도 bad_words에 추가
    normalized_abuse = {
        v.strip()
        for v in NORMALIZATION_DICT.values()
        if len(v.strip()) >= 2 and v.strip() != "욕설"
    }
    filter.bad_words.update(normalized_abuse)

    try:
        logger.info(f"Goodness 모델 로딩: {GOODNESS_MODEL_ID}")
        goodness_tokenizer = AutoTokenizer.from_pretrained(GOODNESS_MODEL_ID)
        goodness_model = AutoModelForSequenceClassification.from_pretrained(
            GOODNESS_MODEL_ID
        ).to(device)
        goodness_model.eval()
        logger.info("Goodness 모델 로딩 완료")
    except Exception as e:
        logger.error(f"Goodness 모델 로딩 실패: {e}")
        goodness_tokenizer = None
        goodness_model = None


# ✅ 입력 데이터
class Comment(BaseModel):
    text: str

# ✅ 욕설 판단
def is_abusive(text: str) -> bool:
    original_text = text.strip()
    cleaned = re.sub(r"[^가-힣ㄱ-ㅎㅏ-ㅣ]", "", original_text)
    normalized_text = normalize_text(cleaned)
    
    logger.info(f"[원문] {original_text}")
    logger.info(f"[전처리 후] {cleaned}")
    logger.info(f"[정규화 후] {normalized_text}")

    if len(normalized_text) <= 1:
        return False
    if is_laughter_pattern(normalized_text):
        logger.info("[감탄사 판단]")
        return False
    if filter.check(normalized_text) or filter.blank_check(normalized_text):
        logger.info("[욕설 감지됨]")
        return True
    return False


# ✅ 선함 판단
def get_goodness_level(text: str) -> int:
    if goodness_tokenizer is None or goodness_model is None:
        logger.warning("Goodness 모델 없음 - 기본값 반환")
        return 2  # 중립

    text = normalize_text(text.strip())
    inputs = goodness_tokenizer(
        text, return_tensors="pt", truncation=True, padding="max_length", max_length=128
    ).to(device)

    with torch.no_grad():
        outputs = goodness_model(**inputs)
        logits = outputs.logits
        if logits.size(1) <= 1:
            return 2
        return torch.argmax(logits, dim=1).item()


# ✅ 헬스 체크
@app.get("/health")
async def health_check():
    loaded = goodness_tokenizer is not None and goodness_model is not None
    return {"status": "healthy" if loaded else "model loading"}


# ✅ 분석 API
@app.post("/analyze")
async def analyze(comment: Comment):
    normalized_text = normalize_text(comment.text)
    if is_abusive(normalized_text):
        return {"reward": -10}
    else:
        level = get_goodness_level(normalized_text)
        reward_score = {0: 10, 1: 3, 2: 0, 3: -5}.get(level, 0)
        return {"reward": reward_score}


# ✅ 실행
if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000)
