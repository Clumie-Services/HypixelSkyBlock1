package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.minestom.server.entity.LivingEntity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.enchantment.abstr.DamageEventEnchant;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.enchantment.abstr.EventBasedEnchant;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EnchantmentTripleStrike implements Ench, DamageEventEnchant, EventBasedEnchant {

    public static final int[] DAMAGE_BONUS = new int[]{10, 20, 30, 40, 50};

    private static final Map<UUID, Map<UUID, Integer>> playerHitCounts = new ConcurrentHashMap<>();

    @Override
    public String getDescription(int level) {
        return "Increases melee damage dealt by §a" + DAMAGE_BONUS[level - 1] +
                "%§7 for the first three hits on a mob.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(
                1, 100,
                2, 200,
                3, 400,
                4, 800,
                5, 1600
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
    }

    @Override
    public ItemStatistics getStatisticsOnDamage(SkyBlockPlayer causer, LivingEntity receiver, int level) {
        if (causer == null || receiver == null) return ItemStatistics.empty();
        if (!(receiver instanceof SkyBlockMob)) return ItemStatistics.empty();

        UUID playerId = causer.getUuid();
        UUID mobId = receiver.getUuid();

        Map<UUID, Integer> mobHits = playerHitCounts.get(playerId);
        int hits = mobHits != null ? mobHits.getOrDefault(mobId, 0) : 0;

        // Only apply bonus for first 3 hits
        if (hits >= 3) return ItemStatistics.empty();

        return ItemStatistics.builder().withBase(ItemStatistic.DAMAGE, (double) DAMAGE_BONUS[level - 1]).build();
    }
}
