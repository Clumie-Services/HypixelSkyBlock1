package net.swofty.type.skyblockgeneric.trade.gui;

import net.minestom.server.item.ItemStack;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.skyblockgeneric.trade.TradeLimitCalculator;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for creating coin skull items with appropriate textures
 * based on the coin amount being traded.
 */
public final class CoinTextureHelper {

    /**
     * Texture for small amounts (1 - 99,999 coins).
     * Single gold coin texture.
     */
    public static final String TEXTURE_SMALL = "16b90f4fa3ec106bfef21f3b75f541a18e4757674f7d58250fa7e74952f087dc";

    /**
     * Texture for medium amounts (100,000 - 9,999,999 coins).
     * Small pile of gold coins texture.
     */
    public static final String TEXTURE_MEDIUM = "c9b77999fed3a2758bfeaf0793e52283817bea64044bf43ef29433f954bb52f6";

    /**
     * Texture for large amounts (10,000,000 - 99,999,999 coins).
     * Large pile of gold coins texture.
     */
    public static final String TEXTURE_LARGE = "740d6e362bc7eee4f911dbd0446307e7458d1050d09aee538ebcb0273cf75742";

    /**
     * Texture for massive amounts (100,000,000+ coins).
     * Overflowing treasure chest texture.
     */
    public static final String TEXTURE_MASSIVE = "c43f12c8369f9c3888a45aaf6d7761578402b4241958f7d4ae4eceb56a867d2a";

    private CoinTextureHelper() {
        // Utility class
    }

    /**
     * Gets the appropriate texture ID for a coin amount.
     *
     * @param amount The coin amount
     * @return The texture ID to use
     */
    public static String getTextureForAmount(long amount) {
        if (amount >= 100_000_000L) {
            return TEXTURE_MASSIVE;
        }
        if (amount >= 10_000_000L) {
            return TEXTURE_LARGE;
        }
        if (amount >= 100_000L) {
            return TEXTURE_MEDIUM;
        }
        return TEXTURE_SMALL;
    }

    /**
     * Creates a coin skull item for display in the trade GUI.
     *
     * @param amount The coin amount
     * @param isOwn  Whether this is the player's own coins (affects lore)
     * @return ItemStack.Builder for the coin skull
     */
    public static ItemStack.Builder createCoinItem(long amount, boolean isOwn) {
        if (amount <= 0) {
            return createEmptyCoinItem(isOwn);
        }

        String texture = getTextureForAmount(amount);
        String formattedAmount = TradeLimitCalculator.formatCoinsWithCommas(amount);
        int displayCount = calculateDisplayCount(amount);

        List<String> lore = new ArrayList<>();
        lore.add("");
        if (isOwn) {
            lore.add("§7You are offering §6" + formattedAmount + " coins§7.");
            lore.add("");
            lore.add("§eClick to modify!");
        } else {
            lore.add("§7They are offering §6" + formattedAmount + " coins§7.");
        }

        return ItemStackCreator.getStackHead(
                "§6" + formattedAmount + " Coins",
                texture,
                displayCount,
                lore
        );
    }

    /**
     * Creates an empty coin placeholder item.
     *
     * @param isOwn Whether this is the player's own coin slot
     * @return ItemStack.Builder for the empty coin item
     */
    public static ItemStack.Builder createEmptyCoinItem(boolean isOwn) {
        List<String> lore = new ArrayList<>();
        lore.add("");
        if (isOwn) {
            lore.add("§7No coins offered.");
            lore.add("");
            lore.add("§eClick to add coins!");
        } else {
            lore.add("§7No coins offered.");
        }

        return ItemStackCreator.getStackHead(
                "§6Coins",
                TEXTURE_SMALL,
                1,
                lore
        );
    }

    /**
     * Calculates the display stack count for a coin amount.
     * Uses a logarithmic scale to represent large amounts.
     *
     * @param amount The coin amount
     * @return Stack count (1-64)
     */
    public static int calculateDisplayCount(long amount) {
        if (amount <= 0) return 1;
        if (amount < 1000) return 1;
        if (amount < 10_000) return 2;
        if (amount < 100_000) return 4;
        if (amount < 1_000_000) return 8;
        if (amount < 10_000_000) return 16;
        if (amount < 100_000_000) return 32;
        return 64;
    }

    /**
     * Creates a coin item showing the trade limit warning.
     *
     * @param amount        The attempted coin amount
     * @param remainingLimit The remaining daily limit
     * @return ItemStack.Builder for the warning coin item
     */
    public static ItemStack.Builder createLimitWarningItem(long amount, long remainingLimit) {
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("§cExceeds daily limit!");
        lore.add("");
        lore.add("§7Attempted: §6" + TradeLimitCalculator.formatCoinsWithCommas(amount));
        lore.add("§7Remaining limit: §6" + TradeLimitCalculator.formatCoinsWithCommas(remainingLimit));
        lore.add("");
        lore.add("§eClick to modify!");

        return ItemStackCreator.getStackHead(
                "§c" + TradeLimitCalculator.formatCoinsWithCommas(amount) + " Coins",
                TEXTURE_SMALL,
                1,
                lore
        );
    }
}
