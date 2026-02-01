package net.swofty.type.skyblockgeneric.event.actions.player.region;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.item.ItemStack;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEvent;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.skyblockgeneric.commands.RegionCommand;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Handles region wand interactions for setting position 1 and position 2.
 * Left-click sets position 1, right-click sets position 2.
 */
public class ActionRegionWandUse implements HypixelEventClass {

    /**
     * Handle right-click on block (set position 2)
     */
    @HypixelEvent(node = EventNodes.PLAYER, requireDataLoaded = true)
    public void onRightClick(PlayerBlockInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        ItemStack item = player.getItemInMainHand();

        if (!RegionCommand.isWand(item)) {
            return;
        }

        // Cancel the interaction to prevent block placement/use
        event.setCancelled(true);
        event.setBlockingItemUse(true);

        // Set position 2 at the clicked block
        Pos blockPos = new Pos(
                event.getBlockPosition().blockX(),
                event.getBlockPosition().blockY(),
                event.getBlockPosition().blockZ()
        );

        RegionCommand.setPosition2(player.getUuid(), blockPos);

        player.sendMessage("§ePosition 2 §aset to §e(" + blockPos.blockX() + ", " + blockPos.blockY() + ", " + blockPos.blockZ() + ")");
        showSelectionInfo(player);
    }

    /**
     * Handle left-click/block break attempt (set position 1)
     */
    @HypixelEvent(node = EventNodes.PLAYER, requireDataLoaded = true)
    public void onLeftClick(PlayerBlockBreakEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        ItemStack item = player.getItemInMainHand();

        if (!RegionCommand.isWand(item)) {
            return;
        }

        // Cancel the block break
        event.setCancelled(true);

        // Set position 1 at the clicked block
        Pos blockPos = new Pos(
                event.getBlockPosition().blockX(),
                event.getBlockPosition().blockY(),
                event.getBlockPosition().blockZ()
        );

        RegionCommand.setPosition1(player.getUuid(), blockPos);

        player.sendMessage("§ePosition 1 §aset to §e(" + blockPos.blockX() + ", " + blockPos.blockY() + ", " + blockPos.blockZ() + ")");
        showSelectionInfo(player);
    }

    private void showSelectionInfo(SkyBlockPlayer player) {
        Pos pos1 = RegionCommand.getPosition1(player.getUuid());
        Pos pos2 = RegionCommand.getPosition2(player.getUuid());

        if (pos1 != null && pos2 != null) {
            int volume = Math.abs(pos2.blockX() - pos1.blockX() + 1) *
                         Math.abs(pos2.blockY() - pos1.blockY() + 1) *
                         Math.abs(pos2.blockZ() - pos1.blockZ() + 1);
            player.sendMessage("§7Selection complete! Volume: §e" + volume + " blocks");
            player.sendMessage("§7Use §e/region create <id> <type> §7to create the region.");
        }
    }
}
