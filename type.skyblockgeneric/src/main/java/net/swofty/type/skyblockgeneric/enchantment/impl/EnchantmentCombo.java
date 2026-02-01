package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.minestom.server.entity.LivingEntity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.enchantment.abstr.EventBasedEnchant;
import net.swofty.type.skyblockgeneric.enchantment.abstr.KillEventEnchant;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EnchantmentCombo implements Ench, KillEventEnchant, EventBasedEnchant {

    public static final int[] DAMAGE_PER_KILL = new int[]{1, 2, 3, 4, 5};
    public static final int[] MAX_KILLS = new int[]{2, 4, 6, 8, 10};
    public static final int[] DURATION_SECONDS = new int[]{2, 4, 6, 8, 10};

    private static final Map<UUID, ComboData> playerComboData = new ConcurrentHashMap<>();

    @Override
    public String getDescription(int level) {
        return "Increases your damage by §a" + DAMAGE_PER_KILL[level - 1] + "%§7 per kill up to §a" +
                MAX_KILLS[level - 1] + "§7 kills within §a" + DURATION_SECONDS[level - 1] + "s§7.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(
                1, 400,
                2, 800,
                3, 1600,
                4, 3200,
                5, 6400
        )));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.SWORD);
    }

    @Override
    public void onMobKilled(SkyBlockPlayer player, SkyBlockMob killedMob, int level) {
        UUID playerId = player.getUuid();
        long currentTime = System.currentTimeMillis();
        long duration = DURATION_SECONDS[level - 1] * 1000L;
        int maxKills = MAX_KILLS[level - 1];

        ComboData data = playerComboData.computeIfAbsent(playerId, k -> new ComboData());

        // Remove expired kills
        data.killTimestamps.removeIf(timestamp -> currentTime - timestamp > duration);

        // Add new kill if under max
        if (data.killTimestamps.size() < maxKills) {
            data.killTimestamps.add(currentTime);
        }

        data.level = level;
    }

    @Override
    public ItemStatistics getStatisticsOnDamage(SkyBlockPlayer causer, LivingEntity receiver, int level) {
        if (causer == null) return ItemStatistics.empty();

        UUID playerId = causer.getUuid();
        ComboData data = playerComboData.get(playerId);

        if (data == null || data.killTimestamps.isEmpty()) return ItemStatistics.empty();

        long currentTime = System.currentTimeMillis();
        long duration = DURATION_SECONDS[level - 1] * 1000L;

        // Remove expired kills
        data.killTimestamps.removeIf(timestamp -> currentTime - timestamp > duration);

        int activeKills = data.killTimestamps.size();
        if (activeKills == 0) return ItemStatistics.empty();

        double damageBonus = activeKills * DAMAGE_PER_KILL[level - 1];
        return ItemStatistics.builder().withBase(ItemStatistic.DAMAGE, damageBonus).build();
    }

    private static class ComboData {
        List<Long> killTimestamps = Collections.synchronizedList(new ArrayList<>());
        int level = 1;
    }
}
