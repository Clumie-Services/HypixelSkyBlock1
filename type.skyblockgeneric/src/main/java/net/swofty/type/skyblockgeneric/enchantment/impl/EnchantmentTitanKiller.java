package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.minestom.server.entity.LivingEntity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.enchantment.abstr.EventBasedEnchant;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentTitanKiller implements Ench, EventBasedEnchant {

    public static final double[] DAMAGE_PER_100_DEFENSE = new double[]{2, 4, 6, 8, 12, 16, 20};
    public static final double[] MAX_DAMAGE_BONUS = new double[]{6, 12, 20, 30, 45, 60, 80};

    @Override
    public String getDescription(int level) {
        return "Increases damage dealt by §a" + DAMAGE_PER_100_DEFENSE[level - 1] +
                "%§7 for every §a100 ❈Defense§7 your target has up to §a" + MAX_DAMAGE_BONUS[level - 1] + "%§7.";
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
                7, 6400
        )));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.SWORD);
    }

    @Override
    public ItemStatistics getStatisticsOnDamage(SkyBlockPlayer causer, LivingEntity receiver, int level) {
        if (receiver == null) return ItemStatistics.empty();

        double targetDefense = 0;
        if (receiver instanceof SkyBlockMob mob) {
            targetDefense = mob.getBaseStatistics().getOverall(ItemStatistic.DEFENSE);
        }

        if (targetDefense <= 0) return ItemStatistics.empty();

        // Calculate damage bonus based on target's defense
        double damagePerHundred = DAMAGE_PER_100_DEFENSE[level - 1];
        double maxBonus = MAX_DAMAGE_BONUS[level - 1];

        double damageBonus = (targetDefense / 100.0) * damagePerHundred;
        damageBonus = Math.min(damageBonus, maxBonus);

        return ItemStatistics.builder().withBase(ItemStatistic.DAMAGE, damageBonus).build();
    }
}
