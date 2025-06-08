package com.tinyshellzz.InvManager.commands;

import com.tinyshellzz.InvManager.ObjectPool;
import com.tinyshellzz.InvManager.utils.ItemStackBase64Converter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.tinyshellzz.InvManager.ObjectPool.currentEnderChestMapper;
import static com.tinyshellzz.InvManager.ObjectPool.currentInventoryMapper;

public class StopCommand implements TabExecutor {
    public static boolean stopCommand = false;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (command.getName().equalsIgnoreCase("stop")) {
            Matcher _m = Pattern.compile("^.*CraftRemoteConsoleCommandSender.*$").matcher(sender.toString());
            if(!(sender instanceof ConsoleCommandSender || _m.find() || sender.isOp())){
                sender.sendMessage("只有控制台和op才能使用该命令");
                return true;
            }

            sender.sendMessage(ChatColor.GREEN + "[NeoTccInv] 关闭服务器");
            stopCommand = true;
            ObjectPool.stopped = true;

            // This runs even during some unexpected terminations
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN +  "[NeoTccInv] 开始保存玩家数据! ");

            Collection<? extends Player> onlinePlayers = Bukkit.getServer().getOnlinePlayers();
            for(Player p: onlinePlayers) {
                currentInventoryMapper.update(p);
                currentEnderChestMapper.update(p);
            }
            Bukkit.shutdown();
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return List.of();
    }
}
