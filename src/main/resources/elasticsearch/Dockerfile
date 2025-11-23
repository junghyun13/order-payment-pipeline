# src/main/resources/elasticsearch/Dockerfile
FROM docker.elastic.co/elasticsearch/elasticsearch:8.8.1

# 단일 노드 테스트용 환경 (production 아님)
ENV discovery.type=single-node
ENV xpack.security.enabled=false
ENV ES_JAVA_OPTS="-Xms512m -Xmx512m"

# nori 플러그인 설치
RUN elasticsearch-plugin install --batch analysis-nori || true

# settings.json 복사 (현재 Dockerfile 위치 기준)
COPY settings.json /usr/share/elasticsearch/config/settings.json

# 권한 문제 방지
USER elasticsearch

# 기본 포트
EXPOSE 9200 9300
