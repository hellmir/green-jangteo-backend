# Vercel 컨테이너 Function 이미지
# Vercel의 빌드 컨텍스트는 이 파일이 있는 디렉터리다. 소스가 같으면 Gradle 단계를 캐시하도록 경로를 나눠 복사한다.
FROM public.ecr.aws/docker/library/eclipse-temurin:17-jdk AS build
WORKDIR /workspace
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
COPY src/main src/main
RUN chmod +x gradlew && ./gradlew bootJar -x test --no-daemon --console=plain
# AppCDS는 평평한 클래스패스의 jar만 보관할 수 있으므로 실행 jar를 풀어 애플리케이션 클래스도 jar로 묶는다
RUN mkdir -p /out/lib /tmp/exploded \
    && cd /tmp/exploded \
    && jar xf /workspace/build/libs/green-jangteo-0.0.1-SNAPSHOT.jar \
    && mv BOOT-INF/lib/*.jar /out/lib/ \
    && jar cf /out/lib/0-application.jar -C BOOT-INF/classes .

FROM public.ecr.aws/docker/library/eclipse-temurin:17-jre
RUN groupadd -r appuser && useradd -r -g appuser -d /home/appuser -s /sbin/nologin appuser
WORKDIR /app
COPY --from=build /out/lib /app/lib
COPY vercel-cds-train.sh /app/vercel-cds-train.sh
# 클래스패스 순서를 학습 실행과 운영 실행에서 똑같이 고정한다
RUN printf -- '-cp %s\n' "$(ls /app/lib/*.jar | sort | tr '\n' ':' | sed 's/:$//')" > /app/classpath.args \
    && sh /app/vercel-cds-train.sh \
    && rm /app/vercel-cds-train.sh
USER appuser
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=vercel PORT=8080
ENTRYPOINT ["java","-XX:SharedArchiveFile=/app/app.jsa","-Xshare:auto","-XX:TieredStopAtLevel=1","-XX:+UseSerialGC","-XX:MaxRAMPercentage=70","-XX:InitialRAMPercentage=40","@/app/classpath.args","com.firstone.greenjangteo.GreenJangteoApplication"]
