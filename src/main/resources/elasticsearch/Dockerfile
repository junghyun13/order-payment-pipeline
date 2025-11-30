# 베이스 이미지
FROM docker.elastic.co/elasticsearch/elasticsearch:8.8.1

# 단일 노드 테스트용 환경
ENV discovery.type=single-node
ENV xpack.security.enabled=false
ENV ES_JAVA_OPTS="-Xms512m -Xmx512m"

# root 권한으로 플러그인 설치
USER root
RUN elasticsearch-plugin install --batch analysis-nori || true

# 설정 파일 복사
COPY settings.json /usr/share/elasticsearch/config/settings.json

# 권한 문제 방지
RUN chown -R elasticsearch:elasticsearch /usr/share/elasticsearch/config/settings.json

# Elasticsearch 기본 포트
EXPOSE 9200 9300

# 최종적으로 elasticsearch 사용자로 실행
USER elasticsearch
