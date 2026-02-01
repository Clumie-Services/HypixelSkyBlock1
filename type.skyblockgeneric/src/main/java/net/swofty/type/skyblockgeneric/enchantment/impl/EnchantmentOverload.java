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

public class EnchantmentOverload implements Ench {

    public static final int[] CRIT_DAMAGE = new int[]{1, 2, 3, 4, 5};
    public static final int[] CRIT_CHANCE = new int[]{1, 2, 3, 4, 5};
    public static final int[] MEGA_CRIT_BONUS = new int[]{10, 20, 30, 40, 50};

    @Override
    public String getDescription(int level) {
        return "Increases §9☠Crit Damage§7 by §a" + CRIT_DAMAGE[level - 1] + "%§7 and §9☣Crit Chance§7 by §a" +
                CRIT_CHANCE[level - 1] + "%§7. Having a Critical chance above §a100%§7 grants a chance to " +
                "perform a §6Mega Critical Hit§7 dealing §a" + MEGA_CRIT_BONUS[level - 1] + "%§7 extra damage.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(
                1, 200,
                2, 400,
                3, 800,
                4, 1600,
                5, 3200
        )));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.BOW);
    }

    @Override
    public ItemStatistics getStatistics(int level) {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.CRITICAL_DAMAGE, (double) CRIT_DAMAGE[level - 1])
                .withBase(ItemStatistic.CRITICAL_CHANCE, (double) CRIT_CHANCE[level - 1])
                .build();
    }
}
