package com.tinyshellzz.InvManager.listeners;

import com.tinyshellzz.InvManager.config.PluginConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import static com.tinyshellzz.InvManager.NeoTccInventoryManager.enderChestMap;
import static com.tinyshellzz.InvManager.NeoTccInventoryManager.inventoryMap;
import static com.tinyshellzz.InvManager.ObjectPool.currentEnderChestMapper;
import static com.tinyshellzz.InvManager.ObjectPool.currentInventoryMapper;

public class PlayerQuitListener implements Listener {
    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        currentInventoryMapper.update(player);
        currentEnderChestMapper.update(player);
        inventoryMap.remove(player.getUniqueId());
        enderChestMap.remove(player.getUniqueId());
    }

    @EventHandler
    public void playerKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        currentInventoryMapper.update(player);
        currentEnderChestMapper.update(player);
        inventoryMap.remove(player.getUniqueId());
        enderChestMap.remove(player.getUniqueId());
    }
}
