package net.swofty.type.skyblockgeneric.trade;

import java.util.UUID;

/**
 * Represents a pending trade request from one player to another.
 *
 * @param senderUUID   UUID of the player who sent the trade request
 * @param targetUUID   UUID of the player who received the trade request
 * @param timestamp    Timestamp when the request was created
 * @param expirationMs Expiration time in milliseconds (default 60000ms = 60 seconds)
 */
public record TradeRequest(
        UUID senderUUID,
        UUID targetUUID,
        long timestamp,
        long expirationMs
) {
    /**
     * Default expiration time: 60 seconds.
     */
    public static final long DEFAULT_EXPIRATION_MS = 60000L;

    /**
     * Creates a new TradeRequest with default expiration.
     */
    public static TradeRequest create(UUID sender, UUID target) {
        return new TradeRequest(sender, target, System.currentTimeMillis(), DEFAULT_EXPIRATION_MS);
    }

    /**
     * Creates a new TradeRequest with custom expiration.
     */
    public static TradeRequest create(UUID sender, UUID target, long expirationMs) {
        return new TradeRequest(sender, target, System.currentTimeMillis(), expirationMs);
    }

    /**
     * Checks if this trade request has expired.
     *
     * @return true if the request has expired, false otherwise
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > timestamp + expirationMs;
    }

    /**
     * Gets the remaining time until expiration in milliseconds.
     *
     * @return remaining time in milliseconds, or 0 if already expired
     */
    public long getRemainingTimeMs() {
        long remaining = (timestamp + expirationMs) - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    /**
     * Gets the remaining time until expiration in seconds.
     *
     * @return remaining time in seconds, or 0 if already expired
     */
    public int getRemainingTimeSeconds() {
        return (int) (getRemainingTimeMs() / 1000);
    }
}
