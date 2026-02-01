package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.minestom.server.entity.LivingEntity;
import net.swofty.type.skyblockgeneric.enchantment.abstr.DamageEventEnchant;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.enchantment.abstr.EnchFromTable;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentPunch implements Ench, EnchFromTable, DamageEventEnchant {

    public static final int[] KNOCKBACK_BLOCKS = new int[]{3, 6};

    @Override
    public String getDescription(int level) {
        int blocks = KNOCKBACK_BLOCKS[level - 1];
        return "Increases arrow knockback by §a" + blocks + " blocks§7.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        return new ApplyLevels(new HashMap<>(Map.of(
                1, 15,
                2, 30
        )));
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.BOW);
    }

    @Override
    public TableLevels getLevelsFromTableToApply(@NotNull SkyBlockPlayer player) {
        return new TableLevels(new HashMap<>(Map.of(
                1, 15,
                2, 30
        )));
    }

    @Override
    public int getRequiredBookshelfPower() {
        return 3;
    }

    @Override
    public void onDamageDealt(SkyBlockPlayer player, LivingEntity target, double damageDealt, int level) {
        if (level < 1 || level > 2) return;
        if (!(target instanceof SkyBlockMob)) return;

        int knockbackBlocks = KNOCKBACK_BLOCKS[level - 1];

        double yawRadians = player.getPosition().yaw() * Math.PI / 180;
        double knockbackX = Math.sin(yawRadians);
        double knockbackZ = -Math.cos(yawRadians);

        float knockbackStrength = knockbackBlocks * 0.1f;

        target.takeKnockback(knockbackStrength, knockbackX, knockbackZ);
    }
}
