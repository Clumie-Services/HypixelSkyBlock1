package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.minestom.server.entity.LivingEntity;
import net.swofty.type.skyblockgeneric.enchantment.abstr.DamageEventEnchant;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.enchantment.abstr.EnchFromTable;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentFlame implements Ench, EnchFromTable, DamageEventEnchant {

    public static final double[] DURATION_SECONDS = new double[]{3.5, 4.0};
    public static final int[] DAMAGE_PERCENT = new int[]{3, 6};

    @Override
    public String getDescription(int level) {
        return "Arrows ignite your enemies for §a" + DURATION_SECONDS[level - 1] + "s§7, dealing §a" +
                DAMAGE_PERCENT[level - 1] + "%§7 of your damage per second.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(
                1, 20,
                2, 40
        )));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.BOW);
    }

    @Override
    public TableLevels getLevelsFromTableToApply(@NotNull SkyBlockPlayer player) {
        return new TableLevels(new HashMap<>(Map.of(
                1, 20,
                2, 40
        )));
    }

    @Override
    public int getRequiredBookshelfPower() {
        return 3;
    }

    @Override
    public void onDamageDealt(SkyBlockPlayer player, LivingEntity target, double damageDealt, int level) {
        if (target == null) return;

        int fireTicks = (int) (DURATION_SECONDS[level - 1] * 20);
        target.setFireTicks(fireTicks);
    }
}
