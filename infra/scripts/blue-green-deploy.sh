#!/bin/bash

# Blue-Green 무중단 배포 스크립트
# 사용법: ./blue-green-deploy.sh [dev|prod] [frontend|backend|all]

# 오류 발생 시 스크립트 중단
set -e

# 변수 초기화
ENV=""
TARGET=""
DEPLOY_PATH=""
CURRENT_COLOR=""
TARGET_COLOR=""
BLUE_PORT=""
GREEN_PORT=""
NGINX_CONF_PATH=""

# 인자 파싱
if [ "$1" == "dev" ] || [ "$1" == "prod" ]; then
    ENV="$1"
else
    echo "Error: First argument must be 'dev' or 'prod'"
    echo "Usage: ./blue-green-deploy.sh [dev|prod] [frontend|backend|all]"
    exit 1
fi

if [ "$2" == "frontend" ] || [ "$2" == "backend" ] || [ "$2" == "all" ]; then
    TARGET="$2"
else
    echo "Error: Second argument must be 'frontend', 'backend', or 'all'"
    echo "Usage: ./blue-green-deploy.sh [dev|prod] [frontend|backend|all]"
    exit 1
fi

# 환경에 따른 경로 설정
if [ "$ENV" == "dev" ]; then
    DEPLOY_PATH="/home/rublin322/lumina/infra/dev"
    if [ "$TARGET" == "frontend" ] || [ "$TARGET" == "all" ]; then
        FRONTEND_NGINX_CONF_PATH="$DEPLOY_PATH/proxy/blue-green/frontend"
    fi
    if [ "$TARGET" == "backend" ] || [ "$TARGET" == "all" ]; then
        BACKEND_NGINX_CONF_PATH="$DEPLOY_PATH/proxy/blue-green/backend"
    fi
else
    DEPLOY_PATH="/home/ubuntu/lumina/infra/prod"
    if [ "$TARGET" == "frontend" ] || [ "$TARGET" == "all" ]; then
        FRONTEND_NGINX_CONF_PATH="$DEPLOY_PATH/proxy/blue-green/frontend"
    fi
    if [ "$TARGET" == "backend" ] || [ "$TARGET" == "all" ]; then
        BACKEND_NGINX_CONF_PATH="$DEPLOY_PATH/proxy/blue-green/backend"
    fi
fi

