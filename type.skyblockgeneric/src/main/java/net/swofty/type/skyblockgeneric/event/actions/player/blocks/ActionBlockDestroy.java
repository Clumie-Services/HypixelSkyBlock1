package net.swofty.type.skyblockgeneric.event.actions.player.blocks;

import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEvent;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.skyblockgeneric.block.SkyBlockBlock;
import net.swofty.type.skyblockgeneric.block.impl.BlockBreakable;
import net.swofty.type.skyblockgeneric.block.impl.CustomSkyBlockBlock;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionBlockDestroy implements HypixelEventClass {

    @HypixelEvent(node = EventNodes.PLAYER, requireDataLoaded = true)
    public void onDestroy(PlayerBlockBreakEvent event) {
        if (SkyBlockBlock.isSkyBlockBlock(event.getBlock())) {
            SkyBlockBlock block = new SkyBlockBlock(event.getBlock());
            Object genericInstance = block.getGenericInstance();

            if (genericInstance instanceof BlockBreakable breakable
                    && genericInstance instanceof CustomSkyBlockBlock customBlock) {
                SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();

                // Check if the block should be destroyable
                if (!customBlock.shouldDestroy(player)) {
                    event.setCancelled(true);
                    return;
                }

                // Call the custom break handler first (before removing the block)
                // This allows handlers like chest to read block data
                breakable.onBreak(event, block);

                // Remove the block from the world
                event.getInstance().setBlock(event.getBlockPosition(), Block.AIR);

                // Drop the block itself as an item
                Block displayBlock = customBlock.getDisplayMaterial();
                Material material = Material.fromKey(displayBlock.name());
                if (material != null) {
                    player.addAndUpdateItem(new SkyBlockItem(material));
                }

                // Cancel the default break behavior since we handled it
                event.setCancelled(true);
            }
        }
    }
}
