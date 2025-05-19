import schedule
import time
import subprocess
import os
import sys
import datetime

def run_crawler():
    """navercrawler.py 실행"""
    print(f"크롤링 시작: {datetime.datetime.now()}")
    
    # 현재 디렉토리 확인
    script_dir = os.path.dirname(os.path.abspath(__file__))
    
    # 파이썬 실행 경로와 navercrawler.py 경로 설정
    python_executable = sys.executable  # 현재 실행 중인 Python 인터프리터
    crawler_script = os.path.join(script_dir, "navercrawler.py")
    
    # 크롤러 스크립트 실행
    try:
        result = subprocess.run(
            [python_executable, crawler_script],
            check=True,
            capture_output=True,
            text=True
        )
        print("크롤링 성공!")
        print(f"출력: {result.stdout}")
    except subprocess.CalledProcessError as e:
        print(f"크롤링 오류 발생: {e}")
        print(f"에러 출력: {e.stderr}")

# 매일 오전 7시에 실행
schedule.every().day.at("07:00").do(run_crawler)

# 시작 시 한 번 실행
print("시작 시 크롤링 실행...")
run_crawler()

print("스케줄러 시작됨 - 매일 오전 7시에 실행")
print("종료하려면 Ctrl+C를 누르세요.")

# 무한 루프로 스케줄러 실행
while True:
    schedule.run_pending()
    time.sleep(60)  # 1분마다 체크