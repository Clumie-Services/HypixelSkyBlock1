package net.swofty.type.skyblockgeneric.entity.mob.ai;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.pathfinding.Navigator;
import net.minestom.server.instance.block.Block;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomRegionStrollGoal extends GoalSelector {

    private static final long DELAY = 2500;
    private static final long LOOK_AROUND_INTERVAL = 1000;

    private final int radius;
    private final Random random = new Random();
    private final @Nullable RegionType type;

    private long lastStroll;
    private long lastLookAround;
    private boolean isWalking = false;

    /**
     * Creates a stroll goal that restricts movement to a specific region.
     */
    public RandomRegionStrollGoal(@NotNull EntityCreature entityCreature, int radius, @Nullable RegionType type) {
        super(entityCreature);
        this.type = type;
        this.radius = radius;
    }

    /**
     * Creates a stroll goal that allows movement anywhere (no region restriction).
     */
    public RandomRegionStrollGoal(@NotNull EntityCreature entityCreature, int radius) {
        this(entityCreature, radius, null);
    }

    @Override
    public boolean shouldStart() {
        return System.currentTimeMillis() - lastStroll >= DELAY;
    }

    @Override
    public void start() {
        List<Vec> closePositions = getNearbyBlocks(radius);
        int remainingAttempt = closePositions.size();

        if (remainingAttempt == 0) {
            isWalking = false;
            return;
        }

        while (remainingAttempt-- > 0) {
            final int index = random.nextInt(closePositions.size());
            final Vec position = closePositions.get(index);

            final var target = entityCreature.getPosition().add(position);
            final boolean result = entityCreature.getNavigator().setPathTo(target);
            if (result) {
                isWalking = true;
                break;
            }
        }
    }

    @Override
    public void tick(long time) {
        Navigator navigator = entityCreature.getNavigator();

        // Check if we've reached destination
        if (isWalking && (navigator.getPathPosition() == null ||
            entityCreature.getPosition().distanceSquared(navigator.getPathPosition()) < 1)) {
            isWalking = false;
        }

        // Random look around when idle
        if (!isWalking && System.currentTimeMillis() - lastLookAround >= LOOK_AROUND_INTERVAL) {
            lastLookAround = System.currentTimeMillis();
            float randomYaw = entityCreature.getPosition().yaw() + (random.nextFloat() - 0.5f) * 60;
            float randomPitch = (random.nextFloat() - 0.5f) * 20;
            entityCreature.setView(randomYaw, randomPitch);
        }
    }

    @Override
    public boolean shouldEnd() {
        Navigator navigator = entityCreature.getNavigator();
        // End when not walking and navigator is done
        return !isWalking || navigator.getPathPosition() == null;
    }

    @Override
    public void end() {
        this.lastStroll = System.currentTimeMillis();
        this.isWalking = false;
        entityCreature.getNavigator().setPathTo(null);
    }

    private @NotNull List<Vec> getNearbyBlocks(int radius) {
        List<Vec> blocks = new ArrayList<>();

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                int entityX = getEntityCreature().getPosition().blockX() + x;
                int y = getEntityCreature().getPosition().blockY();
                int entityZ = getEntityCreature().getPosition().blockZ() + z;

                if (entityCreature.getInstance() != null) {
                    if (!entityCreature.getInstance().isChunkLoaded(new Pos(entityX, y, entityZ)))
                        entityCreature.getInstance().loadChunk(new Pos(entityX, y, entityZ)).join();
                    Block block = entityCreature.getInstance().getBlock(entityX, y, entityZ);

                    if (!block.isAir()) continue;
                }

                // If no region type specified, allow movement anywhere
                if (type == null) {
                    blocks.add(new Vec(x, 0, z));
                    continue;
                }

                // Otherwise restrict to the specified region
                SkyBlockRegion region = SkyBlockRegion.getRegionOfPosition(new Pos(entityX, y, entityZ));

                if (region == null)
                    continue;

                if (region.getType() == type)
                    blocks.add(new Vec(x, 0, z));
            }
        }

        return blocks;
    }
}