# 현재 서비스 상태 확인 (개선된 버전)
check_current_service() {
    local service=$1
    
    # 기본값 설정
    CURRENT_COLOR="blue"
    TARGET_COLOR="green"
    
    # 현재 사용 중인 색상(blue/green) 확인
    if [ "$service" == "frontend" ]; then
        # upstream.conf 파일이 존재하는지 확인
        if [ -f "$FRONTEND_NGINX_CONF_PATH/upstream.conf" ]; then
            # 'active' 주석이 있는 서버가 어떤 색상인지 확인
            if grep -q "frontend-blue.*active" "$FRONTEND_NGINX_CONF_PATH/upstream.conf" && ! grep -q "frontend-blue.*backup" "$FRONTEND_NGINX_CONF_PATH/upstream.conf"; then
                CURRENT_COLOR="blue"
                TARGET_COLOR="green"
            elif grep -q "frontend-green.*active" "$FRONTEND_NGINX_CONF_PATH/upstream.conf" && ! grep -q "frontend-green.*backup" "$FRONTEND_NGINX_CONF_PATH/upstream.conf"; then
                CURRENT_COLOR="green"
                TARGET_COLOR="blue"
            # backup 키워드로 구분
            elif grep -q "frontend-green.*backup" "$FRONTEND_NGINX_CONF_PATH/upstream.conf"; then
                CURRENT_COLOR="blue"
                TARGET_COLOR="green"
            elif grep -q "frontend-blue.*backup" "$FRONTEND_NGINX_CONF_PATH/upstream.conf"; then
                CURRENT_COLOR="green"
                TARGET_COLOR="blue"
            # 순서로 구분 (첫 번째 서버가 active)
            elif grep -q "frontend-blue" "$FRONTEND_NGINX_CONF_PATH/upstream.conf" && grep -q "frontend-green" "$FRONTEND_NGINX_CONF_PATH/upstream.conf"; then
                if grep -n "frontend-blue" "$FRONTEND_NGINX_CONF_PATH/upstream.conf" | cut -d: -f1 < grep -n "frontend-green" "$FRONTEND_NGINX_CONF_PATH/upstream.conf" | cut -d: -f1; then
                    CURRENT_COLOR="blue"
                    TARGET_COLOR="green"
                else
                    CURRENT_COLOR="green"
                    TARGET_COLOR="blue"
                fi
            # 하나만 있는 경우
            elif grep -q "frontend-blue" "$FRONTEND_NGINX_CONF_PATH/upstream.conf"; then
                CURRENT_COLOR="blue"
                TARGET_COLOR="green"
            elif grep -q "frontend-green" "$FRONTEND_NGINX_CONF_PATH/upstream.conf"; then
                CURRENT_COLOR="green"
                TARGET_COLOR="blue"
            else
                echo "Warning: Could not determine current frontend color from upstream.conf, defaulting to blue->green deployment"
            fi
        else
            echo "Warning: No upstream.conf file found for frontend. Assuming blue is active."
        fi
    elif [ "$service" == "backend" ]; then
        # upstream.conf 파일이 존재하는지 확인
        if [ -f "$BACKEND_NGINX_CONF_PATH/upstream.conf" ]; then
            # 'active' 주석이 있는 서버가 어떤 색상인지 확인
            if grep -q "backend-blue.*active" "$BACKEND_NGINX_CONF_PATH/upstream.conf" && ! grep -q "backend-blue.*backup" "$BACKEND_NGINX_CONF_PATH/upstream.conf"; then
                CURRENT_COLOR="blue"
                TARGET_COLOR="green"
            elif grep -q "backend-green.*active" "$BACKEND_NGINX_CONF_PATH/upstream.conf" && ! grep -q "backend-green.*backup" "$BACKEND_NGINX_CONF_PATH/upstream.conf"; then
                CURRENT_COLOR="green"
                TARGET_COLOR="blue"
            # backup 키워드로 구분
            elif grep -q "backend-green.*backup" "$BACKEND_NGINX_CONF_PATH/upstream.conf"; then
                CURRENT_COLOR="blue"
                TARGET_COLOR="green"
            elif grep -q "backend-blue.*backup" "$BACKEND_NGINX_CONF_PATH/upstream.conf"; then
                CURRENT_COLOR="green"
                TARGET_COLOR="blue"
            # 순서로 구분 (첫 번째 서버가 active)
            elif grep -q "backend-blue" "$BACKEND_NGINX_CONF_PATH/upstream.conf" && grep -q "backend-green" "$BACKEND_NGINX_CONF_PATH/upstream.conf"; then
                if grep -n "backend-blue" "$BACKEND_NGINX_CONF_PATH/upstream.conf" | cut -d: -f1 < grep -n "backend-green" "$BACKEND_NGINX_CONF_PATH/upstream.conf" | cut -d: -f1; then
                    CURRENT_COLOR="blue"
                    TARGET_COLOR="green"
                else
                    CURRENT_COLOR="green"
                    TARGET_COLOR="blue"
                fi
            # 하나만 있는 경우
            elif grep -q "backend-blue" "$BACKEND_NGINX_CONF_PATH/upstream.conf"; then
                CURRENT_COLOR="blue"
                TARGET_COLOR="green"
            elif grep -q "backend-green" "$BACKEND_NGINX_CONF_PATH/upstream.conf"; then
                CURRENT_COLOR="green"
                TARGET_COLOR="blue"
            else
                echo "Warning: Could not determine current backend color from upstream.conf, defaulting to blue->green deployment"
            fi
        else
            echo "Warning: No upstream.conf file found for backend. Assuming blue is active."
        fi
    fi
    
    echo "Current $service: $CURRENT_COLOR, Target $service: $TARGET_COLOR"
}

