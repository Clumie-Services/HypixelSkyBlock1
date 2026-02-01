package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.minestom.server.entity.Entity;
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

public class EnchantmentSwarm implements Ench, EventBasedEnchant {

    public static final int[] DAMAGE_PER_ENEMY = new int[]{2, 4, 6, 8, 10};
    public static final int RANGE = 10;
    public static final int MAX_ENEMIES = 10;

    @Override
    public String getDescription(int level) {
        return "Increases your damage by §a" + DAMAGE_PER_ENEMY[level - 1] +
                "%§7 for each enemy within §a" + RANGE + "§7 blocks. Maximum of §a" + MAX_ENEMIES + "§7 enemies.";
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
        return List.of(EnchantItemGroups.SWORD, EnchantItemGroups.BOW);
    }

    @Override
    public ItemStatistics getStatisticsOnDamage(SkyBlockPlayer causer, LivingEntity receiver, int level) {
        if (causer == null || causer.getInstance() == null) return ItemStatistics.empty();

        // Count enemies within range
        int enemyCount = 0;
        for (Entity entity : causer.getInstance().getNearbyEntities(causer.getPosition(), RANGE)) {
            if (entity instanceof SkyBlockMob && entity != receiver) {
                enemyCount++;
                if (enemyCount >= MAX_ENEMIES) break;
            }
        }

        // Include the target being attacked
        if (receiver instanceof SkyBlockMob) {
            enemyCount = Math.min(enemyCount + 1, MAX_ENEMIES);
        }

        if (enemyCount == 0) return ItemStatistics.empty();

        double damageBonus = enemyCount * DAMAGE_PER_ENEMY[level - 1];
        return ItemStatistics.builder().withBase(ItemStatistic.DAMAGE, damageBonus).build();
    }
}
