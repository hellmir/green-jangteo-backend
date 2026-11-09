package com.firstone.greenjangteo.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import springfox.documentation.spring.web.plugins.DocumentationPluginsBootstrapper;

/**
 * Springfox는 웹 서버가 포트를 연 다음 단계에서 문서를 스캔하므로, 콜드 스타트 직후 /v2/api-docs가 404가 된다.
 * 웹 서버 시작 단계보다 먼저 스캔을 끝내 포트가 열리면 곧바로 문서를 줄 수 있게 한다.
 */
@Component
@RequiredArgsConstructor
public class SwaggerDocumentationInitializer implements SmartLifecycle {
    private static final int BEFORE_WEB_SERVER_START_PHASE = Integer.MAX_VALUE - 2;

    private final DocumentationPluginsBootstrapper documentationPluginsBootstrapper;

    private volatile boolean running;

    @Override
    public void start() {
        documentationPluginsBootstrapper.start();
        running = true;
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return BEFORE_WEB_SERVER_START_PHASE;
    }
}
