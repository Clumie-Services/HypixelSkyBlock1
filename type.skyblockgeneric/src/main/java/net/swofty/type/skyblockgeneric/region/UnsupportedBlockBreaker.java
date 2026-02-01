package net.swofty.type.skyblockgeneric.region;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.Set;

/**
 * Utility class to break blocks that require support from below.
 * When a supporting block is broken, blocks like grass, flowers, and crops should also break.
 */
public class UnsupportedBlockBreaker {

    /**
     * Blocks that require solid ground below them to exist.
     * When the block below is broken, these should break too (like vanilla Minecraft).
     */
    private static final Set<Block> BLOCKS_NEEDING_SUPPORT = Set.of(
            // Grass and ferns
            Block.SHORT_GRASS,
            Block.TALL_GRASS,
            Block.FERN,
            Block.LARGE_FERN,
            // Flowers
            Block.DANDELION,
            Block.POPPY,
            Block.BLUE_ORCHID,
            Block.ALLIUM,
            Block.AZURE_BLUET,
            Block.RED_TULIP,
            Block.ORANGE_TULIP,
            Block.WHITE_TULIP,
            Block.PINK_TULIP,
            Block.OXEYE_DAISY,
            Block.CORNFLOWER,
            Block.LILY_OF_THE_VALLEY,
            Block.WITHER_ROSE,
            Block.SUNFLOWER,
            Block.LILAC,
            Block.ROSE_BUSH,
            Block.PEONY,
            Block.TORCHFLOWER,
            Block.PINK_PETALS,
            // Saplings
            Block.OAK_SAPLING,
            Block.SPRUCE_SAPLING,
            Block.BIRCH_SAPLING,
            Block.JUNGLE_SAPLING,
            Block.ACACIA_SAPLING,
            Block.DARK_OAK_SAPLING,
            Block.CHERRY_SAPLING,
            Block.MANGROVE_PROPAGULE,
            // Crops
            Block.WHEAT,
            Block.CARROTS,
            Block.POTATOES,
            Block.BEETROOTS,
            Block.MELON_STEM,
            Block.PUMPKIN_STEM,
            Block.ATTACHED_MELON_STEM,
            Block.ATTACHED_PUMPKIN_STEM,
            Block.SWEET_BERRY_BUSH,
            Block.TORCHFLOWER_CROP,
            Block.PITCHER_CROP,
            // Mushrooms
            Block.RED_MUSHROOM,
            Block.BROWN_MUSHROOM,
            Block.CRIMSON_FUNGUS,
            Block.WARPED_FUNGUS,
            // Stackable plants (will cascade)
            Block.SUGAR_CANE,
            Block.CACTUS,
            Block.BAMBOO,
            Block.BAMBOO_SAPLING,
            Block.KELP,
            Block.KELP_PLANT,
            // Other ground decorations
            Block.DEAD_BUSH,
            Block.NETHER_SPROUTS,
            Block.WARPED_ROOTS,
            Block.CRIMSON_ROOTS,
            Block.SEAGRASS,
            Block.TALL_SEAGRASS,
            // Misc
            Block.SNOW,
            Block.WHITE_CARPET,
            Block.ORANGE_CARPET,
            Block.MAGENTA_CARPET,
            Block.LIGHT_BLUE_CARPET,
            Block.YELLOW_CARPET,
            Block.LIME_CARPET,
            Block.PINK_CARPET,
            Block.GRAY_CARPET,
            Block.LIGHT_GRAY_CARPET,
            Block.CYAN_CARPET,
            Block.PURPLE_CARPET,
            Block.BLUE_CARPET,
            Block.BROWN_CARPET,
            Block.GREEN_CARPET,
            Block.RED_CARPET,
            Block.BLACK_CARPET,
            Block.MOSS_CARPET,
            // Rails
            Block.RAIL,
            Block.POWERED_RAIL,
            Block.DETECTOR_RAIL,
            Block.ACTIVATOR_RAIL,
            // Pressure plates
            Block.STONE_PRESSURE_PLATE,
            Block.OAK_PRESSURE_PLATE,
            Block.SPRUCE_PRESSURE_PLATE,
            Block.BIRCH_PRESSURE_PLATE,
            Block.JUNGLE_PRESSURE_PLATE,
            Block.ACACIA_PRESSURE_PLATE,
            Block.DARK_OAK_PRESSURE_PLATE,
            Block.CHERRY_PRESSURE_PLATE,
            Block.MANGROVE_PRESSURE_PLATE,
            Block.BAMBOO_PRESSURE_PLATE,
            Block.CRIMSON_PRESSURE_PLATE,
            Block.WARPED_PRESSURE_PLATE,
            Block.LIGHT_WEIGHTED_PRESSURE_PLATE,
            Block.HEAVY_WEIGHTED_PRESSURE_PLATE,
            Block.POLISHED_BLACKSTONE_PRESSURE_PLATE,
            // Redstone
            Block.REDSTONE_WIRE,
            Block.REPEATER,
            Block.COMPARATOR,
            // Torches (standing)
            Block.TORCH,
            Block.SOUL_TORCH,
            Block.REDSTONE_TORCH
    );

    /**
     * Breaks blocks above the given position that require support from below.
     * Handles cascading breaks (e.g., stacked sugar cane, cactus).
     *
     * @param instance       The instance where the block was broken
     * @param brokenBlockPos The position of the block that was broken
     */
    public static void breakUnsupportedBlocksAbove(Instance instance, Point brokenBlockPos) {
        Point abovePos = brokenBlockPos.add(0, 1, 0);
        Block aboveBlock = instance.getBlock(abovePos);

        // Check if the block above needs support
        if (needsSupport(aboveBlock)) {
            // Break the block above
            instance.setBlock(abovePos, Block.AIR);

            // Recursively check for more unsupported blocks above (for stacked plants)
            breakUnsupportedBlocksAbove(instance, abovePos);
        }
    }

    /**
     * Checks if a block requires solid support from below.
     *
     * @param block The block to check
     * @return true if the block needs support from below
     */
    public static boolean needsSupport(Block block) {
        // Compare by block ID (registry key) to ignore block states
        String blockKey = block.key().asString();
        for (Block supportedBlock : BLOCKS_NEEDING_SUPPORT) {
            if (supportedBlock.key().asString().equals(blockKey)) {
                return true;
            }
        }
        return false;
    }
}
