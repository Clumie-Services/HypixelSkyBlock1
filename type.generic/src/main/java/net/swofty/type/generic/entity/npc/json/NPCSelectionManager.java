package net.swofty.type.generic.entity.npc.json;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.user.HypixelPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages NPC selection for the /npc sel command.
 * Tracks which NPC each player has selected and handles the wand item.
 * Supports both JSON-configured NPCs and Java-coded NPCs.
 */
public class NPCSelectionManager {
    private static final Map<UUID, HypixelNPC> selectedNPCs = new HashMap<>();
    public static final Tag<Boolean> NPC_WAND_TAG = Tag.Boolean("npc_wand").defaultValue(false);

    /**
     * Creates the NPC positioning wand item.
     */
    public static ItemStack createWand() {
        return ItemStack.builder(Material.BLAZE_ROD)
                .customName(net.kyori.adventure.text.Component.text("§6§lNPC Position Wand"))
                .lore(
                        net.kyori.adventure.text.Component.text("§7Right-click on a block to"),
                        net.kyori.adventure.text.Component.text("§7teleport the selected NPC."),
                        net.kyori.adventure.text.Component.text(""),
                        net.kyori.adventure.text.Component.text("§eRight-click to position NPC")
                )
                .set(NPC_WAND_TAG, true)
                .build();
    }

    /**
     * Checks if an item is the NPC wand.
     */
    public static boolean isWand(ItemStack item) {
        return item != null && item.getTag(NPC_WAND_TAG);
    }

    /**
     * Selects an NPC for a player.
     */
    public static void selectNPC(HypixelPlayer player, HypixelNPC npc) {
        selectedNPCs.put(player.getUuid(), npc);
    }

    /**
     * Gets the selected NPC for a player.
     */
    @Nullable
    public static HypixelNPC getSelectedNPC(HypixelPlayer player) {
        return selectedNPCs.get(player.getUuid());
    }

    /**
     * Gets the selected NPC as a JsonConfiguredNPC if it is one.
     */
    @Nullable
    public static JsonConfiguredNPC getSelectedJsonNPC(HypixelPlayer player) {
        HypixelNPC npc = selectedNPCs.get(player.getUuid());
        return npc instanceof JsonConfiguredNPC jsonNpc ? jsonNpc : null;
    }

    /**
     * Clears the NPC selection for a player.
     */
    public static void clearSelection(HypixelPlayer player) {
        selectedNPCs.remove(player.getUuid());
    }

    /**
     * Checks if a player has an NPC selected.
     */
    public static boolean hasSelection(HypixelPlayer player) {
        return selectedNPCs.containsKey(player.getUuid());
    }

    /**
     * Finds any NPC the player is looking at.
     *
     * @param player The player
     * @param maxDistance Maximum distance to check
     * @return The NPC being looked at, or null if none
     */
    @Nullable
    public static HypixelNPC getNPCLookingAt(HypixelPlayer player, double maxDistance) {
        Pos playerPos = player.getPosition().add(0, player.getEyeHeight(), 0);
        Vec direction = playerPos.direction();

        HypixelNPC closestNpc = null;
        double closestDistance = maxDistance;

        for (HypixelNPC npc : HypixelNPC.getRegisteredNPCs()) {
            Pos npcPos = npc.getParameters().position(player);
            if (npcPos == null) continue;

            // Check if NPC is roughly in the direction the player is looking
            Vec toNpc = new Vec(
                    npcPos.x() - playerPos.x(),
                    npcPos.y() + 1 - playerPos.y(), // Aim at center of NPC
                    npcPos.z() - playerPos.z()
            );

            double distance = toNpc.length();
            if (distance > maxDistance) continue;

            // Normalize and check angle
            Vec toNpcNorm = toNpc.normalize();
            double dot = direction.dot(toNpcNorm);

            // dot > 0.9 means within ~25 degrees of looking direction
            if (dot > 0.9) {
                // Calculate perpendicular distance from look ray to NPC
                Vec cross = direction.cross(toNpcNorm);
                double perpDistance = cross.length() * distance;

                // NPC hitbox is roughly 0.6 wide, allow some margin
                if (perpDistance < 1.5 && distance < closestDistance) {
                    closestDistance = distance;
                    closestNpc = npc;
                }
            }
        }

        return closestNpc;
    }

    /**
     * Finds a JSON-configured NPC the player is looking at.
     *
     * @param player The player
     * @param maxDistance Maximum distance to check
     * @return The JSON NPC being looked at, or null if none
     */
    @Nullable
    public static JsonConfiguredNPC getJsonNPCLookingAt(HypixelPlayer player, double maxDistance) {
        HypixelNPC npc = getNPCLookingAt(player, maxDistance);
        return npc instanceof JsonConfiguredNPC jsonNpc ? jsonNpc : null;
    }

    /**
     * Moves the selected NPC to a new position and saves the change.
     * Only works for JSON-configured NPCs.
     *
     * @param player The player who made the change
     * @param newPos The new position for the NPC
     * @return true if successful
     */
    public static boolean moveSelectedNPC(HypixelPlayer player, Pos newPos) {
        HypixelNPC npc = getSelectedNPC(player);
        if (npc == null) {
            return false;
        }

        if (!(npc instanceof JsonConfiguredNPC jsonNpc)) {
            player.sendMessage("§cThis NPC is not configured via JSON and cannot be repositioned.");
            player.sendMessage("§7Use §e/npc export <file> §7to export this NPC to JSON first.");
            return false;
        }

        String npcId = jsonNpc.getId();
        if (!NPCJsonLoader.isJsonConfigured(npcId)) {
            player.sendMessage("§cThis NPC is not configured via JSON and cannot be repositioned.");
            return false;
        }

        // Update and save
        boolean saved = NPCJsonLoader.updateNPCPosition(npcId, newPos);
        if (saved) {
            // Force the NPC to reload for all players by removing from cache
            // This will cause it to respawn at the new location
            clearNPCFromPlayerCaches(npc);
        }

        return saved;
    }

    /**
     * Clears an NPC from all player caches so it respawns.
     * Used internally for position updates.
     */
    private static void clearNPCFromPlayerCaches(HypixelNPC npc) {
        clearNPCFromAllCaches(npc);
    }

    /**
     * Clears an NPC from all player caches so it respawns with updated data.
     * Use this after updating NPC properties like skin or position.
     *
     * @param npc The NPC to clear from caches
     */
    public static void clearNPCFromAllCaches(HypixelNPC npc) {
        for (Map.Entry<UUID, HypixelNPC.PlayerNPCCache> entry : HypixelNPC.getPerPlayerNPCs().entrySet()) {
            HypixelNPC.PlayerNPCCache cache = entry.getValue();
            Map.Entry<?, Entity> npcEntry = cache.get(npc);
            if (npcEntry != null) {
                Entity entity = npcEntry.getValue();
                if (entity != null) {
                    entity.remove();
                }
                cache.remove(npc);
            }
        }
    }
}