# 도커 컨테이너 배포
deploy_container() {
    local service=$1
    local color=$2
    
    echo "Deploying $service-$color..."
    
    # 환경 변수 설정
    if [ "$ENV" == "dev" ]; then
        TAG="develop"
    else
        TAG="latest"
    fi
    
    # 이미지 이름 설정
    local image_name="rublin322/lumina-$service:$TAG"
    
    # 컨테이너 실행 (기존 컨테이너가 있으면 중지하고 삭제)
    docker stop $service-$color 2>/dev/null || true
    docker rm $service-$color 2>/dev/null || true
    
    # 서비스별 실행 명령
    if [ "$service" == "frontend" ]; then
        if [ "$color" == "blue" ]; then
            PORT=3001
        else
            PORT=3002
        fi
        
        docker run -d --name $service-$color \
            --network lumina-network \
            -p $PORT:80 \
            --restart always \
            --label environment=$ENV \
            $image_name
            
    elif [ "$service" == "backend" ]; then
        if [ "$color" == "blue" ]; then
            PORT=8081
        else
            PORT=8082
        fi
        
        # 백엔드 환경 변수 파일 경로
        local env_file="$DEPLOY_PATH/.env"
        
        docker run -d --name $service-$color \
            --network lumina-network \
            -p $PORT:8080 \
            --env-file $env_file \
            --restart always \
            --label environment=$ENV \
            $image_name
    fi
    
    echo "$service-$color deployed on port $PORT"
}

# 컨테이너가 직접 확인 가능한지 확인
check_container_available() {
    local container_name=$1
    if docker ps -q -f name=$container_name &> /dev/null; then
        return 0  # 컨테이너 정상 실행 중
    else
        return 1  # 컨테이너 없음
    fi
}

# Nginx 설정 업데이트 및 reload (개선된 버전)
update_nginx_config() {
    local service=$1
    local target_color=$2
    local other_color=""
    
    # 다른 색상 결정
    if [ "$target_color" == "blue" ]; then
        other_color="green"
    else
        other_color="blue"
    fi
    
    echo "Updating Nginx configuration for $service to use $target_color..."
    
    # 디렉토리 존재 확인 및 생성
    if [ "$service" == "frontend" ]; then
        mkdir -p "$FRONTEND_NGINX_CONF_PATH"
    elif [ "$service" == "backend" ]; then
        mkdir -p "$BACKEND_NGINX_CONF_PATH"
    fi
    
    # 타겟 컨테이너 존재 여부 확인
    if ! check_container_available "$service-$target_color"; then
        echo "Error: $service-$target_color container is not running"
        exit 1
    fi
    
    # 백업 컨테이너 존재 여부 확인
    local backup_available=0
    if check_container_available "$service-$other_color"; then
        backup_available=1
    fi
    
    # Nginx 설정 파일 생성
    if [ "$service" == "frontend" ]; then
        if [ "$target_color" == "blue" ]; then
            if [ $backup_available -eq 1 ]; then
                echo -e "upstream frontend {\n    server frontend-blue:80;    # active\n    server frontend-green:80 backup;    # backup\n}" > "$FRONTEND_NGINX_CONF_PATH/upstream.conf"
            else
                echo -e "upstream frontend {\n    server frontend-blue:80;    # active\n}" > "$FRONTEND_NGINX_CONF_PATH/upstream.conf"
            fi
        else
            if [ $backup_available -eq 1 ]; then
                echo -e "upstream frontend {\n    server frontend-green:80;    # active\n    server frontend-blue:80 backup;    # backup\n}" > "$FRONTEND_NGINX_CONF_PATH/upstream.conf"
            else
                echo -e "upstream frontend {\n    server frontend-green:80;    # active\n}" > "$FRONTEND_NGINX_CONF_PATH/upstream.conf"
            fi
        fi
    elif [ "$service" == "backend" ]; then
        if [ "$target_color" == "blue" ]; then
            if [ $backup_available -eq 1 ]; then
                echo -e "upstream backend {\n    server backend-blue:8080;    # active\n    server backend-green:8080 backup;    # backup\n}" > "$BACKEND_NGINX_CONF_PATH/upstream.conf"
            else
                echo -e "upstream backend {\n    server backend-blue:8080;    # active\n}" > "$BACKEND_NGINX_CONF_PATH/upstream.conf"
            fi
        else
            if [ $backup_available -eq 1 ]; then
                echo -e "upstream backend {\n    server backend-green:8080;    # active\n    server backend-blue:8080 backup;    # backup\n}" > "$BACKEND_NGINX_CONF_PATH/upstream.conf"
            else
                echo -e "upstream backend {\n    server backend-green:8080;    # active\n}" > "$BACKEND_NGINX_CONF_PATH/upstream.conf"
            fi
        fi
    fi
    
    # Nginx 설정 테스트 및 reload
    if docker exec proxy nginx -t; then
        # 일반적인 리로드 시도
        if docker exec proxy nginx -s reload; then
            echo "Nginx successfully reloaded to use $service-$target_color"
        else
            echo "Warning: Nginx reload failed, trying full restart..."
            # 리로드 실패 시 컨테이너 재시작
            cd "$DEPLOY_PATH/proxy"
            docker compose -f proxy-compose.yml -p proxy down
            docker compose -f proxy-compose.yml -p proxy up -d
            echo "Nginx restarted to use $service-$target_color"
            # 재시작 후 5초 대기 (서비스 안정화)
            sleep 5
        fi
    else
        echo "Error: Nginx configuration test failed"
        # 설정 파일 백업 복원 (개선 가능)
        exit 1
    fi
}

