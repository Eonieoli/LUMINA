#!/bin/bash

# Blue-Green 무중단 배포 스크립트
# 사용법: ./blue-green-deploy.sh [dev|prod] [frontend|backend|all]

# 변수 초기화
ENV=$1
TARGET=$2
DEPLOY_PATH=""

# 인자 검증
validate_arguments() {
    if [[ "$ENV" != "dev" && "$ENV" != "prod" ]]; then
        echo "Error: First argument must be 'dev' or 'prod'"
        echo "Usage: ./blue-green-deploy.sh [dev|prod] [frontend|backend|all]"
        exit 1
    fi

    if [[ "$TARGET" != "frontend" && "$TARGET" != "backend" && "$TARGET" != "all" ]]; then
        echo "Error: Second argument must be 'frontend', 'backend', or 'all'"
        echo "Usage: ./blue-green-deploy.sh [dev|prod] [frontend|backend|all]"
        exit 1
    fi

    # 환경에 따른 경로 설정
    if [ "$ENV" == "dev" ]; then
        DEPLOY_PATH="/home/rublin322/lumina/infra/dev"
    else
        DEPLOY_PATH="/home/ubuntu/lumina/infra/prod"
    fi
}

# 시작 로그 출력
print_start_log() {
    echo "=============================="
    echo "Blue-Green Deployment Started"
    echo "Environment: $ENV"
    echo "Target: $TARGET"
    if [[ "$TARGET" == "backend" ]]; then
        echo "Note: Backend deployment will also deploy AI Server"
    elif [[ "$TARGET" == "all" ]]; then
        echo "Note: All services will be deployed (Frontend, Backend, AI Server)"
    fi
    echo "Deploy Path: $DEPLOY_PATH"
    echo "=============================="
}

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
    local conf_path="$DEPLOY_PATH/proxy/blue-green/$service/upstream.conf"
    
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

# 서비스별 포트 및 컨테이너 포트 가져오기
get_service_ports() {
    local service=$1
    local target_color=$2
    local ports=()
    
    if [ "$service" == "frontend" ]; then
        ports[0]="80"  # container_port
        if [ "$target_color" == "blue" ]; then
            ports[1]="3001"  # host_port
        else
            ports[1]="3002"  # host_port
        fi
    elif [ "$service" == "backend" ]; then
        ports[0]="8080"  # container_port
        if [ "$target_color" == "blue" ]; then
            ports[1]="8081"  # host_port
        else
            ports[1]="8082"  # host_port
        fi
    elif [ "$service" == "ai-server" ]; then
        ports[0]="8000"  # container_port
        if [ "$target_color" == "blue" ]; then
            ports[1]="8001"  # host_port
        else
            ports[1]="8002"  # host_port
        fi
    fi
    
    echo "${ports[0]} ${ports[1]}"
}

# 서비스 헬스체크 엔드포인트 및 설정 가져오기
get_service_health_config() {
    local service=$1
    local health_endpoint="/"
    local max_attempts=20
    local wait_time=10
    
    if [ "$service" == "backend" ]; then
        health_endpoint="/actuator/health"
    elif [ "$service" == "ai-server" ]; then
        health_endpoint="/health"
    fi
    
    echo "$health_endpoint $max_attempts $wait_time"
}

# 서비스 컨테이너 시작
start_service_container() {
    local service=$1
    local color=$2
    local port=$3
    local image_tag=$4
    
    echo "Starting new $service-$color container..."
    
    # 최신 이미지 pull 강제 실행
    echo "Pulling latest image for $service..."
    docker pull "rublin322/lumina-$service:$image_tag"
    
    cd "$DEPLOY_PATH"
    docker compose up "$service-$color" -d --force-recreate
    
    # 컨테이너가 시작되었는지 확인
    if ! container_running "$service-$color"; then
        echo "Failed to start $service-$color container!"
        return 1
    fi
    
    return 0
}

# 서비스 헬스체크
perform_health_check() {
    local service=$1
    local color=$2
    local port=$3
    local endpoint=$4
    local max_attempts=$5
    local wait_time=$6
    
    echo "Performing health check for $service-$color..."
    
    for i in $(seq 1 $max_attempts); do
        echo "Health check attempt $i/$max_attempts..."
        
        if ! container_running "$service-$color"; then
            echo "Error: Container $service-$color is not running anymore!"
            docker logs --tail 50 "$service-$color" || true
            return 1
        fi
        
        # 2초 타임아웃으로 헬스 체크
        if curl -s -m 2 -o /dev/null -w "%{http_code}" "http://localhost:$port$endpoint" | grep -q "200"; then
            echo "$service-$color is healthy!"
            return 0
        fi
        
        if [ $i -eq $max_attempts ]; then
            echo "Error: Health check failed after $max_attempts attempts"
            docker logs --tail 50 "$service-$color"
            return 1
        fi
        
        echo "Health check failed, waiting $wait_time seconds before next attempt..."
        sleep $wait_time
    done
}

