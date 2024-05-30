package me.abisgamer.ultraboomerangs.utils;

import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.api.exceptions.McMMOPlayerNotFoundException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

public class McMMOHelper {
    private static final List<String> VALID_SKILLS = Arrays.asList(
            "ACROBATICS", "ARCHERY", "AXES", "EXCAVATION", "FISHING", "HERBALISM",
            "MINING", "REPAIR", "SALVAGE", "SMELTING", "SWORDS", "TAMING",
            "UNARMED", "WOODCUTTING");

    public static void addMcMMOExperience(Player player, String skill, int xpAmount, String reason) {
        if ("none".equalsIgnoreCase(skill) || player == null || skill == null || xpAmount <= 0) {
            return; // Do nothing if the skill is set to "none" or parameters are invalid
        }

        skill = skill.toUpperCase(); // Ensure skill is in uppercase

        if (!VALID_SKILLS.contains(skill)) {
            Bukkit.getLogger().severe("Invalid mcMMO skill type: " + skill);
            return;
        }

        try {
            Bukkit.getLogger().info("Adding " + xpAmount + " XP to skill " + skill + " for player " + player.getName());
            ExperienceAPI.addRawXP(player, skill, xpAmount, reason);
        } catch (McMMOPlayerNotFoundException e) {
            // Schedule a retry if the player's profile is not yet loaded
            String finalSkill = skill;
            Bukkit.getScheduler().runTaskLater(JavaPlugin.getProvidingPlugin(McMMOHelper.class), () -> {
                try {
                    Bukkit.getLogger().info("Retrying: Adding " + xpAmount + " XP to skill " + finalSkill + " for player " + player.getName());
                    ExperienceAPI.addRawXP(player, finalSkill, xpAmount, reason);
                } catch (McMMOPlayerNotFoundException ex) {
                    ex.printStackTrace(); // Log the error if it still fails
                }
            }, 20L); // 20 ticks = 1 second delay
        } catch (ClassCastException e) {
            Bukkit.getLogger().severe("ClassCastException: Ensure mcMMO is properly loaded and not duplicated.");
            e.printStackTrace();
        }
    }
}
