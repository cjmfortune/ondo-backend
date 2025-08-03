#!/bin/bash

# Ondo Backend Image Update Script
# 이미지 패치 확인 후 imageName 업데이트

set -e

# 설정 변수
DOCKER_REGISTRY="jongmin402"
IMAGE_NAME="ondo-backend"
COMPOSE_FILE="docker-compose.yml"
CONTAINER_NAME="ondo-backend"

# 색상 코드
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

# 현재 실행 중인 이미지 정보 확인
get_current_image_info() {
    log_info "현재 실행 중인 컨테이너 이미지 정보 확인 중..."
    
    if docker ps --format "table {{.Names}}\t{{.Image}}\t{{.Status}}" | grep -q "$CONTAINER_NAME"; then
        CURRENT_IMAGE=$(docker ps --format "{{.Image}}" --filter "name=$CONTAINER_NAME")
        log_info "현재 실행 중인 이미지: $CURRENT_IMAGE"
        return 0
    else
        log_warning "실행 중인 $CONTAINER_NAME 컨테이너를 찾을 수 없습니다."
        CURRENT_IMAGE=""
        return 1
    fi
}

# 최신 이미지 정보 확인
check_latest_image() {
    log_info "Docker Hub에서 최신 이미지 정보 확인 중..."
    
    # Docker Hub API를 통해 최신 태그 정보 확인
    LATEST_TAG=$(curl -s "https://registry.hub.docker.com/v2/repositories/$DOCKER_REGISTRY/$IMAGE_NAME/tags/" | \
                 jq -r '.results[0].name' 2>/dev/null || echo "latest")
    
    LATEST_IMAGE="$DOCKER_REGISTRY/$IMAGE_NAME:$LATEST_TAG"
    log_info "최신 이미지: $LATEST_IMAGE"
}

# 로컬 이미지 업데이트 확인
check_image_updates() {
    log_info "이미지 업데이트 확인 중..."
    
    # 최신 이미지 pull
    if docker pull "$LATEST_IMAGE"; then
        log_success "최신 이미지 pull 완료"
        
        # 현재 이미지와 최신 이미지 비교
        if [ "$CURRENT_IMAGE" != "$LATEST_IMAGE" ]; then
            log_warning "새로운 이미지 버전이 발견되었습니다!"
            log_info "현재: $CURRENT_IMAGE"
            log_info "최신: $LATEST_IMAGE"
            return 0
        else
            # 이미지 ID 비교로 실제 변경사항 확인
            CURRENT_IMAGE_ID=$(docker images --format "{{.ID}}" "$CURRENT_IMAGE" 2>/dev/null | head -1)
            LATEST_IMAGE_ID=$(docker images --format "{{.ID}}" "$LATEST_IMAGE" 2>/dev/null | head -1)
            
            if [ "$CURRENT_IMAGE_ID" != "$LATEST_IMAGE_ID" ]; then
                log_warning "동일한 태그이지만 이미지 내용이 업데이트되었습니다!"
                return 0
            else
                log_success "이미지가 이미 최신 버전입니다."
                return 1
            fi
        fi
    else
        log_error "이미지 pull 실패"
        return 1
    fi
}

# Docker Compose 파일의 이미지명 업데이트
update_compose_file() {
    log_info "Docker Compose 파일 업데이트 중..."
    
    # 백업 생성
    cp "$COMPOSE_FILE" "${COMPOSE_FILE}.backup.$(date +%Y%m%d_%H%M%S)"
    
    # 이미지명 업데이트
    sed -i "s|image: $DOCKER_REGISTRY/$IMAGE_NAME:.*|image: $LATEST_IMAGE|g" "$COMPOSE_FILE"
    
    log_success "Docker Compose 파일 업데이트 완료"
    log_info "업데이트된 이미지: $LATEST_IMAGE"
}

# 컨테이너 재시작
restart_container() {
    log_info "컨테이너 재시작 중..."
    
    # 기존 컨테이너 중지 및 제거
    docker-compose down backend 2>/dev/null || true
    
    # 새 컨테이너 시작
    docker-compose up -d backend
    
    log_success "컨테이너 재시작 완료"
}

# 헬스 체크
health_check() {
    log_info "서비스 헬스 체크 중..."
    
    # 컨테이너 시작 대기
    sleep 10
    
    # 컨테이너 상태 확인
    if docker ps --filter "name=$CONTAINER_NAME" --filter "status=running" | grep -q "$CONTAINER_NAME"; then
        log_success "컨테이너가 정상적으로 실행 중입니다."
        
        # API 헬스 체크
        for i in {1..30}; do
            if curl -s http://localhost:3000/projects > /dev/null 2>&1; then
                log_success "API 헬스 체크 성공!"
                return 0
            fi
            log_info "API 응답 대기 중... ($i/30)"
            sleep 2
        done
        
        log_warning "API 헬스 체크 실패 - 수동으로 확인해주세요."
        return 1
    else
        log_error "컨테이너 실행 실패"
        return 1
    fi
}

# 롤백 함수
rollback() {
    log_error "업데이트 실패! 롤백을 수행합니다..."
    
    # 백업 파일 복원
    BACKUP_FILE=$(ls -t ${COMPOSE_FILE}.backup.* 2>/dev/null | head -1)
    if [ -n "$BACKUP_FILE" ]; then
        cp "$BACKUP_FILE" "$COMPOSE_FILE"
        log_info "Docker Compose 파일 복원 완료"
        
        # 컨테이너 재시작
        docker-compose down backend 2>/dev/null || true
        docker-compose up -d backend
        
        log_warning "롤백 완료. 이전 버전으로 복원되었습니다."
    else
        log_error "백업 파일을 찾을 수 없습니다."
    fi
}

# 메인 실행 함수
main() {
    echo "🚀 Ondo Backend Image Update Script"
    echo "=================================="
    
    # 현재 이미지 정보 확인
    get_current_image_info
    
    # 최신 이미지 확인
    check_latest_image
    
    # 업데이트 필요 여부 확인
    if check_image_updates; then
        log_info "이미지 업데이트를 진행합니다..."
        
        # 사용자 확인 (자동 모드가 아닌 경우)
        if [ "$1" != "--auto" ]; then
            read -p "업데이트를 진행하시겠습니까? (y/N): " -n 1 -r
            echo
            if [[ ! $REPLY =~ ^[Yy]$ ]]; then
                log_info "업데이트가 취소되었습니다."
                exit 0
            fi
        fi
        
        # 업데이트 실행
        if update_compose_file && restart_container && health_check; then
            log_success "🎉 이미지 업데이트가 성공적으로 완료되었습니다!"
            
            # 이전 이미지 정리 (선택사항)
            if [ "$1" = "--cleanup" ] || [ "$2" = "--cleanup" ]; then
                log_info "이전 이미지 정리 중..."
                docker image prune -f
                log_success "이미지 정리 완료"
            fi
        else
            rollback
            exit 1
        fi
    else
        log_success "업데이트할 이미지가 없습니다."
    fi
    
    # 최종 상태 출력
    echo ""
    echo "📊 현재 상태:"
    docker ps --format "table {{.Names}}\t{{.Image}}\t{{.Status}}" | grep -E "(NAMES|$CONTAINER_NAME)"
    echo ""
    log_success "스크립트 실행 완료!"
}

# 스크립트 실행
main "$@"