# 이전 버전 컨테이너 정리 (유예 시간 후)
cleanup_old_container() {
    local service=$1
    local color=$2
    
    echo "Old container $service-$color will be stopped in 30 seconds..."
    sleep 30  # 트래픽이 완전히 새 버전으로 전환될 때까지 대기
    
    # 이전 컨테이너 중지 및 삭제
    docker stop $service-$color 2>/dev/null || true
    docker rm $service-$color 2>/dev/null || true
    
    echo "Old container $service-$color stopped and removed"
    
    # 배포가 완료된 후 Docker 시스템 정리
    # 사용하지 않는 이미지, 네트워크, 벨륨 등을 정리
    if [ "$TARGET" == "all" ] || [ "$service" == "backend" -a "$TARGET" == "backend" ]; then
        echo "Cleaning up Docker system..."
        # 이미지 정리 (현재 실행 중인 컨테이너에서 사용하지 않는 이미지 제거)
        docker image prune -f
        # 중지된 컨테이너 정리
        docker container prune -f
        # 사용하지 않는 네트워크 정리
        docker network prune -f
        # 빌드 캐시 정리
        docker builder prune -f
        
        # 경고: 다음 명령어는 모든 사용하지 않는 이미지를 삭제합니다.
        # 이는 일반적으로 자주 실행하는 것은 좋지 않지만, 디스크 공간이 매우 부족한 경우 사용합니다.
        if [ "$ENV" == "dev" ] && [ $(df -h | grep /dev/sda1 | awk '{print $5}' | sed 's/%//') -gt 80 ]; then
            echo "Disk usage is high (>80%). Removing all unused images..."
            docker image prune -af
        fi
        
        echo "Docker system cleanup completed"
    fi
}

# 상태 확인 함수 (개선된 버전)
health_check() {
    local service=$1
    local color=$2
    local max_attempts=10
    local wait_time=5
    
    echo "Performing health check for $service-$color..."
    
    for i in $(seq 1 $max_attempts); do
        echo "Health check attempt $i/$max_attempts..."
        
        # 컨테이너가 여전히 실행 중인지 확인
        if ! check_container_available "$service-$color"; then
            echo "Error: $service-$color container is not running"
            exit 1
        fi
        
        if [ "$service" == "frontend" ]; then
            # Frontend 상태 확인 (포트는 컨테이너 내부 포트 사용)
            if [ "$color" == "blue" ]; then
                PORT=3001
            else
                PORT=3002
            fi
            
            # curl을 통한 상태 확인
            if curl -s -o /dev/null -w "%{http_code}" http://localhost:$PORT/ | grep -q "200"; then
                echo "$service-$color is healthy"
                return 0
            fi
        elif [ "$service" == "backend" ]; then
            # Backend 상태 확인 (Spring Boot Actuator 활용)
            if [ "$color" == "blue" ]; then
                PORT=8081
            else
                PORT=8082
            fi
            
            # Actuator 헬스 체크
            if curl -s -o /dev/null -w "%{http_code}" http://localhost:$PORT/actuator/health | grep -q "200"; then
                echo "$service-$color is healthy"
                return 0
            fi
        fi
        
        echo "Health check failed, waiting $wait_time seconds before next attempt..."
        sleep $wait_time
    done
    
    echo "Error: Health check failed after $max_attempts attempts"
    # 실패한 컨테이너 로그 출력
    echo "Container logs for $service-$color:"
    docker logs --tail 50 $service-$color
    exit 1
}

