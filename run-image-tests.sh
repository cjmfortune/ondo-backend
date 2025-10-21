#!/bin/bash

echo "=== 이미지 업로드 테스트 실행 ==="

# 테스트 업로드 디렉토리 생성
mkdir -p /tmp/test-uploads

# 권한 설정
chmod 755 /tmp/test-uploads

echo "1. 단위 테스트 실행 중..."
./gradlew test --tests "com.ondoproject.service.image.ImageServiceTest" --info

echo ""
echo "2. 컨트롤러 테스트 실행 중..."
./gradlew test --tests "com.ondoproject.controller.ImageControllerTest" --info

echo ""
echo "3. 통합 테스트 실행 중..."
./gradlew test --tests "com.ondoproject.controller.ImageControllerIntegrationTest" --info

echo ""
echo "4. 모든 이미지 관련 테스트 실행 중..."
./gradlew test --tests "*Image*" --info

echo ""
echo "=== 테스트 완료 ==="

# 테스트 결과 요약
echo ""
echo "테스트 결과 요약:"
echo "- 단위 테스트: ImageServiceTest"
echo "- 컨트롤러 테스트: ImageControllerTest"  
echo "- 통합 테스트: ImageControllerIntegrationTest"
echo ""
echo "상세한 테스트 결과는 build/reports/tests/test/index.html 에서 확인할 수 있습니다."

# 테스트 업로드 디렉토리 정리
rm -rf /tmp/test-uploads
