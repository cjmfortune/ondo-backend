#!/bin/bash

echo "🚀 Starting deployment process..."

# 1. 컨테이너 중지 및 제거
echo "📦 Stopping and removing existing containers..."
docker-compose down backend

# 2. 기존 이미지 제거 (캐시 문제 방지)
echo "🗑️ Removing old image cache..."
docker rmi jongmin402/ondo-backend:latest 2>/dev/null || echo "No existing image to remove"

# 3. 최신 이미지 pull
echo "⬇️ Pulling latest image..."
docker pull jongmin402/ondo-backend:latest

# 4. 새로운 컨테이너 시작
echo "🔄 Starting new container..."
docker-compose up -d backend

# 5. 컨테이너 상태 확인
echo "✅ Checking container status..."
sleep 5
docker ps | grep ondo-backend

# 6. API 테스트
echo "🧪 Testing API..."
sleep 10
curl -s http://localhost:3000/projects > /dev/null && echo "✅ API is working!" || echo "❌ API test failed"

echo "🎉 Deployment completed!"
