#!/bin/bash

# 이 스크립트는 Jenkins가 GCP(개발) 및 AWS(운영) 서버에 Grafana 대시보드를 배포하는 부분을 수정합니다.

# 개발 환경(GCP) 추가 설정
# deploy.sh에 다음 명령어 추가 (Deploy to GCP - Dev 단계)

# 그라파나 프로비저닝 디렉토리 생성
mkdir -p ${GCP_DEPLOY_PATH}/infra/dev/monitoring/grafana/provisioning
mkdir -p ${GCP_DEPLOY_PATH}/infra/dev/monitoring/grafana/provisioning/dashboards
mkdir -p ${GCP_DEPLOY_PATH}/infra/dev/monitoring/grafana/dashboards/prometheus
mkdir -p ${GCP_DEPLOY_PATH}/infra/dev/monitoring/grafana/dashboards/ga

# 그라파나 프로비저닝 설정 파일 전송
scp -o StrictHostKeyChecking=no infra/dev/monitoring/grafana/provisioning/dashboards/dashboards.yml ${GCP_DEPLOY_HOST}:${GCP_DEPLOY_PATH}/infra/dev/monitoring/grafana/provisioning/dashboards/dashboards.yml

# 대시보드 JSON 파일 전송 - Prometheus 대시보드
scp -o StrictHostKeyChecking=no infra/dev/monitoring/grafana/dashboards/prometheus/system-resources.json ${GCP_DEPLOY_HOST}:${GCP_DEPLOY_PATH}/infra/dev/monitoring/grafana/dashboards/prometheus/system-resources.json
scp -o StrictHostKeyChecking=no infra/dev/monitoring/grafana/dashboards/prometheus/container-monitoring.json ${GCP_DEPLOY_HOST}:${GCP_DEPLOY_PATH}/infra/dev/monitoring/grafana/dashboards/prometheus/container-monitoring.json

# 대시보드 JSON 파일 전송 - Google Analytics 대시보드
scp -o StrictHostKeyChecking=no infra/dev/monitoring/grafana/dashboards/ga/ga-plugin-dashboard.json ${GCP_DEPLOY_HOST}:${GCP_DEPLOY_PATH}/infra/dev/monitoring/grafana/dashboards/ga/ga-plugin-dashboard.json
scp -o StrictHostKeyChecking=no infra/dev/monitoring/grafana/dashboards/ga/ga-bigquery-dashboard.json ${GCP_DEPLOY_HOST}:${GCP_DEPLOY_PATH}/infra/dev/monitoring/grafana/dashboards/ga/ga-bigquery-dashboard.json

# 운영 환경(AWS) 추가 설정
# deploy.sh에 다음 명령어 추가 (Deploy to AWS - Prod 단계)

# 그라파나 프로비저닝 디렉토리 생성
mkdir -p ${AWS_DEPLOY_PATH}/infra/prod/monitoring/grafana/provisioning
mkdir -p ${AWS_DEPLOY_PATH}/infra/prod/monitoring/grafana/provisioning/dashboards
mkdir -p ${AWS_DEPLOY_PATH}/infra/prod/monitoring/grafana/dashboards/prometheus
mkdir -p ${AWS_DEPLOY_PATH}/infra/prod/monitoring/grafana/dashboards/ga

# 그라파나 프로비저닝 설정 파일 전송
scp -o StrictHostKeyChecking=no infra/prod/monitoring/grafana/provisioning/dashboards/dashboards.yml ${AWS_DEPLOY_HOST}:${AWS_DEPLOY_PATH}/infra/prod/monitoring/grafana/provisioning/dashboards/dashboards.yml

# 대시보드 JSON 파일 전송 - Prometheus 대시보드
scp -o StrictHostKeyChecking=no infra/prod/monitoring/grafana/dashboards/prometheus/system-resources.json ${AWS_DEPLOY_HOST}:${AWS_DEPLOY_PATH}/infra/prod/monitoring/grafana/dashboards/prometheus/system-resources.json
scp -o StrictHostKeyChecking=no infra/prod/monitoring/grafana/dashboards/prometheus/container-monitoring.json ${AWS_DEPLOY_HOST}:${AWS_DEPLOY_PATH}/infra/prod/monitoring/grafana/dashboards/prometheus/container-monitoring.json

# 대시보드 JSON 파일 전송 - Google Analytics 대시보드
scp -o StrictHostKeyChecking=no infra/prod/monitoring/grafana/dashboards/ga/ga-plugin-dashboard.json ${AWS_DEPLOY_HOST}:${AWS_DEPLOY_PATH}/infra/prod/monitoring/grafana/dashboards/ga/ga-plugin-dashboard.json
scp -o StrictHostKeyChecking=no infra/prod/monitoring/grafana/dashboards/ga/ga-bigquery-dashboard.json ${AWS_DEPLOY_HOST}:${AWS_DEPLOY_PATH}/infra/prod/monitoring/grafana/dashboards/ga/ga-bigquery-dashboard.json
