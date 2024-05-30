package me.abisgamer.ultraboomerangs.listeners;

import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.net.http.WebSocket;

import static me.abisgamer.ultraboomerangs.listeners.throwListener.playerBoomer;

public class mcMMOListener implements Listener {



    @EventHandler
    public void onMcMMOPlayerXpGain(McMMOPlayerXpGainEvent event) {
        Player player = event.getPlayer();

        // Check if the player is in the playerBoomer hashmap
        if (playerBoomer.containsKey(player) && event.getSkill().equals(PrimarySkillType.UNARMED )) {
            //UltraBoomerangs.plugin.getLogger().info("Cancelling XP for player: " + player.getName() + " due to active boomerang");
            event.setCancelled(true);
        } else {
            //UltraBoomerangs.plugin.getLogger().info("We good, no need to cancel XP. Player does not have an active boomerang");
        }
    }

}
