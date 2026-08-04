package com.northstar.crm.health;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class CrmReadinessIndicator implements HealthIndicator {
    private final AtomicBoolean ready = new AtomicBoolean(true);

    public void setReady(boolean value) { ready.set(value); }

    @Override
    public Health health() {
        if (!ready.get()) {
            return Health.outOfService()
                    .withDetail("crm", "not-ready")
                    .withDetail("reason", "dependency-unavailable")
                    .build();
        }
        return Health.up().withDetail("crm", "ready").build();
    }
}