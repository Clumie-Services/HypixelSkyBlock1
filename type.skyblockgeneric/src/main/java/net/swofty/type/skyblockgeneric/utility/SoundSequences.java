package net.swofty.type.skyblockgeneric.utility;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.generic.user.HypixelPlayer;

/**
 * Utility class for playing predefined sound sequences.
 */
public class SoundSequences {

    /**
     * Sound data record for defining individual sounds in a sequence.
     */
    private record SoundData(long delayMs, String sound, float volume, float pitch) {}

    /**
     * The stat unlock sound sequence - plays when a player discovers a new stat.
     */
    private static final SoundData[] STAT_UNLOCK_SEQUENCE = {
            new SoundData(0, "mob/chicken/plop", 0.60f, 1.08f),
            new SoundData(0, "mob/chicken/plop", 0.60f, 1.03f),
            new SoundData(0, "mob/chicken/plop", 0.60f, 0.97f),
            new SoundData(0, "mob/chicken/plop", 0.60f, 0.89f),
            new SoundData(458, "mob/chicken/plop", 0.60f, 1.11f),
            new SoundData(592, "mob/chicken/plop", 0.60f, 1.10f),
            new SoundData(699, "mob/chicken/plop", 0.60f, 1.08f),
            new SoundData(1000, "mob/chicken/plop", 0.60f, 1.05f),
            new SoundData(1449, "mob/chicken/plop", 0.60f, 1.02f),
            new SoundData(1893, "mob/chicken/plop", 0.60f, 1.02f),
            new SoundData(2001, "mob/chicken/plop", 0.60f, 1.02f),
            new SoundData(2002, "mob/chicken/plop", 0.60f, 1.02f),
            new SoundData(2154, "mob/chicken/plop", 0.60f, 1.02f),
            new SoundData(2342, "mob/chicken/plop", 0.60f, 1.06f),
            new SoundData(2508, "mob/chicken/plop", 0.60f, 1.13f),
            new SoundData(2651, "mob/chicken/plop", 0.60f, 1.19f),
            new SoundData(3248, "mob/chicken/plop", 0.60f, 1.29f),
            new SoundData(3605, "mob/chicken/plop", 0.60f, 1.35f),
            new SoundData(3854, "mob/chicken/plop", 0.60f, 1.41f),
            new SoundData(3993, "mob/chicken/plop", 0.60f, 1.43f),
            new SoundData(4042, "mob/chicken/plop", 0.60f, 1.41f),
            new SoundData(4043, "mob/chicken/plop", 0.60f, 1.43f),
            new SoundData(4043, "mob/chicken/plop", 0.60f, 1.35f),
            new SoundData(4043, "mob/chicken/plop", 0.60f, 1.29f),
            new SoundData(4158, "mob/chicken/plop", 0.60f, 1.44f),
            new SoundData(4399, "mob/chicken/plop", 0.60f, 1.43f)
    };

    /**
     * Plays the stat unlock sound sequence for the given player.
     *
     * @param player The player to play the sound for
     */
    public static void playStatUnlockSound(HypixelPlayer player) {
        playSoundSequence(player, STAT_UNLOCK_SEQUENCE);
    }

    /**
     * Plays a sound sequence for a player.
     *
     * @param player The player to play the sounds for
     * @param sequence The sound sequence to play
     */
    private static void playSoundSequence(HypixelPlayer player, SoundData[] sequence) {
        for (SoundData data : sequence) {
            if (data.delayMs() == 0) {
                // Play immediately
                playSound(player, data);
            } else {
                // Schedule for later
                MinecraftServer.getSchedulerManager().scheduleTask(() -> {
                    if (player.isOnline()) {
                        playSound(player, data);
                    }
                }, TaskSchedule.millis(data.delayMs()), TaskSchedule.stop());
            }
        }
    }

    /**
     * Plays a single sound for a player.
     */
    private static void playSound(HypixelPlayer player, SoundData data) {
        player.playSound(Sound.sound()
                .type(Key.key(data.sound()))
                .source(Sound.Source.MASTER)
                .volume(data.volume())
                .pitch(data.pitch())
                .build());
    }
}
