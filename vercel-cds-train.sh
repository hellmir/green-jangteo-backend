#!/bin/sh
# AppCDS 학습 실행: 실제 DB·비밀값 없이 애플리케이션을 띄워 기동과 Swagger 경로의 클래스를 적재한 뒤 종료하며 아카이브를 만든다.
# 아래 값은 전부 빌드 전용 자리 표시자이며, vercel 프로파일은 기동 중 DB·Redis에 연결하지 않는다.
set -eu

SPRING_PROFILES_ACTIVE=vercel \
PORT=18080 \
PGHOST_UNPOOLED=127.0.0.1 PGDATABASE=build_only PGUSER=build_only PGPASSWORD=build-only-not-real \
KV_URL=redis://127.0.0.1:6379 \
JWT_SECRET_KEY=build-only-not-real-build-only-not-real-build-only-not-real \
FRONTEND_IP_FOR_GREEN_JANGTEO=localhost \
FRONTEND_LOCAL_IP_FOR_SEUNGHAK=localhost:3000 \
FRONTEND_LOCAL_IP_FOR_JIYOUNG=localhost:3001 \
java -XX:ArchiveClassesAtExit=/app/app.jsa -XX:TieredStopAtLevel=1 -XX:+UseSerialGC \
    @/app/classpath.args com.firstone.greenjangteo.GreenJangteoApplication &
PID=$!

attempt=0
until curl -fsS -o /dev/null http://127.0.0.1:18080/swagger-ui/index.html; do
    attempt=$((attempt + 1))
    if [ "$attempt" -ge 240 ] || ! kill -0 "$PID" 2>/dev/null; then
        echo "AppCDS training: application did not start" >&2
        exit 1
    fi
    sleep 0.5
done

curl -fsS -o /dev/null 'http://127.0.0.1:18080/v2/api-docs?group=green-jangteo'
curl -fsS -o /dev/null http://127.0.0.1:18080/swagger-resources
# 인증 실패 응답 경로까지 적재한다
curl -s -o /dev/null http://127.0.0.1:18080/cron/coupons/issue || true

kill -TERM "$PID"
wait "$PID" || true

test -s /app/app.jsa
ls -la /app/app.jsa
