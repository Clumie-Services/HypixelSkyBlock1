package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.swofty.type.skyblockgeneric.enchantment.abstr.DamageEventEnchant;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.enchantment.abstr.KillEventEnchant;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EnchantmentChampion implements Ench, DamageEventEnchant, KillEventEnchant {

    public static final double[] COMBAT_XP_BONUS = new double[]{3, 4, 4.5, 5, 5.5, 6, 6.5, 7, 8, 10};
    public static final double[] COINS_PER_HIT = new double[]{1.4, 1.8, 2.2, 2.6, 3.0, 3.4, 3.8, 4.2, 4.6, 5.0};
    public static final int[] EXP_ORBS_PER_HIT = new int[]{7, 9, 11, 13, 15, 17, 19, 21, 23, 25};

    private static final Map<UUID, Map<UUID, Integer>> playerHitCounts = new ConcurrentHashMap<>();

    @Override
    public String getDescription(int level) {
        return "Gain §a+" + COMBAT_XP_BONUS[level - 1] + "%§7 extra Combat XP. The 2nd hit on a mob grants §6+" +
                COINS_PER_HIT[level - 1] + " Coins§7 & §a+" + EXP_ORBS_PER_HIT[level - 1] + "§7 exp orbs.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(
                1, 100,
                2, 200,
                3, 400,
                4, 800,
                5, 1600,
                6, 3200,
                7, 6400,
                8, 12800,
                9, 25600,
                10, 51200
        )));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.SWORD);
    }

    @Override
    public void onDamageDealt(SkyBlockPlayer player, LivingEntity target, double damageDealt, int level) {
        if (!(target instanceof SkyBlockMob)) return;

        UUID playerId = player.getUuid();
        UUID mobId = target.getUuid();

        Map<UUID, Integer> mobHits = playerHitCounts.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());
        int hits = mobHits.getOrDefault(mobId, 0) + 1;
        mobHits.put(mobId, hits);

        // On 2nd hit, grant coins and exp
        if (hits == 2) {
            double coins = COINS_PER_HIT[level - 1];
            int expOrbs = EXP_ORBS_PER_HIT[level - 1];

            player.setCoins(player.getCoins() + coins);
            player.setExp(player.getExp() + expOrbs);
        }
    }

    @Override
    public void onMobKilled(SkyBlockPlayer player, SkyBlockMob killedMob, int level) {
        // Clean up hit tracking when mob dies
        UUID playerId = player.getUuid();
        Map<UUID, Integer> mobHits = playerHitCounts.get(playerId);
        if (mobHits != null) {
            mobHits.remove(killedMob.getUuid());
        }
    }
}
