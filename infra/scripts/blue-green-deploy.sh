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
    DEPLOY_PATH="./infra/dev"
    if [ "$TARGET" == "frontend" ] || [ "$TARGET" == "all" ]; then
        FRONTEND_NGINX_CONF_PATH="$DEPLOY_PATH/proxy/blue-green/frontend"
    fi
    if [ "$TARGET" == "backend" ] || [ "$TARGET" == "all" ]; then
        BACKEND_NGINX_CONF_PATH="$DEPLOY_PATH/proxy/blue-green/backend"
    fi
else
    DEPLOY_PATH="./infra/prod"
    if [ "$TARGET" == "frontend" ] || [ "$TARGET" == "all" ]; then
        FRONTEND_NGINX_CONF_PATH="$DEPLOY_PATH/proxy/blue-green/frontend"
    fi
    if [ "$TARGET" == "backend" ] || [ "$TARGET" == "all" ]; then
        BACKEND_NGINX_CONF_PATH="$DEPLOY_PATH/proxy/blue-green/backend"
    fi
fi

# 현재 서비스 상태 확인
check_current_service() {
    local service=$1
    
    # 현재 사용 중인 색상(blue/green) 확인
    if [ "$service" == "frontend" ]; then
        # upstream.conf 파일에서 현재 사용 중인 frontend 확인
        if grep -q "frontend-blue" "$FRONTEND_NGINX_CONF_PATH/upstream.conf" && ! grep -q "frontend-blue.*#" "$FRONTEND_NGINX_CONF_PATH/upstream.conf"; then
            CURRENT_COLOR="blue"
            TARGET_COLOR="green"
        else
            CURRENT_COLOR="green"
            TARGET_COLOR="blue"
        fi
    elif [ "$service" == "backend" ]; then
        # upstream.conf 파일에서 현재 사용 중인 backend 확인
        if grep -q "backend-blue" "$BACKEND_NGINX_CONF_PATH/upstream.conf" && ! grep -q "backend-blue.*#" "$BACKEND_NGINX_CONF_PATH/upstream.conf"; then
            CURRENT_COLOR="blue"
            TARGET_COLOR="green"
        else
            CURRENT_COLOR="green"
            TARGET_COLOR="blue"
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
    docker stop $service-$color || true
    docker rm $service-$color || true
    
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

# Nginx 설정 업데이트 및 reload
update_nginx_config() {
    local service=$1
    local target_color=$2
    
    echo "Updating Nginx configuration for $service to use $target_color..."
    
    if [ "$service" == "frontend" ]; then
        # frontend upstream 설정 업데이트
        if [ "$target_color" == "blue" ]; then
            sed -i 's/upstream frontend {/upstream frontend {\n    server frontend-blue:80;    # active\n    server frontend-green:80 backup;    # backup/' "$FRONTEND_NGINX_CONF_PATH/upstream.conf"
        else
            sed -i 's/upstream frontend {/upstream frontend {\n    server frontend-green:80;    # active\n    server frontend-blue:80 backup;    # backup/' "$FRONTEND_NGINX_CONF_PATH/upstream.conf"
        fi
    elif [ "$service" == "backend" ]; then
        # backend upstream 설정 업데이트
        if [ "$target_color" == "blue" ]; then
            sed -i 's/upstream backend {/upstream backend {\n    server backend-blue:8080;    # active\n    server backend-green:8080 backup;    # backup/' "$BACKEND_NGINX_CONF_PATH/upstream.conf"
        else
            sed -i 's/upstream backend {/upstream backend {\n    server backend-green:8080;    # active\n    server backend-blue:8080 backup;    # backup/' "$BACKEND_NGINX_CONF_PATH/upstream.conf"
        fi
    fi
    
    # Nginx 설정 테스트 및 reload
    docker exec proxy nginx -t
    if [ $? -eq 0 ]; then
        docker exec proxy nginx -s reload
        echo "Nginx successfully reloaded to use $service-$target_color"
    else
        echo "Error: Nginx configuration test failed"
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
    docker stop $service-$color || true
    docker rm $service-$color || true
    
    echo "Old container $service-$color stopped and removed"
}

# 상태 확인 함수
health_check() {
    local service=$1
    local color=$2
    local max_attempts=10
    local wait_time=5
    
    echo "Performing health check for $service-$color..."
    
    for i in $(seq 1 $max_attempts); do
        echo "Health check attempt $i/$max_attempts..."
        
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
    exit 1
}

# 메인 배포 프로세스
main() {
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
main
