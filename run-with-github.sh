#!/bin/bash

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo "================================================"
echo "  CodeReview AI Assistant - GitHub 연동 실행"
echo "================================================"
echo ""

# 환경 변수 확인
check_env() {
    local var_name=$1
    local var_value=$(eval echo \$$var_name)

    if [ -z "$var_value" ]; then
        echo -e "${RED}✗ $var_name이 설정되지 않았습니다!${NC}"
        return 1
    else
        # API 키는 일부만 표시
        if [[ $var_name == *"KEY"* ]] || [[ $var_name == *"TOKEN"* ]]; then
            local masked_value="${var_value:0:10}...${var_value: -4}"
            echo -e "${GREEN}✓ $var_name: $masked_value${NC}"
        else
            echo -e "${GREEN}✓ $var_name: $var_value${NC}"
        fi
        return 0
    fi
}

# .env 파일 로드 시도
if [ -f .env ]; then
    echo -e "${BLUE}📄 .env 파일을 찾았습니다. 환경 변수를 로드합니다...${NC}"
    export $(grep -v '^#' .env | xargs)
    echo ""
fi

echo "✓ 필수 환경 변수 확인 중..."
echo ""

# 필수 변수 체크
missing_vars=0

if ! check_env "OPENAI_API_KEY"; then
    echo -e "${YELLOW}  OpenAI API Key를 설정하세요: https://platform.openai.com/api-keys${NC}"
    missing_vars=$((missing_vars + 1))
fi

if ! check_env "GITHUB_TOKEN"; then
    echo -e "${YELLOW}  GitHub Token을 설정하세요: https://github.com/settings/tokens${NC}"
    missing_vars=$((missing_vars + 1))
fi

echo ""

if [ $missing_vars -gt 0 ]; then
    echo -e "${RED}================================================${NC}"
    echo -e "${RED}  환경 변수가 누락되었습니다!${NC}"
    echo -e "${RED}================================================${NC}"
    echo ""
    echo "다음 방법 중 하나로 설정하세요:"
    echo ""
    echo "방법 1) .env 파일 생성:"
    echo "  cat > .env << 'EOF'"
    echo "  OPENAI_API_KEY=sk-your-key-here"
    echo "  GITHUB_TOKEN=ghp_your-token-here"
    echo "  EOF"
    echo ""
    echo "방법 2) 환경 변수로 직접 설정:"
    echo "  export OPENAI_API_KEY=sk-your-key-here"
    echo "  export GITHUB_TOKEN=ghp_your-token-here"
    echo "  ./run-with-github.sh"
    echo ""
    exit 1
fi

echo -e "${GREEN}================================================${NC}"
echo -e "${GREEN}  모든 환경 변수가 설정되었습니다!${NC}"
echo -e "${GREEN}================================================${NC}"
echo ""

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
echo "================================================"
echo "  실행 정보"
echo "================================================"
echo "  프로필: local (H2 인메모리 DB)"
echo "  포트: 8080"
echo "  모델: ${OPENAI_MODEL:-gpt-4-turbo-preview}"
echo "================================================"
echo ""

echo -e "${BLUE}📝 다음 단계:${NC}"
echo ""
echo "1. 이 스크립트를 실행한 채로 두세요"
echo "2. 새 터미널에서 ngrok 실행:"
echo -e "   ${YELLOW}ngrok http 8080${NC}"
echo ""
echo "3. ngrok URL을 복사 (예: https://abcd-1234.ngrok.io)"
echo ""
echo "4. GitHub Repository → Settings → Webhooks → Add webhook"
echo "   - Payload URL: https://your-ngrok-url/api/webhook/github"
echo "   - Content type: application/json"
echo "   - Events: Pull requests"
echo ""
echo "5. PR을 생성하고 자동 코드 리뷰를 확인하세요!"
echo ""
echo "================================================"
echo "  애플리케이션 시작 중..."
echo "================================================"
echo ""

# Spring Boot 실행
./gradlew bootRun --args='--spring.profiles.active=local' \
    --console=plain

# 종료 메시지
echo ""
echo "================================================"
echo "  애플리케이션이 종료되었습니다"
echo "================================================"
