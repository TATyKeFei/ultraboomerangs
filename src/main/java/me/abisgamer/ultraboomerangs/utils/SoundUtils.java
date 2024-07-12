package me.abisgamer.ultraboomerangs.utils;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class SoundUtils {
    public static void playThrowSound(Player player, ConfigurationSection soundSection) {
        if (soundSection != null && soundSection.getBoolean("enabled")) {
            String throwSoundName = soundSection.getString("throw-sound");
            float volume = (float) soundSection.getDouble("volume");
            float pitch = (float) soundSection.getDouble("pitch");
            if (throwSoundName != null) {
                Sound throwSound = Sound.valueOf(throwSoundName);
                player.playSound(player.getLocation(), throwSound, volume, pitch);
            }
        }
    }

    public static void playReceiveSound(Player player, ConfigurationSection soundSection) {
        if (soundSection != null && soundSection.getBoolean("enabled")) {
            String receiveSoundName = soundSection.getString("receive-sound");
            float volume = (float) soundSection.getDouble("volume");
            float pitch = (float) soundSection.getDouble("pitch");
            if (receiveSoundName != null) {
                Sound receiveSound = Sound.valueOf(receiveSoundName);
                player.playSound(player.getLocation(), receiveSound, volume, pitch);
            }
        }
    }
}
