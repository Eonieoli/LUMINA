#!/bin/bash

# Blue-Green 무중단 배포 스크립트
# 사용법: ./blue-green-deploy.sh [dev|prod] [frontend|backend|ai|all]

# 변수 초기화
ENV=$1
TARGET=$2
DEPLOY_PATH=""

# 인자 검증
if [[ "$ENV" != "dev" && "$ENV" != "prod" ]]; then
    echo "Error: First argument must be 'dev' or 'prod'"
    echo "Usage: ./blue-green-deploy.sh [dev|prod] [frontend|backend|ai|all]"
    exit 1
fi

if [[ "$TARGET" != "frontend" && "$TARGET" != "backend" && "$TARGET" != "ai" && "$TARGET" != "all" ]]; then
    echo "Error: Second argument must be 'frontend', 'backend', 'ai', or 'all'"
    echo "Usage: ./blue-green-deploy.sh [dev|prod] [frontend|backend|ai|all]"
    exit 1
fi

# 환경에 따른 경로 설정
if [ "$ENV" == "dev" ]; then
    DEPLOY_PATH="/home/rublin322/lumina/infra/dev"
else
    DEPLOY_PATH="/home/ubuntu/lumina/infra/prod"
fi

FRONTEND_NGINX_CONF_PATH="$DEPLOY_PATH/proxy/blue-green/frontend"
BACKEND_NGINX_CONF_PATH="$DEPLOY_PATH/proxy/blue-green/backend"

# 시작 로그
echo "=============================="
echo "Blue-Green Deployment Started"
echo "Environment: $ENV"
echo "Target: $TARGET"
echo "Deploy Path: $DEPLOY_PATH"
echo "=============================="

# 컨테이너 존재 확인
container_exists() {
    docker ps -a -q -f name=$1 2>/dev/null | grep -q .
    return $?
}

# 컨테이너 실행 중인지 확인
container_running() {
    docker ps -q -f name=$1 2>/dev/null | grep -q .
    return $?
}

# 현재 서비스 색상 확인
get_current_color() {
    local service=$1
    local conf_path=""
    
    if [ "$service" == "frontend" ]; then
        conf_path="$FRONTEND_NGINX_CONF_PATH/upstream.conf"
    else
        conf_path="$BACKEND_NGINX_CONF_PATH/upstream.conf"
    fi
    
    # 설정 파일이 없으면 blue로 가정
    if [ ! -f "$conf_path" ]; then
        echo "blue"
        return
    fi
    
    # 설정 파일에서 현재 색상 확인
    if grep -q "$service-blue" "$conf_path" && ! grep -q "$service-blue.*backup" "$conf_path"; then
        echo "blue"
    else
        echo "green"
    fi
}

