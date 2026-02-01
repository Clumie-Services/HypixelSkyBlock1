package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.minestom.server.entity.LivingEntity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.enchantment.abstr.DamageEventEnchant;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.enchantment.abstr.EventBasedEnchant;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EnchantmentFatalTempo implements Ench, DamageEventEnchant, EventBasedEnchant {

    public static final int[] FEROCITY_PER_HIT = new int[]{10, 20, 30, 40, 50};
    public static final int MAX_FEROCITY_BONUS = 200;
    public static final int DURATION_MS = 3000;

    private static final Map<UUID, FatalTempoData> playerData = new ConcurrentHashMap<>();

    @Override
    public String getDescription(int level) {
        return "Attacking increases your §c⫽Ferocity§7 by §a" + FEROCITY_PER_HIT[level - 1] +
                "%§7 per hit, capped at §a" + MAX_FEROCITY_BONUS + "%§7 for §a3§7 seconds after your last attack.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(
                1, 500,
                2, 1000,
                3, 2000,
                4, 4000,
                5, 8000
        )));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.SWORD, EnchantItemGroups.BOW);
    }

    @Override
    public void onDamageDealt(SkyBlockPlayer player, LivingEntity target, double damageDealt, int level) {
        UUID playerId = player.getUuid();
        long currentTime = System.currentTimeMillis();
        int ferocityPerHit = FEROCITY_PER_HIT[level - 1];

        FatalTempoData data = playerData.computeIfAbsent(playerId, k -> new FatalTempoData());

        // Reset if expired
        if (currentTime - data.lastAttackTime > DURATION_MS) {
            data.currentFerocityBonus = 0;
        }

        // Add ferocity up to cap
        data.currentFerocityBonus = Math.min(data.currentFerocityBonus + ferocityPerHit, MAX_FEROCITY_BONUS);
        data.lastAttackTime = currentTime;
    }

    @Override
    public ItemStatistics getStatisticsOnDamage(SkyBlockPlayer causer, LivingEntity receiver, int level) {
        if (causer == null) return ItemStatistics.empty();

        UUID playerId = causer.getUuid();
        FatalTempoData data = playerData.get(playerId);

        if (data == null) return ItemStatistics.empty();

        long currentTime = System.currentTimeMillis();

        // Check if expired
        if (currentTime - data.lastAttackTime > DURATION_MS) {
            data.currentFerocityBonus = 0;
            return ItemStatistics.empty();
        }

        return ItemStatistics.builder().withBase(ItemStatistic.FEROCITY, (double) data.currentFerocityBonus).build();
    }

    private static class FatalTempoData {
        long lastAttackTime = 0;
        int currentFerocityBonus = 0;
    }
}
