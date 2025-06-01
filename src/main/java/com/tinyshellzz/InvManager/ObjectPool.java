package com.tinyshellzz.InvManager;

import com.google.gson.Gson;
import com.tinyshellzz.InvManager.database.CurrentEnderChestMapper;
import com.tinyshellzz.InvManager.database.CurrentInventoryMapper;
import com.tinyshellzz.InvManager.database.HistoryInventoryMapper;
import org.bukkit.plugin.Plugin;

public class ObjectPool {
    public static Plugin plugin;
    public static Gson gson = new Gson();

    public static HistoryInventoryMapper historyInventoryMapper;
    public static CurrentInventoryMapper currentInventoryMapper;
    public static CurrentEnderChestMapper currentEnderChestMapper;
}
