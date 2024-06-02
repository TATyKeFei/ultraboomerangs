package me.abisgamer.ultraboomerangs.listeners;

import dev.aurelium.auraskills.api.event.skill.XpGainEvent;
import dev.aurelium.auraskills.api.skill.Skills;
import me.abisgamer.ultraboomerangs.UltraBoomerangs;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.configuration.file.FileConfiguration;

public class auraSkillsListener implements Listener {

    private final UltraBoomerangs plugin;
    private final throwListener throwListenerInstance;

    public auraSkillsListener(UltraBoomerangs plugin, throwListener throwListenerInstance) {
        this.plugin = plugin;
        this.throwListenerInstance = throwListenerInstance;
    }

    @EventHandler
    public void onAuraSkillsPlayerXpGain(XpGainEvent event) {
        Player player = event.getPlayer();
        FileConfiguration config = plugin.getConfig();

        // Check if the player is in the playerBoomer hashmap
        if (throwListener.playerBoomer.containsKey(player)) {
            String boomerangKey = throwListenerInstance.getBoomerangKey(player);
            String configPath = "boomerangs." + boomerangKey + ".auraskills_skill";
            String configuredSkill = config.getString(configPath, "none");

            if (!configuredSkill.equalsIgnoreCase("none")) {
                try {
                    Skills skillType = Skills.valueOf(configuredSkill.toUpperCase());

                    if (!event.getSkill().equals(skillType)) {
                        event.setCancelled(true);
                        //plugin.getLogger().info("Cancelling XP for player: " + player.getName() + " due to active boomerang with skill: " + configuredSkill);
                    }
                } catch (IllegalArgumentException e) {
                    //plugin.getLogger().warning("Skill type " + configuredSkill + " is not valid.");
                }
            }
        }
    }
}
