package net.swofty.type.generic.event.actions.npc;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.item.ItemStack;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.json.NPCSelectionManager;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEvent;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.user.HypixelPlayer;

/**
 * Handles the NPC positioning wand right-click on blocks.
 * When a player with a selected NPC right-clicks on a block with the wand,
 * it moves the NPC to that position and saves the change to JSON.
 */
public class ActionNPCWandUse implements HypixelEventClass {

    @HypixelEvent(node = EventNodes.PLAYER, requireDataLoaded = true)
    public void run(PlayerUseItemOnBlockEvent event) {
        HypixelPlayer player = (HypixelPlayer) event.getPlayer();
        ItemStack item = player.getItemInMainHand();

        // Check if player is using the NPC wand
        if (!NPCSelectionManager.isWand(item)) {
            return;
        }

        // Check if player has an NPC selected
        HypixelNPC selectedNPC = NPCSelectionManager.getSelectedNPC(player);
        if (selectedNPC == null) {
            player.sendMessage("§cYou don't have an NPC selected. Use §e/npc sel §cwhile looking at an NPC.");
            return;
        }

        // Get the target block position and adjust for NPC placement
        // Place NPC on top of the block
        Pos blockPos = new Pos(
                event.getPosition().blockX() + 0.5,
                event.getPosition().blockY() + 1,
                event.getPosition().blockZ() + 0.5
        );

        // Calculate yaw to face the player
        double dx = player.getPosition().x() - blockPos.x();
        double dz = player.getPosition().z() - blockPos.z();
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90);

        Pos npcPos = blockPos.withYaw(yaw).withPitch(0);

        // Move the NPC
        boolean success = NPCSelectionManager.moveSelectedNPC(player, npcPos);

        if (success) {
            player.sendMessage("§aMoved NPC §e" + selectedNPC.getName() + " §ato " +
                    String.format("§7(%.1f, %.1f, %.1f)", npcPos.x(), npcPos.y(), npcPos.z()));
            player.sendMessage("§7Position saved to JSON file.");
        } else {
            player.sendMessage("§cFailed to move NPC. Check console for errors.");
        }
    }
}