# Nginx 설정 업데이트
update_nginx_config() {
    local service=$1
    local target_color=$2
    local current_color=$3
    local container_port=$4
    
    echo "Updating Nginx configuration for $service..."
    local conf_dir="$DEPLOY_PATH/proxy/blue-green/$service"
    
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
        
        return 1
    fi
    
    # 설정 변경 적용
    if ! docker exec proxy nginx -s reload; then
        echo "Nginx reload failed, trying full restart..."
        (cd "$DEPLOY_PATH/proxy" && docker compose -f proxy-compose.yml -p proxy restart)
        sleep 3
    fi
    
    return 0
}

# 서비스 배포 함수 - 모든 서비스에 공통으로 사용
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
    
    # 이미지 태그 설정
    local image_tag=""
    if [ "$ENV" == "dev" ]; then
        image_tag="develop"
    else
        image_tag="latest"
    fi
    
    # 포트 정보 가져오기
    read container_port host_port <<< $(get_service_ports "$service" "$target_color")
    
    # 헬스체크 설정 가져오기
    read health_endpoint max_attempts wait_time <<< $(get_service_health_config "$service")
    
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
    if ! start_service_container "$service" "$target_color" "$host_port" "$image_tag"; then
        echo "Failed to start $service-$target_color!"
        return 1
    fi
    
    # 건강 상태 확인
    if ! perform_health_check "$service" "$target_color" "$host_port" "$health_endpoint" "$max_attempts" "$wait_time"; then
        echo "Health check failed for $service-$target_color!"
        return 1
    fi
    
    # Nginx 설정 변경
    if ! update_nginx_config "$service" "$target_color" "$current_color" "$container_port"; then
        echo "Failed to update Nginx configuration for $service!"
        return 1
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
    mkdir -p "$DEPLOY_PATH/proxy/blue-green/frontend" "$DEPLOY_PATH/proxy/blue-green/backend" "$DEPLOY_PATH/proxy/blue-green/ai-server"
    
    # 기본 인프라 시작
    cd "$DEPLOY_PATH"
    docker compose up -d mysql redis
    
    # 모든 서비스 배포 - blue와 green 인스턴스 모두 시작
    docker compose up -d frontend-blue frontend-green backend-blue backend-green ai-server-blue ai-server-green
    
    # 각 서비스의 건강 상태 확인 기다리기
    echo "Waiting for services to initialize..."
    sleep 30 # 초기화 시간 확보
    
    # 초기 upstream.conf 설정 - 초기 배포시 active와 backup 서버 모두 설정
    local frontend_conf="$DEPLOY_PATH/proxy/blue-green/frontend/upstream.conf"
    local backend_conf="$DEPLOY_PATH/proxy/blue-green/backend/upstream.conf"
    local ai_conf="$DEPLOY_PATH/proxy/blue-green/ai-server/upstream.conf"
    
    echo -e "upstream frontend {\n    server frontend-blue:80;    # active\n    server frontend-green:80 backup;    # backup\n}" > "$frontend_conf"
    echo -e "upstream backend {\n    server backend-blue:8080;    # active\n    server backend-green:8080 backup;    # backup\n}" > "$backend_conf"
    echo -e "upstream ai-server {\n    server ai-server-blue:8000;    # active\n    server ai-server-green:8000 backup;    # backup\n}" > "$ai_conf"
    
    # monitoring 설정
    cd "$DEPLOY_PATH/monitoring"
    docker compose --env-file "$DEPLOY_PATH/.env" -f monitoring-compose.yml -p monitoring up -d

    # proxy 설정
    cd "$DEPLOY_PATH/proxy"
    docker compose -f proxy-compose.yml -p proxy up -d
    
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
        disk_usage=$(df -h | grep /dev/sda1 | awk '{print $5}' | sed 's/%//' | tr -d '\n')
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

# 모니터링 서비스 재시작
restart_monitoring() {
    echo "Restarting monitoring services to apply changes..."
    cd "$DEPLOY_PATH/monitoring"
    docker compose --env-file "$DEPLOY_PATH/.env" -f monitoring-compose.yml -p monitoring down
    docker compose --env-file "$DEPLOY_PATH/.env" -f monitoring-compose.yml -p monitoring up -d
}

# 메인 함수
main() {
    validate_arguments
    print_start_log
    
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
        # 백엔드 배포
        deploy_service "backend"
        result=$?
        if [ $result -ne 0 ]; then
            echo "Backend deployment failed!"
            exit $result
        fi
        
        # 백엔드가 배포되면 AI 서버도 함께 배포
        echo "Deploying AI Server as part of backend deployment..."
        deploy_service "ai-server"
        result=$?
        if [ $result -ne 0 ]; then
            echo "AI Server deployment failed!"
            exit $result
        fi
    fi
    
    # 모니터링 서비스 재시작 추가
    restart_monitoring
    
    # 성공적으로 배포 완료 후 정리
    cleanup
    
    echo "==============================="
    echo "Blue-Green Deployment Completed"
    echo "==============================="
}

# 스크립트 실행
main
