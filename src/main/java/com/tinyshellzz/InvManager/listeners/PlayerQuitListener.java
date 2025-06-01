package com.tinyshellzz.InvManager.listeners;

import com.tinyshellzz.InvManager.config.PluginConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import static com.tinyshellzz.InvManager.ObjectPool.currentEnderChestMapper;
import static com.tinyshellzz.InvManager.ObjectPool.currentInventoryMapper;

public class PlayerQuitListener implements Listener {
    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if(currentInventoryMapper.exists(player.getUniqueId())) {
            if(PluginConfig.debug) Bukkit.getConsoleSender().sendMessage("玩家离开，保存背包数据");
            currentInventoryMapper.update(player);
        } else {
            currentInventoryMapper.insert(player);
        }

        if(currentEnderChestMapper.exists(player.getUniqueId())) {
            currentInventoryMapper.update(player);
        } else {
            currentEnderChestMapper.insert(player);
        }
    }
}