# 서비스 배포 함수
deploy_service() {
    local service=$1
    
    # 현재 색상 확인 및 타겟 색상 결정
    local current_color=$(get_current_color "$service")
    local target_color=""
    
    if [ "$current_color" == "blue" ]; then
        target_color="green"
    else
        target_color="blue"
    fi
    
    echo "Deploying $service: $current_color -> $target_color"
    
    # 포트 설정
    local port=""
    local container_port=""
    local image_tag=""
    
    if [ "$ENV" == "dev" ]; then
        image_tag="develop"
    else
        image_tag="latest"
    fi
    
    if [ "$service" == "frontend" ]; then
        container_port="80"
        if [ "$target_color" == "blue" ]; then
            port="3001"
        else
            port="3002"
        fi
    else  # backend
        container_port="8080"
        if [ "$target_color" == "blue" ]; then
            port="8081"
        else
            port="8082"
        fi
    fi
    
    # 이전 컬러의 컨테이너가 있을 경우 연결 정리를 위한 대기 시간 추가
    if container_running "$service-$current_color"; then
        echo "Waiting for previous $service-$current_color to stabilize connections..."
        sleep 30  # 30초 대기
    fi
    
    # 기존 컨테이너 정리
    if container_exists "$service-$target_color"; then
        echo "Removing existing $service-$target_color container..."
        docker stop "$service-$target_color" >/dev/null 2>&1 || true
        docker rm "$service-$target_color" >/dev/null 2>&1 || true
    fi
    
    # 새 컨테이너 배포
    echo "Starting new $service-$target_color container..."
    # docker compose up $service-$target_color -d
    
    if [ "$service" == "frontend" ]; then
        docker run -d --name "$service-$target_color" \
            --network lumina-network \
            -p $port:$container_port \
            --restart always \
            --label environment=$ENV \
            "rublin322/lumina-$service:$image_tag"
    else
        docker run -d --name "$service-$target_color" \
            --network lumina-network \
            -p $port:$container_port \
            --env-file "$DEPLOY_PATH/.env" \
            --restart always \
            --label environment=$ENV \
            "rublin322/lumina-$service:$image_tag"
    fi
    
    # 건강 상태 확인
    echo "Performing health check for $service-$target_color..."
    local max_attempts=20
    local wait_time=10
    local endpoint="/"
    
    if [ "$service" == "backend" ]; then
    endpoint="/actuator/health"
    fi
    
    for i in $(seq 1 $max_attempts); do
        echo "Health check attempt $i/$max_attempts..."
        
        if ! container_running "$service-$target_color"; then
            echo "Error: Container $service-$target_color is not running anymore!"
            docker logs --tail 50 "$service-$target_color" || true
            exit 1
        fi
        
        # 2초 타임아웃으로 헬스 체크
        if curl -s -m 2 -o /dev/null -w "%{http_code}" "http://localhost:$port$endpoint" | grep -q "200"; then
            echo "$service-$target_color is healthy!"
            break
        fi
        
        if [ $i -eq $max_attempts ]; then
            echo "Error: Health check failed after $max_attempts attempts"
            docker logs --tail 50 "$service-$target_color"
            exit 1
        fi
        
        echo "Health check failed, waiting $wait_time seconds before next attempt..."
        sleep $wait_time
    done
    
    # Nginx 설정 변경
    echo "Updating Nginx configuration for $service..."
    local conf_dir=""
    
    if [ "$service" == "frontend" ]; then
        conf_dir="$FRONTEND_NGINX_CONF_PATH"
    else
        conf_dir="$BACKEND_NGINX_CONF_PATH"
    fi
    
    mkdir -p "$conf_dir"
    
    # 설정 파일 업데이트 (백업 서버 포함)
    echo "upstream $service {" > "$conf_dir/upstream.conf"
    echo "    server $service-$target_color:$container_port;    # active" >> "$conf_dir/upstream.conf"
    
    # 백업 서버가 존재하는 경우에만 추가
    if container_running "$service-$current_color"; then
        echo "    server $service-$current_color:$container_port backup;    # backup" >> "$conf_dir/upstream.conf"
    fi
    
    echo "}" >> "$conf_dir/upstream.conf"
    
    # Nginx 설정 적용
    echo "Testing and applying Nginx configuration..."
    if ! docker exec proxy nginx -t; then
        echo "Nginx configuration test failed, reverting changes..."
        
        # 설정 복원 (기존 구성으로)
        echo "upstream $service {" > "$conf_dir/upstream.conf"
        echo "    server $service-$current_color:$container_port;    # active" >> "$conf_dir/upstream.conf"
        
        # 이전 타겟이 존재하는 경우 백업으로 추가
        if container_running "$service-$target_color"; then
            echo "    server $service-$target_color:$container_port backup;    # backup" >> "$conf_dir/upstream.conf"
        fi
        
        echo "}" >> "$conf_dir/upstream.conf"
        
        # 새 컨테이너 정리
        docker stop "$service-$target_color" >/dev/null 2>&1 || true
        docker rm "$service-$target_color" >/dev/null 2>&1 || true
        
        echo "Deployment of $service failed!"
        exit 1
    fi
    
    # 설정 변경 적용
    if ! docker exec proxy nginx -s reload; then
        echo "Nginx reload failed, trying full restart..."
        (cd "$DEPLOY_PATH/proxy" && docker-compose -f proxy-compose.yml -p proxy restart)
        sleep 3
    fi
    
    echo "$service successfully switched to $target_color"
    
    # 이전 컨테이너는 유지하되 추후 정리를 위해 태그
    echo "Marking old $service-$current_color for later cleanup"
    docker update --label "pending-cleanup=true" "$service-$current_color" >/dev/null 2>&1 || true
    
    # 배포 성공
    echo "$service deployment completed successfully!"
    return 0
}

