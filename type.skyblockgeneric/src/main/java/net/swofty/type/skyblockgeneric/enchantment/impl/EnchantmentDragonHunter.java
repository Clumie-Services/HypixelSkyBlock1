package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.minestom.server.entity.EntityType;
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

public class EnchantmentDragonHunter implements Ench, EventBasedEnchant {

    public static final int[] DAMAGE_BONUS = new int[]{8, 16, 24, 32, 40};

    @Override
    public String getDescription(int level) {
        return "Increases damage dealt to Ender Dragons by §a" + DAMAGE_BONUS[level - 1] + "%§7.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(
                1, 1000,
                2, 2000,
                3, 4000,
                4, 8000,
                5, 16000
        )));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.SWORD, EnchantItemGroups.BOW);
    }

    @Override
    public ItemStatistics getStatisticsOnDamage(SkyBlockPlayer causer, LivingEntity receiver, int level) {
        if (receiver == null) return ItemStatistics.empty();

        // Check if target is an Ender Dragon
        boolean isDragon = receiver.getEntityType() == EntityType.ENDER_DRAGON;

        // Also check SkyBlockMob type if applicable
        if (!isDragon && receiver instanceof SkyBlockMob mob) {
            String mobName = mob.getDisplayName().toLowerCase();
            isDragon = mobName.contains("dragon");
        }

        if (!isDragon) return ItemStatistics.empty();

        return ItemStatistics.builder().withBase(ItemStatistic.DAMAGE, (double) DAMAGE_BONUS[level - 1]).build();
    }
}
