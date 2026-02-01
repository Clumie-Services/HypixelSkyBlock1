package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentToxophilite implements Ench {

    public static final int[] COMBAT_XP_BONUS = new int[]{3, 4, 5, 5, 6, 6, 7, 8, 9, 10};
    public static final double[] CRIT_CHANCE_BONUS = new double[]{3.7, 4.4, 5.1, 5.8, 6.5, 7.2, 7.9, 8.6, 9.3, 10.0};

    @Override
    public String getDescription(int level) {
        return "Gain §a" + COMBAT_XP_BONUS[level - 1] + "%§7 extra Combat XP. Grants §a+" +
                CRIT_CHANCE_BONUS[level - 1] + " §9☣Crit Chance§7.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.ofEntries(
                Map.entry(1, 50),
                Map.entry(2, 100),
                Map.entry(3, 150),
                Map.entry(4, 200),
                Map.entry(5, 250),
                Map.entry(6, 350),
                Map.entry(7, 500),
                Map.entry(8, 750),
                Map.entry(9, 1000),
                Map.entry(10, 1500)
        )));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.BOW);
    }

    @Override
    public ItemStatistics getStatistics(int level) {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.CRITICAL_CHANCE, CRIT_CHANCE_BONUS[level - 1])
                .build();
    }
}
