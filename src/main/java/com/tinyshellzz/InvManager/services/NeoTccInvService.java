package com.tinyshellzz.InvManager.services;

import com.tinyshellzz.InvManager.config.PluginConfig;
import com.tinyshellzz.InvManager.entities.MCPlayer;
import com.tinyshellzz.InvManager.utils.ItemStackBase64Converter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.tinyshellzz.InvManager.ObjectPool.*;

public class NeoTccInvService {
    public static HashMap<String, Inventory> operatingInv = new HashMap<>();
    public static HashMap<String, Integer> operatingInvNumber = new HashMap<>();    // 用于统计该容器得打开数，无人打开需要移除operatingInv中的数据，以节省存储空间
    public static HashMap<String, Inventory> operatingEnderChest = new HashMap<>();
    public static HashMap<String, Integer> operatingEnderChestNumber = new HashMap<>();

    public static HashMap<String, Inventory> editingInv = new HashMap<>();
    public static HashMap<String, Integer> editingInvNumber = new HashMap<>();
    public static HashMap<String, Inventory> editingEnderChest = new HashMap<>();   // 注: 注玩家离线后，末影箱不会自动关闭
    public static HashMap<String, Integer> editingEnderChestNumber = new HashMap<>();

    public static boolean rollback(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Matcher _m = Pattern.compile("^.*CraftRemoteConsoleCommandSender.*$").matcher(sender.toString());
        if(!(sender instanceof ConsoleCommandSender || _m.find() || sender.isOp() || sender.hasPermission("NeoTccInv.use"))){
            sender.sendMessage(ChatColor.RED + "只有控制台, op以及拥有NeoTccInv.use权限的玩家才能使用该命令");
            return true;
        }

        if (args.length < 4) {
            sender.sendMessage(ChatColor.YELLOW + "用法: /necotccinv rollback [inv|ender] <玩家> 时间");
            return true;
        }

//        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
        MCPlayer userByName = mcPlayerMapper.get_user_by_name(args[2]);
        if(userByName == null) {
            sender.sendMessage(ChatColor.YELLOW + "玩家 " + args[2] + " 不存在");
            return true;
        }
        if(!args[3].matches("[0-9]+")) {
            sender.sendMessage(ChatColor.YELLOW + "时间参数 " + args[3] + " 必须是数字");
            return true;
        }

        UUID player_uuid = userByName.uuid;
        String[] ret = historyInventoryMapper.get_contents_before_x_seconds(player_uuid, Integer.parseInt(args[3]));
        if(ret == null) {
            sender.sendMessage(ChatColor.YELLOW + "未找到该时间的背包备份，请尝试回档更多的时间");
            return true;
        }

        // 如果玩家在线，就直接加载
        Player onlinePlayer = Bukkit.getPlayer(args[2]);
        if(args[1].toLowerCase().equals("inv")) {
            if(ret[0] != null) {
                currentInventoryMapper.update(player_uuid, ret[0]);

                if (onlinePlayer != null) {
                    currentInventoryMapper.load_current_content(onlinePlayer);
                }

            } else {
                sender.sendMessage(ChatColor.YELLOW + "未找到该时间的背包备份");
            }
        } else if(args[1].toLowerCase().equals("ender")) {
            if(ret[1] != null) {
                currentEnderChestMapper.update(player_uuid, ret[1]);

                if (onlinePlayer != null) {
                    currentEnderChestMapper.load_current_content(onlinePlayer);
                }
            } else {
                sender.sendMessage(ChatColor.YELLOW + "未找到该时间的背包备份");
            }
        } else {
            sender.sendMessage(ChatColor.YELLOW + "用法: /necotccinv rollback [inv|ender] <玩家> 时间");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "回档成功");

        return true;
    }

