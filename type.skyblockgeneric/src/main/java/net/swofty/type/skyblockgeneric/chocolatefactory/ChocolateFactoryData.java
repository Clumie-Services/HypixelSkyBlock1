package net.swofty.type.skyblockgeneric.chocolatefactory;

import lombok.Getter;
import lombok.Setter;
import net.swofty.type.skyblockgeneric.chocolatefactory.employee.ChocolateEmployee;

import java.util.HashMap;
import java.util.Map;

/**
 * Main data holder for a player's Chocolate Factory state.
 * Stores all progression, upgrades, and production data.
 */
@Getter
@Setter
public class ChocolateFactoryData {
    // Core chocolate values
    private long chocolate = 0;
    private long allTimeChocolate = 0;
    private long prestigeChocolate = 0;
    private long shopSpent = 0;

    // Factory level (1-6)
    private int factoryLevel = 1;

    // Employee levels (7 employees, each can be leveled up)
    private Map<ChocolateEmployee, Integer> employeeLevels = new HashMap<>();

    // Rabbit Barn - increases rabbit collection capacity
    private int rabbitBarnLevel = 0;

    // Hand-Baked Chocolate - increases chocolate per click (1-10)
    private int handBakedLevel = 0;

    // Time Tower (unlocked at CF2+)
    private int timeTowerLevel = 0;
    private int timeTowerCharges = 0;
    private long timeTowerActivatedAt = -1; // -1 means inactive
    private long timeTowerLastChargeAt = System.currentTimeMillis();

    // Rabbit Shrine (unlocked at CF3+) - increases rabbit rarity chance
    private int rabbitShrineLevel = 0;

    // Coach Jackrabbit (unlocked at CF4+) - multiplier bonus
    private int coachJackrabbitLevel = 0;

    // El Dorado rabbit tracking (0-3 finds for the legendary rabbit)
    private int elDoradoFinds = 0;

    // Last click time for click cooldown
    private long lastClickTime = 0;

    // Hitman slots
    private int rabbitHitmanSlots = 0;

    public ChocolateFactoryData() {
        // Initialize all employees at level 0
        for (ChocolateEmployee employee : ChocolateEmployee.values()) {
            employeeLevels.put(employee, 0);
        }
    }

    /**
     * Gets the level of a specific employee
     */
    public int getEmployeeLevel(ChocolateEmployee employee) {
        return employeeLevels.getOrDefault(employee, 0);
    }

    /**
     * Sets the level of a specific employee
     */
    public void setEmployeeLevel(ChocolateEmployee employee, int level) {
        employeeLevels.put(employee, level);
    }

    /**
     * Adds chocolate to the player's balance and tracks all-time/prestige totals
     */
    public void addChocolate(long amount) {
        this.chocolate += amount;
        this.allTimeChocolate += amount;
        this.prestigeChocolate += amount;
    }

    /**
     * Removes chocolate from the player's balance (for purchases)
     * @return true if the player had enough chocolate
     */
    public boolean removeChocolate(long amount) {
        if (this.chocolate >= amount) {
            this.chocolate -= amount;
            return true;
        }
        return false;
    }

    /**
     * Spends chocolate at the shop, tracking the spent amount
     * @return true if the player had enough chocolate
     */
    public boolean spendAtShop(long amount) {
        if (removeChocolate(amount)) {
            this.shopSpent += amount;
            return true;
        }
        return false;
    }

    /**
     * Checks if the Time Tower is currently active
     */
    public boolean isTimeTowerActive() {
        if (timeTowerActivatedAt == -1) return false;
        // Time Tower lasts 1 hour (3,600,000 ms)
        return System.currentTimeMillis() - timeTowerActivatedAt < 3_600_000;
    }

    /**
     * Gets the remaining time for Time Tower in milliseconds
     */
    public long getTimeTowerRemainingMs() {
        if (!isTimeTowerActive()) return 0;
        long elapsed = System.currentTimeMillis() - timeTowerActivatedAt;
        return Math.max(0, 3_600_000 - elapsed);
    }

