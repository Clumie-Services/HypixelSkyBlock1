package net.swofty.type.skyblockgeneric.entity;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.item.ItemEntityMeta;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.scoreboard.TeamBuilder;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class DroppedItemEntityImpl extends Entity {
    @Getter
    private static final Map<SkyBlockPlayer, List<DroppedItemEntityImpl>> droppedItems = new HashMap<>();
    private static final Map<Rarity, Team> rarityTeams = new ConcurrentHashMap<>();

    private final SkyBlockPlayer player;
    private final long endPickupDelay;
    private final Rarity rarity;
    @Setter
    private boolean giveCollection;

    public DroppedItemEntityImpl(SkyBlockItem item, SkyBlockPlayer player) {
        super(EntityType.ITEM);

        this.player = player;
        this.endPickupDelay = System.currentTimeMillis() + 500;
        this.rarity = item.getAttributeHandler().getRarity();

        ItemEntityMeta meta = (ItemEntityMeta) this.entityMeta;
        meta.setItem(new NonPlayerItemUpdater(item.getItemStack()).getUpdatedItem().build());

        // Enable glowing effect
        setGlowing(true);

        setAutoViewable(false);
        this.scheduleRemove(Duration.ofSeconds(60));

        droppedItems.computeIfPresent(player, (key, value) -> {
            if (value.size() > 50) {
                value.getFirst().remove();
            }
            value.add(this);
            return value;
        });
        droppedItems.putIfAbsent(player, new ArrayList<>(List.of(this)));
    }

    /**
     * Gets the team color for a given rarity.
     */
    private static NamedTextColor getRarityColor(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> NamedTextColor.WHITE;
            case UNCOMMON -> NamedTextColor.GREEN;
            case RARE -> NamedTextColor.BLUE;
            case EPIC -> NamedTextColor.DARK_PURPLE;
            case LEGENDARY -> NamedTextColor.GOLD;
            case MYTHIC -> NamedTextColor.LIGHT_PURPLE;
            case DIVINE -> NamedTextColor.AQUA;
            case SPECIAL, VERY_SPECIAL -> NamedTextColor.RED;
            case ADMIN -> NamedTextColor.DARK_RED;
        };
    }

    /**
     * Gets or creates a team for the given rarity.
     */
    private static Team getOrCreateRarityTeam(Rarity rarity) {
        return rarityTeams.computeIfAbsent(rarity, r -> {
            NamedTextColor color = getRarityColor(r);
            return new TeamBuilder("drop_" + r.name().toLowerCase(), MinecraftServer.getTeamManager())
                    .teamColor(color)
                    .build();
        });
    }

    @Override
    public void spawn() {
        super.spawn();
        addViewer(player);

        // Apply rarity-based glow color via team
        Team team = getOrCreateRarityTeam(rarity);
        team.addMember(this.getUuid().toString());
    }

    public SkyBlockItem getItem() {
        return new SkyBlockItem(((ItemEntityMeta) this.entityMeta).getItem());
    }
}
