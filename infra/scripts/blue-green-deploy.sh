#!/bin/bash

# Blue-Green 무중단 배포 스크립트
# 사용법: ./blue-green-deploy.sh [dev|prod] [frontend|backend|all]

set -e

# 변수 초기화 및 인자 파싱
ENV=$1
TARGET=$2
DEPLOY_PATH=""

# 인자 검증
if [ "$ENV" != "dev" ] && [ "$ENV" != "prod" ]; then
    echo "Error: First argument must be 'dev' or 'prod'"
    exit 1
fi

if [ "$TARGET" != "frontend" ] && [ "$TARGET" != "backend" ] && [ "$TARGET" != "all" ]; then
    echo "Error: Second argument must be 'frontend', 'backend', or 'all'"
    exit 1
fi

# 환경에 따른 경로 설정
DEPLOY_PATH="/home/$([ "$ENV" == "dev" ] && echo "rublin322" || echo "ubuntu")/lumina/infra/$ENV"
FRONTEND_NGINX_CONF_PATH="$DEPLOY_PATH/proxy/blue-green/frontend"
BACKEND_NGINX_CONF_PATH="$DEPLOY_PATH/proxy/blue-green/backend"

# 컨테이너 실행 확인
check_container() {
    docker ps -q -f name=$1 &> /dev/null
}

# 컨테이너 배포
deploy_container() {
    local service=$1
    local color=$2
    local port=$([ "$service" == "frontend" ] && echo "3001" || echo "8081")
    local container_port=$([ "$service" == "frontend" ] && echo "80" || echo "8080")
    
    [ "$color" == "green" ] && port=$((port + 1))
    
    echo "Deploying $service-$color..."
    docker stop $service-$color 2>/dev/null || true
    docker rm $service-$color 2>/dev/null || true
    
    local tag=$([ "$ENV" == "dev" ] && echo "develop" || echo "latest")
    local env_arg=$([ "$service" == "backend" ] && echo "--env-file $DEPLOY_PATH/.env" || echo "")
    
    docker run -d --name $service-$color \
        --network lumina-network \
        -p $port:$container_port \
        $env_arg \
        --restart always \
        --label environment=$ENV \
        rublin322/lumina-$service:$tag
        
    echo "$service-$color deployed on port $port"
}

# 건강 상태 확인
health_check() {
    local service=$1
    local color=$2
    local port=$([ "$service" == "frontend" ] && echo "3001" || echo "8081")
    local endpoint=$([ "$service" == "frontend" ] && echo "/" || echo "/actuator/health")
    
    [ "$color" == "green" ] && port=$((port + 1))
    
    echo "Performing health check for $service-$color..."
    
    for i in {1..10}; do
        echo "Health check attempt $i/10..."
        if curl -s -o /dev/null -w "%{http_code}" http://localhost:$port$endpoint | grep -q "200"; then
            echo "$service-$color is healthy"
            return 0
        fi
        echo "Health check failed, waiting 5 seconds before next attempt..."
        sleep 5
    done
    
    echo "Error: Health check failed after 10 attempts"
    docker logs --tail 50 $service-$color
    exit 1
}

# 현재 서비스 색상 확인
check_current_service() {
    local service=$1
    local conf_path=$([ "$service" == "frontend" ] && echo "$FRONTEND_NGINX_CONF_PATH" || echo "$BACKEND_NGINX_CONF_PATH")
    local current="blue"
    
    if [ -f "$conf_path/upstream.conf" ]; then
        if grep -q "$service-green.*active" "$conf_path/upstream.conf" || 
           grep -q "$service-blue.*backup" "$conf_path/upstream.conf" || 
           (! grep -q "$service-blue" "$conf_path/upstream.conf" && grep -q "$service-green" "$conf_path/upstream.conf"); then
            current="green"
        fi
    fi
    
    echo "$current"
}

# 모든 컨테이너 존재 확인
ensure_containers() {
    local services=()
    [ "$TARGET" == "frontend" ] || [ "$TARGET" == "all" ] && services+=("frontend")
    [ "$TARGET" == "backend" ] || [ "$TARGET" == "all" ] && services+=("backend")
    
    local missing=0
    for service in "${services[@]}"; do
        for color in "blue" "green"; do
            if ! check_container "$service-$color"; then
                echo "Creating missing $service-$color container..."
                deploy_container "$service" "$color"
                missing=1
            fi
        done
    done
    
    [ $missing -eq 1 ] && sleep 10
}

