package com.tinyshellzz.InvManager.utils.rcon;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.logging.Level;

public class MyLogger {
    String name;
    public MyLogger(String name) {
        this.name = name;
    }

    public static MyLogger getLogger(String name) {
        return new MyLogger(name);
    }

    public void log(Level level, String message, Exception e) {
        if(level == Level.SEVERE) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[" + name + "] " + message + Arrays.toString(e.getStackTrace()));
        } else if(level == Level.INFO) {
            Bukkit.getConsoleSender().sendMessage("[" + name + "] " + message+ Arrays.toString(e.getStackTrace()));
        }
    }

    public void log(Level level, String message) {
        if(level == Level.SEVERE) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[" + name + "] " + message);
        } else if(level == Level.INFO) {
            Bukkit.getConsoleSender().sendMessage("[" + name + "] " + message);
        }
    }

    public void severe(String message, Exception e) {
        log(Level.SEVERE, message, e);
    }

    public void severe(String message) {
        log(Level.SEVERE, message);
    }
}
