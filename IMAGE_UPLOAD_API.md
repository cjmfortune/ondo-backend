# Image Upload API Documentation

## 개요
ondo-backend 프로젝트에 이미지 업로드 기능이 추가되었습니다. 이 API를 통해 단일 또는 다중 이미지를 업로드하고, 이미지 정보를 관리할 수 있습니다.

## API 엔드포인트

### 1. 모든 이미지 조회
```
GET /images
```
**응답**: 모든 이미지 목록을 반환합니다.

### 2. 단일 이미지 업로드
```
POST /images/upload
```
**파라미터**:
- `file` (required): 업로드할 이미지 파일
- `projectId` (optional): 연결할 프로젝트 ID
- `description` (optional): 이미지 설명
- `isShow` (optional, default: true): 표시 여부
- `isBasic` (optional, default: false): 기본 이미지 여부
- `index` (optional, default: 0): 이미지 순서

**응답 예시**:
```json
{
  "id": 1,
  "fileName": "uuid-generated-name.jpg",
  "originalFileName": "original-name.jpg",
  "imageURL": "/uploads/uuid-generated-name.jpg",
  "fileSize": 1024000,
  "contentType": "image/jpeg",
  "createDateTime": "2025-06-23 12:00:00",
  "isShow": true,
  "isBasic": false,
  "index": 0,
  "projectId": 1,
  "projectName": "Sample Project",
  "message": "Image uploaded successfully"
}
```

### 3. 다중 이미지 업로드
```
POST /images/upload/multiple
```
**파라미터**:
- `files` (required): 업로드할 이미지 파일들 (배열)
- `projectId` (optional): 연결할 프로젝트 ID
- `description` (optional): 이미지 설명
- `isShow` (optional, default: true): 표시 여부
- `isBasic` (optional, default: false): 기본 이미지 여부

**응답**: ImageUploadResponse 객체의 배열

### 4. 이미지 정보 업데이트
```
PUT /images/{imageId}
```
**파라미터**:
- `projectId` (optional): 연결할 프로젝트 ID
- `description` (optional): 이미지 설명
- `isShow` (optional): 표시 여부
- `isBasic` (optional): 기본 이미지 여부
- `index` (optional): 이미지 순서

### 5. 이미지 삭제
```
DELETE /images/{imageId}
```
**응답**: 성공 메시지 또는 오류 정보

## 파일 제한사항
- **최대 파일 크기**: 10MB
- **허용된 파일 형식**: JPG, JPEG, PNG, GIF, WEBP
- **업로드 디렉토리**: `uploads/`

## 오류 응답 형식
```json
{
  "error": "ERROR_CODE",
  "message": "Error description",
  "timestamp": "2025-06-23 12:00:00"
}
```

## 사용 예시

### cURL을 사용한 단일 이미지 업로드
```bash
curl -X POST http://localhost:8080/images/upload \
  -F "file=@/path/to/image.jpg" \
  -F "projectId=1" \
  -F "description=Sample image" \
  -F "isShow=true" \
  -F "isBasic=false" \
  -F "index=0"
```

### cURL을 사용한 다중 이미지 업로드
```bash
curl -X POST http://localhost:8080/images/upload/multiple \
  -F "files=@/path/to/image1.jpg" \
  -F "files=@/path/to/image2.jpg" \
  -F "projectId=1" \
  -F "description=Sample images"
```

### JavaScript를 사용한 업로드
```javascript
const formData = new FormData();
formData.append('file', fileInput.files[0]);
formData.append('projectId', '1');
formData.append('description', 'Sample image');

fetch('http://localhost:8080/images/upload', {
  method: 'POST',
  body: formData
})
.then(response => response.json())
.then(data => console.log(data));
```

## 테스트
브라우저에서 `http://localhost:8080/image-upload-test.html`에 접속하여 업로드 기능을 테스트할 수 있습니다.

## 주요 기능
1. **파일 유효성 검사**: 파일 크기, 형식 검증
2. **고유 파일명 생성**: UUID를 사용하여 파일명 중복 방지
3. **정적 리소스 서빙**: 업로드된 이미지를 웹에서 접근 가능
4. **데이터베이스 연동**: 이미지 정보를 데이터베이스에 저장
5. **프로젝트 연결**: 이미지를 특정 프로젝트와 연결 가능
6. **오류 처리**: 상세한 오류 메시지 제공
7. **CORS 지원**: 프론트엔드에서 API 호출 가능

## 설정
- 업로드 디렉토리는 자동으로 생성됩니다.
- 정적 리소스 경로가 WebConfig에서 설정되어 있습니다.
- CORS가 모든 도메인에 대해 허용되어 있습니다.