    public static boolean see(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.YELLOW + "用法: /necotccinv see <玩家>");
            return true;
        }
        if(!(sender instanceof Player)) {
            return true;
        }
        Player viewer = (Player) sender;

        Inventory gui = null;
        ItemStack[] contents = null;
        ItemStack[] armor = null;
        ItemStack offhand = null;
        String name = args[1].toLowerCase();

        // 确保不同用该命令的人，打开同一个容器
        if(operatingInv.containsKey(name)) {
            gui = operatingInv.get(name);
        } else {
            Player target = Bukkit.getPlayer(args[1]);
            if(target != null) {
                if(name.equals(viewer.getName().toLowerCase())) {
                    sender.sendMessage(ChatColor.YELLOW + "你不能打开自己的背包");
                    return true;
                }
                contents = target.getInventory().getContents();
                armor = target.getInventory().getArmorContents();
                offhand = target.getInventory().getItemInOffHand();
            } else {
//            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                MCPlayer userByName = mcPlayerMapper.get_user_by_name(args[1]);
                if(userByName == null) {
                    sender.sendMessage(ChatColor.YELLOW + "该玩家的数据不存在");
                    return true;
                }
                if(userByName.shutdown == 0 || userByName.shutdown == -2) {
                    sender.sendMessage(ChatColor.YELLOW +"该玩家的离线数据因" + ChatColor.RED + "非正常重启" + ChatColor.YELLOW + "而无法查看");
                    return true;
                }

                name = args[1];
                if(name.equalsIgnoreCase(viewer.getName())) {
                    sender.sendMessage(ChatColor.YELLOW + "你不能打开自己的背包");
                    return true;
                }

                String invBase64 = currentInventoryMapper.get(userByName.uuid);
                if(invBase64 == null) {
                    sender.sendMessage(ChatColor.YELLOW +"该玩家的离线数据不存在");
                    return true;
                }
                ItemStack[] full = ItemStackBase64Converter.Base64ToItemStackArray(invBase64);

                contents = Arrays.copyOfRange(full, 0, 36);
                armor = Arrays.copyOfRange(full, 36, 40);
                offhand = full[40];
            }

            gui = Bukkit.createInventory(null, 45, "Viewing " + args[1]);

            ItemStack[] guiContents = gui.getContents();
            // Add main inventory
            System.arraycopy(contents, 0, guiContents, 9, Math.min(contents.length, 36));
            // Armor slots (top-left)
            guiContents[0] = armor.length > 0 ? armor[0] : null; // Boots
            guiContents[1] =  armor.length > 1 ? armor[1] : null; // Legs
            guiContents[2] = armor.length > 2 ? armor[2] : null; // Chest
            guiContents[3] = armor.length > 3 ? armor[3] : null; // Helmet
            guiContents[4] = offhand; // Offhand

            gui.setContents(guiContents);
            operatingInv.put(name, gui);
        }

        // Open it for the admin
        viewer.openInventory(gui);
        if(operatingInvNumber.containsKey(name)) {
            operatingInvNumber.put(name, operatingInvNumber.get(name) + 1);
        } else {
            operatingInvNumber.put(name, 1);
        }

        return true;
    }

    public static boolean ender(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.YELLOW + "用法: /necotccinv see <玩家>");
            return true;
        }
        if(!(sender instanceof Player)) {
            return true;
        }
        Player viewer = (Player) sender;

        Inventory gui = null;
        String name = args[1].toLowerCase();
        ItemStack[] contents = null;

        // 确保不同用该命令的人，打开同一个容器
        if(operatingEnderChest.containsKey(name)) {
            gui = operatingEnderChest.get(name);
        } else {
            Player target = Bukkit.getPlayer(args[1]);
            if(target != null) {
                if(name.equals(viewer.getName().toLowerCase())) {
                    sender.sendMessage(ChatColor.YELLOW + "你不能打开自己的背包");
                    return true;
                }

                contents = target.getEnderChest().getContents();
            } else {
//            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                MCPlayer userByName = mcPlayerMapper.get_user_by_name(args[1]);
                if(userByName == null) {
                    sender.sendMessage(ChatColor.YELLOW + "该玩家的数据不存在");
                    return true;
                }
                if(userByName.shutdown == 0 || userByName.shutdown == -2) {
                    sender.sendMessage(ChatColor.YELLOW +"该玩家的离线数据因" + ChatColor.RED + "非正常重启" + ChatColor.YELLOW + "而无法查看");
                    return true;
                }
                name = args[1];
                if(name.equalsIgnoreCase(viewer.getName())) {
                    sender.sendMessage(ChatColor.YELLOW + "你不能打开自己的背包");
                    return true;
                }
                String invBase64 = currentEnderChestMapper.get(userByName.uuid);
                if(invBase64 == null) {
                    sender.sendMessage(ChatColor.YELLOW + "该玩家的数据不存在");
                    return true;
                }

                contents = ItemStackBase64Converter.Base64ToItemStackArray(invBase64);
            }

            gui = Bukkit.createInventory(null, 27, "ViewEnder " + args[1]);
            gui.setContents(contents);
            operatingEnderChest.put(name, gui);
        }
        // Open it for the viewer
        viewer.openInventory(gui);
        if(operatingEnderChestNumber.containsKey(name)) {
            operatingEnderChestNumber.put(name, operatingEnderChestNumber.get(name) + 1);
        } else {
            operatingEnderChestNumber.put(name, 1);
        }

        return true;
    }

    public static boolean edit(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.YELLOW + "用法: /necotccinv edit [inv|ender|armor] <玩家>");
            return true;
        }
        if(!(sender instanceof Player)) return true;
        if(!sender.hasPermission("NeoTccInv.use") && !sender.isOp()) {
            sender.sendMessage(ChatColor.YELLOW + "只有op或拥有NeoTccInv.use的玩家可以使用该命令");
        }

        MCPlayer userByName = mcPlayerMapper.get_user_by_name(args[2]);
        if(userByName == null) {
            sender.sendMessage(ChatColor.YELLOW + "玩家 " + args[2] + " 不存在");
            return true;
        }

        Player player = Bukkit.getPlayer(args[2]);
        if(player != null) {    // 玩家在线
            if(args[1].toLowerCase().equals("inv")) {
                ((Player) sender).openInventory(player.getInventory());
            } else if(args[1].toLowerCase().equals("ender")) {
                ((Player) sender).openInventory(player.getEnderChest());
                editingEnderChest.put(player.getName().toLowerCase(), player.getEnderChest());
            }else {
                sender.sendMessage(ChatColor.YELLOW + "用法: /necotccinv edit [inv|ender] <玩家>");
                return true;
            }
        } else {
            if(userByName.shutdown == 0 || userByName.shutdown == -2) {
                sender.sendMessage(ChatColor.YELLOW +"该玩家的离线数据因" + ChatColor.RED + "非正常重启" + ChatColor.YELLOW + "而无法查看");
                return true;
            }

            Player viewer = (Player) sender;
            Inventory gui = null;
            String name = args[2].toLowerCase();

            if(args[1].toLowerCase().equals("inv")) {
                // 确保不同用该命令的人，打开同一个容器
                if(editingInv.containsKey(name)) {
                    gui = editingInv.get(name);
                } else {
                    String invBase64 = currentInventoryMapper.get(userByName.uuid);
                    ItemStack[] full = ItemStackBase64Converter.Base64ToItemStackArray(invBase64);

                    ItemStack[] contents = Arrays.copyOfRange(full, 0, 36);
                    ItemStack[] armor = Arrays.copyOfRange(full, 36, 40);
                    ItemStack offhand = full[40];

                    gui = Bukkit.createInventory(null, 45, "EditInv " + args[2]);

                    ItemStack[] guiContents = gui.getContents();
                    // Add main inventory
                    System.arraycopy(contents, 0, guiContents, 9, Math.min(contents.length, 36));
                    // Armor slots (top-left)
                    guiContents[0] = armor.length > 0 ? armor[0] : null; // Boots
                    guiContents[1] =  armor.length > 1 ? armor[1] : null; // Legs
                    guiContents[2] = armor.length > 2 ? armor[2] : null; // Chest
                    guiContents[3] = armor.length > 3 ? armor[3] : null; // Helmet
                    guiContents[4] = offhand; // Offhand
                    gui.setContents(guiContents);
                    editingInv.put(name, gui);
                }

                // Open it for the admin
                viewer.openInventory(gui);
                if(editingInvNumber.containsKey(name)) {
                    editingInvNumber.put(name, editingInvNumber.get(name) + 1);
                } else {
                    editingInvNumber.put(name, 1);
                }
            } else if(args[1].toLowerCase().equals("ender")) {
                // 确保不同用该命令的人，打开同一个容器
                if(editingEnderChest.containsKey(name)) {
                    gui = editingEnderChest.get(name);
                } else {
                    String invBase64 = currentEnderChestMapper  .get(userByName.uuid);
                    ItemStack[] contents = ItemStackBase64Converter.Base64ToItemStackArray(invBase64);

                    gui = Bukkit.createInventory(null, 27, "EditEnder " + args[2]);
                    gui.setContents(contents);
                    editingEnderChest.put(name, gui);
                }

                // Open it for the admin
                viewer.openInventory(gui);
                if(editingEnderChestNumber.containsKey(name)) {
                    editingEnderChestNumber.put(name, editingEnderChestNumber.get(name) + 1);
                } else {
                    editingEnderChestNumber.put(name, 1);
                }
            }
        }

        return true;
    }

    public static boolean history(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length < 4) {
            sender.sendMessage(ChatColor.YELLOW + "用法: /necotccinv history [inv|ender] <玩家> 时间");
            return true;
        }
        if(!(sender instanceof Player)) {
            return true;
        }

