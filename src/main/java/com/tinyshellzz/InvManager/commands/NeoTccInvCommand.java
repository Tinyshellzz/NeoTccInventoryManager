package com.tinyshellzz.InvManager.commands;

import com.tinyshellzz.InvManager.config.PluginConfig;
import com.tinyshellzz.InvManager.services.NeoTccInvService;
import com.tinyshellzz.InvManager.utils.MyPair;
import com.tinyshellzz.InvManager.utils.MyUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NeoTccInvCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {        // 判断命令发送者是否是玩家，
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "参数不足");
            return true;
        }

        String subcommand = args[0].toLowerCase();
        if(subcommand.equals("reload")) {
            Matcher _m = Pattern.compile("^.*CraftRemoteConsoleCommandSender.*$").matcher(sender.toString());
            if(!(sender instanceof ConsoleCommandSender || _m.find() || sender.isOp())){
                sender.sendMessage("只有控制台和op才能使用该命令");
                return true;
            }
            PluginConfig.reload();
            return true;
        } else if (subcommand.equals("rollback")) {
            return NeoTccInvService.rollback(sender, command, s, args);
        } else if (subcommand.equals("see")) {
            return NeoTccInvService.see(sender, command, s, args);
        } else if (subcommand.equals("ender")) {
            return NeoTccInvService.ender(sender, command, s, args);
        } else if (subcommand.equals("history")) {
            return NeoTccInvService.history(sender, command, s, args);
        }

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return null;
        } else if (args.length == 1) {
            String input = args[0].toLowerCase();

            return MyUtil.tabComplete(Arrays.asList("reload", "rollback", "see" , "ender", "history"), input) ;
        } else if (args.length == 2) {
            String input = args[1].toLowerCase();
            ArrayList<String> res = new ArrayList<>();

            switch (args[0].toLowerCase()) {
                case "rollback": case "history":
                    return MyUtil.tabComplete(Arrays.asList("inv" , "ender"), input);
                case "see":
                case "ender":
                    OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
                    for(OfflinePlayer p: offlinePlayers) {
                        if(p.getName() != null && p.getName().toLowerCase().startsWith(input)) {
                            res.add(p.getName());
                        }
                    }

                    return MyUtil.tabComplete(res, input);
            }
        } else if (args.length == 3) {
            String input = args[2].toLowerCase();
            ArrayList<String> res = new ArrayList<>();

            switch (args[1].toLowerCase()) {
                case "inv": case "ender":
                    OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
                    for(OfflinePlayer p: offlinePlayers) {
                        if(p.getName() != null && p.getName().toLowerCase().startsWith(input)) {
                            res.add(p.getName());
                        }
                    }

                    return MyUtil.tabComplete(res, input);
            }
        } else if (args.length == 4) {
            String input = args[3].toLowerCase();

            return MyUtil.tabComplete(Arrays.asList("时间(单位为秒)"), input);
        }

        return null;
    }
}
