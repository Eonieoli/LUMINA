import torch
import os
import logging
from transformers import AutoTokenizer, AutoModelForSequenceClassification

# 로깅 설정
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

# ✅ Hugging Face에서 모델 가져오기
# 환경 변수로 모델 ID 설정 가능, 기본값은 woobae/kcelectra-swear-detector
MODEL_ID = os.environ.get("ABUSE_MODEL_ID", "woobae/kcelectra-swear-detector")
FALLBACK_MODEL_ID = os.environ.get("FALLBACK_MODEL_ID", "monologg/koelectra-base-v3-discriminator")

# ✅ 모델, 토크나이저 로드 함수
def load_model():
    global tokenizer, model
    try:
        logger.info(f"모델 로드 시도: {MODEL_ID}")
        tokenizer = AutoTokenizer.from_pretrained(MODEL_ID)
        model = AutoModelForSequenceClassification.from_pretrained(MODEL_ID).to(device)
        model.eval()
        logger.info(f"모델 로드 성공: {MODEL_ID}")
    except Exception as e:
        logger.error(f"모델 로드 실패: {e}")
        logger.info(f"대체 모델 사용: {FALLBACK_MODEL_ID}")
        try:
            tokenizer = AutoTokenizer.from_pretrained(FALLBACK_MODEL_ID)
            model = AutoModelForSequenceClassification.from_pretrained(FALLBACK_MODEL_ID).to(device)
            model.eval()
            logger.info(f"대체 모델 로드 성공")
        except Exception as fallback_error:
            logger.error(f"대체 모델도 로드 실패: {fallback_error}")
            raise RuntimeError("모델 로드 실패")

# ✅ 모델, 토크나이저 로드
load_model()

# ✅ 기본 Threshold 설정
THRESHOLD_DEFAULT = 0.53
THRESHOLD_SHORT = 0.7  # 2~3글자 짧은 입력용 threshold

# ✅ 비욕설 감정 표현 리스트
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
    "제발"
]

# ✅ 욕설 사전 리스트
abuse_words = [
    "애미",
    "시발",
    "씨발",
    "ㅅㅂ",
    "ㅆㅂ",
    "씨ㅂ",
    "시ㅂ",
    "ㅅ발",
    "ㅆ발",
    "좆",
    "좃",
    "존나",
    "조또",
    "좇",
    "ㅈ같",
    "ㅈ같다",
    "좆같",
    "좆같다",
    "개새끼",
    "개색기",
    "개쉐끼",
    "개시키",
    "개소리",
    "개지랄",
    "개같",
    "병신",
    "ㅂㅅ",
    "ㅄ",
    "븅신",
    "병쉰",
    "지랄",
    "찌질",
    "쪼다",
    "졸라",
    "썅",
    "썅놈",
    "썅년",
    "쌍놈",
    "쌍년",
    "염병",
    "엠병",
    "얌병",
    "옘병",
    "꺼져",
    "뒤져",
    "죽어라",
    "패죽인다",
    "죽여버린다",
    "꼴통",
    "걸레",
    "창녀",
    "걸레년",
    "미친년",
    "미친놈",
    "호로새끼",
    "후레자식",
    "후래자식",
    "호로자식",
    "허접",
    "듣보잡",
    "찌질이",
    "상놈",
    "상년",
    "쓰레기",
    "개같은",
    "맘충",
    "급식충",
    "틀딱",
    "관종",
    "지잡대",
    "한남충",
    "김치녀",
    "씨바",
    "씨빨",
    "쉬발",
    "쉬발롬",
    "슈발",
    "쉬바",
    "쒸발",
    "쒸팔",
    "병신같",
    "ㅂㅅ같",
    "ㅂㅅ이다",
    "븅신",
    "븅ㅅ",
    "조또",
    "존내",
    "존니",
    "조낸",
    "존맛",
    "존좋",
    "죽여버",
    "뒤져버",
    "때려죽",
    "쌍놈",
    "쌍년",
    "썅놈",
    "썅년",
    "개빡쳐",
    "개같",
    "개노답",
    "개망신",
    "개극혐",
    "ㅈㅅ",
    "ㅁㅊ",
    "ㅄ",
    "ㄱㅅㄲ",
    "ㅍㅊ",
    "ㅅㅂㄹㅁ",
    "ㄱㅆ",
    "ㄴㅅ",
    "ㄴㄱㅆ",
]

# ✅ 테스트 루프
while True:
    text = input("\n👉 테스트할 문장을 입력하세요 (종료하려면 q 입력): ")
    if text.lower() == "q":
        break

    text_strip = text.strip()
    length = len(text_strip)

    # ✅ 1. 길이 필터
    if length <= 1:
        pred = 0
        prob_abuse = 0.0
        print(f"✅ (길이 필터 적용) 1글자 이하는 비욕설로 간주합니다.")

    # ✅ 2. 비욕설 사전 필터
    elif text_strip in non_abuse_words:
        pred = 0
        prob_abuse = 0.0
        print(f"✅ (비욕설 사전 필터) 감정 표현 감지 → 비욕설로 간주합니다.")

    # ✅ 3. 욕설 사전 필터
    elif any(bad_word in text_strip for bad_word in abuse_words):
        pred = 1
        prob_abuse = 1.0
        print(f"✅ (욕설 사전 필터) 욕설 단어 감지 → 욕설로 간주합니다.")

    # ✅ 4. 모델 softmax 추론 (Threshold를 길이에 따라 다르게 적용)
    else:
        inputs = tokenizer(
            text_strip,
            return_tensors="pt",
            truncation=True,
            padding="max_length",
            max_length=128,
        ).to(device)

        with torch.no_grad():
            outputs = model(**inputs)
            logits = outputs.logits
            probs = torch.softmax(logits, dim=-1)
            prob_non_abuse = probs[0][0].item()
            prob_abuse = probs[0][1].item()

            # ✅ Threshold 선택
            if 2 <= length <= 3:
                threshold = THRESHOLD_SHORT
            else:
                threshold = THRESHOLD_DEFAULT

            if prob_abuse >= threshold:
                pred = 1
            else:
                pred = 0

    label_map = {0: "비욕설 (0)", 1: "욕설 (1)"}
    print(f"✅ 예측 결과: {label_map[pred]} (욕설 확률: {prob_abuse:.2f})")