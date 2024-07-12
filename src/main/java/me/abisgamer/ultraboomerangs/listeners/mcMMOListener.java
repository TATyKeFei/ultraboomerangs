package me.abisgamer.ultraboomerangs.listeners;

import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;
import me.abisgamer.ultraboomerangs.handlers.BoomerangHandler;
import me.abisgamer.ultraboomerangs.utils.ItemUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.configuration.file.FileConfiguration;

import me.abisgamer.ultraboomerangs.UltraBoomerangs;

public class mcMMOListener implements Listener {

    private final UltraBoomerangs plugin;
    private final ItemUtils itemUtilsInstance;

    public mcMMOListener(UltraBoomerangs plugin, ItemUtils itemUtilsInstance) {
        this.plugin = plugin;
        this.itemUtilsInstance = itemUtilsInstance;
    }

    @EventHandler
    public void onMcMMOPlayerXpGain(McMMOPlayerXpGainEvent event) {
        Player player = event.getPlayer();
        FileConfiguration config = plugin.getConfig();

        // Check if the player is in the playerBoomer hashmap
        if (BoomerangHandler.playerBoomer.containsKey(player)) {
            String boomerangKey = itemUtilsInstance.getBoomerangKeyWithPlayer(player);
            String configPath = "boomerangs." + boomerangKey + ".mcmmo_skill";
            String configuredSkill = config.getString(configPath, "none");
            //plugin.getLogger().info("configSkill: " + configuredSkill);

            if (!configuredSkill.equalsIgnoreCase("none")) {
                PrimarySkillType skillType = PrimarySkillType.getSkill(configuredSkill);
                if (skillType != null) {
                    // Cancel event if the configured skill is not unarmed and the event skill is unarmed
                    if (event.getSkill().equals(PrimarySkillType.UNARMED) && !configuredSkill.equalsIgnoreCase("unarmed")) {
                        event.setCancelled(true);
                        //plugin.getLogger().info("Cancelling unarmed XP for player: " + player.getName() + " due to active boomerang with skill: " + configuredSkill);
                    } else if (event.getSkill().equals(skillType)) {
                        //plugin.getLogger().info("Allowing XP gain for player: " + player.getName() + " with skill: " + configuredSkill);
                    }
                }
            }
        }
    }
}
