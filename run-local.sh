#!/bin/bash

echo "================================================"
echo "  CodeReview AI Assistant - 로컬 실행"
echo "================================================"
echo ""

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Java 버전 확인
echo "✓ Java 버전 확인 중..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    echo -e "${GREEN}✓ Java $JAVA_VERSION 감지됨${NC}"

    if [ "$JAVA_VERSION" -lt 17 ]; then
        echo -e "${RED}✗ Java 17 이상이 필요합니다!${NC}"
        exit 1
    fi
else
    echo -e "${RED}✗ Java가 설치되지 않았습니다!${NC}"
    exit 1
fi

echo ""
echo "✓ 프로필: local (H2 인메모리 데이터베이스)"
echo "✓ 포트: 8080"
echo "✓ OpenAI API: ${OPENAI_API_KEY:-demo-key (테스트 모드)}"
echo ""

# 환경 변수 설정 (선택적)
export SPRING_PROFILES_ACTIVE=local

# Gradle wrapper 실행
echo "================================================"
echo "  애플리케이션 시작 중..."
echo "================================================"
echo ""

./gradlew bootRun --args='--spring.profiles.active=local'

# 종료 메시지
echo ""
echo "================================================"
echo "  애플리케이션이 종료되었습니다"
echo "================================================"
