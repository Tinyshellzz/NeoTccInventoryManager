package com.tinyshellzz.InvManager.listeners;

import com.tinyshellzz.InvManager.config.PluginConfig;
import com.tinyshellzz.InvManager.services.NeoTccInvService;
import com.tinyshellzz.InvManager.utils.ItemStackBase64Converter;
import com.tinyshellzz.InvManager.utils.MyUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

import static com.tinyshellzz.InvManager.ObjectPool.*;

public class PlayerQuitListener implements Listener {
    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if(PluginConfig.debug) Bukkit.getConsoleSender().sendMessage("玩家退出，保存背包");
        String invContent = ItemStackBase64Converter.PlayerInvToBase64(player);
        String enderContents = ItemStackBase64Converter.ItemStackArrayToBase64(player.getEnderChest().getContents());
        UUID playerUUID = player.getUniqueId();
        new Thread(() -> {
            currentInventoryMapper.update(playerUUID, invContent);
            currentEnderChestMapper.update(playerUUID, enderContents);
            mcPlayerMapper.update_shutdown_by_uuid(playerUUID, -1);   // shutdown 是-1
        }).start();

        String target = player.getName().toLowerCase();
        if(NeoTccInvService.editingEnderChest.containsKey(target)) {    // 玩家离线, 关闭末影箱
            MyUtil.closeInventory(NeoTccInvService.editingEnderChest.get(target));
            NeoTccInvService.editingEnderChest.remove(target);
        }
    }
}
