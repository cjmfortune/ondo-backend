#!/bin/bash

echo "=== 이미지 업로드 API 실제 테스트 ==="

# 서버 URL 설정
SERVER_URL="http://localhost:8080"

# 테스트 이미지 파일 생성
TEST_IMAGE="/tmp/test-image.jpg"
echo "Creating test image file..."
# 간단한 테스트 이미지 데이터 생성 (실제로는 유효한 이미지 파일을 사용해야 함)
echo "fake image data for testing" > $TEST_IMAGE

echo ""
echo "1. 서버 상태 확인..."
curl -s -o /dev/null -w "%{http_code}" $SERVER_URL/images
if [ $? -eq 0 ]; then
    echo "✅ 서버 연결 성공"
else
    echo "❌ 서버 연결 실패 - 서버가 실행 중인지 확인하세요"
    exit 1
fi

echo ""
echo "2. 모든 이미지 조회 테스트..."
curl -X GET $SERVER_URL/images \
  -H "Content-Type: application/json" \
  | jq '.' 2>/dev/null || echo "JSON 파싱 실패 또는 jq 미설치"

echo ""
echo "3. 단일 이미지 업로드 테스트..."
curl -X POST $SERVER_URL/images/upload \
  -F "file=@$TEST_IMAGE" \
  -F "description=Test upload from script" \
  -F "isShow=true" \
  -F "isBasic=false" \
  -F "index=1" \
  | jq '.' 2>/dev/null || echo "업로드 응답 (JSON 파싱 실패 또는 jq 미설치)"

echo ""
echo "4. 다중 이미지 업로드 테스트..."
# 두 번째 테스트 파일 생성
TEST_IMAGE2="/tmp/test-image2.jpg"
echo "fake image data for testing 2" > $TEST_IMAGE2

curl -X POST $SERVER_URL/images/upload/multiple \
  -F "files=@$TEST_IMAGE" \
  -F "files=@$TEST_IMAGE2" \
  -F "description=Multiple upload test" \
  -F "isShow=true" \
  -F "isBasic=false" \
  | jq '.' 2>/dev/null || echo "다중 업로드 응답 (JSON 파싱 실패 또는 jq 미설치)"

echo ""
echo "5. 잘못된 파일 형식 테스트..."
TEST_TEXT="/tmp/test-document.txt"
echo "This is a text file, not an image" > $TEST_TEXT

curl -X POST $SERVER_URL/images/upload \
  -F "file=@$TEST_TEXT" \
  -F "description=Invalid file type test" \
  | jq '.' 2>/dev/null || echo "잘못된 파일 형식 응답"

echo ""
echo "6. 빈 파일 테스트..."
EMPTY_FILE="/tmp/empty.jpg"
touch $EMPTY_FILE

curl -X POST $SERVER_URL/images/upload \
  -F "file=@$EMPTY_FILE" \
  -F "description=Empty file test" \
  | jq '.' 2>/dev/null || echo "빈 파일 응답"

echo ""
echo "=== API 테스트 완료 ==="

# 테스트 파일 정리
rm -f $TEST_IMAGE $TEST_IMAGE2 $TEST_TEXT $EMPTY_FILE

echo ""
echo "참고사항:"
echo "- 서버가 실행 중이어야 합니다 (./gradlew bootRun)"
echo "- jq가 설치되어 있으면 JSON 응답을 예쁘게 볼 수 있습니다"
echo "- 실제 이미지 파일로 테스트하려면 파일 경로를 수정하세요"
