package net.swofty.type.skyblockgeneric.gui.inventories.chocolatefactory;

import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.chocolatefactory.HoppityCollectionData;
import net.swofty.type.skyblockgeneric.chocolatefactory.rabbit.ChocolateRabbit;
import net.swofty.type.skyblockgeneric.chocolatefactory.rabbit.ChocolateRabbitRarity;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Hoppity Collection GUI - Paginated rabbit browser
 */
public class GUIHoppityCollection extends HypixelInventoryGUI {
    private static final String RABBIT_HEAD_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjBjZWZhNjdlNzU2NDJkMjY5MzM4MzY5ZTJmNjJlNzY3ZDQ5NDVjZTcyMjNjNmQyZTZlMGYxYTFhNjdkNjg0YyJ9fX0=";

    private final int page;
    private final ChocolateRabbitRarity filterRarity;
    private static final int ITEMS_PER_PAGE = 28;

    public GUIHoppityCollection() {
        this(1, null);
    }

    public GUIHoppityCollection(int page, ChocolateRabbitRarity filterRarity) {
        super("Hoppity's Collection" + (page > 1 ? " (Page " + page + ")" : ""), InventoryType.CHEST_6_ROW);
        this.page = page;
        this.filterRarity = filterRarity;
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(ItemStackCreator.createNamedItemStack(Material.BLACK_STAINED_GLASS_PANE, " "));
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        HoppityCollectionData collection = player.getHoppityCollectionData();

        // Back button
        set(new GUIClickableItem(49) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                new GUIChocolateFactory().open((SkyBlockPlayer) p);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStackCreator.getStack("§aGo Back", Material.ARROW, 1, "§7To Chocolate Factory");
            }
        });

        // Info item
        set(new GUIItem(4) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                HoppityCollectionData collectionData = sbPlayer.getHoppityCollectionData();

                List<String> lore = new ArrayList<>();
                lore.add("§7Collect rabbits from Hoppity's");
                lore.add("§7Hunt and the Chocolate Factory!");
                lore.add("");
                lore.add("§7Total Collected: §a" + collectionData.getUniqueRabbitCount() + "§7/§a" + ChocolateRabbit.getTotalRabbitCount());
                lore.add("§7Completion: §a" + String.format("%.1f%%", collectionData.getCollectionPercentage()));
                lore.add("");
                lore.add("§7Eggs Found: §a" + collectionData.getEggsFound());
                lore.add("§7Stray Rabbits: §a" + collectionData.getStrayRabbitsFound());
                lore.add("§7Golden Rabbits: §6" + collectionData.getGoldenRabbitsFound());

                return ItemStackCreator.getStack("§dHoppity's Collection", Material.RABBIT_HIDE, 1, lore.toArray(new String[0]));
            }
        });

        // Rarity filter buttons
        int filterSlot = 45;
        set(new GUIClickableItem(filterSlot) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                new GUIHoppityCollection(1, null).open((SkyBlockPlayer) p);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStackCreator.getStack(
                        filterRarity == null ? "§aAll Rabbits §7(Selected)" : "§7All Rabbits",
                        Material.PAPER, 1,
                        "§7Show all rabbits",
                        "",
                        "§eClick to filter!"
                );
            }
        });

        // Rarity filters
        ChocolateRabbitRarity[] rarities = {ChocolateRabbitRarity.COMMON, ChocolateRabbitRarity.UNCOMMON,
                ChocolateRabbitRarity.RARE, ChocolateRabbitRarity.EPIC, ChocolateRabbitRarity.LEGENDARY,
                ChocolateRabbitRarity.MYTHIC, ChocolateRabbitRarity.DIVINE};

        for (int i = 0; i < rarities.length && filterSlot + i + 1 < 49; i++) {
            ChocolateRabbitRarity rarity = rarities[i];
            int slot = 46 + i;
            if (slot >= 49) break;

            set(new GUIClickableItem(slot) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    new GUIHoppityCollection(1, rarity).open((SkyBlockPlayer) p);
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                    HoppityCollectionData collectionData = sbPlayer.getHoppityCollectionData();
                    int collected = collectionData.getUniqueRabbitCountByRarity(rarity);
                    int total = ChocolateRabbit.getRabbitCountByRarity(rarity);

                    boolean selected = filterRarity == rarity;
                    String name = (selected ? "§a" : "§7") + rarity.getFormattedName() + (selected ? " §7(Selected)" : "");

                    return ItemStackCreator.getStack(name, getMaterialForRarity(rarity), 1,
                            "§7Collected: §a" + collected + "§7/§a" + total,
                            "",
                            "§eClick to filter!"
                    );
                }
            });
        }

        // Get filtered rabbits
        List<ChocolateRabbit> allRabbits = new ArrayList<>();
        if (filterRarity != null) {
            allRabbits.addAll(ChocolateRabbit.getRabbitsByRarity(filterRarity));
        } else {
            allRabbits.addAll(ChocolateRabbit.getAllRabbits());
        }

        // Sort by rarity (highest first) then name
        allRabbits.sort((a, b) -> {
            int rarityCompare = Integer.compare(b.getRarity().getRarityValue(), a.getRarity().getRarityValue());
            if (rarityCompare != 0) return rarityCompare;
            return a.getName().compareTo(b.getName());
        });

        int totalPages = (int) Math.ceil((double) allRabbits.size() / ITEMS_PER_PAGE);
        int startIndex = (page - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allRabbits.size());

        // Display rabbits (slots 10-16, 19-25, 28-34, 37-43)
        int[] slots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        for (int i = 0; i < slots.length && startIndex + i < endIndex; i++) {
            ChocolateRabbit rabbit = allRabbits.get(startIndex + i);
            int slot = slots[i];

            set(new GUIItem(slot) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    SkyBlockPlayer sbPlayer = (SkyBlockPlayer) p;
                    HoppityCollectionData collectionData = sbPlayer.getHoppityCollectionData();
                    boolean collected = collectionData.hasRabbit(rabbit.getId());
                    int duplicates = collectionData.getDuplicateCount(rabbit.getId());

                    List<String> lore = new ArrayList<>();
                    lore.add(rabbit.getRarity().getFormattedName());
                    lore.add("");
                    lore.add("§7" + rabbit.getDescription());
                    lore.add("");
                    lore.add("§7CpS Bonus: §6+" + rabbit.getBaseCps());
                    lore.add("§7Multiplier: §6+" + String.format("%.3f", rabbit.getMultiplierBonus()));

                    if (rabbit.getResidentIsland() != null) {
                        lore.add("");
                        lore.add("§7Found on: §a" + rabbit.getResidentIsland());
                    }

                    lore.add("");
                    if (collected) {
                        lore.add("§a§lCOLLECTED!");
                        if (duplicates > 0) {
                            lore.add("§7Duplicates: §a" + duplicates);
                        }
                    } else {
                        lore.add("§c§lNOT COLLECTED");
                        lore.add("§7" + rabbit.getRequirement().getDisplayDescription());
                    }

                    String name = collected ? rabbit.getFormattedName() : "§c???";

                    if (collected) {
                        return ItemStackCreator.getStackHead(name, rabbit.getSkinTexture(), 1, lore.toArray(new String[0]));
                    } else {
                        return ItemStackCreator.getStack(name, Material.GRAY_DYE, 1, lore.toArray(new String[0]));
                    }
                }
            });
        }

        // Previous page button
        if (page > 1) {
            set(new GUIClickableItem(48) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    new GUIHoppityCollection(page - 1, filterRarity).open((SkyBlockPlayer) p);
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return ItemStackCreator.getStack("§aPrevious Page", Material.ARROW, 1,
                            "§7Page " + (page - 1) + "/" + totalPages);
                }
            });
        }

        // Next page button
        if (page < totalPages) {
            set(new GUIClickableItem(50) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    new GUIHoppityCollection(page + 1, filterRarity).open((SkyBlockPlayer) p);
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return ItemStackCreator.getStack("§aNext Page", Material.ARROW, 1,
                            "§7Page " + (page + 1) + "/" + totalPages);
                }
            });
        }

        // Page indicator
        set(new GUIItem(53) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStackCreator.getStack("§ePage " + page + "/" + totalPages, Material.BOOK, page);
            }
        });
    }

    private Material getMaterialForRarity(ChocolateRabbitRarity rarity) {
        return switch (rarity) {
            case COMMON -> Material.WHITE_DYE;
            case UNCOMMON -> Material.LIME_DYE;
            case RARE -> Material.LIGHT_BLUE_DYE;
            case EPIC -> Material.PURPLE_DYE;
            case LEGENDARY -> Material.ORANGE_DYE;
            case MYTHIC -> Material.PINK_DYE;
            case DIVINE -> Material.CYAN_DYE;
        };
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
