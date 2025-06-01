package com.tinyshellzz.InvManager.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyUtil {
    private static final Pattern colorPattern = Pattern.compile("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");

    /**
     *
     * @param colorCode 以'#'开头的 rgb 颜色代码
     * @return
     */
    public static String msgColor(String colorCode) {
        Matcher matcher = colorPattern.matcher(colorCode);

        String colorStr = "";
        if (matcher.find()) {
            // 将 RGB 转换为 & 颜色
            colorStr = ChatColor.of(colorCode).toString();
        }

        // 将 & 颜色显示
        return ChatColor.translateAlternateColorCodes('&', colorStr);
    }

    /**
     *
     * @param msg 含有'&'开头的颜色代码
     * @return
     */
    public static String msgColor2(String msg) {
        // 将 & 颜色显示
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static void teleport(Player player, Location location) {
        // 播放瞬间移动声音效果，增强玩家体验
        player.playSound(player, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

        try {
            Class<?> pluginMetaClass = Class.forName("io.papermc.paper.plugin.configuration.PluginMeta");
            pluginMetaClass.getMethod("isFoliaSupported");

            // 该平台是folia，调用folia传送方法
            player.teleportAsync(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
        } catch (ClassNotFoundException e) {
            // 该平台是bukkit, 调用bukkit传送方法
            player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
        } catch (NoSuchMethodException e) {
            // 该平台是paper, 调用paper传送方法 (与bukkit一样)
            player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
        }
    }

    public static boolean isOnline(UUID uuid) {
        return Bukkit.getPlayer(uuid) != null;
    }

    public boolean isPlaceHolderAPIInstalled() {
        Plugin placeholderAPI = Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI");
        return placeholderAPI != null && placeholderAPI.isEnabled();
    }
}

