package me.abisgamer.ultraboomerangs.utils;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.skill.Skills;
import dev.aurelium.auraskills.api.user.SkillsUser;
import me.abisgamer.ultraboomerangs.UltraBoomerangs;
import org.bukkit.entity.Player;

public class auraSkillsHelper {

    public static void addAuraSkillsEXP(Player player, String skill, int xpAmount) {

        AuraSkillsApi auraSkills = AuraSkillsApi.get();
        SkillsUser user = auraSkills.getUser(player.getUniqueId());
        UltraBoomerangs.plugin.getLogger().info("Got to add exp");

        if ("none".equalsIgnoreCase(skill) || player == null || skill == null || xpAmount <= 0) {
            return; // Do nothing if the skill is set to "none" or parameters are invalid
        }

        try {
            Skills skillEnum = Skills.valueOf(skill.toUpperCase()); // Convert the skill string to enum
            user.addSkillXp(skillEnum, xpAmount); // Add XP to the specified skill
            UltraBoomerangs.plugin.getLogger().info("Added XP");
        } catch (IllegalArgumentException e) {
            // Handle the case where the skill string does not match any enum constant
            UltraBoomerangs.plugin.getLogger().info("Invalid skill name: " + skill);
        }
    }
}