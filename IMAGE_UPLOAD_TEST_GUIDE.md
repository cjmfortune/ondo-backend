# 이미지 업로드 테스트 가이드

## 개요
이 문서는 이미지 업로드 기능에 대한 다양한 테스트 방법을 설명합니다.

## 테스트 구성

### 1. 단위 테스트 (Unit Tests)
- **ImageControllerTest**: 컨트롤러 레이어 테스트
- **ImageServiceTest**: 서비스 레이어 테스트

### 2. 통합 테스트 (Integration Tests)
- **ImageControllerIntegrationTest**: 전체 애플리케이션 컨텍스트를 사용한 통합 테스트

### 3. API 테스트 (Manual API Tests)
- **test-api-upload.sh**: 실제 서버에 대한 curl 기반 API 테스트

## 테스트 실행 방법

### 1. 전체 테스트 실행
```bash
# 모든 이미지 관련 테스트 실행
./gradlew test --tests "*Image*"

# 또는 전용 스크립트 사용
./run-image-tests.sh
```

### 2. 개별 테스트 클래스 실행
```bash
# 컨트롤러 테스트만 실행
./gradlew test --tests "com.ondoproject.controller.ImageControllerTest"

# 서비스 테스트만 실행
./gradlew test --tests "com.ondoproject.service.image.ImageServiceTest"

# 통합 테스트만 실행
./gradlew test --tests "com.ondoproject.controller.ImageControllerIntegrationTest"
```

### 3. 특정 테스트 메서드 실행
```bash
# 단일 이미지 업로드 테스트만 실행
./gradlew test --tests "com.ondoproject.controller.ImageControllerTest.단일_이미지_업로드_성공"
```

### 4. 컴파일 테스트
```bash
# 테스트 코드 컴파일 확인
./test-image-upload.sh
```

### 5. 실제 API 테스트
```bash
# 서버 실행 후
./gradlew bootRun

# 다른 터미널에서
./test-api-upload.sh
```

## 테스트 시나리오

### 성공 케이스
1. **단일 이미지 업로드**: 유효한 이미지 파일 업로드
2. **다중 이미지 업로드**: 여러 이미지 파일 동시 업로드
3. **이미지 정보 업데이트**: 업로드된 이미지의 메타데이터 수정
4. **이미지 삭제**: 업로드된 이미지 삭제
5. **프로젝트 연결**: 특정 프로젝트와 이미지 연결

### 실패 케이스
1. **빈 파일 업로드**: 크기가 0인 파일 업로드 시도
2. **잘못된 파일 형식**: 이미지가 아닌 파일 업로드 시도
3. **존재하지 않는 프로젝트**: 유효하지 않은 프로젝트 ID로 업로드
4. **파일 크기 제한**: 허용된 크기를 초과하는 파일 업로드
5. **존재하지 않는 이미지 삭제**: 유효하지 않은 이미지 ID로 삭제 시도

### 경계 케이스
1. **파일명 중복**: 같은 이름의 파일 여러 번 업로드
2. **특수문자 파일명**: 한글, 공백, 특수문자가 포함된 파일명
3. **대용량 파일**: 허용 한계에 근접한 크기의 파일
4. **다양한 이미지 형식**: JPEG, PNG, GIF, WebP 등

## 테스트 데이터

### 지원하는 이미지 형식
- `image/jpeg`
- `image/jpg`
- `image/png`
- `image/gif`
- `image/webp`

### 테스트용 파라미터
- `projectId`: 프로젝트 ID (선택사항)
- `description`: 이미지 설명 (선택사항)
- `isShow`: 표시 여부 (기본값: true)
- `isBasic`: 기본 이미지 여부 (기본값: false)
- `index`: 정렬 순서 (기본값: 0)

## 테스트 결과 확인

### 1. 콘솔 출력
테스트 실행 시 콘솔에서 성공/실패 결과를 확인할 수 있습니다.

### 2. HTML 리포트
```bash
# 테스트 실행 후 브라우저에서 확인
open build/reports/tests/test/index.html
```

### 3. 로그 파일
애플리케이션 로그에서 상세한 실행 정보를 확인할 수 있습니다.

## 문제 해결

### 컴파일 오류
1. DTO 필드명 확인: `fileName`, `imageURL` 등
2. 엔티티 필드명 확인: `ImageName`, `imageURL` 등
3. 메서드 시그니처 확인

### 테스트 실패
1. Mock 설정 확인
2. 데이터베이스 상태 확인 (통합 테스트)
3. 파일 시스템 권한 확인

### API 테스트 실패
1. 서버 실행 상태 확인
2. 포트 번호 확인 (기본: 8080)
3. 업로드 디렉토리 권한 확인

## 추가 개선사항

### 1. 성능 테스트
- 대량 파일 업로드 테스트
- 동시 업로드 테스트
- 메모리 사용량 모니터링

### 2. 보안 테스트
- 악성 파일 업로드 방지
- 파일 크기 제한 검증
- 파일 형식 검증 강화

### 3. 사용성 테스트
- 프론트엔드 연동 테스트
- 파일 업로드 진행률 표시
- 에러 메시지 개선

## 참고 자료
- [Spring Boot Testing Guide](https://spring.io/guides/gs/testing-web/)
- [MockMvc Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#spring-mvc-test-framework)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
