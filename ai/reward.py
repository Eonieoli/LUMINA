from fastapi import FastAPI
from pydantic import BaseModel
from transformers import AutoTokenizer, AutoModelForSequenceClassification
import torch
import uvicorn

app = FastAPI()
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

# ✅ 모델 로딩
abuse_model_path = "./finetuned-kcelectra"
abuse_tokenizer = AutoTokenizer.from_pretrained(abuse_model_path)
abuse_model = AutoModelForSequenceClassification.from_pretrained(abuse_model_path).to(
    device
)
abuse_model.eval()

goodness_model_path = "./goodness-electra-v3"
goodness_tokenizer = AutoTokenizer.from_pretrained(goodness_model_path)
goodness_model = AutoModelForSequenceClassification.from_pretrained(
    goodness_model_path
).to(device)
goodness_model.eval()

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
            prob_abuse = probs[0][1].item()
            threshold = THRESHOLD_SHORT if 2 <= length <= 3 else THRESHOLD_DEFAULT
            return prob_abuse >= threshold


# ✅ 선플 등급 판정 (0~3)
def get_goodness_level(text: str) -> int:
    inputs = goodness_tokenizer(
        text, return_tensors="pt", truncation=True, padding="max_length", max_length=128
    ).to(device)
    with torch.no_grad():
        outputs = goodness_model(**inputs)
        logits = outputs.logits
        prediction = torch.argmax(logits, dim=1).item()
        return prediction


@app.post("/analyze")
async def analyze(comment: Comment):
    if is_abusive(comment.text):
        return {"reward": -10}
    else:
        level = get_goodness_level(comment.text)
        reward_score = {0: 10, 1: 3, 2: 0, 3: -5}[level]
        return {"reward": reward_score}


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