# Nginx 설정 업데이트 (중요: 백업 컨테이너 참조 제거)
update_nginx() {
    local service=$1
    local color=$2
    local conf_path=$([ "$service" == "frontend" ] && echo "$FRONTEND_NGINX_CONF_PATH" || echo "$BACKEND_NGINX_CONF_PATH")
    local port=$([ "$service" == "frontend" ] && echo "80" || echo "8080")
    
    mkdir -p "$conf_path"
    echo -e "upstream $service {\n    server $service-$color:$port;    # active\n}" > "$conf_path/upstream.conf"
    
    # Nginx 설정 테스트 및 적용
    if docker exec proxy nginx -t; then
        docker exec proxy nginx -s reload
        echo "Nginx successfully updated to use $service-$color"
    else
        echo "Warning: Nginx test failed, trying full restart..."
        cd "$DEPLOY_PATH/proxy"
        docker compose -f proxy-compose.yml -p proxy down
        docker compose -f proxy-compose.yml -p proxy up -d
        sleep 5
    fi
}

# 메인 함수
main() {
    echo "Starting blue-green deployment: ENV=$ENV, TARGET=$TARGET"
    
    # 초기 배포 확인
    if [ ! -f "$DEPLOY_PATH/../.initial_deploy_done" ]; then
        echo "Performing initial deployment..."
        mkdir -p "$FRONTEND_NGINX_CONF_PATH" "$BACKEND_NGINX_CONF_PATH"
        
        cd "$DEPLOY_PATH"
        docker compose up -d mysql redis
        docker compose up -d frontend-blue backend-blue
        
        echo -e "upstream frontend {\n    server frontend-blue:80;    # active\n}" > "$FRONTEND_NGINX_CONF_PATH/upstream.conf"
        echo -e "upstream backend {\n    server backend-blue:8080;    # active\n}" > "$BACKEND_NGINX_CONF_PATH/upstream.conf"
        
        cd "$DEPLOY_PATH/proxy"
        docker compose -f proxy-compose.yml -p proxy up -d
        
        cd "$DEPLOY_PATH/monitoring"
        docker compose -f monitoring-compose.yml -p monitoring up -d
        
        touch "$DEPLOY_PATH/../.initial_deploy_done"
        echo "Initial deployment completed."
        return
    fi
    
    # 모든 필요한 컨테이너 확인
    ensure_containers
    
    # 각 서비스 배포
    if [ "$TARGET" == "frontend" ] || [ "$TARGET" == "all" ]; then
        current_color=$(check_current_service "frontend")
        target_color=$([ "$current_color" == "blue" ] && echo "green" || echo "blue")
        
        echo "=== Deploying Frontend: $current_color -> $target_color ==="
        deploy_container "frontend" "$target_color"
        health_check "frontend" "$target_color"
        update_nginx "frontend" "$target_color"
        
        # 이전 컨테이너 정리
        echo "Waiting 30s before stopping old frontend-$current_color..."
        sleep 30
        docker stop frontend-$current_color 2>/dev/null || true
    fi
    
    if [ "$TARGET" == "backend" ] || [ "$TARGET" == "all" ]; then
        current_color=$(check_current_service "backend")
        target_color=$([ "$current_color" == "blue" ] && echo "green" || echo "blue")
        
        echo "=== Deploying Backend: $current_color -> $target_color ==="
        deploy_container "backend" "$target_color"
        health_check "backend" "$target_color"
        update_nginx "backend" "$target_color"
        
        # 이전 컨테이너 정리
        echo "Waiting 30s before stopping old backend-$current_color..."
        sleep 30
        docker stop backend-$current_color 2>/dev/null || true
        
        # 시스템 정리
        if [ $(df -h | grep /dev/sda1 | awk '{print $5}' | sed 's/%//') -gt 70 ]; then
            echo "Cleaning up Docker system..."
            docker image prune -f
            docker container prune -f
            docker network prune -f
            docker builder prune -f
        fi
    fi
    
    echo "Blue-Green deployment completed successfully!"
}

# 스크립트 실행
main
