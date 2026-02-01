package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.skyblockgeneric.enchantment.abstr.DamageEventEnchant;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EnchantmentInferno implements Ench, DamageEventEnchant {

    public static final double[] DAMAGE_MULTIPLIERS = new double[]{1.25, 1.50, 1.75, 2.00, 2.25};
    public static final int HITS_TO_TRIGGER = 10;
    public static final int TRAP_DURATION_SECONDS = 5;

    private static final Map<UUID, Map<UUID, Integer>> playerHitCounts = new ConcurrentHashMap<>();

    @Override
    public String getDescription(int level) {
        return "Every §a" + HITS_TO_TRIGGER + "th§7 hit on a mob traps it for §a" +
                TRAP_DURATION_SECONDS + "s§7 and deals §c" + DAMAGE_MULTIPLIERS[level - 1] +
                "x§7 of that hit's damage over the trap duration.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(
                1, 600,
                2, 1200,
                3, 2400,
                4, 4800,
                5, 9600
        )));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.SWORD, EnchantItemGroups.BOW);
    }

    @Override
    public void onDamageDealt(SkyBlockPlayer player, LivingEntity target, double damageDealt, int level) {
        if (!(target instanceof SkyBlockMob mob)) return;

        UUID playerId = player.getUuid();
        UUID mobId = target.getUuid();

        Map<UUID, Integer> mobHits = playerHitCounts.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());
        int hits = mobHits.getOrDefault(mobId, 0) + 1;

        if (hits >= HITS_TO_TRIGGER) {
            // Reset hit counter
            mobHits.remove(mobId);

            // Calculate total bonus damage
            double totalBonusDamage = damageDealt * DAMAGE_MULTIPLIERS[level - 1];
            double damagePerTick = totalBonusDamage / (TRAP_DURATION_SECONDS * 4); // 4 ticks per second

            // Apply slowness (trap effect)
            mob.addEffect(new Potion(PotionEffect.SLOWNESS, (byte) 127, TRAP_DURATION_SECONDS * 20));

            // Apply damage over time
            int totalTicks = TRAP_DURATION_SECONDS * 4;
            for (int i = 1; i <= totalTicks; i++) {
                final int tickIndex = i;
                MinecraftServer.getSchedulerManager().scheduleTask(() -> {
                    if (mob.isDead() || mob.isRemoved()) return;
                    mob.damage(new Damage(DamageType.MAGIC, player, player, null, (float) damagePerTick));
                }, TaskSchedule.tick(tickIndex * 5), TaskSchedule.stop());
            }
        } else {
            mobHits.put(mobId, hits);
        }
    }
}
