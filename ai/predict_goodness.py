import sys
import torch
import os
import logging
from transformers import AutoTokenizer, AutoModelForSequenceClassification

# 로깅 설정
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

# ✅ Hugging Face에서 모델 가져오기
# 환경 변수로 모델 ID 설정 가능, 기본값은 woobae/goodness-electra-v3
MODEL_ID = os.environ.get("GOODNESS_MODEL_ID", "woobae/goodness-electra-v3")
FALLBACK_MODEL_ID = os.environ.get("FALLBACK_MODEL_ID", "monologg/koelectra-base-v3-discriminator")

# ✅ 모델, 토크나이저 로드 함수
def load_model():
    global tokenizer, model
    try:
        logger.info(f"모델 로드 시도: {MODEL_ID}")
        tokenizer = AutoTokenizer.from_pretrained(MODEL_ID)
        model = AutoModelForSequenceClassification.from_pretrained(MODEL_ID)
        model.eval()
        logger.info(f"모델 로드 성공: {MODEL_ID}")
    except Exception as e:
        logger.error(f"모델 로드 실패: {e}")
        logger.info(f"대체 모델 사용: {FALLBACK_MODEL_ID}")
        try:
            tokenizer = AutoTokenizer.from_pretrained(FALLBACK_MODEL_ID)
            model = AutoModelForSequenceClassification.from_pretrained(FALLBACK_MODEL_ID)
            model.eval()
            logger.info(f"대체 모델 로드 성공")
        except Exception as fallback_error:
            logger.error(f"대체 모델도 로드 실패: {fallback_error}")
            raise RuntimeError("모델 로드 실패")

# ✅ 모델, 토크나이저 로드
load_model()


# ✅ 문장 입력 받아 예측
def predict(text):
    inputs = tokenizer(
        text, return_tensors="pt", truncation=True, padding=True, max_length=128
    )
    with torch.no_grad():
        outputs = model(**inputs)
        probs = torch.softmax(outputs.logits, dim=-1)
        pred = torch.argmax(probs, dim=1).item()
    return pred, probs.squeeze().tolist()


# ✅ 테스트 루프
label_dict = {0: "선한 생각 + 선한 행동", 1: "선한 생각만", 2: "중립", 3: "나쁜 말"}

print("👉 테스트할 문장을 입력하세요 (종료하려면 q 입력):")
while True:
    try:
        # ✅ readline을 직접 UTF-8로 디코딩
        raw = sys.stdin.buffer.readline()
        if not raw:
            break
        text = raw.decode("utf-8", errors="ignore").strip()
        if text.lower() == "q":
            break
        pred, probs = predict(text)
        print(f"✅ 예측 결과: {label_dict[pred]} ({pred}) (확률: {probs[pred]:.2f})\n")
    except Exception as e:
        print(f"⚠️ 예외 발생: {e}")
