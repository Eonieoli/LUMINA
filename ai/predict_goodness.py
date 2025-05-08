import sys
import torch
from transformers import ElectraTokenizer, ElectraForSequenceClassification

# ✅ 모델과 토크나이저 로드
model_path = "C:/Users/SSAFY/Desktop/lumina/S12P31S306/ai/goodness-electra-v3"
tokenizer = ElectraTokenizer.from_pretrained(model_path)
model = ElectraForSequenceClassification.from_pretrained(model_path)
model.eval()


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
