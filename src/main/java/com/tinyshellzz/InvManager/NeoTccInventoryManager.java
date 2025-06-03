package com.tinyshellzz.InvManager;

import com.tinyshellzz.InvManager.commands.NeoTccInvCommand;
import com.tinyshellzz.InvManager.config.PluginConfig;
import com.tinyshellzz.InvManager.database.CurrentEnderChestMapper;
import com.tinyshellzz.InvManager.database.CurrentInventoryMapper;
import com.tinyshellzz.InvManager.database.HistoryInventoryMapper;
import com.tinyshellzz.InvManager.listeners.InventoryCloseListener;
import com.tinyshellzz.InvManager.listeners.InventoryInteractListener;
import com.tinyshellzz.InvManager.listeners.PlayerJoinListener;
import com.tinyshellzz.InvManager.listeners.PlayerQuitListener;
import com.tinyshellzz.InvManager.services.InventoryBackup;
import com.tinyshellzz.InvManager.services.InventorySyncService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import static com.tinyshellzz.InvManager.ObjectPool.currentEnderChestMapper;
import static com.tinyshellzz.InvManager.ObjectPool.currentInventoryMapper;

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
        register();
        runServices();
    }

    public void init(){
        ObjectPool.plugin = this;
        PluginConfig.reload();

        ObjectPool.historyInventoryMapper = new HistoryInventoryMapper();
        ObjectPool.currentInventoryMapper = new CurrentInventoryMapper();
        ObjectPool.currentEnderChestMapper = new CurrentEnderChestMapper();
    }

    public void register() {
        // 注册监听器
        this.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        this.getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
        this.getServer().getPluginManager().registerEvents(new InventoryCloseListener(), this);
        this.getServer().getPluginManager().registerEvents(new InventoryInteractListener(), this);

        // 注册命令
        this.getCommand("NeoTccInv").setExecutor(new NeoTccInvCommand());
    }

    public void runServices() {
        InventoryBackup.run();
        InventorySyncService.run();
    }

    @Override
    public void onDisable() {
        //TODO
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[NeoTccInv]" + ChatColor.RED + "######################");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[NeoTccInv]" + ChatColor.RED + "#                    #");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[NeoTccInv]" + ChatColor.RED + "#NeoTccInv已关闭#");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[NeoTccInv]" + ChatColor.RED + "#                    #");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[NeoTccInv]" + ChatColor.RED + "######################");

        // 插件关闭时，保存所有玩家的背包
        for (Player player : Bukkit.getOnlinePlayers()) {
            if(currentEnderChestMapper.exists(player.getUniqueId())) {
                currentInventoryMapper.update(player);
            } else {
                currentEnderChestMapper.insert(player);
            }
        }
    }

}
