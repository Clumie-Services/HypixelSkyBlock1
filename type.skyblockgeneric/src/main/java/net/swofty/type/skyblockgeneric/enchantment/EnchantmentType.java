package net.swofty.type.skyblockgeneric.enchantment;

import lombok.Getter;
import lombok.SneakyThrows;
import net.swofty.commons.StringUtility;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.enchantment.abstr.EnchFromTable;
import net.swofty.type.skyblockgeneric.enchantment.impl.*;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

@Getter
public enum EnchantmentType {
    SHARPNESS(EnchantmentSharpness.class),
    EFFICIENCY(EnchantmentEfficiency.class),
    SCAVENGER(EnchantmentScavenger.class),
    PROTECTION(EnchantmentProtection.class),
    CRITICAL(EnchantmentCritical.class),
    FIRST_STRIKE(EnchantmentFirstStrike.class),
    GROWTH(EnchantmentGrowth.class),
    LUCK(EnchantmentLuck.class),
    LOOTING(EnchantmentLooting.class),
    ENDER_SLAYER(EnchantmentEnderSlayer.class),
    SMITE(EnchantmentSmite.class),
    SILK_TOUCH(EnchantmentSilkTouch.class),
    SMELTING_TOUCH(EnchantmentSmeltingTouch.class),
    GIANT_KILLER(EnchantmentGiantKiller.class),
    EXECUTE(EnchantmentExecute.class),
    IMPALING(EnchantmentImpaling.class),
    BANE_OF_ARTHROPODS(EnchantmentBaneOfArthropods.class),
    CUBISM(EnchantmentCubism.class),
    FORTUNE(EnchantmentFortune.class),
    CLEAVE(EnchantmentCleave.class),
    EXPERIENCE(EnchantmentExperience.class),
    FIRE_ASPECT(EnchantmentFireAspect.class),
    KNOCKBACK(EnchantmentKnockback.class),
    LETHALITY(EnchantmentLethality.class),
    LIFE_STEAL(EnchantmentLifeSteal.class),
    DRAIN(EnchantmentDrain.class),
    MANA_STEAL(EnchantmentManaSteal.class),
    PROSECUTE(EnchantmentProsecute.class),
    THUNDERBOLT(EnchantmentThunderbolt.class),
    THUNDERLORD(EnchantmentThunderlord.class),
    VAMPIRISM(EnchantmentVampirism.class),
    VENOMOUS(EnchantmentVenomous.class),
    VICIOUS(EnchantmentVicious.class),

    // Ultimate Enchantments
    CHIMERA(EnchantmentChimera.class),
    COMBO(EnchantmentCombo.class),
    FATAL_TEMPO(EnchantmentFatalTempo.class),
    INFERNO(EnchantmentInferno.class),
    ONE_FOR_ALL(EnchantmentOneForAll.class),
    SOUL_EATER(EnchantmentSoulEater.class),
    SWARM(EnchantmentSwarm.class),
    ULTIMATE_JERRY(EnchantmentUltimateJerry.class),
    ULTIMATE_WISE(EnchantmentUltimateWise.class),

    // Additional Sword Enchantments
    CHAMPION(EnchantmentChampion.class),
    DIVINE_GIFT(EnchantmentDivineGift.class),
    DRAGON_HUNTER(EnchantmentDragonHunter.class),
    SMOLDERING(EnchantmentSmoldering.class),
    TABASCO(EnchantmentTabasco.class),
    TITAN_KILLER(EnchantmentTitanKiller.class),
    TRIPLE_STRIKE(EnchantmentTripleStrike.class),

    // Bow Enchantments
    CHANCE(EnchantmentChance.class),
    DRAGON_TRACER(EnchantmentDragonTracer.class),
    DUPLEX(EnchantmentDuplex.class),
    FLAME(EnchantmentFlame.class),
    INFINITE_QUIVER(EnchantmentInfiniteQuiver.class),
    OVERLOAD(EnchantmentOverload.class),
    PIERCING(EnchantmentPiercing.class),
    POWER(EnchantmentPower.class),
    PUNCH(EnchantmentPunch.class),
    REND(EnchantmentRend.class),
    SNIPE(EnchantmentSnipe.class),
    TOXOPHILITE(EnchantmentToxophilite.class),
    ;

    private final Class<? extends Ench> clazz;
    private final Ench ench;

    @SneakyThrows
    EnchantmentType(Class<? extends Ench> ench) {
        this.clazz = ench;

        this.ench = ench.getConstructor().newInstance();
    }

    public int getApplyCost(int level, SkyBlockPlayer player) {
        if (level < 1 || level > ench.getLevelsToApply(player).maximumLevel())
            throw new IllegalArgumentException("level cannot be less than 1 and more than " +
                    ench.getLevelsToApply(player).maximumLevel() + " for " + name());
        return ench.getLevelsToApply(player).get(level);
    }

    public String getDescription(int level, SkyBlockPlayer player) {
        if (level < 1 || level > ench.getLevelsToApply(player).maximumLevel())
            return ("level cannot be less than 1 and more than " +
                    ench.getLevelsToApply(player).maximumLevel() + " for " + name());
        return ench.getDescription(level);
    }

    public @Nullable EnchFromTable getEnchFromTable() {
        if (ench instanceof EnchFromTable)
            return (EnchFromTable) ench;
        return null;
    }

    public String getName() {
        return StringUtility.toNormalCase(this.name());
    }

    public boolean isUltimate() {
        return this == CHIMERA || this == COMBO || this == FATAL_TEMPO ||
               this == INFERNO || this == ONE_FOR_ALL || this == SOUL_EATER ||
               this == SWARM || this == ULTIMATE_JERRY || this == ULTIMATE_WISE ||
               this == DUPLEX || this == REND;
    }

    public String getFormattedName(int level) {
        String romanLevel = StringUtility.getAsRomanNumeral(level);
        if (isUltimate()) {
            return "§d§l" + getName() + " " + romanLevel;
        } else {
            return "§9" + getName() + " " + romanLevel;
        }
    }
}
