package net.swofty.type.skyblockgeneric.trade.gui;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.generic.gui.HypixelSignGUI;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.v2.*;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointTradeData;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.trade.TradeLimitCalculator;
import net.swofty.type.skyblockgeneric.trade.TradeSessionManager;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.*;

/**
 * Trading GUI view that uses SharedContext for synchronized state between two players.
 */
public final class TradeView implements View<TradeState> {

    private static final ItemStack.Builder SEPARATOR = ItemStackCreator.getStack(
            " ", Material.BLACK_STAINED_GLASS_PANE, 1);

    private static final ItemStack.Builder FILLER = ItemStackCreator.getStack(
            " ", Material.GRAY_STAINED_GLASS_PANE, 1);

    @Override
    public ViewConfiguration<TradeState> configuration() {
        return ViewConfiguration.withString((state, ctx) -> {
            SkyBlockPlayer viewer = (SkyBlockPlayer) ctx.player();
            UUID otherUUID = state.getOtherPlayer(viewer.getUuid());
            SkyBlockPlayer other = getPlayer(otherUUID);
            String otherName = other != null ? other.getUsername() : "Unknown";

            // Dynamic title showing both perspectives
            return "§8You                    §8" + otherName;
        }, InventoryType.CHEST_5_ROW);
    }

    @Override
    public void layout(ViewLayout<TradeState> layout, TradeState state, ViewContext ctx) {
        SkyBlockPlayer viewer = (SkyBlockPlayer) ctx.player();
        UUID viewerUUID = viewer.getUuid();
        boolean isPlayer1 = state.isPlayer1(viewerUUID);

        SharedContext<TradeState> shared = ctx.session(TradeState.class).sharedContext();

        // Fill background
        layout.filler(FILLER);

        // Place separator column
        for (int slot : TradeState.SEPARATOR_SLOTS) {
            layout.slot(slot, (s, c) -> SEPARATOR);
        }

        // Setup item slots
        setupItemSlots(layout, state, isPlayer1, shared, viewer);

        // Setup control buttons (bottom row)
        setupControlButtons(layout, state, isPlayer1, viewer, shared);
    }

    private void setupItemSlots(ViewLayout<TradeState> layout, TradeState state,
                                boolean isPlayer1, SharedContext<TradeState> shared,
                                SkyBlockPlayer viewer) {
        // My editable slots (left side for me)
        int[] mySlots = isPlayer1 ? TradeState.PLAYER1_SLOTS : TradeState.PLAYER2_SLOTS;
        int[] theirSlots = isPlayer1 ? TradeState.PLAYER2_SLOTS : TradeState.PLAYER1_SLOTS;

        // My items - editable
        for (int slotIndex : mySlots) {
            final int slot = slotIndex;
            layout.editable(slot, (s, c) -> {
                Map<Integer, ItemStack> myItems = isPlayer1 ? s.player1Items() : s.player2Items();
                ItemStack item = myItems.getOrDefault(slot, ItemStack.AIR);
                return item.builder();
            }, (changedSlot, oldItem, newItem, currentState) -> {
                // Item changed - update state and reset confirmations
                handleItemChange(changedSlot, newItem, isPlayer1, shared);
            });
        }

        // Their items - view only (display as non-editable)
        for (int slot : theirSlots) {
            layout.slot(slot, (s, c) -> {
                Map<Integer, ItemStack> theirItems = isPlayer1 ? s.player2Items() : s.player1Items();
                ItemStack item = theirItems.getOrDefault(slot, ItemStack.AIR);
                if (item.isAir()) {
                    return ItemStack.AIR.builder();
                }
                // Show their item but make it non-clickable visually
                return item.builder();
            });
        }
    }

    private void handleItemChange(int slot, ItemStack newItem, boolean isPlayer1,
                                  SharedContext<TradeState> shared) {
        TradeState currentState = shared.state();
        Map<Integer, ItemStack> items;
        if (isPlayer1) {
            items = new HashMap<>(currentState.player1Items());
        } else {
            items = new HashMap<>(currentState.player2Items());
        }

        if (newItem.isAir()) {
            items.remove(slot);
        } else {
            items.put(slot, newItem);
        }

        TradeState newState;
        if (isPlayer1) {
            newState = currentState.withPlayer1Items(items);
        } else {
            newState = currentState.withPlayer2Items(items);
        }
        shared.setState(newState);
    }

