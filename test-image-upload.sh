#!/bin/bash

echo "=== 이미지 업로드 기능 테스트 ==="

# 테스트 업로드 디렉토리 생성
mkdir -p /tmp/test-uploads
chmod 755 /tmp/test-uploads

echo ""
echo "1. 컴파일 테스트..."
./gradlew compileTestJava --no-daemon

if [ $? -eq 0 ]; then
    echo "✅ 컴파일 성공"
else
    echo "❌ 컴파일 실패"
    exit 1
fi

echo ""
echo "2. 단위 테스트 실행..."
./gradlew test --tests "com.ondoproject.controller.ImageControllerTest.단일_이미지_업로드_성공" --no-daemon

echo ""
echo "3. 서비스 테스트 실행..."
./gradlew test --tests "com.ondoproject.service.image.ImageServiceTest.단일_이미지_업로드_성공_프로젝트_있음" --no-daemon

echo ""
echo "=== 테스트 완료 ==="

# 테스트 업로드 디렉토리 정리
rm -rf /tmp/test-uploads

echo ""
echo "테스트 결과는 build/reports/tests/test/index.html 에서 확인할 수 있습니다."