# 초기 환경 설정
initialize_environment() {
    echo "Initializing environment..."
    
    # 필요한 디렉토리 생성
    mkdir -p "$FRONTEND_NGINX_CONF_PATH" "$BACKEND_NGINX_CONF_PATH"
    
    # 기본 인프라 시작
    cd "$DEPLOY_PATH"
    docker compose up -d mysql redis
    
    # 모든 서비스 배포 - blue와 green 인스턴스 모두 시작
    docker compose up -d frontend-blue frontend-green backend-blue backend-green ai-server-blue ai-server-green
    
    # 각 서비스의 건강 상태 확인 기다리기
    echo "Waiting for services to initialize..."
    sleep 30 # 초기화 시간 확보
    
    # 초기 upstream.conf 설정 - 초기 배포시 active와 backup 서버 모두 설정
    echo -e "upstream frontend {\n    server frontend-blue:80;    # active\n    server frontend-green:80 backup;    # backup\n}" > "$FRONTEND_NGINX_CONF_PATH/upstream.conf"
    echo -e "upstream backend {\n    server backend-blue:8080;    # active\n    server backend-green:8080 backup;    # backup\n}" > "$BACKEND_NGINX_CONF_PATH/upstream.conf"
    echo -e "upstream ai-server {\n    server ai-server-blue:8000;    # active\n    server ai-server-green:8000 backup;    # backup\n}" > "$DEPLOY_PATH/proxy/blue-green/ai-server/upstream.conf"
    
    # proxy 설정
    cd "$DEPLOY_PATH/proxy"
    docker compose -f proxy-compose.yml -p proxy up -d
    
    # monitoring 설정
    cd "$DEPLOY_PATH/monitoring"
    docker compose --env-file "$DEPLOY_PATH/.env" -f monitoring-compose.yml -p monitoring up -d
    
    # 초기 배포 표시 파일 생성
    local init_flag=""
    if [ "$ENV" == "dev" ]; then
        init_flag="/home/rublin322/lumina/.initial_deploy_done"
    else
        init_flag="/home/ubuntu/lumina/.initial_deploy_done"
    fi
    
    touch "$init_flag"
    echo "Initial environment setup completed"
}

# 정리 작업
cleanup() {
    echo "Performing cleanup tasks..."
    
    # 필요한 경우에만 Docker 시스템 정리
    if [ "$ENV" == "dev" ]; then
        # 디스크 사용량이 70% 이상인 경우에만 정리
        disk_usage=$(df -h | grep /dev/sda1 | awk '{print $5}' | sed 's/%//')
        if [ "$disk_usage" -gt 70 ]; then
            echo "Disk usage is high ($disk_usage%), cleaning up Docker resources..."
            docker image prune -f
            docker container prune -f
            docker network prune -f
            
            # 매우 심각한 경우 (90% 이상) 모든 미사용 이미지 제거
            if [ "$disk_usage" -gt 90 ]; then
                echo "Critical disk usage! Removing all unused images..."
                docker image prune -a -f
            fi
        fi
    fi
    
    echo "Cleanup completed!"
}

