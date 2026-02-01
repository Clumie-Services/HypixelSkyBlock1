package net.swofty.type.skyblockgeneric.gui.inventories.chocolatefactory;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.inventory.RefreshingGUI;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.chocolatefactory.ChocolateCalculator;
import net.swofty.type.skyblockgeneric.chocolatefactory.ChocolateFactoryData;
import net.swofty.type.skyblockgeneric.chocolatefactory.HoppityCollectionData;
import net.swofty.type.skyblockgeneric.chocolatefactory.employee.ChocolateEmployee;
import net.swofty.type.skyblockgeneric.chocolatefactory.milestone.FactoryMilestone;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Main Chocolate Factory GUI - 54 slot inventory matching Hypixel's exact layout.
 */
public class GUIChocolateFactory extends HypixelInventoryGUI implements RefreshingGUI {
    // Chocolate head texture
    private static final String CHOCOLATE_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTcxOTkzOTQxMzQ3NCwKICAicHJvZmlsZUlkIiA6ICJlNGUxYmY5NzMwZWI0NDRhYmIyOGIxODgxN2Q0M2YzZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSU1PR0FNRVMwMzIxIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzlhODE1Mzk4ZTdkYTg5YjFiYzA4ZjY0NmNhZmM4ZTdiODEzZGEwYmUwZWVjMGNjZTZkM2VmZjUyMDc4MDEwMjYiCiAgICB9CiAgfQp9";
    private static final String HOPPITY_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTcxMTYzNDM5MTg3OCwKICAicHJvZmlsZUlkIiA6ICIxNmQ4NjI4NzYzMWY0NDY2OGQ0NDM2ZTJlY2IwNTllNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZXphVG91cm5leSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9iNzllN2YzMzQxYjY3MmQ5ZGU2NTY0Y2JhY2EwNTJhNmE3MjNlYTQ2NmEyZTY2YWYzNWJhMWJhODU1ZjBkNjkyIgogICAgfQogIH0KfQ==";

