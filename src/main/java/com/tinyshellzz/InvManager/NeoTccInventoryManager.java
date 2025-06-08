package com.tinyshellzz.InvManager;

import com.tinyshellzz.InvManager.commands.NeoTccInvCommand;
import com.tinyshellzz.InvManager.commands.StopCommand;
import com.tinyshellzz.InvManager.config.PluginConfig;
import com.tinyshellzz.InvManager.database.CurrentEnderChestMapper;
import com.tinyshellzz.InvManager.database.CurrentInventoryMapper;
import com.tinyshellzz.InvManager.database.HistoryInventoryMapper;
import com.tinyshellzz.InvManager.listeners.*;
import com.tinyshellzz.InvManager.services.InventoryBackup;
import com.tinyshellzz.InvManager.services.InventorySyncService;
import com.tinyshellzz.InvManager.utils.ItemStackBase64Converter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

import static com.tinyshellzz.InvManager.ObjectPool.currentEnderChestMapper;
import static com.tinyshellzz.InvManager.ObjectPool.currentInventoryMapper;

public class NeoTccInventoryManager extends JavaPlugin {
    public static HashMap<UUID, ItemStack[]> inventoryMap = new HashMap<>();
    public static HashMap<UUID, ItemStack[]> enderChestMap = new HashMap<>();

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

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ObjectPool.stopped = true;

            if(StopCommand.stopCommand) return;
            // This runs even during some unexpected terminations
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN +  "[NeoTccInv] 开始保存玩家数据! ");

            for(UUID playerUUID: inventoryMap.keySet()) {
                currentInventoryMapper.update(playerUUID, ItemStackBase64Converter.ItemStackArrayToBase64(inventoryMap.get(playerUUID)));
            }
            for(UUID playerUUID: enderChestMap.keySet()) {
                currentEnderChestMapper.update(playerUUID, ItemStackBase64Converter.ItemStackArrayToBase64(enderChestMap.get(playerUUID)));
            }
        }));
    }

    public void init(){
        ObjectPool.plugin = this;
        PluginConfig.reload();

        ObjectPool.historyInventoryMapper = new HistoryInventoryMapper();
        currentInventoryMapper = new CurrentInventoryMapper();
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
        this.getCommand("stop").setExecutor(new StopCommand());
    }

    public void runServices() {
        InventoryBackup.run();
        InventorySyncService.run();
    }

    @Override
    public void onDisable() {
        ObjectPool.stopped = true;
        //TODO
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[NeoTccInv]" + ChatColor.RED + "######################");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[NeoTccInv]" + ChatColor.RED + "#                    #");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[NeoTccInv]" + ChatColor.RED + "#NeoTccInv已关闭#");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[NeoTccInv]" + ChatColor.RED + "#                    #");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[NeoTccInv]" + ChatColor.RED + "######################");
    }
}
