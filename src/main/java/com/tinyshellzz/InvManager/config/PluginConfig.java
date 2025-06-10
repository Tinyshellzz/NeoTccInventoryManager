package com.tinyshellzz.InvManager.config;


import org.bukkit.configuration.file.YamlConfiguration;
import static com.tinyshellzz.InvManager.ObjectPool.gson;
import static com.tinyshellzz.InvManager.ObjectPool.plugin;

public class PluginConfig {
    public static boolean debug;
    public static String db_host;
    public static int db_port;
    public static String db_user;
    public static String db_passwd;
    public static String db_database;
    public static long backup_interval;
    public static int clean_data_older_than;
    public static int server_id;
    private static final ConfigWrapper configWrapper = new ConfigWrapper(plugin, "config.yml");

    public static void reload() {
        configWrapper.reloadConfig(); // 重新加载配置文件

        YamlConfiguration yamlconfig = configWrapper.getConfig();
        debug = yamlconfig.getBoolean("debug");
        db_host = yamlconfig.getString("db_host");
        db_port = yamlconfig.getInt("db_port");
        db_user = yamlconfig.getString("db_user");
        db_passwd = yamlconfig.getString("db_passwd");
        db_database = yamlconfig.getString("db_database");
        backup_interval = yamlconfig.getLong("backup_interval");
        clean_data_older_than = yamlconfig.getInt("clean_data_older_than");
        server_id = yamlconfig.getInt("server_id");
    }

    @Override
    public String toString() {
        return gson.toJson(this);
    }
}