    public GUIChocolateFactory() {
        super("Chocolate Factory", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(Material.BLACK_STAINED_GLASS_PANE, " ");
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        ChocolateFactoryData data = player.getChocolateFactoryData();

        // Slot 13: Clickable chocolate head (main interaction)
        set(createChocolateHeadItem(player, data));

        // Slot 27: Factory level / Prestige info (Dropper)
        set(createPrestigeItem(player, data));

        // Slots 28-34: 7 employees
        int employeeSlot = 28;
        for (ChocolateEmployee employee : ChocolateEmployee.values()) {
            set(createEmployeeItem(player, data, employee, employeeSlot));
            employeeSlot++;
        }

        // Slot 35: Rabbit Barn
        set(createRabbitBarnItem(player, data));

        // Slot 38: Hand-Baked Chocolate
        set(createHandBakedItem(player, data));

        // Slot 39: Time Tower (CF2+)
        set(createTimeTowerItem(player, data));

        // Slot 41: Rabbit Shrine (CF3+)
        set(createRabbitShrineItem(player, data));

        // Slot 42: Coach Jackrabbit (CF4+)
        set(createCoachJackrabbitItem(player, data));

        // Slot 47: Chocolate Shop button
        set(createShopButton());

        // Slot 48: Go Back button
        set(createBackButton());

        // Slot 49: Close button
        set(createCloseButton());

        // Slot 50: Hoppity's Collection button
        set(createCollectionButton(player));

        // Slot 51: Rabbit Hitman button
        set(createHitmanButton(player));

        // Slot 52: Chocolate Factory Ranking
        set(createRankingButton(player, data));

        // Slot 53: Factory Milestones button
        set(createMilestonesButton(player, data));
    }

    private GUIClickableItem createChocolateHeadItem(SkyBlockPlayer player, ChocolateFactoryData data) {
        return new GUIClickableItem(13) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                ChocolateFactoryData factoryData = sbPlayer.getChocolateFactoryData();

                // Check click cooldown (500ms)
                long now = System.currentTimeMillis();
                if (now - factoryData.getLastClickTime() < 500) {
                    return;
                }
                factoryData.setLastClickTime(now);

                // Add chocolate from click
                int clickChocolate = factoryData.getChocolatePerClick();
                factoryData.addChocolate(clickChocolate);

                // Save data
                sbPlayer.getSkyblockDataHandler()
                        .get(SkyBlockDataHandler.Data.CHOCOLATE_FACTORY, DatapointChocolateFactory.class)
                        .setValue(factoryData);

                // Play sound
                sbPlayer.playSound(Sound.sound(Key.key("entity.generic.eat"), Sound.Source.PLAYER, 1f, 1f), Sound.Emitter.self());

                // Refresh GUI
                refreshItems(sbPlayer);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                ChocolateFactoryData factoryData = sbPlayer.getChocolateFactoryData();
                double cps = ChocolateCalculator.calculateTotalCps(sbPlayer);

                List<String> lore = new ArrayList<>();
                lore.add("§6Chocolate§7, of course, is not a valid");
                lore.add("§7source of §anutrition§7. This, however,");
                lore.add("§7from being §dawesome§7.");
                lore.add("");
                lore.add("§7Chocolate Production");
                lore.add("§6" + ChocolateCalculator.formatCps(cps) + " §8per second");
                lore.add("");
                lore.add("§7All-time Chocolate: §6" + StringUtility.commaify(factoryData.getAllTimeChocolate()));
                lore.add("");
                lore.add("§eClick to uncover the meaning of life!");

                return ItemStackCreator.getStackHead(
                        "§e" + factoryData.getChocolatePerClick() + " §6Chocolate",
                        CHOCOLATE_TEXTURE,
                        1,
                        lore.toArray(new String[0])
                );
            }
        };
    }

    private GUIClickableItem createPrestigeItem(SkyBlockPlayer player, ChocolateFactoryData data) {
        return new GUIClickableItem(27) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                ChocolateFactoryData factoryData = sbPlayer.getChocolateFactoryData();

                if (factoryData.getFactoryLevel() >= 6) {
                    sbPlayer.sendMessage("§cYou have reached the maximum factory level!");
                    return;
                }

                long required = ChocolateCalculator.calculatePrestigeRequirement(factoryData.getFactoryLevel());
                if (factoryData.getPrestigeChocolate() < required) {
                    sbPlayer.sendMessage("§cYou need " + StringUtility.commaify(required) + " prestige chocolate to prestige!");
                    return;
                }

                if (factoryData.prestige()) {
                    sbPlayer.getSkyblockDataHandler()
                            .get(SkyBlockDataHandler.Data.CHOCOLATE_FACTORY, DatapointChocolateFactory.class)
                            .setValue(factoryData);

                    sbPlayer.sendMessage("§a§lPRESTIGE! §7You are now " + ChocolateCalculator.getFactoryLevelName(factoryData.getFactoryLevel()) + "§7!");
                    sbPlayer.playSound(Sound.sound(Key.key("ui.toast.challenge_complete"), Sound.Source.PLAYER, 1f, 1f), Sound.Emitter.self());
                    refreshItems(sbPlayer);
                }
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                ChocolateFactoryData factoryData = sbPlayer.getChocolateFactoryData();
                int level = factoryData.getFactoryLevel();

                List<String> lore = new ArrayList<>();
                lore.add("§7Chocolate Production Multiplier: §6" + ChocolateCalculator.getFactoryProductionMultiplier(level) + "x");
                lore.add("§7Max Rabbit Rarity: " + getMaxRarityForLevel(level));
                lore.add("§7Max Chocolate: §6" + getMaxChocolateForLevel(level));
                lore.add("§7Max Employee: " + getMaxEmployeeForLevel(level));
                lore.add("§7Max §cRabbit Hitman §7Slots: §6" + getMaxHitmanSlotsForLevel(level));
                lore.add("");

                if (level < 6) {
                    lore.add("§8§m-----------------");
                    lore.add("§d§lPRESTIGE §8➜ " + ChocolateCalculator.getFactoryLevelName(level + 1));
                    lore.add("");
                    lore.add("§8▶ §cResets Chocolate Factory progress.");
                    lore.add("§8▶ §cResets Chocolate.");
                    lore.add("§8▶ §7Chocolate Production Multiplier: §8" + ChocolateCalculator.getFactoryProductionMultiplier(level) + " ➜ §6" + ChocolateCalculator.getFactoryProductionMultiplier(level + 1) + "x");
                    lore.add("§8▶ §7Max Rabbit Rarity: §8" + getMaxRarityNameForLevel(level) + " ➜ " + getMaxRarityForLevel(level + 1));
                    lore.add("§8▶ §7Max Chocolate: §8" + getMaxChocolateForLevel(level) + " ➜ §6" + getMaxChocolateForLevel(level + 1));
                    lore.add("§8▶ §7Max Employee: §8" + getMaxEmployeeLevelForLevel(level) + " ➜ " + getMaxEmployeeForLevel(level + 1));
                    lore.add("§8▶ §cRabbit Hitman §7Slots: §8" + getMaxHitmanSlotsForLevel(level) + " ➜ §6" + getMaxHitmanSlotsForLevel(level + 1));
                    lore.add("§8▶ §cRabbit Hitman §7progress is kept.");
                    lore.add("§8▶ §aHoppity's Collection §7progress is kept.");
                    lore.add("§8▶ §aRabbit Barn §7progress is kept.");
                    lore.add("§8▶ §b+" + getSkyBlockXpForLevel(level + 1) + " SkyBlock XP");
                    lore.add("");
                    lore.add("§7Chocolate this Prestige: §6" + StringUtility.commaify(factoryData.getPrestigeChocolate()));
                    lore.add("");

                    long required = ChocolateCalculator.calculatePrestigeRequirement(level);
                    if (factoryData.getPrestigeChocolate() >= required) {
                        lore.add("§eClick to prestige!");
                    } else {
                        lore.add("§cRequires " + ChocolateCalculator.formatNumber(required) + " Chocolate this");
                        lore.add("§cPrestige!");
                    }
                } else {
                    lore.add("");
                    lore.add("§aYou have reached the maximum level!");
                }

                return ItemStackCreator.getStack(
                        ChocolateCalculator.getFactoryLevelName(level),
                        Material.DROPPER,
                        1,
                        lore.toArray(new String[0])
                );
            }
        };
    }

    private GUIClickableItem createEmployeeItem(SkyBlockPlayer player, ChocolateFactoryData data, ChocolateEmployee employee, int slot) {
        return new GUIClickableItem(slot) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                ChocolateFactoryData factoryData = sbPlayer.getChocolateFactoryData();

                // Check if previous employee requirement is met
                if (!canHireEmployee(factoryData, employee)) {
                    sbPlayer.sendMessage("§cYou need to promote the previous employee first!");
                    return;
                }

                int currentLevel = factoryData.getEmployeeLevel(employee);
                long cost = ChocolateCalculator.calculateEmployeeUpgradeCost(employee, currentLevel, factoryData.getFactoryLevel());

                if (factoryData.removeChocolate(cost)) {
                    factoryData.setEmployeeLevel(employee, currentLevel + 1);
                    sbPlayer.getSkyblockDataHandler()
                            .get(SkyBlockDataHandler.Data.CHOCOLATE_FACTORY, DatapointChocolateFactory.class)
                            .setValue(factoryData);

                    sbPlayer.playSound(Sound.sound(Key.key("entity.player.levelup"), Sound.Source.PLAYER, 0.5f, 2f), Sound.Emitter.self());
                    refreshItems(sbPlayer);
                } else {
                    sbPlayer.sendMessage("§cYou don't have enough chocolate!");
                    sbPlayer.playSound(Sound.sound(Key.key("entity.villager.no"), Sound.Source.PLAYER, 1f, 1f), Sound.Emitter.self());
                }
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                ChocolateFactoryData factoryData = sbPlayer.getChocolateFactoryData();

                int level = factoryData.getEmployeeLevel(employee);

                // Check if employee is locked (previous employee not at level 20)
                if (!canHireEmployee(factoryData, employee)) {
                    List<String> lore = new ArrayList<>();
                    lore.add("§8§o" + employee.getFlavor() + "");
                    lore.add("");
                    ChocolateEmployee prev = getPreviousEmployee(employee);
                    if (prev != null) {
                        lore.add("§cRequires " + prev.getDisplayName() + " to be");
                        lore.add("§cpromoted to §7[20§7] §aEmployee§c.");
                    }

                    return ItemStackCreator.getStack(
                            "§c" + employee.getDisplayName(),
                            Material.GRAY_DYE,
                            1,
                            lore.toArray(new String[0])
                    );
                }

                // Employee is unlocked
                long cost = ChocolateCalculator.calculateEmployeeUpgradeCost(employee, level, factoryData.getFactoryLevel());
                int cps = employee.getCpsAtLevel(level);

                List<String> lore = new ArrayList<>();
                lore.add("§8§o" + employee.getFlavor() + "");
                lore.add("");

                if (level == 0) {
                    // Not hired yet
                    lore.add("§c" + employee.getDisplayName() + " §7is currently");
                    lore.add("§7fun-employed. They are looking for");
                    lore.add("§7a job at your §6Chocolate Factory§7,");
                    lore.add("§7and are capable of producing extra");
                    lore.add("§6Chocolate§7!");
                    lore.add("");
                    lore.add("§8§m-----------------");
                    lore.add("§a§lHIRE §8➜ §7[1§7] " + getEmployeeRankName(1));
                    lore.add("    §6+" + employee.getCpsPerLevel() + " Chocolate §8per second");
                    lore.add("");
                    lore.add("§7Cost");
                    lore.add("§6" + StringUtility.commaify(cost) + " Chocolate");
                    lore.add("");
                    if (factoryData.getChocolate() >= cost) {
                        lore.add("§eClick to hire!");
                    } else {
                        lore.add("§cNot enough Chocolate!");
                    }
                } else {
                    // Already hired - show promotion info
                    lore.add("§7" + employee.getDisplayName() + " produces §6Chocolate");
                    lore.add("§6§7for your factory!");
                    lore.add("");
                    lore.add("§7Current Production");
                    lore.add("§6" + cps + " Chocolate §8per second");
                    lore.add("");
                    lore.add("§8§m-----------------");
                    lore.add("§a§lPROMOTE §8➜ §7[" + (level + 1) + "§7] " + getEmployeeRankName(level + 1));
                    lore.add("    §6+" + employee.getCpsPerLevel() + " Chocolate §8per second");
                    lore.add("");
                    lore.add("§7Cost");
                    lore.add("§6" + StringUtility.commaify(cost) + " Chocolate");
                    lore.add("");
                    if (factoryData.getChocolate() >= cost) {
                        lore.add("§eClick to promote!");
                    } else {
                        lore.add("§cNot enough Chocolate!");
                    }
                }

                String name = (level == 0 ? "§c" : employee.getColorCode()) + employee.getDisplayName();
                if (level > 0) {
                    name += "§8 - §7[" + level + "§7] " + getEmployeeRankName(level);
                } else {
                    name += "§8 - §cUnemployed";
                }

                return ItemStackCreator.getStackHead(
                        name,
                        employee.getSkinTexture(),
                        Math.max(1, Math.min(64, level)),
                        lore.toArray(new String[0])
                );
            }
        };
    }

    private GUIClickableItem createRabbitBarnItem(SkyBlockPlayer player, ChocolateFactoryData data) {
        return new GUIClickableItem(35) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                ChocolateFactoryData factoryData = sbPlayer.getChocolateFactoryData();

                long cost = ChocolateCalculator.calculateRabbitBarnCost(factoryData.getRabbitBarnLevel());

                if (factoryData.removeChocolate(cost)) {
                    factoryData.setRabbitBarnLevel(factoryData.getRabbitBarnLevel() + 1);
                    sbPlayer.getSkyblockDataHandler()
                            .get(SkyBlockDataHandler.Data.CHOCOLATE_FACTORY, DatapointChocolateFactory.class)
                            .setValue(factoryData);

                    sbPlayer.playSound(Sound.sound(Key.key("entity.player.levelup"), Sound.Source.PLAYER, 0.5f, 2f), Sound.Emitter.self());
                    refreshItems(sbPlayer);
                } else {
                    sbPlayer.sendMessage("§cYou don't have enough chocolate!");
                }
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                ChocolateFactoryData factoryData = sbPlayer.getChocolateFactoryData();
                HoppityCollectionData collection = sbPlayer.getHoppityCollectionData();

                int level = factoryData.getRabbitBarnLevel();
                int capacity = factoryData.getRabbitBarnCapacity();
                int collected = collection.getUniqueRabbitCount();
                long cost = ChocolateCalculator.calculateRabbitBarnCost(level);

                List<String> lore = new ArrayList<>();
                lore.add("§7Your §aRabbit Barn §7can only hold so");
                lore.add("§7many §aChocolate Rabbits§7.");
                lore.add("");
                lore.add("§7If you try collecting more unique");
                lore.add("§7rabbits, they will run away!");
                lore.add("");
                lore.add("§7Your Barn: §a" + collected + "§7/§a" + capacity + " Rabbits");
                lore.add("§8§m-----------------");
                lore.add("§a§lUPGRADE §8➜ §aRabbit Barn " + StringUtility.getAsRomanNumeral(level + 2));
                lore.add("    §a+2 Capacity");
                lore.add("");
                lore.add("§7Cost");
                lore.add("§6" + StringUtility.commaify(cost) + " Chocolate");
                lore.add("");
                if (factoryData.getChocolate() >= cost) {
                    lore.add("§eClick to upgrade!");
                } else {
                    lore.add("§cNot enough Chocolate!");
                }

                return ItemStackCreator.getStack(
                        "§aRabbit Barn " + StringUtility.getAsRomanNumeral(level + 1),
                        Material.OAK_FENCE,
                        Math.max(1, Math.min(64, level + 1)),
                        lore.toArray(new String[0])
                );
            }
        };
    }

    private GUIClickableItem createHandBakedItem(SkyBlockPlayer player, ChocolateFactoryData data) {
        return new GUIClickableItem(38) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                ChocolateFactoryData factoryData = sbPlayer.getChocolateFactoryData();

                if (factoryData.getHandBakedLevel() >= 10) {
                    sbPlayer.sendMessage("§cHand-Baked Chocolate is already maxed!");
                    return;
                }

                long cost = ChocolateCalculator.calculateHandBakedCost(factoryData.getHandBakedLevel());

                if (factoryData.removeChocolate(cost)) {
                    factoryData.setHandBakedLevel(factoryData.getHandBakedLevel() + 1);
                    sbPlayer.getSkyblockDataHandler()
                            .get(SkyBlockDataHandler.Data.CHOCOLATE_FACTORY, DatapointChocolateFactory.class)
                            .setValue(factoryData);

                    sbPlayer.playSound(Sound.sound(Key.key("entity.player.levelup"), Sound.Source.PLAYER, 0.5f, 2f), Sound.Emitter.self());
                    refreshItems(sbPlayer);
                } else {
                    sbPlayer.sendMessage("§cYou don't have enough chocolate!");
                }
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                ChocolateFactoryData factoryData = sbPlayer.getChocolateFactoryData();

                int level = factoryData.getHandBakedLevel();
                int chocolatePerClick = factoryData.getChocolatePerClick();

                List<String> lore = new ArrayList<>();
                lore.add("§7A good boss can get down in the");
                lore.add("§7trenches and help out their");
                lore.add("§7workforce. In exchange for some");
                lore.add("§6Chocolate§7, you can increase the");
                lore.add("§7amount of §6Chocolate §7that you");
                lore.add("§7produce each time you click!");
                lore.add("");

                if (level < 10) {
                    long cost = ChocolateCalculator.calculateHandBakedCost(level);
                    lore.add("§a§lUPGRADE §8➜ §dHand-Baked Chocolate " + StringUtility.getAsRomanNumeral(level + 2));
                    lore.add("    §6+" + 2 + " Chocolate §8per click");
                    lore.add("");
                    lore.add("§7Cost");
                    lore.add("§6" + StringUtility.commaify(cost) + " Chocolate");
                    lore.add("");
                    if (factoryData.getChocolate() >= cost) {
                        lore.add("§eClick to upgrade!");
                    } else {
                        lore.add("§cNot enough Chocolate!");
                    }
                } else {
                    lore.add("§aMAXED OUT!");
                }

                return ItemStackCreator.getStack(
                        "§dHand-Baked Chocolate " + StringUtility.getAsRomanNumeral(level + 1),
                        Material.COOKIE,
                        Math.max(1, Math.min(64, level + 1)),
                        lore.toArray(new String[0])
                );
            }
        };
    }

    private GUIItem createTimeTowerItem(SkyBlockPlayer player, ChocolateFactoryData data) {
        return new GUIClickableItem(39) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                ChocolateFactoryData factoryData = sbPlayer.getChocolateFactoryData();

                if (factoryData.getFactoryLevel() < 2) {
                    sbPlayer.sendMessage("§cUnlock Chocolate Factory II to use this!");
                    return;
                }

                // Right click to activate, left click to upgrade
                if (e.getClick() instanceof Click.Right) {
                    // Activate Time Tower
                    if (factoryData.activateTimeTower()) {
                        sbPlayer.getSkyblockDataHandler()
                                .get(SkyBlockDataHandler.Data.CHOCOLATE_FACTORY, DatapointChocolateFactory.class)
                                .setValue(factoryData);

                        sbPlayer.sendMessage("§aTime Tower activated! Production boosted for 1 hour!");
                        sbPlayer.playSound(Sound.sound(Key.key("block.beacon.activate"), Sound.Source.PLAYER, 1f, 1f), Sound.Emitter.self());
                        refreshItems(sbPlayer);
                    } else {
                        sbPlayer.sendMessage("§cNo charges available or Time Tower is already active!");
                    }
                } else {
                    // Upgrade Time Tower
                    if (factoryData.getTimeTowerLevel() >= 15) {
                        sbPlayer.sendMessage("§cTime Tower is already maxed!");
                        return;
                    }

                    long cost = ChocolateCalculator.calculateTimeTowerCost(factoryData.getTimeTowerLevel(), factoryData.getFactoryLevel());

                    if (factoryData.removeChocolate(cost)) {
                        factoryData.setTimeTowerLevel(factoryData.getTimeTowerLevel() + 1);
                        sbPlayer.getSkyblockDataHandler()
                                .get(SkyBlockDataHandler.Data.CHOCOLATE_FACTORY, DatapointChocolateFactory.class)
                                .setValue(factoryData);

                        sbPlayer.playSound(Sound.sound(Key.key("entity.player.levelup"), Sound.Source.PLAYER, 0.5f, 2f), Sound.Emitter.self());
                        refreshItems(sbPlayer);
                    } else {
                        sbPlayer.sendMessage("§cYou don't have enough chocolate!");
                    }
                }
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                ChocolateFactoryData factoryData = sbPlayer.getChocolateFactoryData();

                List<String> lore = new ArrayList<>();

                if (factoryData.getFactoryLevel() < 2) {
                    lore.add("§7What does it do? Nobody knows...");
                    lore.add("");
                    lore.add("§cChocolate Factory II");
                    return ItemStackCreator.getStack("§c???", Material.GRAY_DYE, 1, lore.toArray(new String[0]));
                }

                int level = factoryData.getTimeTowerLevel();
                int charges = factoryData.getTimeTowerCharges();
                boolean active = factoryData.isTimeTowerActive();
                int bonusPercent = level * 10;

                lore.add("§7When activated, boosts chocolate");
                lore.add("§7production for §a1 hour§7!");
                lore.add("");
                lore.add("§7Level: §a" + level + "§7/§a15");
                lore.add("§7Bonus: §6+" + bonusPercent + "% §7production");
                lore.add("§7Charges: §a" + charges + "§7/§a3");
                lore.add("");

                if (active) {
                    long remaining = factoryData.getTimeTowerRemainingMs();
                    lore.add("§a§lACTIVE! §7" + formatTime(remaining) + " remaining");
                } else {
                    lore.add("§7Status: §cInactive");
                }

                lore.add("");
                if (level < 15) {
                    long cost = ChocolateCalculator.calculateTimeTowerCost(level, factoryData.getFactoryLevel());
                    lore.add("§7Upgrade Cost: §6" + StringUtility.commaify(cost));
                } else {
                    lore.add("§aMAXED OUT!");
                }
                lore.add("");
                lore.add("§eLeft-click to upgrade!");
                lore.add("§eRight-click to activate!");

                return ItemStackCreator.getStack(
                        "§bTime Tower",
                        active ? Material.CLOCK : Material.CLOCK,
                        Math.max(1, Math.min(64, level)),
                        lore.toArray(new String[0])
                );
            }
        };
    }

    private GUIItem createRabbitShrineItem(SkyBlockPlayer player, ChocolateFactoryData data) {
        return new GUIClickableItem(41) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                ChocolateFactoryData factoryData = sbPlayer.getChocolateFactoryData();

                if (factoryData.getFactoryLevel() < 3) {
                    sbPlayer.sendMessage("§cUnlock Chocolate Factory III to use this!");
                    return;
                }

                if (factoryData.getRabbitShrineLevel() >= 20) {
                    sbPlayer.sendMessage("§cRabbit Shrine is already maxed!");
                    return;
                }

                long cost = ChocolateCalculator.calculateRabbitShrineCost(factoryData.getRabbitShrineLevel());

                if (factoryData.removeChocolate(cost)) {
                    factoryData.setRabbitShrineLevel(factoryData.getRabbitShrineLevel() + 1);
                    sbPlayer.getSkyblockDataHandler()
                            .get(SkyBlockDataHandler.Data.CHOCOLATE_FACTORY, DatapointChocolateFactory.class)
                            .setValue(factoryData);

                    sbPlayer.playSound(Sound.sound(Key.key("entity.player.levelup"), Sound.Source.PLAYER, 0.5f, 2f), Sound.Emitter.self());
                    refreshItems(sbPlayer);
                } else {
                    sbPlayer.sendMessage("§cYou don't have enough chocolate!");
                }
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                ChocolateFactoryData factoryData = sbPlayer.getChocolateFactoryData();

                List<String> lore = new ArrayList<>();

                if (factoryData.getFactoryLevel() < 3) {
                    lore.add("§7What does it do? Nobody knows...");
                    lore.add("");
                    lore.add("§cChocolate Factory III");
                    return ItemStackCreator.getStack("§c???", Material.GRAY_DYE, 1, lore.toArray(new String[0]));
                }

                int level = factoryData.getRabbitShrineLevel();
                int bonusPercent = level * 5;

                lore.add("§7Increases the chance of finding");
                lore.add("§7higher rarity rabbits!");
                lore.add("");
                lore.add("§7Level: §a" + level + "§7/§a20");
                lore.add("§7Rarity Bonus: §d+" + bonusPercent + "%");
                lore.add("");

                if (level < 20) {
                    long cost = ChocolateCalculator.calculateRabbitShrineCost(level);
                    lore.add("§7Cost: §6" + StringUtility.commaify(cost) + " Chocolate");
                    lore.add("");
                    if (factoryData.getChocolate() >= cost) {
                        lore.add("§eClick to upgrade!");
                    } else {
                        lore.add("§cNot enough Chocolate!");
                    }
                } else {
                    lore.add("§aMAXED OUT!");
                }

                return ItemStackCreator.getStack(
                        "§dRabbit Shrine",
                        Material.RABBIT_FOOT,
                        Math.max(1, Math.min(64, level)),
                        lore.toArray(new String[0])
                );
            }
        };
    }

    private GUIItem createCoachJackrabbitItem(SkyBlockPlayer player, ChocolateFactoryData data) {
        return new GUIClickableItem(42) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                ChocolateFactoryData factoryData = sbPlayer.getChocolateFactoryData();

                if (factoryData.getFactoryLevel() < 4) {
                    sbPlayer.sendMessage("§cUnlock Chocolate Factory IV to use this!");
                    return;
                }

                if (factoryData.getCoachJackrabbitLevel() >= 20) {
                    sbPlayer.sendMessage("§cCoach Jackrabbit is already maxed!");
                    return;
                }

                long cost = ChocolateCalculator.calculateCoachJackrabbitCost(factoryData.getCoachJackrabbitLevel());

                if (factoryData.removeChocolate(cost)) {
                    factoryData.setCoachJackrabbitLevel(factoryData.getCoachJackrabbitLevel() + 1);
                    sbPlayer.getSkyblockDataHandler()
                            .get(SkyBlockDataHandler.Data.CHOCOLATE_FACTORY, DatapointChocolateFactory.class)
                            .setValue(factoryData);

                    sbPlayer.playSound(Sound.sound(Key.key("entity.player.levelup"), Sound.Source.PLAYER, 0.5f, 2f), Sound.Emitter.self());
                    refreshItems(sbPlayer);
                } else {
                    sbPlayer.sendMessage("§cYou don't have enough chocolate!");
                }
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                ChocolateFactoryData factoryData = sbPlayer.getChocolateFactoryData();

                List<String> lore = new ArrayList<>();

                if (factoryData.getFactoryLevel() < 4) {
                    lore.add("§7What does it do? Nobody knows...");
                    lore.add("");
                    lore.add("§cChocolate Factory IV");
                    return ItemStackCreator.getStack("§c???", Material.GRAY_DYE, 1, lore.toArray(new String[0]));
                }

                int level = factoryData.getCoachJackrabbitLevel();
                int bonusPercent = level;

                lore.add("§7Increases your total chocolate");
                lore.add("§7production multiplier!");
                lore.add("");
                lore.add("§7Level: §a" + level + "§7/§a20");
                lore.add("§7Multiplier Bonus: §6+" + bonusPercent + "%");
                lore.add("");

                if (level < 20) {
                    long cost = ChocolateCalculator.calculateCoachJackrabbitCost(level);
                    lore.add("§7Cost: §6" + StringUtility.commaify(cost) + " Chocolate");
                    lore.add("");
                    if (factoryData.getChocolate() >= cost) {
                        lore.add("§eClick to upgrade!");
                    } else {
                        lore.add("§cNot enough Chocolate!");
                    }
                } else {
                    lore.add("§aMAXED OUT!");
                }

                return ItemStackCreator.getStack(
                        "§6Coach Jackrabbit",
                        Material.GOLDEN_CARROT,
                        Math.max(1, Math.min(64, level)),
                        lore.toArray(new String[0])
                );
            }
        };
    }

    private GUIClickableItem createShopButton() {
        return new GUIClickableItem(47) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                new GUIChocolateShop().open(sbPlayer);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                List<String> lore = new ArrayList<>();
                lore.add("§7Spend your §6Chocolate §7on the world's");
                lore.add("§7finest goods from the §6Chocolate Shop§7.");
                lore.add("");
                lore.add("§eClick to view!");

                return ItemStackCreator.getStack("§6Chocolate Shop", Material.EMERALD, 1, lore.toArray(new String[0]));
            }
        };
    }

    private GUIClickableItem createBackButton() {
        return new GUIClickableItem(48) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                p.closeInventory();
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStackCreator.getStack(
                        "§aGo Back",
                        Material.ARROW,
                        1,
                        "§7To Calendar and Events"
                );
            }
        };
    }

    private GUIClickableItem createCloseButton() {
        return new GUIClickableItem(49) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                p.closeInventory();
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStackCreator.getStack("§cClose", Material.BARRIER, 1);
            }
        };
    }

    private GUIClickableItem createCollectionButton(SkyBlockPlayer player) {
        return new GUIClickableItem(50) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                new GUIHoppityCollection().open(sbPlayer);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                HoppityCollectionData collection = sbPlayer.getHoppityCollectionData();

                List<String> lore = new ArrayList<>();
                lore.add("§7Help §aHoppity §7find all of his §aChocolate");
                lore.add("§aRabbits §7during the §dHoppity's Hunt");
                lore.add("§d§7event!");
                lore.add("");
                lore.add("§7The more unique §aChocolate Rabbits");
                lore.add("§a§7that you find, the more your");
                lore.add("§6Chocolate Factory §7will produce!");
                lore.add("");
                lore.add("§7Finding duplicate Rabbits grants");
                lore.add("§a+10% §7extra §6Chocolate §7per duplicate,");
                lore.add("§7up to §a+100%§7!");
                lore.add("");
                lore.add("§7Rabbits Found: §e" + String.format("%.0f", collection.getCollectionPercentage()) + "§6%");
                lore.add("§f§l§m                         §r §e" + collection.getUniqueRabbitCount() + "§6/§e512");
                lore.add("");
                lore.add("§eClick to view!");

                return ItemStackCreator.getStackHead("§aHoppity's Collection", HOPPITY_TEXTURE, 1, lore.toArray(new String[0]));
            }
        };
    }

    private GUIClickableItem createHitmanButton(SkyBlockPlayer player) {
        return new GUIClickableItem(51) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                new GUIRabbitHitman().open(sbPlayer);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                ChocolateFactoryData factoryData = sbPlayer.getChocolateFactoryData();

                List<String> lore = new ArrayList<>();
                lore.add("§7Hire this private rabbit to hunt eggs");
                lore.add("§7for you, they will collect eggs you");
                lore.add("§7missed!");
                lore.add("");
                lore.add("§7Available eggs: §a0");
                lore.add("§7Purchased slots: §e0§7/§a" + getMaxHitmanSlotsForLevel(factoryData.getFactoryLevel()));
                lore.add("");
                lore.add("§eClick to view!");

                return ItemStackCreator.getStack("§cRabbit Hitman", Material.BOW, 1, lore.toArray(new String[0]));
            }
        };
    }

    private GUIItem createRankingButton(SkyBlockPlayer player, ChocolateFactoryData data) {
        return new GUIItem(52) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                ChocolateFactoryData factoryData = sbPlayer.getChocolateFactoryData();

                List<String> lore = new ArrayList<>();
                if (factoryData.getAllTimeChocolate() < 1000) {
                    lore.add("§7Ranking information requires §61,000");
                    lore.add("§6Chocolate §7or more.");
                } else {
                    lore.add("§7View your ranking among all");
                    lore.add("§7chocolate producers!");
                    lore.add("");
                    lore.add("§eClick to view!");
                }

                return ItemStackCreator.getStack("§dChocolate Factory Ranking", Material.MILK_BUCKET, 1, lore.toArray(new String[0]));
            }
        };
    }

    private GUIClickableItem createMilestonesButton(SkyBlockPlayer player, ChocolateFactoryData data) {
        return new GUIClickableItem(53) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                new GUIChocolateFactoryMilestones().open(sbPlayer);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                ChocolateFactoryData factoryData = sbPlayer.getChocolateFactoryData();
                int reached = FactoryMilestone.getMilestonesReached(factoryData.getAllTimeChocolate());

                List<String> lore = new ArrayList<>();
                lore.add("§7Unlock special §aChocolate Rabbits §7by");
                lore.add("§7reaching all-time §6Chocolate");
                lore.add("§6§7milestones!");
                lore.add("");
                lore.add("§7Milestones: §a" + reached + "§7/§a" + FactoryMilestone.values().length);
                lore.add("");
                lore.add("§eClick to view!");

                return ItemStackCreator.getStack("§6Chocolate Factory Milestones", Material.LADDER, 1, lore.toArray(new String[0]));
            }
        };
    }

    // Helper methods for prestige display
    private String getMaxRarityForLevel(int level) {
        return switch (level) {
            case 1 -> "§a§lUNCOMMON";
            case 2 -> "§9§lRARE";
            case 3 -> "§5§lEPIC";
            case 4 -> "§6§lLEGENDARY";
            case 5, 6 -> "§d§lMYTHIC";
            default -> "§f§lCOMMON";
        };
    }

    private String getMaxRarityNameForLevel(int level) {
        return switch (level) {
            case 1 -> "UNCOMMON";
            case 2 -> "RARE";
            case 3 -> "EPIC";
            case 4 -> "LEGENDARY";
            case 5, 6 -> "MYTHIC";
            default -> "COMMON";
        };
    }

    private String getMaxChocolateForLevel(int level) {
        return switch (level) {
            case 1 -> "500M";
            case 2 -> "1.2B";
            case 3 -> "5B";
            case 4 -> "15B";
            case 5 -> "100B";
            case 6 -> "∞";
            default -> "100M";
        };
    }

    private String getMaxEmployeeForLevel(int level) {
        return switch (level) {
            case 1 -> "§7[120§7] §9Assistant";
            case 2 -> "§7[140§7] §5Manager";
            case 3 -> "§7[160§7] §6Director";
            case 4 -> "§7[180§7] §dExecutive";
            case 5 -> "§7[200§7] §cCEO";
            case 6 -> "§7[∞§7] §c§lGOD";
            default -> "§7[100§7] §aEmployee";
        };
    }

    private String getMaxEmployeeLevelForLevel(int level) {
        return switch (level) {
            case 1 -> "[120]";
            case 2 -> "[140]";
            case 3 -> "[160]";
            case 4 -> "[180]";
            case 5 -> "[200]";
            case 6 -> "[∞]";
            default -> "[100]";
        };
    }

    private int getMaxHitmanSlotsForLevel(int level) {
        return switch (level) {
            case 1 -> 6;
            case 2 -> 12;
            case 3 -> 18;
            case 4 -> 24;
            case 5 -> 28;
            case 6 -> 28;
            default -> 6;
        };
    }

    private int getSkyBlockXpForLevel(int level) {
        return switch (level) {
            case 2 -> 25;
            case 3 -> 50;
            case 4 -> 100;
            case 5 -> 200;
            case 6 -> 500;
            default -> 0;
        };
    }

    private String getEmployeeRankName(int level) {
        if (level <= 0) return "§cUnemployed";
        if (level <= 20) return "§aEmployee";
        if (level <= 40) return "§9Assistant";
        if (level <= 60) return "§5Manager";
        if (level <= 80) return "§6Director";
        if (level <= 100) return "§dExecutive";
        return "§cCEO";
    }

    private boolean canHireEmployee(ChocolateFactoryData data, ChocolateEmployee employee) {
        ChocolateEmployee prev = getPreviousEmployee(employee);
        if (prev == null) return true; // First employee can always be hired
        return data.getEmployeeLevel(prev) >= 20;
    }

    private ChocolateEmployee getPreviousEmployee(ChocolateEmployee employee) {
        ChocolateEmployee[] employees = ChocolateEmployee.values();
        int index = employee.ordinal();
        if (index == 0) return null;
        return employees[index - 1];
    }

    private String formatTime(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        minutes %= 60;
        seconds %= 60;

        if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        } else {
            return seconds + "s";
        }
    }

    @Override
    public void refreshItems(HypixelPlayer p) {
        SkyBlockPlayer player = (SkyBlockPlayer) p;
        ChocolateFactoryData data = player.getChocolateFactoryData();

        // Refresh main items
        set(createChocolateHeadItem(player, data));
        set(createPrestigeItem(player, data));

        // Refresh employees
        int employeeSlot = 28;
        for (ChocolateEmployee employee : ChocolateEmployee.values()) {
            set(createEmployeeItem(player, data, employee, employeeSlot));
            employeeSlot++;
        }

        // Refresh upgrades
        set(createRabbitBarnItem(player, data));
        set(createHandBakedItem(player, data));
        set(createTimeTowerItem(player, data));
        set(createRabbitShrineItem(player, data));
        set(createCoachJackrabbitItem(player, data));

        // Refresh buttons
        set(createCollectionButton(player));
        set(createHitmanButton(player));
        set(createRankingButton(player, data));
        set(createMilestonesButton(player, data));
    }

    @Override
    public int refreshRate() {
        return 20; // Refresh every second (20 ticks)
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onClose(InventoryCloseEvent e, CloseReason reason) {
    }

    @Override
    public void suddenlyQuit(Inventory inventory, HypixelPlayer player) {
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        e.setCancelled(true);
    }
}
