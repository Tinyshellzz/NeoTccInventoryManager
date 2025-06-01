package com.tinyshellzz.InvManager.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;

public class ConfigWrapper {
    private final File file;
    private final String path;
    private final Plugin plugin;
    private YamlConfiguration config;

    public ConfigWrapper(@NotNull Plugin plugin, @NotNull String path) {
        this.path = path;
        File dataFolder = plugin.getDataFolder();
        this.file = new File(dataFolder, path);
        this.plugin = plugin;
        this.saveDefaultConfig();
    }

    public ConfigWrapper(@NotNull File file) {
        this.file = file;
        this.path = file.getPath();
        if (!this.file.exists()) {
            try {
                this.file.getParentFile().mkdirs();
                this.file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        this.config = YamlConfiguration.loadConfiguration(file);
        this.plugin = null;
    }

    public void saveDefaultConfig() {
        if (!this.file.exists()) {
            try {
                this.plugin.saveResource(this.path, false);
            } catch (IllegalArgumentException | NullPointerException var2) {
                try {
                    this.file.getParentFile().mkdirs();
                    this.file.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        this.reloadConfig();
    }

    public @NotNull YamlConfiguration getConfig() {
        if (this.config == null) {
            this.reloadConfig();
        }

        return this.config;
    }

    public void setConfig(YamlConfiguration config) {
        this.config = config;
    }

    public boolean contains(String key) {
        return this.config.contains(key);
    }

    public void set(@NotNull String key, @Nullable Object object) {
        this.config.set(key, object);
    }

    public void reloadConfig() {
        this.config = YamlConfiguration.loadConfiguration(this.file);
    }

    public synchronized void saveConfig() {
        try {
            String s = this.config.saveToString();
            String s1 = s.replaceAll(" *?!!.*?\n", "\n");
            String content = s1.replaceAll("\n.*?: null\n", "\n");
            Writer writer = new BufferedWriter(new FileWriter(file));
            writer.write(content);
            writer.flush();
            writer.close();
        } catch (IOException var2) {
            var2.printStackTrace();
        }
    }

    public @NotNull String getPath() {
        return this.path;
    }

    public @NotNull File getFile() {
        return this.file;
    }
}