    /**
     * Activates the Time Tower if charges are available
     * @return true if activated successfully
     */
    public boolean activateTimeTower() {
        if (timeTowerCharges > 0 && !isTimeTowerActive()) {
            timeTowerCharges--;
            timeTowerActivatedAt = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    /**
     * Updates Time Tower charges based on time elapsed.
     * Charges accumulate every 8 hours, max 3 charges.
     */
    public void updateTimeTowerCharges() {
        if (timeTowerLevel == 0) return;

        long now = System.currentTimeMillis();
        long elapsed = now - timeTowerLastChargeAt;
        long chargeTime = 8 * 60 * 60 * 1000L; // 8 hours in ms

        int newCharges = (int) (elapsed / chargeTime);
        if (newCharges > 0) {
            timeTowerCharges = Math.min(3, timeTowerCharges + newCharges);
            timeTowerLastChargeAt = now - (elapsed % chargeTime);
        }
    }

    /**
     * Gets time until next Time Tower charge in milliseconds
     */
    public long getTimeUntilNextCharge() {
        if (timeTowerCharges >= 3) return 0;
        long chargeTime = 8 * 60 * 60 * 1000L;
        long elapsed = System.currentTimeMillis() - timeTowerLastChargeAt;
        return chargeTime - (elapsed % chargeTime);
    }

    /**
     * Prestiges the factory, resetting progress but keeping certain upgrades.
     * @return true if prestige requirements were met
     */
    public boolean prestige() {
        int nextLevel = factoryLevel + 1;
        if (nextLevel > 6) return false;

        long required = ChocolateCalculator.calculatePrestigeRequirement(factoryLevel);
        if (prestigeChocolate < required) return false;

        // Reset values
        chocolate = 0;
        prestigeChocolate = 0;

        // Reset employees
        for (ChocolateEmployee employee : ChocolateEmployee.values()) {
            employeeLevels.put(employee, 0);
        }

        // Reset upgrades (but NOT Rabbit Barn)
        handBakedLevel = 0;
        timeTowerLevel = 0;
        timeTowerCharges = 0;
        timeTowerActivatedAt = -1;
        rabbitShrineLevel = 0;
        coachJackrabbitLevel = 0;

        // Increase factory level
        factoryLevel = nextLevel;

        return true;
    }

    /**
     * Gets the maximum rabbit barn capacity based on level
     */
    public int getRabbitBarnCapacity() {
        // Base capacity + 2 per level
        return 18 + (rabbitBarnLevel * 2);
    }

    /**
     * Gets the chocolate per click based on hand-baked level
     */
    public int getChocolatePerClick() {
        // Base 1 + level
        return 1 + handBakedLevel;
    }

    /**
     * Creates a copy of this data
     */
    public ChocolateFactoryData copy() {
        ChocolateFactoryData copy = new ChocolateFactoryData();
        copy.chocolate = this.chocolate;
        copy.allTimeChocolate = this.allTimeChocolate;
        copy.prestigeChocolate = this.prestigeChocolate;
        copy.shopSpent = this.shopSpent;
        copy.factoryLevel = this.factoryLevel;
        copy.employeeLevels = new HashMap<>(this.employeeLevels);
        copy.rabbitBarnLevel = this.rabbitBarnLevel;
        copy.handBakedLevel = this.handBakedLevel;
        copy.timeTowerLevel = this.timeTowerLevel;
        copy.timeTowerCharges = this.timeTowerCharges;
        copy.timeTowerActivatedAt = this.timeTowerActivatedAt;
        copy.timeTowerLastChargeAt = this.timeTowerLastChargeAt;
        copy.rabbitShrineLevel = this.rabbitShrineLevel;
        copy.coachJackrabbitLevel = this.coachJackrabbitLevel;
        copy.elDoradoFinds = this.elDoradoFinds;
        copy.lastClickTime = this.lastClickTime;
        copy.rabbitHitmanSlots = this.rabbitHitmanSlots;
        return copy;
    }
}