# 초기 환경 설정 함수
initialize_environment() {
    echo "Initializing environment..."
    
    # 전역 변수로 저장 - 절대 경로 사용
    if [ "$ENV" == "dev" ]; then
        DEPLOY_PATH="/home/rublin322/lumina/infra/dev"
        FRONTEND_NGINX_CONF_PATH="$DEPLOY_PATH/proxy/blue-green/frontend"
        BACKEND_NGINX_CONF_PATH="$DEPLOY_PATH/proxy/blue-green/backend"
    else
        DEPLOY_PATH="/home/ubuntu/lumina/infra/prod"
        FRONTEND_NGINX_CONF_PATH="$DEPLOY_PATH/proxy/blue-green/frontend"
        BACKEND_NGINX_CONF_PATH="$DEPLOY_PATH/proxy/blue-green/backend"
    fi
    
    # 필요한 디렉토리 생성
    mkdir -p "$FRONTEND_NGINX_CONF_PATH"
    mkdir -p "$BACKEND_NGINX_CONF_PATH"
    
    # DB, Redis 설정
    cd "$DEPLOY_PATH"
    docker compose up -d mysql redis
    
    # frontend-blue와 backend-blue 한번에 배포
    docker compose up -d frontend-blue backend-blue
    
    # 초기 upstream.conf 설정
    echo -e "upstream frontend {\n    server frontend-blue:80;    # active\n}" > "$FRONTEND_NGINX_CONF_PATH/upstream.conf"
    echo -e "upstream backend {\n    server backend-blue:8080;    # active\n}" > "$BACKEND_NGINX_CONF_PATH/upstream.conf"
    
    # proxy 설정
    cd "$DEPLOY_PATH/proxy"
    docker compose -f proxy-compose.yml -p proxy up -d
    
    # monitoring 설정
    cd "$DEPLOY_PATH/monitoring"
    docker compose -f monitoring-compose.yml -p monitoring up -d
    
    # 초기 배포 표시 파일 생성
    if [ "$ENV" == "dev" ]; then
        touch /home/rublin322/lumina/.initial_deploy_done
    else
        touch /home/ubuntu/lumina/.initial_deploy_done
    fi
    
    echo "Initial environment setup completed"
}

