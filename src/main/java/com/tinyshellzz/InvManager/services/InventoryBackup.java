package com.tinyshellzz.InvManager.services;

import com.tinyshellzz.InvManager.config.PluginConfig;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.tinyshellzz.InvManager.ObjectPool.plugin;
import static com.tinyshellzz.InvManager.ObjectPool.historyInventoryMapper;

public class InventoryBackup {
    public static void run() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        // 每隔 PluginConfig.backup_interval 时间备份一次所有在线玩家的背包
        scheduler.scheduleAtFixedRate(() -> {
                    try {
                        if (!PluginConfig.debug) Bukkit.getConsoleSender().sendMessage("[NeoTccInv] 背包备份");
                        for (Player p : plugin.getServer().getOnlinePlayers()) {
                            historyInventoryMapper.insert(p);
                        }
                    } catch (RuntimeException e) {
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw);
                        e.printStackTrace(pw);
                        String sStackTrace = sw.toString();
                        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + sStackTrace);
                    }
                },
                0,
                PluginConfig.backup_interval,
                TimeUnit.SECONDS);
    }
}
