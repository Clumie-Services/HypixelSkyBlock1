package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentUltimateWise implements Ench {

    public static final int[] MANA_COST_REDUCTION = new int[]{10, 20, 30, 40, 50};

    @Override
    public String getDescription(int level) {
        return "Reduces the ability mana cost of this item by §a" + MANA_COST_REDUCTION[level - 1] + "%§7.";
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

    /**
     * Gets the mana cost reduction percentage for the given level.
     * This should be used by ability handlers to reduce mana costs.
     *
     * @param level The enchantment level (1-5)
     * @return The mana cost reduction percentage (10-50)
     */
    public static int getManaCostReduction(int level) {
        if (level < 1 || level > 5) return 0;
        return MANA_COST_REDUCTION[level - 1];
    }
}
