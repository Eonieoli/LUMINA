import requests
import pandas as pd
import re
import os
from dotenv import load_dotenv

load_dotenv()

# API 키와 기본 설정
API_KEY = os.getenv("API_KEY")
print(f"✅ API_KEY 로드 확인: {API_KEY}")

BASE_URL = "https://www.googleapis.com/youtube/v3/commentThreads"

# 크롤링.txt에서 videoId 읽기
with open("크롤링.txt", "r", encoding="utf-8") as f:
    video_ids = [line.strip().split("v=")[-1] for line in f if "v=" in line]

# 댓글 수집
comments = []
for video_id in video_ids:
    params = {
        "part": "snippet",
        "videoId": video_id,
        "key": API_KEY,
        "maxResults": 100,
        "textFormat": "plainText",
    }
    response = requests.get(BASE_URL, params=params)
    if response.status_code == 200:
        items = response.json().get("items", [])
        for item in items:
            snippet = item["snippet"]["topLevelComment"]["snippet"]
            comments.append(
                {
                    "video_url": f"https://www.youtube.com/watch?v={video_id}",
                    "comment": snippet["textOriginal"],
                }
            )

# DataFrame 생성
df = pd.DataFrame(comments)


# 전처리 함수들
def clean_whitespace(text):
    text = text.replace("\n", " ").replace("\r", " ")  # 줄바꿈 → 공백
    text = re.sub(r"\s+", " ", text)  # 연속된 공백 → 하나로
    return text.strip()  # 앞뒤 공백 제거


def reduce_repeated_punctuations(text):
    text = re.sub(r"!{2,}", "!", text)
    text = re.sub(r"\?{2,}", "?", text)
    text = re.sub(r"~{2,}", "~", text)
    text = re.sub(r"(ㅠ){3,}", "ㅠㅠ", text)
    text = re.sub(r"(ㅜ){3,}", "ㅜㅜ", text)
    text = re.sub(r"(ㅋ){3,}", "ㅋㅋ", text)
    text = re.sub(r"(ㅎ){3,}", "ㅎㅎ", text)
    text = re.sub(r"\.{3,}", "..", text)
    return text


def remove_html_like_tags(text):
    return re.sub(r"<[^>]+>", "", text)


def filter_by_length(text, min_len=5, max_len=300):
    return min_len <= len(text.strip()) <= max_len


def keep_korean_full_and_jamo_with_specific_punctuation(text):
    if not isinstance(text, str):
        return ""
    #  한글 음절/자모 + 숫자 + 띄어쓰기 + 명시한 특수문자만 남기고 나머지는 전부 제거
    return re.sub(r"[^가-힣ㄱ-ㅎㅏ-ㅣ0-9\s,./?!()\^$~]", "", text)


def is_valid_comment(text):
    if not isinstance(text, str):
        return False
    text = text.strip()

    # 길이로 1차 거르기
    if len(text) < 5:
        return False

    # 특정 키워드 포함하면 제거
    bad_patterns = [
        "더 많은 비디오 보기",
        "//../?",
        "더보기",
        "See more",
        "더 많은 영상",
    ]
    for pattern in bad_patterns:
        if pattern in text:
            return False

    # 정상 글자(한글, 영어, 숫자)가 하나라도 있으면 통과
    if re.search(r"[가-힣a-zA-Z0-9]", text):
        return True
    return False


# 글 앞쪽에 붙은 3자리 이상 숫자 제거
def remove_leading_numbers(text):
    if not isinstance(text, str):
        return ""
    # 문장 맨 앞에 오는 3~8자리 숫자 제거
    return re.sub(r"^\d{3,8}\s*", "", text)


# 전처리 적용
df["text"] = df["comment"].apply(keep_korean_full_and_jamo_with_specific_punctuation)
df["text"] = df["text"].apply(remove_leading_numbers)
df["text"] = df["text"].apply(reduce_repeated_punctuations)
df["text"] = df["text"].apply(remove_html_like_tags)
df["text"] = df["text"].apply(clean_whitespace)
df = df[df["text"].apply(is_valid_comment)]
df = df[df["text"].apply(filter_by_length)]
df = df.drop_duplicates(subset=["text"]).reset_index(drop=True)

# 최종 저장
df[["text"]].to_csv("train.csv", index=False, encoding="utf-8-sig")
print("✅ 댓글 수집 및 전처리 완료! → train.csv 저장됨")
