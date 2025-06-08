package com.tinyshellzz.InvManager.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static com.tinyshellzz.InvManager.ObjectPool.currentEnderChestMapper;
import static com.tinyshellzz.InvManager.ObjectPool.currentInventoryMapper;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        currentInventoryMapper.load_current_content(player);
        currentEnderChestMapper.load_current_content(player);

        if(!currentInventoryMapper.exists(player.getUniqueId())) {
            currentInventoryMapper.insert(player);
        }
        if(!currentEnderChestMapper.exists(player.getUniqueId())) {
            currentEnderChestMapper.insert(player);
        }
    }
}