    private void setupControlButtons(ViewLayout<TradeState> layout, TradeState state,
                                     boolean isPlayer1, SkyBlockPlayer viewer,
                                     SharedContext<TradeState> shared) {
        // Slot 36: My coins button
        layout.slot(36, (s, c) -> {
            long myCoins = isPlayer1 ? s.player1Coins() : s.player2Coins();
            return CoinTextureHelper.createCoinItem(myCoins, true);
        }, (click, context) -> handleCoinsClick(isPlayer1, shared, viewer));

        // Slot 37-38: Filler
        layout.slot(37, (s, c) -> FILLER);
        layout.slot(38, (s, c) -> FILLER);

        // Slot 39: Deal status / Confirm button
        layout.slot(39, (s, c) -> createDealButton(s, isPlayer1, viewer), (click, context) -> {
            handleConfirmClick(isPlayer1, shared, viewer);
        });

        // Slot 41: Other player's deal status
        layout.slot(41, (s, c) -> createOtherDealStatus(s, isPlayer1));

        // Slot 42-43: Filler
        layout.slot(42, (s, c) -> FILLER);
        layout.slot(43, (s, c) -> FILLER);

        // Slot 44: Their coins (view only)
        layout.slot(44, (s, c) -> {
            long theirCoins = isPlayer1 ? s.player2Coins() : s.player1Coins();
            return CoinTextureHelper.createCoinItem(theirCoins, false);
        });
    }

    private ItemStack.Builder createDealButton(TradeState state, boolean isPlayer1, SkyBlockPlayer viewer) {
        boolean myConfirmed = isPlayer1 ? state.player1Confirmed() : state.player2Confirmed();

        // Check if trade is valid
        boolean isValid = validateTrade(state, viewer);

        if (!isValid) {
            return ItemStackCreator.getStack(
                    "§cInvalid Trade!",
                    Material.RED_TERRACOTTA,
                    1,
                    "§7This trade cannot be completed.",
                    "",
                    "§cExceeds daily trade limit!"
            );
        }

        if (state.bothConfirmed()) {
            return ItemStackCreator.getStack(
                    "§aCompleting Trade...",
                    Material.LIME_TERRACOTTA,
                    1,
                    "§7Both players have confirmed!",
                    "",
                    "§aFinalizing the trade..."
            );
        }

        if (myConfirmed) {
            return ItemStackCreator.getStack(
                    "§eWaiting for other player...",
                    Material.YELLOW_TERRACOTTA,
                    1,
                    "§7You have confirmed the trade.",
                    "",
                    "§7Waiting for the other player",
                    "§7to confirm.",
                    "",
                    "§eClick to cancel confirmation!"
            );
        }

        int myItemCount = state.countItems(viewer.getUuid());
        long myCoins = isPlayer1 ? state.player1Coins() : state.player2Coins();

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("§7Items to trade: §e" + myItemCount);
        lore.add("§7Coins to trade: §6" + TradeLimitCalculator.formatCoinsWithCommas(myCoins));
        lore.add("");
        lore.add("§eClick to confirm!");

        return ItemStackCreator.getStack(
                "§aConfirm Trade",
                Material.GREEN_TERRACOTTA,
                1,
                lore
        );
    }

    private ItemStack.Builder createOtherDealStatus(TradeState state, boolean isPlayer1) {
        boolean theirConfirmed = isPlayer1 ? state.player2Confirmed() : state.player1Confirmed();

        if (theirConfirmed) {
            return ItemStackCreator.getStack(
                    "§aThey confirmed!",
                    Material.LIME_STAINED_GLASS_PANE,
                    1,
                    "§7The other player has",
                    "§7confirmed the trade."
            );
        }

        return ItemStackCreator.getStack(
                "§7Waiting...",
                Material.GRAY_STAINED_GLASS_PANE,
                1,
                "§7The other player has not",
                "§7confirmed yet."
        );
    }

