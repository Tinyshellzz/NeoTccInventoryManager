package com.tinyshellzz.InvManager;

import com.tinyshellzz.InvManager.commands.NeoTccInvCommand;
import com.tinyshellzz.InvManager.commands.StopCommand;
import com.tinyshellzz.InvManager.config.PluginConfig;
import com.tinyshellzz.InvManager.database.CurrentEnderChestMapper;
import com.tinyshellzz.InvManager.database.CurrentInventoryMapper;
import com.tinyshellzz.InvManager.database.HistoryInventoryMapper;
import com.tinyshellzz.InvManager.database.MCPlayerMapper;
import com.tinyshellzz.InvManager.listeners.*;
import com.tinyshellzz.InvManager.services.InventoryBackup;
import com.tinyshellzz.InvManager.services.InventorySyncService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import static com.tinyshellzz.InvManager.ObjectPool.*;

public class NeoTccInventoryManager extends JavaPlugin {
    @Override
    public void onEnable() {
        // team,启动！
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[NeoTccInv]" + ChatColor.AQUA + "######################");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[NeoTccInv]" + ChatColor.AQUA + "#                    #");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[NeoTccInv]" + ChatColor.AQUA + "#NeoTccInv已启动#");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[NeoTccInv]" + ChatColor.AQUA + "#                    #");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[NeoTccInv]" + ChatColor.AQUA + "######################");

        init();
        mcPlayerMapper.update_shutdown(-2);     // 非正常重启的标记为-2
        register();
        runServices();
    }

    public void init() {
        ObjectPool.plugin = this;
        PluginConfig.reload();

        ObjectPool.historyInventoryMapper = new HistoryInventoryMapper();
        currentInventoryMapper = new CurrentInventoryMapper();
        ObjectPool.currentEnderChestMapper = new CurrentEnderChestMapper();
        ObjectPool.mcPlayerMapper = new MCPlayerMapper();
    }

    public void register() {
        // 注册监听器
        this.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        this.getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
        this.getServer().getPluginManager().registerEvents(new InventoryCloseListener(), this);
        this.getServer().getPluginManager().registerEvents(new InventoryInteractListener(), this);
        this.getServer().getPluginManager().registerEvents(new ServerCommandListener(), this);

        // 注册命令
        this.getCommand("NeoTccInv").setExecutor(new NeoTccInvCommand());
        this.getCommand("stop").setExecutor(new StopCommand());
    }

    public void runServices() {
        InventoryBackup.run();
    }

    @Override
    public void onDisable() {
        if(!ObjectPool.stopped) {
            mcPlayerMapper.update_shutdown(-2);   // 非正常重启
        }

        //TODO
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[NeoTccInv]" + ChatColor.RED + "######################");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[NeoTccInv]" + ChatColor.RED + "#                    #");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[NeoTccInv]" + ChatColor.RED + "#NeoTccInv已关闭#");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[NeoTccInv]" + ChatColor.RED + "#                    #");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[NeoTccInv]" + ChatColor.RED + "######################");
    }
}
