#!/bin/bash

# Docker 환경 테스트 스크립트
# 사용법: ./test-docker.sh

set -e

echo "🚀 CodeReview AI Assistant - Docker 테스트 스크립트"
echo "=================================================="
echo ""

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 1. 환경 변수 파일 확인
echo "📋 1단계: 환경 변수 파일 확인"
if [ ! -f .env ]; then
    echo -e "${YELLOW}⚠️  .env 파일이 없습니다. .env.docker 파일을 복사합니다...${NC}"
    cp .env.docker .env
    echo -e "${YELLOW}⚠️  .env 파일을 열어서 실제 값으로 수정해주세요!${NC}"
    echo -e "${YELLOW}   특히 OPENAI_API_KEY는 필수입니다.${NC}"
    echo ""
    read -p "계속하시겠습니까? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
else
    echo -e "${GREEN}✅ .env 파일이 존재합니다${NC}"
fi
echo ""

# 2. Docker 및 Docker Compose 확인
echo "🐳 2단계: Docker 환경 확인"
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker가 설치되어 있지 않습니다${NC}"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}❌ Docker Compose가 설치되어 있지 않습니다${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Docker: $(docker --version)${NC}"
echo -e "${GREEN}✅ Docker Compose: $(docker-compose --version)${NC}"
echo ""

# 3. 기존 컨테이너 정리 (선택사항)
echo "🧹 3단계: 기존 컨테이너 정리"
read -p "기존 컨테이너를 정리하시겠습니까? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "컨테이너를 중지하고 제거합니다..."
    docker-compose down -v
    echo -e "${GREEN}✅ 정리 완료${NC}"
fi
echo ""

# 4. Docker Compose 빌드 및 실행
echo "🏗️  4단계: Docker 이미지 빌드 및 서비스 시작"
echo "이 과정은 몇 분 정도 소요될 수 있습니다..."
docker-compose up -d --build

echo -e "${GREEN}✅ 서비스 시작 완료${NC}"
echo ""

# 5. 서비스 상태 확인
echo "📊 5단계: 서비스 상태 확인"
sleep 5
docker-compose ps
echo ""

# 6. 헬스 체크 대기
echo "🏥 6단계: 애플리케이션 헬스 체크"
echo "애플리케이션이 시작될 때까지 대기 중..."

MAX_ATTEMPTS=30
ATTEMPT=0

while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo -e "${GREEN}✅ 애플리케이션이 정상적으로 시작되었습니다!${NC}"
        break
    fi

    ATTEMPT=$((ATTEMPT + 1))
    echo -n "."
    sleep 2

    if [ $ATTEMPT -eq $MAX_ATTEMPTS ]; then
        echo -e "${RED}❌ 애플리케이션 시작 시간 초과${NC}"
        echo "로그를 확인하세요: docker-compose logs app"
        exit 1
    fi
done
echo ""

# 7. 상세 헬스 체크
echo "🔍 7단계: 상세 헬스 정보"
if command -v jq &> /dev/null; then
    curl -s http://localhost:8080/actuator/health | jq
else
    curl -s http://localhost:8080/actuator/health
    echo ""
    echo -e "${YELLOW}💡 jq를 설치하면 더 보기 좋은 출력을 볼 수 있습니다${NC}"
fi
echo ""

# 8. API 테스트
echo "🧪 8단계: API 엔드포인트 테스트"

# 웹훅 헬스 체크
echo "  - 웹훅 헬스 체크..."
WEBHOOK_RESPONSE=$(curl -s http://localhost:8080/api/webhook/health)
if [ "$WEBHOOK_RESPONSE" == "OK" ]; then
    echo -e "    ${GREEN}✅ 웹훅 엔드포인트 정상${NC}"
else
    echo -e "    ${RED}❌ 웹훅 엔드포인트 오류${NC}"
fi

# 대시보드 통계 (빈 데이터일 수 있음)
echo "  - 대시보드 통계 조회..."
if curl -s http://localhost:8080/api/dashboard/statistics > /dev/null 2>&1; then
    echo -e "    ${GREEN}✅ 대시보드 엔드포인트 정상${NC}"
else
    echo -e "    ${RED}❌ 대시보드 엔드포인트 오류${NC}"
fi

# 리뷰 규칙 조회
echo "  - 커스텀 규칙 조회..."
if curl -s http://localhost:8080/api/rules > /dev/null 2>&1; then
    echo -e "    ${GREEN}✅ 규칙 관리 엔드포인트 정상${NC}"
else
    echo -e "    ${RED}❌ 규칙 관리 엔드포인트 오류${NC}"
fi

echo ""

# 9. 접속 정보 출력
echo "🎉 테스트 완료!"
echo "=================================================="
echo ""
echo "📍 서비스 접속 정보:"
echo "  - 애플리케이션: http://localhost:8080"
echo "  - Swagger UI: http://localhost:8080/swagger-ui/index.html"
echo "  - Actuator Health: http://localhost:8080/actuator/health"
echo "  - RabbitMQ Management: http://localhost:15672"
echo ""
echo "📝 유용한 명령어:"
echo "  - 로그 보기: docker-compose logs -f"
echo "  - 특정 서비스 로그: docker-compose logs -f app"
echo "  - 서비스 상태: docker-compose ps"
echo "  - 서비스 중지: docker-compose stop"
echo "  - 서비스 삭제: docker-compose down"
echo "  - 볼륨 포함 삭제: docker-compose down -v"
echo ""
echo "📚 자세한 정보는 docs/DOCKER_TESTING.md 를 참고하세요"
echo ""
