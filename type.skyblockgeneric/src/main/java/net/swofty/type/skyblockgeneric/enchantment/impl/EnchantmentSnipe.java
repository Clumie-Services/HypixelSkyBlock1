package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentSnipe implements Ench {

    public static final int[] DAMAGE_PER_10_BLOCKS = new int[]{1, 2, 3, 4};

    @Override
    public String getDescription(int level) {
        return "Arrows deal §a+" + DAMAGE_PER_10_BLOCKS[level - 1] + "%§7 damage for every §a10§7 blocks traveled.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(
                1, 50,
                2, 100,
                3, 200,
                4, 400
        )));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.BOW);
    }
}
