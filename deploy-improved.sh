#!/bin/bash

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 로그 함수
log_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

# 에러 발생 시 스크립트 중단
set -e

# 에러 핸들링
handle_error() {
    log_error "Deployment failed at line $1"
    log_info "Rolling back to previous version..."
    docker-compose up -d backend 2>/dev/null || true
    exit 1
}

trap 'handle_error $LINENO' ERR

echo "🚀 Starting deployment process..."

# 환경 변수 확인
if [ -z "$DOCKER_IMAGE" ]; then
    DOCKER_IMAGE="jongmin402/ondo-backend:latest"
fi

log_info "Using Docker image: $DOCKER_IMAGE"

# 1. 현재 실행 중인 컨테이너 백업 정보 저장
log_info "Backing up current container info..."
CURRENT_CONTAINER=$(docker ps --filter "name=ondo-backend" --format "{{.ID}}" | head -1)
if [ ! -z "$CURRENT_CONTAINER" ]; then
    log_info "Current container ID: $CURRENT_CONTAINER"
else
    log_warning "No existing container found"
fi

# 2. 헬스체크 함수
health_check() {
    local max_attempts=30
    local attempt=1
    
    log_info "Performing health check..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s -f http://localhost:3000/projects > /dev/null 2>&1; then
            log_success "Health check passed (attempt $attempt/$max_attempts)"
            return 0
        fi
        
        log_info "Health check attempt $attempt/$max_attempts failed, retrying in 5 seconds..."
        sleep 5
        ((attempt++))
    done
    
    log_error "Health check failed after $max_attempts attempts"
    return 1
}

# 3. 컨테이너 중지 및 제거
log_info "Stopping and removing existing containers..."
docker-compose down backend --timeout 30

# 4. 기존 이미지 제거 (선택적)
if [ "$FORCE_REBUILD" = "true" ]; then
    log_info "Force rebuild enabled - removing old image cache..."
    docker rmi $DOCKER_IMAGE 2>/dev/null || log_warning "No existing image to remove"
fi

# 5. 최신 이미지 pull
log_info "Pulling latest image: $DOCKER_IMAGE"
if ! docker pull $DOCKER_IMAGE; then
    log_error "Failed to pull Docker image"
    exit 1
fi

# 6. 새로운 컨테이너 시작
log_info "Starting new container..."
docker-compose up -d backend

# 7. 컨테이너 시작 대기
log_info "Waiting for container to start..."
sleep 10

# 8. 컨테이너 상태 확인
log_info "Checking container status..."
if docker ps | grep -q ondo-backend; then
    log_success "Container is running"
    docker ps | grep ondo-backend
else
    log_error "Container failed to start"
    log_info "Container logs:"
    docker-compose logs backend --tail 50
    exit 1
fi

# 9. 헬스체크 수행
if health_check; then
    log_success "Deployment completed successfully!"
    
    # 10. 배포 정보 출력
    echo ""
    log_info "=== Deployment Summary ==="
    echo "Image: $DOCKER_IMAGE"
    echo "Container: $(docker ps --filter 'name=ondo-backend' --format '{{.Names}}' | head -1)"
    echo "Status: $(docker ps --filter 'name=ondo-backend' --format '{{.Status}}' | head -1)"
    echo "Ports: $(docker ps --filter 'name=ondo-backend' --format '{{.Ports}}' | head -1)"
    echo "Deployed at: $(date)"
    
else
    log_error "Deployment failed - health check unsuccessful"
    
    # 롤백 시도
    if [ ! -z "$CURRENT_CONTAINER" ]; then
        log_info "Attempting rollback..."
        docker start $CURRENT_CONTAINER 2>/dev/null || log_warning "Rollback failed"
    fi
    
    exit 1
fi

echo "🎉 Deployment process completed!"
