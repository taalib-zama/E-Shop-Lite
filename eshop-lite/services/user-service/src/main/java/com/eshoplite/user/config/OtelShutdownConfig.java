package com.eshoplite.user.config;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OtelShutdownConfig implements DisposableBean {

    @Override
    public void destroy() {
        if (GlobalOpenTelemetry.get() instanceof OpenTelemetrySdk sdk) {
            sdk.getSdkTracerProvider().shutdown();
        }
    }
}
