package com.tinyshellzz.InvManager.listeners;

import com.tinyshellzz.InvManager.ObjectPool;
import com.tinyshellzz.InvManager.utils.ItemStackBase64Converter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.Collection;

import static com.tinyshellzz.InvManager.ObjectPool.*;

public class ServerCommandListener implements Listener {
    @EventHandler
    public void onServerCommand(ServerCommandEvent event) {     // 拦截 stop指令
        if (event.getCommand().equalsIgnoreCase("stop")) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[NeoTccInv] Stopping Server Command");
            if(!ObjectPool.stopped) {
                ObjectPool.stopped = true;

                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[NeoTccInv] Saving Player Data! ");

                Collection<? extends Player> onlinePlayers = Bukkit.getServer().getOnlinePlayers();
                for (Player p : onlinePlayers) {
                    Player playerExact = Bukkit.getPlayerExact(p.getName());

                    if (playerExact != null && playerExact.isOnline()) {
                        currentInventoryMapper.update(playerExact);
                        currentEnderChestMapper.update(playerExact);
                    }
                }
                mcPlayerMapper.update_shutdown();   // 正常重启
            }
        }
    }
}
