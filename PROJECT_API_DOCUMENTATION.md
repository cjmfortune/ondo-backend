# Project API Documentation

## 개요
ondo-backend 프로젝트에 프로젝트 관리 및 이미지 연동 기능이 추가되었습니다. 이 API를 통해 프로젝트를 생성, 조회, 업데이트, 삭제하고, 프로젝트와 이미지를 연결할 수 있습니다.

## API 엔드포인트

### 1. 전체 프로젝트 조회
```
GET /projects
```
**응답**: 모든 프로젝트 목록을 이미지 URL과 함께 반환합니다.

**응답 예시**:
```json
[
  {
    "id": 1,
    "projectName": "Sample Project",
    "description": "Project description",
    "isAvailable": true,
    "CreatedDateTime": "2025-06-23 12:00:00",
    "duration": "6개월",
    "grossFloorArea": "1000㎡",
    "client": "Client Name",
    "architect": "Architect Name",
    "index": 0,
    "projectImageUrl": "/uploads/image.jpg"
  }
]
```

### 2. 특정 프로젝트 조회
```
GET /projects/{id}
```
**응답**: 특정 프로젝트의 상세 정보를 반환합니다.

### 3. 프로젝트 생성
```
POST /projects
```
**요청 본문**:
```json
{
  "projectName": "New Project",
  "description": "Project description",
  "isAvailable": true,
  "duration": "6개월",
  "grossFloorArea": "1000㎡",
  "client": "Client Name",
  "architect": "Architect Name",
  "index": 0
}
```

### 4. 프로젝트 업데이트
```
PUT /projects/{id}
```
**요청 본문** (모든 필드는 선택사항):
```json
{
  "projectName": "Updated Project Name",
  "description": "Updated description",
  "isAvailable": false,
  "duration": "8개월",
  "grossFloorArea": "1200㎡",
  "client": "Updated Client",
  "architect": "Updated Architect",
  "index": 1
}
```

### 5. 이미지와 함께 프로젝트 생성
```
POST /projects/with-images
```
**요청 본문**:
```json
{
  "projectName": "Project with Images",
  "description": "Project description",
  "isAvailable": true,
  "duration": "6개월",
  "grossFloorArea": "1000㎡",
  "client": "Client Name",
  "architect": "Architect Name",
  "index": 0,
  "imageIds": [1, 2, 3]
}
```

### 6. 프로젝트 이미지 업데이트 (기존 이미지 연결 해제 후 새로운 이미지 연결)
```
PUT /projects/{id}/images
```
**요청 본문**:
```json
[1, 2, 3]
```

### 7. 프로젝트에 이미지 추가
```
POST /projects/{id}/images
```
**요청 본문**:
```json
[4, 5, 6]
```

### 8. 프로젝트에서 이미지 제거
```
DELETE /projects/{id}/images
```
**요청 본문**:
```json
[1, 2]
```

### 9. 프로젝트 삭제
```
DELETE /projects/{id}
```
**응답**: 성공 메시지 또는 오류 정보

## 오류 응답 형식
```json
{
  "error": "ERROR_CODE",
  "message": "Error description",
  "timestamp": "2025-06-23 12:00:00"
}
```

## 사용 예시

### cURL을 사용한 프로젝트 생성
```bash
curl -X POST http://localhost:8080/projects \
  -H "Content-Type: application/json" \
  -d '{
    "projectName": "New Project",
    "description": "Project description",
    "isAvailable": true,
    "duration": "6개월",
    "grossFloorArea": "1000㎡",
    "client": "Client Name",
    "architect": "Architect Name",
    "index": 0
  }'
```

### cURL을 사용한 프로젝트 업데이트
```bash
curl -X PUT http://localhost:8080/projects/1 \
  -H "Content-Type: application/json" \
  -d '{
    "projectName": "Updated Project Name",
    "description": "Updated description"
  }'
```

### cURL을 사용한 프로젝트 이미지 연결
```bash
curl -X PUT http://localhost:8080/projects/1/images \
  -H "Content-Type: application/json" \
  -d '[1, 2, 3]'
```

### JavaScript를 사용한 프로젝트 생성
```javascript
const projectData = {
  projectName: "New Project",
  description: "Project description",
  isAvailable: true,
  duration: "6개월",
  grossFloorArea: "1000㎡",
  client: "Client Name",
  architect: "Architect Name",
  index: 0
};

fetch('http://localhost:8080/projects', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify(projectData)
})
.then(response => response.json())
.then(data => console.log(data));
```

## 테스트
브라우저에서 `http://localhost:8080/project-management-test.html`에 접속하여 프로젝트 관리 기능을 테스트할 수 있습니다.

## 주요 기능

### 프로젝트 관리
1. **CRUD 작업**: 프로젝트 생성, 조회, 업데이트, 삭제
2. **부분 업데이트**: 필요한 필드만 업데이트 가능
3. **인덱스 관리**: 프로젝트 순서 관리
4. **상태 관리**: 프로젝트 사용 가능 여부 관리

### 이미지 연동
1. **이미지 연결**: 프로젝트에 여러 이미지 연결
2. **이미지 업데이트**: 기존 이미지 연결 해제 후 새로운 이미지 연결
3. **이미지 추가**: 기존 이미지에 새로운 이미지 추가
4. **이미지 제거**: 특정 이미지 연결 해제
5. **대표 이미지**: 프로젝트 목록에서 첫 번째 이미지를 대표 이미지로 표시

### 데이터 관리
1. **트랜잭션 처리**: 데이터 일관성 보장
2. **오류 처리**: 상세한 오류 메시지 제공
3. **유효성 검사**: 입력 데이터 검증
4. **관계 관리**: 프로젝트-이미지 간 관계 자동 관리

## 데이터 모델

### Project 엔티티
- `id`: 프로젝트 고유 ID
- `projectName`: 프로젝트명
- `description`: 프로젝트 설명
- `isAvailable`: 사용 가능 여부
- `CreatedDateTime`: 생성 일시
- `duration`: 프로젝트 기간
- `grossFloorArea`: 연면적
- `client`: 클라이언트
- `architect`: 건축가
- `index`: 정렬 순서
- `images`: 연결된 이미지 목록

### Images_Info 엔티티
- `id`: 이미지 고유 ID
- `ImageName`: 파일명
- `imageURL`: 이미지 URL
- `project`: 연결된 프로젝트 (선택사항)
- 기타 이미지 메타데이터

## 설정
- CORS가 모든 도메인에 대해 허용되어 있습니다.
- 트랜잭션 처리가 활성화되어 있습니다.
- Swagger UI를 통해 API 문서를 확인할 수 있습니다.
