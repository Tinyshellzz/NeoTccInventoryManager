package com.tinyshellzz.InvManager.listeners;

import com.tinyshellzz.InvManager.config.PluginConfig;
import com.tinyshellzz.InvManager.services.NeoTccInvService;
import com.tinyshellzz.InvManager.utils.MyUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import static com.tinyshellzz.InvManager.ObjectPool.*;

public class PlayerQuitListener implements Listener {
    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if(PluginConfig.debug) Bukkit.getConsoleSender().sendMessage("玩家退出，保存背包");
        currentInventoryMapper.update(player);
        currentEnderChestMapper.update(player);
        mcPlayerMapper.update_shutdown_by_uuid(player.getUniqueId(), -1);   // shutdown 是-1

        String target = player.getName().toLowerCase();
        if(NeoTccInvService.editingEnderChest.containsKey(target)) {    // 玩家离线, 关闭末影箱
            MyUtil.closeInventory(NeoTccInvService.editingEnderChest.get(target));
            NeoTccInvService.editingEnderChest.remove(target);
        }
    }
}
