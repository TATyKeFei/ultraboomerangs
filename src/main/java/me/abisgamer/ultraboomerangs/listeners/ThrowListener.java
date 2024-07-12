package me.abisgamer.ultraboomerangs.listeners;

import me.abisgamer.ultraboomerangs.handlers.BoomerangHandler;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;


public class ThrowListener implements Listener {
    private final BoomerangHandler boomerangHandler;

    public ThrowListener(ConfigurationSection config, boolean updateOldBoomerangs, boolean isMcMMO, boolean isAuraSkills) {
        this.boomerangHandler = new BoomerangHandler(config, updateOldBoomerangs, isMcMMO, isAuraSkills);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        boomerangHandler.handleInteract(event);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        boomerangHandler.handlePlayerDropItem(event);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        boomerangHandler.handlePlayerQuit(event);
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        boomerangHandler.handlePluginDisable(event);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        boomerangHandler.handleEntityDamageByEntity(event);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        boomerangHandler.handleEntityDeath(event);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        boomerangHandler.handleInventoryClick(event);
    }
}