    private boolean validateTrade(TradeState state, SkyBlockPlayer viewer) {
        // Check single trade limit
        long p1Coins = state.player1Coins();
        long p2Coins = state.player2Coins();

        if (p1Coins > TradeLimitCalculator.MAX_COINS_PER_TRADE ||
                p2Coins > TradeLimitCalculator.MAX_COINS_PER_TRADE) {
            return false;
        }

        // Check daily limits for both players
        SkyBlockPlayer player1 = getPlayer(state.player1UUID());
        SkyBlockPlayer player2 = getPlayer(state.player2UUID());

        if (player1 == null || player2 == null) {
            return false;
        }

        // Player 1 receives p2Coins
        DatapointTradeData.TradeData p1TradeData = getTradeData(player1);
        int p1Level = player1.getSkyBlockExperience().getLevel().asInt();
        if (!p1TradeData.canTrade(p1Level, p2Coins)) {
            return false;
        }

        // Player 2 receives p1Coins
        DatapointTradeData.TradeData p2TradeData = getTradeData(player2);
        int p2Level = player2.getSkyBlockExperience().getLevel().asInt();
        if (!p2TradeData.canTrade(p2Level, p1Coins)) {
            return false;
        }

        return true;
    }

    private void handleCoinsClick(boolean isPlayer1, SharedContext<TradeState> shared, SkyBlockPlayer viewer) {
        // Open sign GUI for coin input
        HypixelSignGUI signGUI = new HypixelSignGUI(viewer);
        signGUI.open(new String[]{"Enter coin amount", ""}).thenAccept(input -> {
            MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
                try {
                    // Parse the input
                    String cleanInput = input.trim().replace(",", "").replace(" ", "");

                    // Handle multipliers (k, m, b)
                    long amount = parseCoinsInput(cleanInput);

                    if (amount < 0) {
                        viewer.sendMessage("§cInvalid amount!");
                        return;
                    }

                    // Check if player has enough coins
                    if (amount > viewer.getCoins()) {
                        viewer.sendMessage("§cYou don't have enough coins!");
                        amount = (long) viewer.getCoins();
                    }

                    // Check single trade limit
                    if (amount > TradeLimitCalculator.MAX_COINS_PER_TRADE) {
                        viewer.sendMessage("§cMaximum coins per trade is " +
                                TradeLimitCalculator.formatCoinsWithCommas(TradeLimitCalculator.MAX_COINS_PER_TRADE) + "!");
                        amount = TradeLimitCalculator.MAX_COINS_PER_TRADE;
                    }

                    TradeState currentState = shared.state();
                    TradeState newState;
                    if (isPlayer1) {
                        newState = currentState.withPlayer1Coins(amount);
                    } else {
                        newState = currentState.withPlayer2Coins(amount);
                    }
                    shared.setState(newState);

                    viewer.sendMessage("§aYou are now offering §6" +
                            TradeLimitCalculator.formatCoinsWithCommas(amount) + " coins§a.");

                } catch (NumberFormatException e) {
                    viewer.sendMessage("§cInvalid amount! Please enter a number.");
                }
            });
        });
    }

    private long parseCoinsInput(String input) {
        if (input.isEmpty()) return 0;

        input = input.toLowerCase();
        double multiplier = 1;

        if (input.endsWith("k")) {
            multiplier = 1_000;
            input = input.substring(0, input.length() - 1);
        } else if (input.endsWith("m")) {
            multiplier = 1_000_000;
            input = input.substring(0, input.length() - 1);
        } else if (input.endsWith("b")) {
            multiplier = 1_000_000_000;
            input = input.substring(0, input.length() - 1);
        }

        double value = Double.parseDouble(input);
        return (long) (value * multiplier);
    }

    private void handleConfirmClick(boolean isPlayer1, SharedContext<TradeState> shared, SkyBlockPlayer viewer) {
        TradeState currentState = shared.state();

        // Check if trade is valid
        if (!validateTrade(currentState, viewer)) {
            viewer.sendMessage("§cThis trade cannot be completed!");
            return;
        }

        boolean currentlyConfirmed = isPlayer1 ? currentState.player1Confirmed() : currentState.player2Confirmed();

        if (currentlyConfirmed) {
            // Cancel confirmation
            TradeState newState;
            if (isPlayer1) {
                newState = currentState.withPlayer1Confirmed(false);
            } else {
                newState = currentState.withPlayer2Confirmed(false);
            }
            shared.setState(newState);
            viewer.sendMessage("§eYou cancelled your confirmation.");
        } else {
            // Confirm
            TradeState newState;
            if (isPlayer1) {
                newState = currentState.withPlayer1Confirmed(true);
            } else {
                newState = currentState.withPlayer2Confirmed(true);
            }

            // Check if both confirmed - if so, complete the trade
            if (newState.bothConfirmed()) {
                shared.setState(newState);
                // Schedule trade completion
                MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
                    completeTrade(shared);
                });
            } else {
                shared.setState(newState);
            }
            viewer.sendMessage("§aYou confirmed the trade!");
        }
    }

    private void completeTrade(SharedContext<TradeState> shared) {
        TradeState state = shared.state();

        SkyBlockPlayer player1 = getPlayer(state.player1UUID());
        SkyBlockPlayer player2 = getPlayer(state.player2UUID());

        if (player1 == null || player2 == null) {
            cancelTrade(shared, "§cThe trade was cancelled because a player disconnected.");
            return;
        }

        // Final validation
        if (!validateTrade(state, player1)) {
            cancelTrade(shared, "§cThe trade was cancelled because it became invalid.");
            return;
        }

        // Exchange items
        // Give player 1's items to player 2
        for (ItemStack item : state.player1Items().values()) {
            if (!item.isAir()) {
                player2.addAndUpdateItem(new SkyBlockItem(item));
            }
        }

        // Give player 2's items to player 1
        for (ItemStack item : state.player2Items().values()) {
            if (!item.isAir()) {
                player1.addAndUpdateItem(new SkyBlockItem(item));
            }
        }

        // Exchange coins
        if (state.player1Coins() > 0) {
            player1.removeCoins(state.player1Coins());
            player2.addCoins(state.player1Coins());

            // Update trade data
            DatapointTradeData.TradeData p2TradeData = getTradeData(player2);
            p2TradeData.addTradedAmount(state.player1Coins());
        }

        if (state.player2Coins() > 0) {
            player2.removeCoins(state.player2Coins());
            player1.addCoins(state.player2Coins());

            // Update trade data
            DatapointTradeData.TradeData p1TradeData = getTradeData(player1);
            p1TradeData.addTradedAmount(state.player2Coins());
        }

        // Update state to completed
        shared.setState(state.withStatus(TradeStatus.COMPLETED));

        // Send success messages
        player1.sendMessage("§aTrade completed successfully!");
        player2.sendMessage("§aTrade completed successfully!");

        // Close GUIs
        player1.closeInventory();
        player2.closeInventory();

        // Clean up session
        TradeSessionManager.getInstance().endSessionForPlayer(state.player1UUID());
    }

    private void cancelTrade(SharedContext<TradeState> shared, String message) {
        TradeState state = shared.state();

        SkyBlockPlayer player1 = getPlayer(state.player1UUID());
        SkyBlockPlayer player2 = getPlayer(state.player2UUID());

        // Return items to original owners
        if (player1 != null) {
            for (ItemStack item : state.player1Items().values()) {
                if (!item.isAir()) {
                    player1.addAndUpdateItem(new SkyBlockItem(item));
                }
            }
            player1.sendMessage(message);
            player1.closeInventory();
        }

        if (player2 != null) {
            for (ItemStack item : state.player2Items().values()) {
                if (!item.isAir()) {
                    player2.addAndUpdateItem(new SkyBlockItem(item));
                }
            }
            player2.sendMessage(message);
            player2.closeInventory();
        }

        // Update state
        shared.setState(state.withStatus(TradeStatus.INVALID));

        // Clean up session
        TradeSessionManager.getInstance().endSessionForPlayer(state.player1UUID());
    }

    @Override
    public void onClose(TradeState state, ViewContext ctx, ViewSession.CloseReason reason) {
        if (reason == ViewSession.CloseReason.PLAYER_EXITED) {
            // Player closed GUI - cancel trade
            SharedContext<TradeState> shared = ctx.session(TradeState.class).sharedContext();
            if (shared != null && state.status() != TradeStatus.COMPLETED) {
                SkyBlockPlayer viewer = (SkyBlockPlayer) ctx.player();
                cancelTrade(shared, "§cThe trade was cancelled because " + viewer.getUsername() + " closed the menu.");
            }
        }
    }

    @Override
    public boolean onBottomClick(ClickContext<TradeState> click, ViewContext ctx) {
        // Allow bottom inventory clicks for adding items
        return true;
    }

    private SkyBlockPlayer getPlayer(UUID uuid) {
        for (HypixelPlayer player : MinecraftServer.getConnectionManager().getOnlinePlayers()
                .stream()
                .filter(p -> p instanceof SkyBlockPlayer)
                .map(p -> (SkyBlockPlayer) p)
                .toList()) {
            if (player.getUuid().equals(uuid)) {
                return (SkyBlockPlayer) player;
            }
        }
        return null;
    }

    private DatapointTradeData.TradeData getTradeData(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.TRADE_DATA, DatapointTradeData.class)
                .getValue();
    }
}
