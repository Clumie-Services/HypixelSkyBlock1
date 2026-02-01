package net.swofty.type.hub.npcs;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.NPCOption;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.hub.gui.GUIMaxwell;
import net.swofty.type.skyblockgeneric.collection.CustomCollectionAward;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class NPCMaxwell extends HypixelNPC {

    public NPCMaxwell() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"§6Thaumaturgist", "§9Maxwell", "§e§lCLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "CALy0nHX/3+BBjSHm3DEfAUEkDJKHU+jgvi3iS9Z3SCxwTGUaP9DiL0Izyc2MJth3B4Qxe9CwYzJ2l5KFBpHu52KTGWyv/cwyJn4GxZ+rDeOkCqeK1aWN7hzOQsZH1kYQ5Xf/KgeF0oIhOgCpexYtIUsU+bERMcl4QCVeogZ6Ewzvyux3tdLTw/TjYDpuAgW+594QtcjKYKE1lFb51DrHxl/c38nddaAPi8Ss4rPe/O3gUp5NVLNnaJ3+hZ8rsOhkEk3YwFCEBLMZ2xlAFm3s8yyILCzT4huZ8lqmZDj3xUM5bFKwl+iGdjfvZyrZUDNr/+zf2ZwhGllzIfttWm6y8xwzwv6JIqu/XTo+taohn7MPZGmJVt0nkrpmrXNBeZAK/g6pvJzNvqiNugXIaokcLsNYzTSKsV++/qbz7ZIaWqsImB7efH6/7fcz30JUga+wdnOMauISONzjvsQOzcGKHTHpaNP402IBjVa3hu0lIDxLwfhm4JYa/RfNGdQwJV8L3G7Vu3EdGMmu28MhfRtmge8CTI5cCf5Jt40HSr4rlTPM+PG2uq6iruOCoK1vtNKbqJubcuNO0AYWfXInlHB/NzrwZ9blemPe4/Lmack8skjrsTxzoM/X6Ze3cBIvOCLtUN8s+v0V4RAkyiA38eR4Z6cLNqVLiGbc3zCTBic34s=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTY0OTM1MTIyNzU1NywKICAicHJvZmlsZUlkIiA6ICIwNjNhMTc2Y2RkMTU0ODRiYjU1MjRhNjQyMGM1YjdhNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJkYXZpcGF0dXJ5IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzQ0Y2RmNGE4NDJjMWRjZTNhZDJjZmFlNDAzNjZlNTM5ZTlmNGYwNTdhMzliZGI3NzdlM2MxOWYyZWE2ODZlODIiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(46.5, 69, -34.5, 180, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        if (isInDialogue(player)) return;

        if (!player.hasCustomCollectionAward(CustomCollectionAward.ACCESSORY_BAG)) {
            setDialogue(player, "no-bag");
            return;
        }

        setDialogue(player, "intro").thenRun(() -> {
            NPCOption.sendOption(player, "maxwell", true, List.of(
                    new NPCOption.Option(
                            "ok_then_what",
                            NamedTextColor.GREEN,
                            false,
                            "Ok, then what?",
                            (p) -> {
                                setDialogue(p, "magical-power").thenRun(() -> {
                                    NPCOption.sendOption(p, "maxwell", true, List.of(
                                            new NPCOption.Option(
                                                    "magical_power",
                                                    NamedTextColor.GREEN,
                                                    false,
                                                    "Magical power?",
                                                    (p2) -> {
                                                        setDialogue(p2, "stats").thenRun(() -> {
                                                            NPCOption.sendOption(p2, "maxwell", true, List.of(
                                                                    new NPCOption.Option(
                                                                            "thats_amazing",
                                                                            NamedTextColor.GREEN,
                                                                            false,
                                                                            "That's amazing!",
                                                                            (p3) -> {
                                                                                setDialogue(p3, "finale").thenRun(() -> {
                                                                                    new GUIMaxwell().open(p3);
                                                                                });
                                                                            }
                                                                    )
                                                            ));
                                                        });
                                                    }
                                            )
                                    ));
                                });
                            }
                    )
            ));
        });
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[] {
                DialogueSet.builder()
                        .key("no-bag")
                        .lines(new String[]{
                                "Accessories are §6§kX§6 magical §kX§f pieces of gear.",
                                "To truly harness their power, you need an §aAccessory Bag§f!",
                                "Unlock it from the §cRedstone §fcollection.",
                                "We can talk after that!"
                        })
                        .sound(Sound.sound()
                                .type(Key.key("entity.villager.yes"))
                                .pitch(0.70f)
                                .build())
                        .build(),
                DialogueSet.builder()
                        .key("intro")
                        .lines(new String[]{
                                "Accessories are §6§kX§6 magical §kX§f pieces of gear.",
                                "To truly harness their power, collect as many as possible and store them in your §aAccessory Bag§f!"
                        })
                        .sound(Sound.sound()
                                .type(Key.key("entity.villager.yes"))
                                .pitch(0.70f)
                                .build())
                        .build(),
                DialogueSet.builder()
                        .key("magical-power")
                        .lines(new String[]{
                                "On top of their existing abilities, each accessory makes your §aAccessory Bag §fmore powerful!",
                                "Accessories add some §6§lMAGICAL POWER §fto the bag depending on their §drarity§f."
                        })
                        .build(),
                DialogueSet.builder()
                        .key("stats")
                        .lines(new String[]{
                                "Yes! The more §6Magical Power§f, the more stats like §c❤ Health §for §b✎ Intelligence §fyou get from your §aAccessory Bag§f."
                        })
                        .build(),
                DialogueSet.builder()
                        .key("finale")
                        .lines(new String[]{
                                "No, it's §6magic§f!",
                                "Even better, YOU choose what stats you get!",
                                "Check it out!"
                        })
                        .build()
        };
    }
}