//        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
        MCPlayer userByName = mcPlayerMapper.get_user_by_name(args[2]);
        if(userByName == null) {
            sender.sendMessage(ChatColor.YELLOW + "玩家 " + args[2] + " 不存在");
            return true;
        }
        if(!args[3].matches("[0-9]+")) {
            sender.sendMessage(ChatColor.YELLOW + "时间参数 " + args[3] + " 必须是数字");
            return true;
        }

        UUID player_uuid = userByName.uuid;
        String[] ret = historyInventoryMapper.get_contents_before_x_seconds(player_uuid, Integer.parseInt(args[3]));
        if(ret == null) {
            sender.sendMessage(ChatColor.YELLOW + "未找到该时间的背包备份，请尝试更久的时间");
            return true;
        }


        Player admin = (Player) sender;
        if(args[1].toLowerCase().equals("inv")) {
            if(ret[0] != null) {
                String invBase64 = ret[0];
                ItemStack[] full = ItemStackBase64Converter.Base64ToItemStackArray(invBase64);

                ItemStack[] contents = Arrays.copyOfRange(full, 0, 36);
                ItemStack[] armor = Arrays.copyOfRange(full, 36, 40);
                ItemStack offhand = full[40];

                Inventory gui = Bukkit.createInventory(null, 45, "History " + args[2]);

                ItemStack[] guiContents = gui.getContents();
                // Add main inventory
                System.arraycopy(contents, 0, guiContents, 9, Math.min(contents.length, 36));
                // Armor slots (top-left)
                guiContents[0] = armor.length > 0 ? armor[0] : null; // Boots
                guiContents[1] =  armor.length > 1 ? armor[1] : null; // Legs
                guiContents[2] = armor.length > 2 ? armor[2] : null; // Chest
                guiContents[3] = armor.length > 3 ? armor[3] : null; // Helmet
                guiContents[4] = offhand; // Offhand
                gui.setContents(guiContents);

                // Open it for the admin
                admin.openInventory(gui);
            } else {
                sender.sendMessage(ChatColor.YELLOW + "未找到该时间的背包备份");
            }
        } else if(args[1].toLowerCase().equals("ender")) {
            if(ret[1] != null) {
                ItemStack[] contents = ItemStackBase64Converter.Base64ToItemStackArray(ret[1]);
                Inventory gui = Bukkit.createInventory(null, 27, "History " + args[2]);

                gui.setContents(contents);

                // Open it for the admin
                admin.openInventory(gui);
            }  else {
                sender.sendMessage(ChatColor.YELLOW + "未找到该时间的背包备份");
            }
        } else {
            sender.sendMessage(ChatColor.YELLOW + "用法: /necotccinv history [inv|ender] <玩家> 时间");
            return true;
        }

        return true;
    }
}
