package com.tinyshellzz.InvManager.services;

import com.tinyshellzz.InvManager.config.PluginConfig;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.tinyshellzz.InvManager.ObjectPool.historyInventoryMapper;

public class InventoryCleanService {
    public static void run() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        // 每分钟执行一次
        scheduler.scheduleAtFixedRate(() -> {
                    try {
                        historyInventoryMapper.clean_data_older_then(PluginConfig.clean_data_older_than);
                    } catch (RuntimeException e) {
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw);
                        e.printStackTrace(pw);
                        String sStackTrace = sw.toString();
                        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + sStackTrace);
                    }
                },
                0,
                60,
                TimeUnit.SECONDS);
    }
}
