#!/bin/bash

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo "================================================"
echo "  ngrok 설정 도우미"
echo "================================================"
echo ""

# ngrok 설치 확인
if command -v ngrok &> /dev/null; then
    echo -e "${GREEN}✓ ngrok이 이미 설치되어 있습니다!${NC}"
    NGROK_VERSION=$(ngrok version | head -1)
    echo -e "  버전: $NGROK_VERSION"
else
    echo -e "${YELLOW}✗ ngrok이 설치되지 않았습니다.${NC}"
    echo ""
    echo "설치 방법:"
    echo ""

    # OS 감지
    if [[ "$OSTYPE" == "darwin"* ]]; then
        echo "macOS:"
        echo -e "  ${BLUE}brew install ngrok${NC}"
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        echo "Linux:"
        echo -e "  ${BLUE}wget https://bin.equinox.io/c/bNyj1mQVY4c/ngrok-v3-stable-linux-amd64.tgz${NC}"
        echo -e "  ${BLUE}tar xvzf ngrok-v3-stable-linux-amd64.tgz${NC}"
        echo -e "  ${BLUE}sudo mv ngrok /usr/local/bin${NC}"
    fi

    echo ""
    echo "또는 공식 웹사이트에서 다운로드:"
    echo -e "  ${BLUE}https://ngrok.com/download${NC}"
    echo ""
    exit 1
fi

echo ""
echo "================================================"
echo "  ngrok 계정 설정"
echo "================================================"
echo ""
echo "1. ngrok 계정이 없다면 무료로 가입하세요:"
echo -e "   ${BLUE}https://dashboard.ngrok.com/signup${NC}"
echo ""
echo "2. Auth Token을 복사하세요:"
echo -e "   ${BLUE}https://dashboard.ngrok.com/get-started/your-authtoken${NC}"
echo ""
read -p "Auth Token을 입력하세요 (Enter로 스킵): " AUTH_TOKEN

if [ ! -z "$AUTH_TOKEN" ]; then
    ngrok authtoken "$AUTH_TOKEN"
    echo -e "${GREEN}✓ Auth Token이 설정되었습니다!${NC}"
else
    echo -e "${YELLOW}⚠ Auth Token 설정을 스킵했습니다.${NC}"
fi

echo ""
echo "================================================"
echo "  ngrok 터널 시작"
echo "================================================"
echo ""
echo "다음 명령어로 터널을 시작하세요:"
echo -e "  ${BLUE}ngrok http 8080${NC}"
echo ""
echo "터널이 시작되면 다음과 같은 URL이 표시됩니다:"
echo -e "  ${GREEN}Forwarding    https://abcd-1234-5678.ngrok.io -> http://localhost:8080${NC}"
echo ""
echo "이 URL을 복사하여 GitHub Webhook 설정에 사용하세요!"
echo ""
echo "================================================"
echo "  GitHub Webhook 설정"
echo "================================================"
echo ""
echo "1. GitHub Repository → Settings → Webhooks → Add webhook"
echo ""
echo "2. 다음 정보 입력:"
echo -e "   ${BLUE}Payload URL:${NC} https://your-ngrok-url.ngrok.io/api/webhook/github"
echo -e "   ${BLUE}Content type:${NC} application/json"
echo -e "   ${BLUE}Secret:${NC} (비워두기)"
echo -e "   ${BLUE}Events:${NC} Pull requests"
echo ""
echo "3. Add webhook 클릭"
echo ""
echo "================================================"
echo ""
echo "준비가 되었다면 새 터미널에서 ngrok을 시작하세요:"
echo -e "  ${YELLOW}ngrok http 8080${NC}"
echo ""