# AI 서버 블루-그린 배포 함수
deploy_ai_server_blue_green() {
local service="ai-server"

# 현재 색상 확인 및 타겟 색상 결정
local current_color=$(get_current_color "$service")
local target_color=""

if [ "$current_color" == "blue" ]; then
target_color="green"
else
    target_color="blue"
fi

echo "Deploying $service: $current_color -> $target_color"

# 포트 설정
local port=""
local container_port="8000"
local image_tag=""

if [ "$ENV" == "dev" ]; then
    image_tag="develop"
else
image_tag="latest"
fi

if [ "$target_color" == "blue" ]; then
port="8001"
else
    port="8002"
fi

# 이전 컨테이너가 있을 경우 연결 정리를 위한 대기 시간 추가
if container_running "$service-$current_color"; then
    echo "Waiting for previous $service-$current_color to stabilize connections..."
sleep 30  # 30초 대기
fi

# 기존 컨테이너 정리
if container_exists "$service-$target_color"; then
echo "Removing existing $service-$target_color container..."
docker stop "$service-$target_color" >/dev/null 2>&1 || true
docker rm "$service-$target_color" >/dev/null 2>&1 || true
fi

# 새 컨테이너 배포
echo "Starting new $service-$target_color container..."
docker run -d --name "$service-$target_color" \
--network lumina-network \
-p $port:$container_port \
--restart always \
--label environment=$ENV \
"rublin322/lumina-ai:$image_tag"

# 건강 상태 확인
echo "Performing health check for $service-$target_color..."
local max_attempts=20
local wait_time=10
local endpoint="/health"

    for i in $(seq 1 $max_attempts); do
        echo "Health check attempt $i/$max_attempts..."
        
        if ! container_running "$service-$target_color"; then
            echo "Error: Container $service-$target_color is not running anymore!"
            docker logs --tail 50 "$service-$target_color" || true
            exit 1
        fi
        
        # 2초 타임아웃으로 헬스 체크
        if curl -s -m 2 -o /dev/null -w "%{http_code}" "http://localhost:$port$endpoint" | grep -q "200"; then
            echo "$service-$target_color is healthy!"
            break
        fi
        
        if [ $i -eq $max_attempts ]; then
            echo "Error: Health check failed after $max_attempts attempts"
            docker logs --tail 50 "$service-$target_color"
            exit 1
        fi
        
        echo "Health check failed, waiting $wait_time seconds before next attempt..."
        sleep $wait_time
    done
    
    # Nginx 설정 변경
    echo "Updating Nginx configuration for $service..."
    local conf_dir=""
    
    conf_dir="$DEPLOY_PATH/proxy/blue-green/ai-server"
    
    mkdir -p "$conf_dir"
    
    # 설정 파일 업데이트 (백업 서버 포함)
    echo "upstream $service {" > "$conf_dir/upstream.conf"
    echo "    server $service-$target_color:$container_port;    # active" >> "$conf_dir/upstream.conf"
    
    # 백업 서버가 존재하는 경우에만 추가
    if container_running "$service-$current_color"; then
        echo "    server $service-$current_color:$container_port backup;    # backup" >> "$conf_dir/upstream.conf"
    fi
    
    echo "}" >> "$conf_dir/upstream.conf"
    
    # Nginx 설정 적용
    echo "Testing and applying Nginx configuration..."
    if ! docker exec proxy nginx -t; then
        echo "Nginx configuration test failed, reverting changes..."
        
        # 설정 복원 (기존 구성으로)
        echo "upstream $service {" > "$conf_dir/upstream.conf"
        echo "    server $service-$current_color:$container_port;    # active" >> "$conf_dir/upstream.conf"
        
        # 이전 타겟이 존재하는 경우 백업으로 추가
        if container_running "$service-$target_color"; then
            echo "    server $service-$target_color:$container_port backup;    # backup" >> "$conf_dir/upstream.conf"
        fi
        
        echo "}" >> "$conf_dir/upstream.conf"
        
        # 새 컨테이너 정리
        docker stop "$service-$target_color" >/dev/null 2>&1 || true
        docker rm "$service-$target_color" >/dev/null 2>&1 || true
        
        echo "Deployment of $service failed!"
        exit 1
    fi
    
    # 설정 변경 적용
    if ! docker exec proxy nginx -s reload; then
        echo "Nginx reload failed, trying full restart..."
        (cd "$DEPLOY_PATH/proxy" && docker-compose -f proxy-compose.yml -p proxy restart)
        sleep 3
    fi
    
    echo "$service successfully switched to $target_color"
    
    # 이전 컨테이너는 유지하되 추후 정리를 위해 태그
    echo "Marking old $service-$current_color for later cleanup"
    docker update --label "pending-cleanup=true" "$service-$current_color" >/dev/null 2>&1 || true
    
    # 배포 성공
    echo "$service deployment completed successfully!"
    return 0
}

# 메인 함수
main() {
    # 초기 배포 확인
    local init_flag=""
    if [ "$ENV" == "dev" ]; then
        init_flag="/home/rublin322/lumina/.initial_deploy_done"
    else
        init_flag="/home/ubuntu/lumina/.initial_deploy_done"
    fi
    
    if [ ! -f "$init_flag" ]; then
        initialize_environment
        return
    fi
    
    # 해당 서비스 배포
    local result=0
    
    if [ "$TARGET" == "frontend" ] || [ "$TARGET" == "all" ]; then
        deploy_service "frontend"
        result=$?
        if [ $result -ne 0 ]; then
            echo "Frontend deployment failed!"
            exit $result
        fi
    fi
    
    if [ "$TARGET" == "backend" ] || [ "$TARGET" == "all" ]; then
        deploy_service "backend"
        result=$?
        if [ $result -ne 0 ]; then
            echo "Backend deployment failed!"
            exit $result
        fi
    fi
    
    # AI 서버 배포 (backend 또는 all 또는 ai가 지정된 경우에 배포)
    if [ "$TARGET" == "backend" ] || [ "$TARGET" == "all" ] || [ "$TARGET" == "ai" ]; then
        deploy_ai_server_blue_green
        result=$?
        if [ $result -ne 0 ]; then
            echo "AI Server blue-green deployment failed!"
            exit $result
        fi
    fi

    # 모니터링 서비스 재시작 추가
    echo "Restarting monitoring services to apply changes..."
    cd "$DEPLOY_PATH/monitoring"
    docker compose --env-file "$DEPLOY_PATH/.env" -f monitoring-compose.yml -p monitoring down
    docker compose --env-file "$DEPLOY_PATH/.env" -f monitoring-compose.yml -p monitoring up -d
    
    # 성공적으로 배포 완료 후 정리
    cleanup
    
    echo "==============================="
    echo "Blue-Green Deployment Completed"
    echo "==============================="
}

# 스크립트 실행
main
