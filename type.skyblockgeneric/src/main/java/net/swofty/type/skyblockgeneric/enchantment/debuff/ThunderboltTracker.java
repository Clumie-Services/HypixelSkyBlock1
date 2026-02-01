package net.swofty.type.skyblockgeneric.enchantment.debuff;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.DamageIndicator;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class ThunderboltTracker {

    public static final double[] THUNDERBOLT_DAMAGE_PERCENTAGES = new double[]{0.04, 0.08, 0.12, 0.16, 0.20, 0.25, 0.30};
    public static final double[] THUNDERLORD_DAMAGE_PERCENTAGES = new double[]{0.08, 0.16, 0.24, 0.32, 0.40, 0.50, 0.60};

    private static final int HITS_PER_LIGHTNING = 3;

    // Map<LightningType, Map<playerId, Map<mobId, hitCount>>> - tracks hits per mob per player per enchantment type
    private static final Map<LightningType, Map<UUID, Map<UUID, Integer>>> typePlayerMobHitCounters = new ConcurrentHashMap<>();

    public enum LightningType {
        THUNDERBOLT,
        THUNDERLORD
    }

    public static void registerHit(SkyBlockPlayer player, LivingEntity target, double damageDealt, int level, LightningType type) {
        if (level < 1 || level > 7) return;
        if (!(target instanceof SkyBlockMob)) return;

        UUID playerId = player.getUuid();
        UUID mobId = target.getUuid();

        // Get or create the type-specific player's mob hit counter map
        Map<UUID, Map<UUID, Integer>> playerMobCounters = typePlayerMobHitCounters.computeIfAbsent(type, k -> new ConcurrentHashMap<>());
        Map<UUID, Integer> mobCounters = playerMobCounters.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());

        // Increment hit counter for this specific mob
        int currentHits = mobCounters.getOrDefault(mobId, 0);
        currentHits++;

        if (currentHits >= HITS_PER_LIGHTNING) {
            triggerLightningStrike(player, target, damageDealt, level, type);
            mobCounters.remove(mobId); // Reset counter for this mob
        } else {
            mobCounters.put(mobId, currentHits);
        }
    }

    private static void triggerLightningStrike(SkyBlockPlayer player, LivingEntity originalTarget, double baseDamage, int level, LightningType type) {
        if (!(originalTarget instanceof SkyBlockMob)) return;

        double[] damagePercentages = (type == LightningType.THUNDERBOLT) ? THUNDERBOLT_DAMAGE_PERCENTAGES : THUNDERLORD_DAMAGE_PERCENTAGES;
        double damagePercentage = damagePercentages[level - 1];
        double lightningDamage = baseDamage * damagePercentage;

        if (lightningDamage <= 0) return;

        // Thunderbolt hits enemies within 2 blocks, Thunderlord only hits the target
        if (type == LightningType.THUNDERBOLT) {
            // AOE damage to nearby mobs
            originalTarget.getInstance().getNearbyEntities(originalTarget.getPosition(), 2).stream()
                    .filter(e -> e instanceof SkyBlockMob && e != player)
                    .map(e -> (SkyBlockMob) e)
                    .forEach(mob -> {
                        mob.damage(new Damage(DamageType.PLAYER_ATTACK, player, player, player.getPosition(), (float) lightningDamage));
                        new DamageIndicator()
                                .damage((float) lightningDamage)
                                .pos(mob.getPosition())
                                .critical(false)
                                .display(mob.getInstance());
                    });
        } else {
            // Single target damage
            originalTarget.damage(new Damage(DamageType.PLAYER_ATTACK, player, player, player.getPosition(), (float) lightningDamage));
            new DamageIndicator()
                    .damage((float) lightningDamage)
                    .pos(originalTarget.getPosition())
                    .critical(false)
                    .display(originalTarget.getInstance());
        }

        // Visual lightning effect only (no damage from the entity itself)
        net.minestom.server.entity.Entity lightningBolt = new net.minestom.server.entity.Entity(EntityType.LIGHTNING_BOLT);
        lightningBolt.setInstance(originalTarget.getInstance(), originalTarget.getPosition());
        lightningBolt.scheduleRemove(java.time.Duration.ofMillis(500));
    }

    /**
     * Cleans up hit counters for a specific mob (call when mob dies or is removed)
     */
    public static void cleanupMobCounters(UUID mobId) {
        for (Map<UUID, Map<UUID, Integer>> playerMobCounters : typePlayerMobHitCounters.values()) {
            for (Map<UUID, Integer> mobCounters : playerMobCounters.values()) {
                mobCounters.remove(mobId);
            }
        }
    }

}

