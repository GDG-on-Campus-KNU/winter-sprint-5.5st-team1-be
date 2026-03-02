#!/usr/bin/env bash
# nginx HTTPS용 self-signed 인증서 생성 (스테이징/개발용)
# 실행: ./nginx/certs/generate-selfsigned.sh
# 생성 파일: cert.pem, key.pem (Git에는 커밋하지 않음)

set -e
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR"

openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout key.pem -out cert.pem \
  -subj "/CN=localhost/O=GDG-Sprint/C=KR"

echo "Created cert.pem and key.pem in $DIR"
echo "For production, replace these with real certificates (e.g. Let's Encrypt)."
