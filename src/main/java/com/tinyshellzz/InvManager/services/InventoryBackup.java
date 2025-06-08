package com.tinyshellzz.InvManager.services;

import com.tinyshellzz.InvManager.ObjectPool;
import com.tinyshellzz.InvManager.config.PluginConfig;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.tinyshellzz.InvManager.ObjectPool.*;

public class InventoryBackup {
    private static int k = 0;
    public static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void run() {
        // 每隔 PluginConfig.backup_interval 时间备份一次所有在线玩家的背包
        scheduler.scheduleAtFixedRate(() -> {
                    try {
                        if (k>=20) {
                            Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[NeoTccInv] 背包备份");
                            k = 0;
                        } else {
                            k++;
                        }

                        if(!ObjectPool.stopped) {
                            for (Player p : plugin.getServer().getOnlinePlayers()) {
                                if (ObjectPool.stopped) break;
                                historyInventoryMapper.insert(p);
                                if (ObjectPool.stopped) break;
                                currentEnderChestMapper.update(p);
                                if (ObjectPool.stopped) break;
                                currentInventoryMapper.update(p);
                            }
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
