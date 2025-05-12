import csv
from collections import defaultdict

# 라벨별로 문장을 모을 딕셔너리
data_by_label = defaultdict(list)

# 원본 파일 읽기
with open('train.csv', 'r', encoding='utf-8') as infile:
    lines = infile.readlines()
    for line in lines:
        line = line.strip()
        if not line:
            continue
        # 마지막 콤마를 기준으로 나누기
        last_comma_index = line.rfind(',')
        if last_comma_index == -1:
            continue  # 콤마가 없으면 건너뜀
        sentence = line[:last_comma_index].strip()
        label = line[last_comma_index + 1:].strip()
        data_by_label[label].append(sentence)

# 새로운 파일로 저장
with open('sorted_train.csv', 'w', encoding='utf-8', newline='') as outfile:
    writer = csv.writer(outfile)
    for label in sorted(data_by_label.keys()):
        sentences = data_by_label[label]
        print(f"레이블 {label}: {len(sentences)}개")  # 개수 출력
        for sentence in sentences:
            writer.writerow([sentence, label])
