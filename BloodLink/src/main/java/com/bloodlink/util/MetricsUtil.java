package com.bloodlink.util;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * MetricsUtil - Custom application metrics tracking
 * 
 * Tracks:
 * - Blood requests created, accepted, completed
 * - Donor registrations and donations
 * - Chat messages sent
 * - Notifications sent
 * - Authentication attempts
 */
@Component
@RequiredArgsConstructor
public class MetricsUtil {

    private final MeterRegistry meterRegistry;

    // Counters
    public void incrementBloodRequestsCreated() {
        Counter.builder("bloodlink.requests.created")
            .description("Total blood requests created")
            .register(meterRegistry)
            .increment();
    }

    public void incrementBloodRequestsAccepted() {
        Counter.builder("bloodlink.requests.accepted")
            .description("Total blood requests accepted by donors")
            .register(meterRegistry)
            .increment();
    }

    public void incrementBloodRequestsCompleted() {
        Counter.builder("bloodlink.requests.completed")
            .description("Total blood requests successfully completed")
            .register(meterRegistry)
            .increment();
    }

    public void incrementDonorRegistrations() {
        Counter.builder("bloodlink.donors.registered")
            .description("Total donor registrations")
            .register(meterRegistry)
            .increment();
    }

    public void incrementDonations() {
        Counter.builder("bloodlink.donations.recorded")
            .description("Total donations recorded")
            .register(meterRegistry)
            .increment();
    }

    public void incrementChatMessagesCreated() {
        Counter.builder("bloodlink.messages.created")
            .description("Total chat messages sent")
            .register(meterRegistry)
            .increment();
    }

    public void incrementNotificationsSent() {
        Counter.builder("bloodlink.notifications.sent")
            .description("Total notifications sent")
            .register(meterRegistry)
            .increment();
    }

    public void incrementAuthenticationAttempts(boolean success) {
        String outcome = success ? "success" : "failure";
        Counter.builder("bloodlink.auth.attempts")
            .tag("outcome", outcome)
            .description("Authentication attempts")
            .register(meterRegistry)
            .increment();
    }

    // Timers
    public void recordDonorSearchTime(long milliseconds) {
        Timer.builder("bloodlink.search.donor.time")
            .description("Time to search for donors")
            .register(meterRegistry)
            .record(java.time.Duration.ofMillis(milliseconds));
    }

    public void recordRequestMatchingTime(long milliseconds) {
        Timer.builder("bloodlink.matching.time")
            .description("Time to find suitable donors")
            .register(meterRegistry)
            .record(java.time.Duration.ofMillis(milliseconds));
    }

    public void recordDatabaseQueryTime(String query, long milliseconds) {
        Timer.builder("bloodlink.db.query.time")
            .tag("query", query)
            .description("Database query execution time")
            .register(meterRegistry)
            .record(java.time.Duration.ofMillis(milliseconds));
    }

    public void recordWebSocketMessageTime(long milliseconds) {
        Timer.builder("bloodlink.websocket.message.time")
            .description("WebSocket message processing time")
            .register(meterRegistry)
            .record(java.time.Duration.ofMillis(milliseconds));
    }

    // Gauge metrics (for snapshot values)
    public void registerActiveUsersGauge(int count) {
        io.micrometer.core.instrument.Gauge.builder("bloodlink.users.active", () -> count)
            .description("Current active users")
            .register(meterRegistry);
    }

    public void registerOnlineDonorsGauge(int count) {
        io.micrometer.core.instrument.Gauge.builder("bloodlink.donors.online", () -> count)
            .description("Current online donors")
            .register(meterRegistry);
    }

    public void registerPendingRequestsGauge(int count) {
        io.micrometer.core.instrument.Gauge.builder("bloodlink.requests.pending", () -> count)
            .description("Current pending blood requests")
            .register(meterRegistry);
    }
}
