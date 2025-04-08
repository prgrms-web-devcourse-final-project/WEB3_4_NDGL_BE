#!/bin/bash

# Elasticsearch 호스트 설정
ES_HOST="http://elasticsearch:9200"

# 템플릿 루트 디렉토리
TEMPLATE_ROOT="/templates"

# Elasticsearch가 살아날 때까지 기다리기
echo "Waiting for Elasticsearch to be ready..."

until curl -s "$ES_HOST" >/dev/null; do
  echo "Waiting for Elasticsearch at $ES_HOST..."
  sleep 5
done

echo "Elasticsearch is ready!"

# 환경(dev, prod)
for ENV in dev prod; do
  # 로그 레벨(error, info, warn)
  for LEVEL in error info warn; do
    TEMPLATE_DIR="${TEMPLATE_ROOT}/${ENV}/${LEVEL}"

    echo ">>> Registering templates in: $TEMPLATE_DIR"

    for TEMPLATE_FILE in "$TEMPLATE_DIR"/*.json; do
      # 파일 존재 여부 확인
      [ -e "$TEMPLATE_FILE" ] || continue

      # 템플릿 이름을 파일명에서 추출 (예: error_template.json -> error_template)
      TEMPLATE_NAME=$(basename "$TEMPLATE_FILE" .json)

      echo "Uploading template: $TEMPLATE_NAME from $TEMPLATE_FILE"

      curl -X PUT "$ES_HOST/_index_template/$TEMPLATE_NAME" \
        -H "Content-Type: application/json" \
        --data-binary "@$TEMPLATE_FILE"

      echo -e "\nDone."
    done
  done
done