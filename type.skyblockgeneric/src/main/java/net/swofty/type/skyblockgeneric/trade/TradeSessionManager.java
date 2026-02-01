package net.swofty.type.skyblockgeneric.trade;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.generic.gui.v2.SharedContext;
import net.swofty.type.generic.gui.v2.ViewNavigator;
import net.swofty.type.skyblockgeneric.trade.gui.TradeState;
import net.swofty.type.skyblockgeneric.trade.gui.TradeView;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages trade requests and active trading sessions.
 * This is a singleton that handles all trading state globally.
 */
public final class TradeSessionManager {
    private static final TradeSessionManager INSTANCE = new TradeSessionManager();

    /**
     * Pending trade requests: target UUID -> request
     */
    private final Map<UUID, TradeRequest> pendingRequests = new ConcurrentHashMap<>();

    /**
     * Active trading sessions: context ID -> shared context
     */
    private final Map<String, SharedContext<TradeState>> activeSessions = new ConcurrentHashMap<>();

    /**
     * Player UUID to context ID mapping for quick lookup
     */
    private final Map<UUID, String> playerToSession = new ConcurrentHashMap<>();

    private TradeSessionManager() {
        // Start cleanup task
        startCleanupTask();
    }

    /**
     * Gets the singleton instance.
     */
    public static TradeSessionManager getInstance() {
        return INSTANCE;
    }

    /**
     * Starts the periodic cleanup task for expired requests.
     */
    private void startCleanupTask() {
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            cleanupExpiredRequests();
            return TaskSchedule.seconds(5);
        });
    }

    /**
     * Removes all expired trade requests.
     */
    public void cleanupExpiredRequests() {
        pendingRequests.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    /**
     * Sends a trade request from one player to another.
     *
     * @param sender The player sending the request
     * @param target The target player's UUID
     * @return true if the request was sent, false if there's already a pending request
     */
    public boolean sendRequest(SkyBlockPlayer sender, UUID target) {
        // Check if sender already has an outgoing request to this target
        TradeRequest existing = pendingRequests.get(target);
        if (existing != null && existing.senderUUID().equals(sender.getUuid()) && !existing.isExpired()) {
            return false; // Already has a pending request
        }

        // Check if either player is already in a trade
        if (isInTrade(sender.getUuid()) || isInTrade(target)) {
            return false;
        }

        // Create and store the request
        TradeRequest request = TradeRequest.create(sender.getUuid(), target);
        pendingRequests.put(target, request);
        return true;
    }

    /**
     * Gets a pending request for a target player.
     *
     * @param target The target player's UUID
     * @return Optional containing the request if one exists and hasn't expired
     */
    public Optional<TradeRequest> getRequest(UUID target) {
        TradeRequest request = pendingRequests.get(target);
        if (request == null || request.isExpired()) {
            pendingRequests.remove(target);
            return Optional.empty();
        }
        return Optional.of(request);
    }

    /**
     * Gets a pending request from a specific sender to a target.
     *
     * @param sender The sender's UUID
     * @param target The target's UUID
     * @return Optional containing the request if one exists
     */
    public Optional<TradeRequest> getRequest(UUID sender, UUID target) {
        TradeRequest request = pendingRequests.get(target);
        if (request == null || request.isExpired() || !request.senderUUID().equals(sender)) {
            return Optional.empty();
        }
        return Optional.of(request);
    }

    /**
     * Accepts a trade request and starts a trading session.
     *
     * @param player1 The original sender of the request
     * @param player2 The player who is accepting
     * @return true if the trade session was started successfully
     */
    public boolean acceptRequest(SkyBlockPlayer player1, SkyBlockPlayer player2) {
        UUID target = player2.getUuid();

        // Verify request exists
        TradeRequest request = pendingRequests.get(target);
        if (request == null || request.isExpired() || !request.senderUUID().equals(player1.getUuid())) {
            return false;
        }

        // Check neither player is already in a trade
        if (isInTrade(player1.getUuid()) || isInTrade(player2.getUuid())) {
            return false;
        }

        // Remove the pending request
        pendingRequests.remove(target);

        // Start the trading session
        startTradeSession(player1, player2);
        return true;
    }

    /**
     * Denies a trade request.
     *
     * @param target The target player's UUID
     * @param sender The sender's UUID (optional, to verify)
     * @return true if a request was denied
     */
    public boolean denyRequest(UUID target, UUID sender) {
        TradeRequest request = pendingRequests.get(target);
        if (request == null) {
            return false;
        }
        if (sender != null && !request.senderUUID().equals(sender)) {
            return false;
        }
        pendingRequests.remove(target);
        return true;
    }

    /**
     * Starts a new trade session between two players.
     */
    private void startTradeSession(SkyBlockPlayer player1, SkyBlockPlayer player2) {
        String contextId = "trade-" + UUID.randomUUID();

        // Create initial state
        TradeState initialState = TradeState.create(player1.getUuid(), player2.getUuid());

        // Create shared context
        SharedContext<TradeState> sharedContext = SharedContext.create(contextId, initialState);

        // Store session references
        activeSessions.put(contextId, sharedContext);
        playerToSession.put(player1.getUuid(), contextId);
        playerToSession.put(player2.getUuid(), contextId);

        // Open the trade GUI for both players
        TradeView view = new TradeView();
        ViewNavigator.get(player1).pushShared(view, sharedContext);
        ViewNavigator.get(player2).pushShared(view, sharedContext);
    }

    /**
     * Ends a trade session and cleans up resources.
     *
     * @param contextId The context ID of the session
     */
    public void endSession(String contextId) {
        SharedContext<TradeState> context = activeSessions.remove(contextId);
        if (context != null) {
            TradeState state = context.state();
            playerToSession.remove(state.player1UUID());
            playerToSession.remove(state.player2UUID());
        }
    }

    /**
     * Ends a player's current trade session.
     *
     * @param playerUUID The player's UUID
     */
    public void endSessionForPlayer(UUID playerUUID) {
        String contextId = playerToSession.get(playerUUID);
        if (contextId != null) {
            endSession(contextId);
        }
    }

    /**
     * Checks if a player is currently in a trade.
     *
     * @param playerUUID The player's UUID
     * @return true if in a trade
     */
    public boolean isInTrade(UUID playerUUID) {
        return playerToSession.containsKey(playerUUID);
    }

    /**
     * Gets the context ID for a player's current trade session.
     *
     * @param playerUUID The player's UUID
     * @return Optional containing the context ID if in a trade
     */
    public Optional<String> getSessionForPlayer(UUID playerUUID) {
        return Optional.ofNullable(playerToSession.get(playerUUID));
    }

    /**
     * Gets an active trading session.
     *
     * @param contextId The context ID
     * @return Optional containing the shared context if it exists
     */
    public Optional<SharedContext<TradeState>> getSession(String contextId) {
        return Optional.ofNullable(activeSessions.get(contextId));
    }

    /**
     * Checks if a player has a pending outgoing request.
     *
     * @param senderUUID The sender's UUID
     * @return true if they have a pending request
     */
    public boolean hasPendingOutgoingRequest(UUID senderUUID) {
        return pendingRequests.values().stream()
                .anyMatch(request -> request.senderUUID().equals(senderUUID) && !request.isExpired());
    }

    /**
     * Removes any pending outgoing request from a player.
     *
     * @param senderUUID The sender's UUID
     */
    public void cancelOutgoingRequest(UUID senderUUID) {
        pendingRequests.entrySet().removeIf(entry ->
                entry.getValue().senderUUID().equals(senderUUID));
    }
}
