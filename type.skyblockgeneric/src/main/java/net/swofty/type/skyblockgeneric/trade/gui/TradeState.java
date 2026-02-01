package net.swofty.type.skyblockgeneric.trade.gui;

import net.minestom.server.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Immutable state record for a trading session between two players.
 * This state is shared via SharedContext between both players' GUIs.
 *
 * @param player1UUID      UUID of the first player (initiator)
 * @param player2UUID      UUID of the second player (accepter)
 * @param player1Items     Items offered by player 1 (slot -> item)
 * @param player2Items     Items offered by player 2 (slot -> item)
 * @param player1Coins     Coins offered by player 1
 * @param player2Coins     Coins offered by player 2
 * @param player1Confirmed Whether player 1 has confirmed the trade
 * @param player2Confirmed Whether player 2 has confirmed the trade
 * @param status           Current status of the trade
 */
public record TradeState(
        UUID player1UUID,
        UUID player2UUID,
        Map<Integer, ItemStack> player1Items,
        Map<Integer, ItemStack> player2Items,
        long player1Coins,
        long player2Coins,
        boolean player1Confirmed,
        boolean player2Confirmed,
        TradeStatus status
) {
    /**
     * GUI slots for player 1's items (left side).
     * 4 columns x 4 rows = 16 slots.
     */
    public static final int[] PLAYER1_SLOTS = {0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21, 27, 28, 29, 30};

    /**
     * GUI slots for player 2's items (right side, view-only for player 1).
     * 4 columns x 4 rows = 16 slots.
     */
    public static final int[] PLAYER2_SLOTS = {5, 6, 7, 8, 14, 15, 16, 17, 23, 24, 25, 26, 32, 33, 34, 35};

    /**
     * Separator column slots (middle divider).
     */
    public static final int[] SEPARATOR_SLOTS = {4, 13, 22, 31, 40};

    /**
     * Creates a new TradeState with default empty values.
     */
    public static TradeState create(UUID player1, UUID player2) {
        return new TradeState(
                player1,
                player2,
                new HashMap<>(),
                new HashMap<>(),
                0L,
                0L,
                false,
                false,
                TradeStatus.TRADING
        );
    }

    /**
     * Returns a new TradeState with updated player 1 items.
     */
    public TradeState withPlayer1Items(Map<Integer, ItemStack> items) {
        return new TradeState(player1UUID, player2UUID, items, player2Items,
                player1Coins, player2Coins, false, false, TradeStatus.TRADING);
    }

    /**
     * Returns a new TradeState with updated player 2 items.
     */
    public TradeState withPlayer2Items(Map<Integer, ItemStack> items) {
        return new TradeState(player1UUID, player2UUID, player1Items, items,
                player1Coins, player2Coins, false, false, TradeStatus.TRADING);
    }

    /**
     * Returns a new TradeState with updated player 1 coins.
     */
    public TradeState withPlayer1Coins(long coins) {
        return new TradeState(player1UUID, player2UUID, player1Items, player2Items,
                coins, player2Coins, false, false, TradeStatus.TRADING);
    }

    /**
     * Returns a new TradeState with updated player 2 coins.
     */
    public TradeState withPlayer2Coins(long coins) {
        return new TradeState(player1UUID, player2UUID, player1Items, player2Items,
                player1Coins, coins, false, false, TradeStatus.TRADING);
    }

    /**
     * Returns a new TradeState with player 1's confirmation toggled.
     */
    public TradeState withPlayer1Confirmed(boolean confirmed) {
        boolean bothConfirmed = confirmed && player2Confirmed;
        TradeStatus newStatus = bothConfirmed ? TradeStatus.PENDING_CONFIRM : TradeStatus.TRADING;
        return new TradeState(player1UUID, player2UUID, player1Items, player2Items,
                player1Coins, player2Coins, confirmed, player2Confirmed, newStatus);
    }

    /**
     * Returns a new TradeState with player 2's confirmation toggled.
     */
    public TradeState withPlayer2Confirmed(boolean confirmed) {
        boolean bothConfirmed = player1Confirmed && confirmed;
        TradeStatus newStatus = bothConfirmed ? TradeStatus.PENDING_CONFIRM : TradeStatus.TRADING;
        return new TradeState(player1UUID, player2UUID, player1Items, player2Items,
                player1Coins, player2Coins, player1Confirmed, confirmed, newStatus);
    }

    /**
     * Returns a new TradeState with the specified status.
     */
    public TradeState withStatus(TradeStatus status) {
        return new TradeState(player1UUID, player2UUID, player1Items, player2Items,
                player1Coins, player2Coins, player1Confirmed, player2Confirmed, status);
    }

    /**
     * Returns a new TradeState with both confirmations reset.
     */
    public TradeState resetConfirmations() {
        return new TradeState(player1UUID, player2UUID, player1Items, player2Items,
                player1Coins, player2Coins, false, false, TradeStatus.TRADING);
    }

    /**
     * Checks if both players have confirmed the trade.
     */
    public boolean bothConfirmed() {
        return player1Confirmed && player2Confirmed;
    }

    /**
     * Gets the items map for the specified player.
     */
    public Map<Integer, ItemStack> getItemsForPlayer(UUID playerUUID) {
        if (playerUUID.equals(player1UUID)) {
            return player1Items;
        } else if (playerUUID.equals(player2UUID)) {
            return player2Items;
        }
        return new HashMap<>();
    }

    /**
     * Gets the coins amount for the specified player.
     */
    public long getCoinsForPlayer(UUID playerUUID) {
        if (playerUUID.equals(player1UUID)) {
            return player1Coins;
        } else if (playerUUID.equals(player2UUID)) {
            return player2Coins;
        }
        return 0L;
    }

    /**
     * Gets the confirmation status for the specified player.
     */
    public boolean isPlayerConfirmed(UUID playerUUID) {
        if (playerUUID.equals(player1UUID)) {
            return player1Confirmed;
        } else if (playerUUID.equals(player2UUID)) {
            return player2Confirmed;
        }
        return false;
    }

    /**
     * Checks if the specified player is player 1.
     */
    public boolean isPlayer1(UUID playerUUID) {
        return player1UUID.equals(playerUUID);
    }

    /**
     * Gets the UUID of the other player.
     */
    public UUID getOtherPlayer(UUID playerUUID) {
        if (playerUUID.equals(player1UUID)) {
            return player2UUID;
        }
        return player1UUID;
    }

    /**
     * Counts non-air items in a player's items map.
     */
    public int countItems(UUID playerUUID) {
        Map<Integer, ItemStack> items = getItemsForPlayer(playerUUID);
        return (int) items.values().stream().filter(item -> !item.isAir()).count();
    }

    /**
     * Checks if the trade has any items or coins being offered.
     */
    public boolean hasAnyOffers() {
        boolean hasItems = !player1Items.isEmpty() || !player2Items.isEmpty();
        boolean hasCoins = player1Coins > 0 || player2Coins > 0;
        return hasItems || hasCoins;
    }
}
