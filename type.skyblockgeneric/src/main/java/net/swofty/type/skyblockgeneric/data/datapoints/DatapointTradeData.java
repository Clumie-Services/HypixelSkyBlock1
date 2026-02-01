package net.swofty.type.skyblockgeneric.data.datapoints;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.swofty.commons.protocol.Serializer;
import net.swofty.type.skyblockgeneric.data.SkyBlockDatapoint;
import net.swofty.type.skyblockgeneric.trade.TradeLimitCalculator;
import org.json.JSONObject;

/**
 * Datapoint for tracking daily trade limits.
 * Resets automatically when a new day starts.
 */
public class DatapointTradeData extends SkyBlockDatapoint<DatapointTradeData.TradeData> {
    private static final Serializer<TradeData> serializer = new Serializer<>() {
        @Override
        public String serialize(TradeData value) {
            JSONObject json = new JSONObject();
            json.put("lastResetTimestamp", value.getLastResetTimestamp());
            json.put("tradedToday", value.getTradedToday());
            return json.toString();
        }

        @Override
        public TradeData deserialize(String json) {
            if (json == null || json.isEmpty()) {
                return new TradeData();
            }
            JSONObject jsonObject = new JSONObject(json);
            long lastReset = jsonObject.optLong("lastResetTimestamp", System.currentTimeMillis());
            long tradedToday = jsonObject.optLong("tradedToday", 0L);
            return new TradeData(lastReset, tradedToday);
        }

        @Override
        public TradeData clone(TradeData value) {
            return new TradeData(value.getLastResetTimestamp(), value.getTradedToday());
        }
    };

    public DatapointTradeData(String key, TradeData value) {
        super(key, value, serializer);
    }

    public DatapointTradeData(String key) {
        this(key, new TradeData());
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradeData {
        /**
         * Timestamp of the last daily reset.
         */
        private long lastResetTimestamp = System.currentTimeMillis();

        /**
         * Total coins traded today.
         */
        private long tradedToday = 0L;

        /**
         * Milliseconds in a day.
         */
        private static final long MS_PER_DAY = 86400000L;

        /**
         * Resets the daily counter if a new day has started.
         */
        public void resetIfNewDay() {
            long currentDay = System.currentTimeMillis() / MS_PER_DAY;
            long lastDay = lastResetTimestamp / MS_PER_DAY;

            if (currentDay > lastDay) {
                tradedToday = 0L;
                lastResetTimestamp = System.currentTimeMillis();
            }
        }

        /**
         * Gets the remaining trade limit for today based on the player's SkyBlock level.
         *
         * @param skyBlockLevel The player's SkyBlock level
         * @return Remaining coins that can be traded today
         */
        public long getRemainingLimit(int skyBlockLevel) {
            resetIfNewDay();
            long dailyLimit = TradeLimitCalculator.getDailyLimit(skyBlockLevel);
            return Math.max(0, dailyLimit - tradedToday);
        }

        /**
         * Checks if the player can trade the specified amount.
         *
         * @param skyBlockLevel The player's SkyBlock level
         * @param amount        The amount to check
         * @return true if the trade is within limits, false otherwise
         */
        public boolean canTrade(int skyBlockLevel, long amount) {
            resetIfNewDay();
            long dailyLimit = TradeLimitCalculator.getDailyLimit(skyBlockLevel);
            return (tradedToday + amount) <= dailyLimit;
        }

        /**
         * Adds to the traded amount for today.
         *
         * @param amount The amount to add
         */
        public void addTradedAmount(long amount) {
            resetIfNewDay();
            tradedToday += amount;
        }

        /**
         * Gets the daily limit for the specified level.
         *
         * @param skyBlockLevel The player's SkyBlock level
         * @return The daily limit in coins
         */
        public long getDailyLimit(int skyBlockLevel) {
            return TradeLimitCalculator.getDailyLimit(skyBlockLevel);
        }

        /**
         * Gets the percentage of daily limit used.
         *
         * @param skyBlockLevel The player's SkyBlock level
         * @return Percentage used (0.0 - 100.0)
         */
        public double getUsedPercentage(int skyBlockLevel) {
            resetIfNewDay();
            long dailyLimit = TradeLimitCalculator.getDailyLimit(skyBlockLevel);
            if (dailyLimit == 0) return 100.0;
            return (tradedToday * 100.0) / dailyLimit;
        }
    }
}
