#!/bin/bash

# 배포 스크립트
set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 로그 함수
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 환경 변수 확인
check_env() {
    if [ ! -f .env ]; then
        log_error ".env 파일이 존재하지 않습니다. .env.example을 참고하여 생성하세요."
        exit 1
    fi
    
    source .env
    
    if [ -z "$DB_PASSWORD" ] || [ -z "$JWT_SECRET" ]; then
        log_error "필수 환경 변수가 설정되지 않았습니다."
        exit 1
    fi
}

# Docker 및 Docker Compose 확인
check_docker() {
    if ! command -v docker &> /dev/null; then
        log_error "Docker가 설치되지 않았습니다."
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose가 설치되지 않았습니다."
        exit 1
    fi
}

# 백업 생성
backup_data() {
    log_info "데이터베이스 백업 생성 중..."
    
    BACKUP_DIR="./backup/$(date +%Y%m%d_%H%M%S)"
    mkdir -p $BACKUP_DIR
    
    # PostgreSQL 백업
    docker-compose exec -T db pg_dump -U $DB_USERNAME ondodb > $BACKUP_DIR/database.sql
    
    # 업로드 파일 백업
    if [ -d "./uploads" ]; then
        cp -r ./uploads $BACKUP_DIR/
    fi
    
    log_info "백업 완료: $BACKUP_DIR"
}

# 애플리케이션 빌드
build_app() {
    log_info "애플리케이션 빌드 중..."
    
    # 테스트 실행
    ./gradlew test --no-daemon
    
    if [ $? -ne 0 ]; then
        log_error "테스트 실패"
        exit 1
    fi
    
    # 빌드 실행
    ./gradlew build --no-daemon
    
    if [ $? -ne 0 ]; then
        log_error "빌드 실패"
        exit 1
    fi
    
    log_info "빌드 완료"
}

# Docker 이미지 빌드
build_docker() {
    log_info "Docker 이미지 빌드 중..."
    
    docker build -t ondo-backend:latest .
    
    if [ $? -ne 0 ]; then
        log_error "Docker 이미지 빌드 실패"
        exit 1
    fi
    
    log_info "Docker 이미지 빌드 완료"
}

# 서비스 배포
deploy_services() {
    log_info "서비스 배포 중..."
    
    # 기존 서비스 중지
    docker-compose -f docker-compose.prod.yml down
    
    # 새 서비스 시작
    docker-compose -f docker-compose.prod.yml up -d
    
    # 헬스체크 대기
    log_info "서비스 시작 대기 중..."
    sleep 30
    
    # 헬스체크
    if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
        log_info "배포 성공!"
    else
        log_error "헬스체크 실패"
        rollback
        exit 1
    fi
}

# 롤백
rollback() {
    log_warn "롤백 실행 중..."
    
    # 최신 백업 찾기
    LATEST_BACKUP=$(ls -t backup/ | head -n1)
    
    if [ -n "$LATEST_BACKUP" ]; then
        log_info "백업에서 복원 중: $LATEST_BACKUP"
        
        # 서비스 중지
        docker-compose -f docker-compose.prod.yml down
        
        # 데이터베이스 복원
        docker-compose -f docker-compose.prod.yml up -d db
        sleep 10
        docker-compose exec -T db psql -U $DB_USERNAME -d ondodb < backup/$LATEST_BACKUP/database.sql
        
        # 파일 복원
        if [ -d "backup/$LATEST_BACKUP/uploads" ]; then
            rm -rf ./uploads
            cp -r backup/$LATEST_BACKUP/uploads ./
        fi
        
        # 이전 이미지로 복원 (태그 기반)
        docker-compose -f docker-compose.prod.yml up -d
        
        log_info "롤백 완료"
    else
        log_error "백업을 찾을 수 없습니다"
    fi
}

# 로그 확인
show_logs() {
    docker-compose -f docker-compose.prod.yml logs -f
}

# 상태 확인
check_status() {
    log_info "서비스 상태 확인"
    docker-compose -f docker-compose.prod.yml ps
    
    log_info "헬스체크 결과"
    curl -s http://localhost:8080/actuator/health | jq '.' || echo "헬스체크 실패"
}

# 메인 함수
main() {
    case "$1" in
        "deploy")
            check_env
            check_docker
            backup_data
            build_app
            build_docker
            deploy_services
            ;;
        "rollback")
            rollback
            ;;
        "logs")
            show_logs
            ;;
        "status")
            check_status
            ;;
        "backup")
            check_env
            backup_data
            ;;
        *)
            echo "사용법: $0 {deploy|rollback|logs|status|backup}"
            echo ""
            echo "  deploy  - 애플리케이션 배포"
            echo "  rollback - 이전 버전으로 롤백"
            echo "  logs    - 애플리케이션 로그 확인"
            echo "  status  - 서비스 상태 확인"
            echo "  backup  - 데이터 백업"
            exit 1
            ;;
    esac
}

main "$@"
