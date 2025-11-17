# API 테스트 가이드

이 문서는 CodeReview AI Assistant의 REST API를 테스트하는 방법을 설명합니다.

## 목차
- [환경 설정](#환경-설정)
- [Webhook API](#webhook-api)
- [Dashboard API](#dashboard-api)
- [Review Rules API](#review-rules-api)
- [Postman Collection](#postman-collection)
- [cURL 예제](#curl-예제)

## 환경 설정

### 로컬 서버 실행

```bash
# 1. 의존성 서비스 시작
docker-compose up -d postgres redis rabbitmq

# 2. 환경 변수 설정
cp .env.example .env
# .env 파일 수정

# 3. 애플리케이션 실행
./gradlew bootRun
```

### 기본 URL
```
http://localhost:8080
```

## Webhook API

### GitHub Webhook 수신

**엔드포인트:** `POST /api/webhook/github`

**Headers:**
```
Content-Type: application/json
X-GitHub-Event: pull_request
X-Hub-Signature-256: sha256=...
```

**Request Body:**
```json
{
  "action": "opened",
  "pull_request": {
    "id": 123456,
    "number": 1,
    "title": "Add new feature",
    "body": "This PR adds...",
    "state": "open",
    "user": {
      "login": "testuser"
    },
    "head": {
      "sha": "abc123def456"
    },
    "diff_url": "https://github.com/..."
  },
  "repository": {
    "id": 789012,
    "name": "my-repo",
    "full_name": "testowner/my-repo",
    "owner": {
      "login": "testowner"
    }
  },
  "installation": {
    "id": 345678
  }
}
```

**Response: 200 OK**
```json
"Webhook processed successfully"
```

**테스트 예제:**
```bash
curl -X POST http://localhost:8080/api/webhook/github \
  -H "Content-Type: application/json" \
  -H "X-GitHub-Event: pull_request" \
  -H "X-Hub-Signature-256: sha256=test" \
  -d @webhook-payload.json
```

### Health Check

**엔드포인트:** `GET /api/webhook/health`

**Response: 200 OK**
```
OK
```

**테스트 예제:**
```bash
curl http://localhost:8080/api/webhook/health
```

## Dashboard API

### 1. 전체 통계 조회

**엔드포인트:** `GET /api/dashboard/statistics`

**Response: 200 OK**
```json
{
  "overallStats": {
    "totalRepositories": 5,
    "totalPullRequests": 20,
    "totalReviews": 30,
    "totalComments": 100,
    "averageCommentsPerReview": 3.33,
    "averageProcessingTimeMs": 5000,
    "totalTokensUsed": 50000
  },
  "severityDistribution": {
    "info": 50,
    "warning": 30,
    "error": 20
  },
  "categoryDistribution": {
    "bug": 40,
    "performance": 30,
    "security": 20,
    "style": 10
  },
  "recentActivity": {
    "reviewsToday": 5,
    "reviewsThisWeek": 15,
    "reviewsThisMonth": 30,
    "lastReviewTime": "2025-01-17T10:30:00"
  }
}
```

**테스트 예제:**
```bash
curl http://localhost:8080/api/dashboard/statistics | jq
```

### 2. 최근 리뷰 목록 조회

**엔드포인트:** `GET /api/dashboard/reviews/recent?limit=10`

**Query Parameters:**
- `limit` (optional): 조회할 리뷰 수 (기본값: 10)

**Response: 200 OK**
```json
[
  {
    "reviewId": 1,
    "repositoryName": "my-repo",
    "repositoryOwner": "testowner",
    "prNumber": 1,
    "prTitle": "Add new feature",
    "commitSha": "abc123",
    "reviewStatus": "completed",
    "totalComments": 5,
    "severityCounts": {
      "info": 2,
      "warning": 2,
      "error": 1
    },
    "tokensUsed": 1000,
    "processingTimeMs": 5000,
    "createdAt": "2025-01-17T10:00:00"
  }
]
```

**테스트 예제:**
```bash
# 기본 (10개)
curl http://localhost:8080/api/dashboard/reviews/recent | jq

# 최근 5개만
curl http://localhost:8080/api/dashboard/reviews/recent?limit=5 | jq
```

### 3. 트렌드 데이터 조회

**엔드포인트:** `GET /api/dashboard/trends?days=30`

**Query Parameters:**
- `days` (optional): 조회할 일수 (기본값: 30)

**Response: 200 OK**
```json
{
  "dailyReviews": [
    {
      "date": "2025-01-17",
      "count": 5
    },
    {
      "date": "2025-01-16",
      "count": 3
    }
  ],
  "dailyComments": [
    {
      "date": "2025-01-17",
      "count": 25
    }
  ],
  "dailyIssues": [
    {
      "date": "2025-01-17",
      "count": 3
    }
  ]
}
```

**테스트 예제:**
```bash
# 최근 30일
curl http://localhost:8080/api/dashboard/trends | jq

# 최근 7일
curl http://localhost:8080/api/dashboard/trends?days=7 | jq
```

### 4. Repository별 통계

**엔드포인트:** `GET /api/dashboard/repositories/statistics`

**Response: 200 OK**
```json
{
  "testowner/my-repo": {
    "totalReviews": 10,
    "totalComments": 50,
    "averageCommentsPerReview": 5.0
  },
  "testowner/another-repo": {
    "totalReviews": 5,
    "totalComments": 20,
    "averageCommentsPerReview": 4.0
  }
}
```

**테스트 예제:**
```bash
curl http://localhost:8080/api/dashboard/repositories/statistics | jq
```

## Review Rules API

### 1. 규칙 목록 조회

**엔드포인트:** `GET /api/rules`

**Query Parameters:**
- `repositoryId` (optional): Repository ID로 필터링

**Response: 200 OK**
```json
[
  {
    "id": 1,
    "repositoryId": null,
    "name": "보안: 하드코딩된 비밀번호 검사",
    "description": "코드에 하드코딩된 비밀번호나 API 키가 있는지 검사합니다.",
    "ruleType": "code_pattern",
    "ruleConfig": {
      "patterns": [
        "password\\s*=\\s*[\"'].*[\"']",
        "api[_-]?key\\s*=\\s*[\"'].*[\"']"
      ],
      "severity": "error"
    },
    "enabled": true,
    "priority": 100,
    "targetFiles": "**/*",
    "excludeFiles": null,
    "minSeverity": null,
    "customMessage": null,
    "createdAt": "2025-01-17T10:00:00",
    "updatedAt": "2025-01-17T10:00:00"
  }
]
```

**테스트 예제:**
```bash
# 모든 규칙
curl http://localhost:8080/api/rules | jq

# Repository별 규칙
curl http://localhost:8080/api/rules?repositoryId=1 | jq
```

### 2. 규칙 생성

**엔드포인트:** `POST /api/rules`

**Request Body:**
```json
{
  "name": "커스텀 성능 규칙",
  "description": "N+1 쿼리 문제를 검사합니다",
  "ruleType": "custom_prompt",
  "ruleConfig": {
    "prompt": "Check for N+1 query problems in database access code",
    "severity": "warning"
  },
  "enabled": true,
  "priority": 80,
  "targetFiles": "**/*.java",
  "excludeFiles": "**/test/**",
  "minSeverity": "warning"
}
```

**Response: 201 Created**
```json
{
  "id": 4,
  "name": "커스텀 성능 규칙",
  ...
}
```

**테스트 예제:**
```bash
curl -X POST http://localhost:8080/api/rules \
  -H "Content-Type: application/json" \
  -d '{
    "name": "테스트 규칙",
    "ruleType": "custom_prompt",
    "ruleConfig": {"prompt": "Check code quality"},
    "enabled": true,
    "priority": 50,
    "targetFiles": "**/*.java"
  }' | jq
```

### 3. 규칙 수정

**엔드포인트:** `PUT /api/rules/{id}`

**Request Body:**
```json
{
  "enabled": false,
  "priority": 90
}
```

**Response: 200 OK**
```json
{
  "id": 4,
  "enabled": false,
  "priority": 90,
  ...
}
```

**테스트 예제:**
```bash
curl -X PUT http://localhost:8080/api/rules/4 \
  -H "Content-Type: application/json" \
  -d '{
    "enabled": false
  }' | jq
```

### 4. 규칙 삭제

**엔드포인트:** `DELETE /api/rules/{id}`

**Response: 204 No Content**

**테스트 예제:**
```bash
curl -X DELETE http://localhost:8080/api/rules/4
```

## Postman Collection

### Collection Import

Postman Collection 파일을 다운로드하여 사용:

```json
{
  "info": {
    "name": "CodeReview AI Assistant",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Dashboard",
      "item": [
        {
          "name": "Get Statistics",
          "request": {
            "method": "GET",
            "url": "{{base_url}}/api/dashboard/statistics"
          }
        }
      ]
    }
  ],
  "variable": [
    {
      "key": "base_url",
      "value": "http://localhost:8080"
    }
  ]
}
```

## cURL 예제 모음

### 전체 워크플로우 테스트

```bash
#!/bin/bash

BASE_URL="http://localhost:8080"

echo "1. Health Check"
curl $BASE_URL/api/webhook/health
echo -e "\n"

echo "2. Dashboard Statistics"
curl $BASE_URL/api/dashboard/statistics | jq '.overallStats'
echo -e "\n"

echo "3. Recent Reviews"
curl $BASE_URL/api/dashboard/reviews/recent?limit=5 | jq '.[0]'
echo -e "\n"

echo "4. Get Rules"
curl $BASE_URL/api/rules | jq '.[0]'
echo -e "\n"

echo "5. Create Rule"
curl -X POST $BASE_URL/api/rules \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Rule",
    "ruleType": "custom_prompt",
    "ruleConfig": {"prompt": "Test"},
    "enabled": true
  }' | jq
echo -e "\n"
```

### Python 요청 예제

```python
import requests
import json

BASE_URL = "http://localhost:8080"

# Dashboard Statistics
response = requests.get(f"{BASE_URL}/api/dashboard/statistics")
print("Statistics:", response.json())

# Create Rule
rule_data = {
    "name": "Python Test Rule",
    "ruleType": "custom_prompt",
    "ruleConfig": {"prompt": "Check Python code"},
    "enabled": True,
    "priority": 50
}

response = requests.post(
    f"{BASE_URL}/api/rules",
    json=rule_data,
    headers={"Content-Type": "application/json"}
)
print("Created Rule:", response.json())
```

## 에러 응답

### 400 Bad Request
```json
{
  "timestamp": "2025-01-17T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/rules"
}
```

### 401 Unauthorized
```json
{
  "timestamp": "2025-01-17T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid signature",
  "path": "/api/webhook/github"
}
```

### 500 Internal Server Error
```json
{
  "timestamp": "2025-01-17T10:00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Error processing webhook: ...",
  "path": "/api/webhook/github"
}
```

## 부하 테스트

### Apache Bench 사용

```bash
# 100개 요청, 동시성 10
ab -n 100 -c 10 http://localhost:8080/api/dashboard/statistics

# POST 요청 테스트
ab -n 100 -c 10 -p rule.json -T application/json \
  http://localhost:8080/api/rules
```

### k6 사용

```javascript
import http from 'k6/http';
import { check } from 'k6';

export let options = {
  vus: 10,
  duration: '30s',
};

export default function () {
  let response = http.get('http://localhost:8080/api/dashboard/statistics');
  check(response, {
    'status is 200': (r) => r.status === 200,
  });
}
```

## 참고 사항

### 인증
현재 모든 API는 인증 없이 접근 가능합니다 (개발 환경).
프로덕션에서는 적절한 인증 메커니즘을 추가해야 합니다.

### Rate Limiting
현재 Rate Limiting이 적용되지 않았습니다.
프로덕션에서는 API 남용 방지를 위해 Rate Limiting을 추가해야 합니다.

### CORS
로컬 개발 시 CORS 이슈가 발생할 수 있습니다.
필요시 CORS 설정을 추가하세요.
