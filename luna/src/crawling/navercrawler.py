import requests
from bs4 import BeautifulSoup
import time 
import json
import os
from urllib.parse import urlparse
import datetime
import trafilatura

url = 'https://search.naver.com/search.naver?sm=tab_hty.top&where=news&ssc=tab.news.all&query=%EA%B8%B0%EB%B6%80+%EC%BA%A0%ED%8E%98%EC%9D%B8'
headers = {'User-Agent': 'Mozilla/5.0'}
res = requests.get(url, headers=headers)
soup = BeautifulSoup(res.text, 'html.parser')


# 디버깅: 클래스 정보 출력
print("=== 클래스 패턴 분석 ===")
# 포함할 클래스 패턴(메인 기사)
include_pattern = "sAGrdWzUXvn6RiKe_Qln"
# 제외할 클래스 패턴(밑에 유사한 기사)
exclude_pattern = "_GAZ8_ld3QUhAyQ1ch7s"

# 1. 포함할 요소: div.m6eR7KmFjLYOLhYRyXPh 내부의 a 태그
include_containers = soup.select(f'div.sds-comps-base-layout.sds-comps-full-layout.{include_pattern}')
print(f"찾은 메인 기사 컨테이너 수: {len(include_containers)}")

# 2. 제외할 요소: div.m7VJzZ2bSaunam2mv1Xd 내부의 a 태그
exclude_containers = soup.select(f'div.sds-comps-base-layout.sds-comps-full-layout.{exclude_pattern}')
print(f"제외할 작은 기사 컨테이너 수: {len(exclude_containers)}")

# 메인 뉴스 링크 수집
main_news_links = []
visited_urls = set()  # 중복 URL 방지

# 포함할 컨테이너에서 a 태그 추출
for container in include_containers:
    a_tags = container.find_all('a')
    for a in a_tags:
        href = a.get('href')
        text = a.get_text(strip=True)
        
        # 유효한 URL이고, 텍스트가 있고, 중복이 아닌 경우만 추가
        if href and text and href.startswith('http') and href not in visited_urls and len(text) > 5:
            main_news_links.append((href, text))
            visited_urls.add(href)
            # print(f"메인 기사 추가: {text[:30]}... | {href}")

# 결과가 부족하면 추가로 찾기
if len(main_news_links) < 10:
    print(f"메인 기사를 {len(main_news_links)}개만 찾았습니다. 추가로 검색합니다.")
    
    # 제외할 요소들의 URL 수집
    exclude_urls = set()
    for container in exclude_containers:
        a_tags = container.find_all('a')
        for a in a_tags:
            href = a.get('href')
            if href and href.startswith('http'):
                exclude_urls.add(href)
    
    # 모든 a 태그에서 유효한 뉴스 링크 찾기 (제외할 컨테이너의 링크는 건너뜀)
    all_a_tags = soup.find_all('a')
    for a in all_a_tags:
        href = a.get('href')
        text = a.get_text(strip=True)
        
        # 유효한 링크인지 확인
        if (href and text and href.startswith('http') and 
            href not in visited_urls and 
            href not in exclude_urls and 
            len(text) > 10):
            
            main_news_links.append((href, text))
            visited_urls.add(href)
            print(f"추가 기사 찾음: {text[:30]}... | {href}")
            
            # 10개가 채워지면 중단
            if len(main_news_links) >= 10:
                break

# main_news_links를 news_links 형태로 변환하는 코드
def convert_to_dict_list(tuples_list):
    dict_list = []
    for url, title in tuples_list:
        dict_list.append({
            "title": title,
            "url": url
        })
    return dict_list

# 변환 실행
news_links = convert_to_dict_list(main_news_links)

print(f"변환된 리스트에는 {len(news_links)}개의 항목이 있습니다.")

# 결과 확인
for i, news in enumerate(news_links[:10], 1):
    print(f"{i}. 제목: {news['title']}")
    print(f"   URL: {news['url']}")


# 결과를 저장할 리스트
news_data = []

