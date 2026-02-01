package net.swofty.type.skyblockgeneric.trade;

import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Calculates daily trade limits based on a player's SkyBlock level.
 * Higher levels unlock higher trading limits.
 */
public final class TradeLimitCalculator {

    /**
     * Maximum coins that can be traded in a single trade.
     * 1,024,000,000 coins (approximately 1 billion).
     */
    public static final long MAX_COINS_PER_TRADE = 1_024_000_000L;

    /**
     * Default daily limit for players below level 50.
     * 50,000,000 coins (50 million).
     */
    public static final long LIMIT_BELOW_50 = 50_000_000L;

    /**
     * Daily limit for players at level 50-99.
     * 1,000,000,000 coins (1 billion).
     */
    public static final long LIMIT_50_TO_99 = 1_000_000_000L;

    /**
     * Daily limit for players at level 100 and above.
     * 10,000,000,000 coins (10 billion).
     */
    public static final long LIMIT_100_PLUS = 10_000_000_000L;

    private TradeLimitCalculator() {
        // Utility class
    }

    /**
     * Gets the daily trading limit based on the player's SkyBlock level.
     *
     * @param skyBlockLevel The player's SkyBlock level
     * @return The daily trading limit in coins
     */
    public static long getDailyLimit(int skyBlockLevel) {
        if (skyBlockLevel >= 100) {
            return LIMIT_100_PLUS;
        }
        if (skyBlockLevel >= 50) {
            return LIMIT_50_TO_99;
        }
        return LIMIT_BELOW_50;
    }

    /**
     * Gets the daily trading limit for a player.
     *
     * @param player The SkyBlock player
     * @return The daily trading limit in coins
     */
    public static long getDailyLimit(SkyBlockPlayer player) {
        int level = player.getSkyBlockExperience().getLevel().asInt();
        return getDailyLimit(level);
    }

    /**
     * Formats a coin amount with appropriate suffixes (k, M, B).
     *
     * @param amount The amount to format
     * @return Formatted string representation
     */
    public static String formatCoins(long amount) {
        if (amount >= 1_000_000_000L) {
            return String.format("%.1fB", amount / 1_000_000_000.0);
        }
        if (amount >= 1_000_000L) {
            return String.format("%.1fM", amount / 1_000_000.0);
        }
        if (amount >= 1_000L) {
            return String.format("%.1fk", amount / 1_000.0);
        }
        return String.valueOf(amount);
    }

    /**
     * Formats a coin amount with commas for readability.
     *
     * @param amount The amount to format
     * @return Formatted string with commas
     */
    public static String formatCoinsWithCommas(long amount) {
        return String.format("%,d", amount);
    }

    /**
     * Checks if a trade amount is within the single trade limit.
     *
     * @param amount The amount to check
     * @return true if within limit, false otherwise
     */
    public static boolean isWithinSingleTradeLimit(long amount) {
        return amount <= MAX_COINS_PER_TRADE;
    }

    /**
     * Checks if a trade amount is within the player's remaining daily limit.
     *
     * @param player The player to check
     * @param amount The amount to check
     * @param alreadyTradedToday Amount already traded today
     * @return true if within limit, false otherwise
     */
    public static boolean isWithinDailyLimit(SkyBlockPlayer player, long amount, long alreadyTradedToday) {
        long dailyLimit = getDailyLimit(player);
        return (alreadyTradedToday + amount) <= dailyLimit;
    }

    /**
     * Gets the remaining daily limit for a player.
     *
     * @param player The player
     * @param alreadyTradedToday Amount already traded today
     * @return Remaining amount that can be traded today
     */
    public static long getRemainingDailyLimit(SkyBlockPlayer player, long alreadyTradedToday) {
        long dailyLimit = getDailyLimit(player);
        return Math.max(0, dailyLimit - alreadyTradedToday);
    }
}
