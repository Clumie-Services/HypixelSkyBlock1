package net.swofty.type.skyblockgeneric.trade.gui;

/**
 * Represents the current status of a trade session.
 */
public enum TradeStatus {
    /**
     * Both players are actively trading and can modify items/coins.
     */
    TRADING,

    /**
     * Both players have confirmed the trade and are waiting for final validation.
     */
    PENDING_CONFIRM,

    /**
     * The trade is invalid (e.g., exceeds daily limit, player disconnected).
     */
    INVALID,

    /**
     * The trade has been completed successfully.
     */
    COMPLETED
}