# 서비스 구성 동기화 (실제 상태와 설정 일치시키기)
sync_service_config() {
    local service=$1
    
    echo "Syncing $service configuration with actual state..."
    
    # 양쪽 컨테이너 모두 실행 중인지 확인
    local blue_running=0
    local green_running=0
    
    if check_container_available "$service-blue"; then
        blue_running=1
    fi
    
    if check_container_available "$service-green"; then
        green_running=1
    fi
    
    # 어느 쪽도 실행 중이지 않은 경우
    if [ $blue_running -eq 0 ] && [ $green_running -eq 0 ]; then
        echo "Warning: Neither $service-blue nor $service-green is running!"
        return 1
    fi
    
    # 설정 파일 존재 여부 확인
    local conf_path=""
    if [ "$service" == "frontend" ]; then
        conf_path="$FRONTEND_NGINX_CONF_PATH/upstream.conf"
    else
        conf_path="$BACKEND_NGINX_CONF_PATH/upstream.conf"
    fi
    
    # 설정 파일 없는 경우 생성
    if [ ! -f "$conf_path" ]; then
        mkdir -p "$(dirname "$conf_path")"
        
        if [ $blue_running -eq 1 ]; then
            if [ "$service" == "frontend" ]; then
                echo -e "upstream frontend {\n    server frontend-blue:80;    # active\n}" > "$conf_path"
            else
                echo -e "upstream backend {\n    server backend-blue:8080;    # active\n}" > "$conf_path"
            fi
            echo "Created new $service configuration with blue as active"
        else
            if [ "$service" == "frontend" ]; then
                echo -e "upstream frontend {\n    server frontend-green:80;    # active\n}" > "$conf_path"
            else
                echo -e "upstream backend {\n    server backend-green:8080;    # active\n}" > "$conf_path"
            fi
            echo "Created new $service configuration with green as active"
        fi
        
        # Nginx 설정 적용
        if docker exec proxy nginx -t; then
            # 일반적인 리로드 시도
            if docker exec proxy nginx -s reload; then
                echo "Nginx configuration applied."
            else
                echo "Warning: Nginx reload failed, trying full restart..."
                # 리로드 실패 시 컨테이너 재시작
                cd "$DEPLOY_PATH/proxy"
                docker compose -f proxy-compose.yml -p proxy down
                docker compose -f proxy-compose.yml -p proxy up -d
                echo "Nginx restarted with new configuration"
                # 재시작 후 5초 대기 (서비스 안정화)
                sleep 5
            fi
        else
            echo "Error: Nginx configuration test failed"
            # 문제 해결을 위한 로그 출력
            cat "$conf_path"
            return 1
        fi
    fi
    
    return 0
}

# 메인 배포 프로세스
main() {
    # 초기 배포인지 확인
    if [ "$ENV" == "dev" ]; then
        if [ ! -f /home/rublin322/lumina/.initial_deploy_done ]; then
            initialize_environment
            # 초기 배포인 경우 여기서 종료
            return
        fi
    else
        if [ ! -f /home/ubuntu/lumina/.initial_deploy_done ]; then
            initialize_environment
            # 초기 배포인 경우 여기서 종료
            return
        fi
    fi
    
    # 기존 서비스 구성 동기화 (설정 파일과 실제 상태 일치 확인)
    if [ "$TARGET" == "frontend" ] || [ "$TARGET" == "all" ]; then
        sync_service_config "frontend" || echo "Warning: Frontend sync failed but continuing..."
    fi
    
    if [ "$TARGET" == "backend" ] || [ "$TARGET" == "all" ]; then
        sync_service_config "backend" || echo "Warning: Backend sync failed but continuing..."
    fi
    
    # 실제 배포 시작
    if [ "$TARGET" == "frontend" ] || [ "$TARGET" == "all" ]; then
        echo "=== Deploying Frontend ==="
        check_current_service "frontend"
        deploy_container "frontend" "$TARGET_COLOR"
        health_check "frontend" "$TARGET_COLOR"
        update_nginx_config "frontend" "$TARGET_COLOR"
        cleanup_old_container "frontend" "$CURRENT_COLOR"
    fi
    
    if [ "$TARGET" == "backend" ] || [ "$TARGET" == "all" ]; then
        echo "=== Deploying Backend ==="
        check_current_service "backend"
        deploy_container "backend" "$TARGET_COLOR"
        health_check "backend" "$TARGET_COLOR"
        update_nginx_config "backend" "$TARGET_COLOR"
        cleanup_old_container "backend" "$CURRENT_COLOR"
    fi
    
    echo "Blue-Green deployment completed successfully!"
}

# 스크립트 실행
echo "Starting blue-green deployment with ENV=$ENV, TARGET=$TARGET"
echo "DEPLOY_PATH=$DEPLOY_PATH"
if [ "$TARGET" == "frontend" ] || [ "$TARGET" == "all" ]; then
    echo "FRONTEND_NGINX_CONF_PATH=$FRONTEND_NGINX_CONF_PATH"
fi
if [ "$TARGET" == "backend" ] || [ "$TARGET" == "all" ]; then
    echo "BACKEND_NGINX_CONF_PATH=$BACKEND_NGINX_CONF_PATH"
fi
main