print("\n=== 뉴스 기사 본문 추출 시작 ===")
# 각 URL의 HTML 대신 newspaper3k로 본문 추출
for i, news in enumerate(news_links[:10], 1):
    title = news["title"]
    url = news["url"]
    
    print(f"[{i}/10] '{title}' 본문 추출 중...")
    
    try:
       # Trafilatura를 사용하여 기사 파싱
        downloaded_content = trafilatura.fetch_url(url)
        text_content = trafilatura.extract(downloaded_content)
        
        if not text_content:
            raise Exception("본문을 추출할 수 없습니다.")
        
        # 도메인 추출
        domain = urlparse(url).netloc.replace("www.", "")
        
        # 기사 정보 저장
        news_item = {
            "id": i,
            "title": title,  # 원래 제목 사용
            "url": url,
            "domain": domain,
            "text_content": text_content,
            "crawled_at": datetime.datetime.now().strftime("%Y-%m-%dT%H:%M:%S+09:00")
        }

        news_data.append(news_item)
        print(f"  ✓ 성공: {domain} ({len(text_content)} 글자)")
    except Exception as e:
        print(f"  ✗ 오류 발생: {str(e)}")
        # 오류 정보 저장
        news_data.append({
            "id": i,
            "title": title,
            "url": url,
            "domain": urlparse(url).netloc.replace("www.", ""),
            "text_content": "",  # 빈 텍스트로 설정
            "error": str(e),
            "crawled_at": time.strftime("%Y-%m-%d %H:%M:%S")
        })

    # 서버에 부담을 주지 않기 위해 잠시 대기
    if i < len(news_links[:10]):
        print("  다음 URL로 이동합니다... (1초 대기)")
        time.sleep(1)

# 게시물 내용을 담을 DB 파일 관리 함수 
def read_news_database():
    """뉴스 DB 파일 읽기"""
    script_dir = os.path.dirname(os.path.abspath(__file__))
    db_path = os.path.join(script_dir, "news_data", "news_database.json")
    
    # 파일이 없으면 초기 구조 생성
    if not os.path.exists(db_path):
        return {
            "metadata": {
                "last_crawled": "",
                "next_crawl": ""
            },
            "articles": [],
            "post_history": []
        }
    
    # 파일이 있으면 읽기
    try:
        with open(db_path, "r", encoding="utf-8") as f:
            return json.load(f)  # json파일을 파이썬 객체로 변환 
    except Exception as e:
        print(f"DB 파일 읽기 오류: {e}")
        return {
            "metadata": {
                "last_crawled": "",
                "next_crawl": ""
            },
            "articles": [],
            "post_history": []
        }

def write_news_database(db_data):
    """뉴스 DB 파일 쓰기"""
    script_dir = os.path.dirname(os.path.abspath(__file__))
    db_path = os.path.join(script_dir, "news_data", "news_database.json")
    
    try:
        with open(db_path, "w", encoding="utf-8") as f:
            json.dump(db_data, f, ensure_ascii=False, indent=2)
            # 저장할 파이썬 객체, 한글 등 유니코드 문자를 그대로 저장, 들여쓰기 2칸
        print(f"DB 파일 저장 완료: {db_path}")
    except Exception as e:
        print(f"DB 파일 쓰기 오류: {e}")

# DB 업데이트
db_data = read_news_database()

# 기존의 사용 여부 정보 보존
used_status = {}
for article in db_data["articles"]:
    used_status[article["url"]] = {
        "used": article.get("used", False),
        "used_at": article.get("used_at"),
        "time_slot": article.get("time_slot")
    }

# 새 기사 목록으로 기존 기사 목록 완전히 교체
new_articles = []
for article in news_data:
    # ID 생성 (기존 ID가 있으면 유지)
    article_id = str(article["id"])
    
    # 새 기사 생성
    db_article = {
        "id": article_id,
        "title": article["title"],
        "url": article["url"],
        "domain": article["domain"],
        "text_content": article.get("text_content", ""),
        "crawled_at": article["crawled_at"],
        # 기존 상태 정보 유지 (URL이 같은 기사가 있었다면)
        "used": used_status.get(article["url"], {}).get("used", False),
        "used_at": used_status.get(article["url"], {}).get("used_at"),
        "time_slot": used_status.get(article["url"], {}).get("time_slot")
    }
    new_articles.append(db_article)

# 기존 기사 목록을 새 기사 목록으로 완전히 교체
db_data["articles"] = new_articles

# 메타데이터 업데이트
now = datetime.datetime.now()

# 특정 시간 설정
current_date = now.date()  # 현재 날짜만 가져옴
scheduled_time = datetime.datetime.combine(current_date, datetime.time(7, 0, 0))  # 오전 7:00:00

# 메타데이터에 저장
db_data["metadata"]["last_crawled"] = now.strftime("%Y-%m-%dT%H:%M:%S+09:00")

# 다음 크롤링 시간은 다음 날 오전 7시로 설정
next_date = now.date() + datetime.timedelta(days=1)
next_crawl = datetime.datetime.combine(next_date, datetime.time(7, 0, 0))
db_data["metadata"]["next_crawl"] = next_crawl.strftime("%Y-%m-%dT%H:%M:%S+09:00")

# DB 저장
write_news_database(db_data)
print("뉴스 데이터베이스 업데이트 완료")