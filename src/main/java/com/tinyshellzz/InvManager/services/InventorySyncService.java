package com.tinyshellzz.InvManager.services;

import com.tinyshellzz.InvManager.config.PluginConfig;
import com.tinyshellzz.InvManager.utils.ItemStackBase64Converter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.tinyshellzz.InvManager.ObjectPool.currentEnderChestMapper;
import static com.tinyshellzz.InvManager.ObjectPool.currentInventoryMapper;

public class InventorySyncService {
    private static final HashMap<UUID, ArrayList<ItemStack>> old_seeContents_map = new HashMap<>();
    private static final HashMap<UUID, ArrayList<ItemStack>> old_enderChest_map = new HashMap<>();

    public static void run() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        // 每隔 1gt 同步一次数据
        scheduler.scheduleAtFixedRate(() -> {
                    syncInventory();
                    syncEnderChest();
                },
                0,
                25,
                TimeUnit.MILLISECONDS);
    }

    private static void syncInventory() {
        try {
            for (String name : NeoTccInvService.operatingInv.keySet()) {
                Player player = Bukkit.getServer().getPlayer(name);
                // 如果玩家在线，则开始同步
                if (player != null) {
                    Inventory seeInventory = NeoTccInvService.operatingInv.get(player.getName());
                    ItemStack[] seeContents_ = seeInventory.getContents();

                    // 玩家背包->see命令看到的背包
                    ItemStack[] contents_ = player.getInventory().getContents();
                    ItemStack[] armor_ = player.getInventory().getArmorContents();
                    ItemStack offhand = player.getInventory().getItemInOffHand();

                    // 将所有数据统一为同一长度
                    ArrayList<ItemStack> contents = new ArrayList<>();
                    for (int i = 0; i < 36; i++) {
                        if (i < contents_.length) {
                            contents.add(contents_[i]);
                        } else {
                            contents.add(null);
                        }
                    }
                    ArrayList<ItemStack> copyOfContents = new ArrayList<>(contents);
                    ArrayList<ItemStack> armor = new ArrayList<>();
                    for (int i = 0; i < 4; i++) {
                        if (i < armor_.length) {
                            armor.add(armor_[i]);
                        } else {
                            armor.add(null);
                        }
                    }
                    ArrayList<ItemStack> copyOfArmor = new ArrayList<>(armor);
                    ArrayList<ItemStack> seeContents = new ArrayList<>();
                    for (int i = 0; i < 45; i++) {
                        if (i < seeContents_.length) {
                            seeContents.add(seeContents_[i]);
                        } else {
                            seeContents.add(null);
                        }
                    }
                    ArrayList<ItemStack> copyOfSeeContents = new ArrayList<>(seeContents);
                    ItemStack copyOfOffhand = offhand;

                    // 获取旧的背包数据
                    boolean seeToPlayer = false;
                    boolean playerToSee = false;
                    if (!old_seeContents_map.containsKey(player.getUniqueId())) {
                        old_seeContents_map.put(player.getUniqueId(), seeContents);
                    } else {
                        ArrayList<ItemStack> old_seeContents = old_seeContents_map.get(player.getUniqueId());
                        boolean[] conflictFlags = new boolean[45];
                        for (int i = 0; i < 45; i++) {
                            conflictFlags[i] = false;
                        }
                        for (int i = 0; i < 4; i++) {
                            // 盔甲位置 seeContent 同步到 玩家背包
                            if (!ItemStackEquals(old_seeContents.get(i), copyOfSeeContents.get(i))) {
                                conflictFlags[i] = true;
                                seeToPlayer = true;
                                armor.set(i, copyOfContents.get(i));
                            }

                            // 盔甲位置 玩家背包 同步到 seeContent
                            if (!ItemStackEquals(old_seeContents.get(i), copyOfArmor.get(i))) {
                                if (conflictFlags[i]) { // 发现数据冲突
                                    closeInventory(seeInventory);
                                    NeoTccInvService.operatingInv.remove(player.getName());
                                    NeoTccInvService.operatingInvNumber.remove(player.getName());
                                    return;
                                }
                                else {
                                    playerToSee = true;
                                    seeContents.set(i, copyOfArmor.get(i));
                                }
                            }
                        }

                        // 左手位置 seeContent 同步到 玩家背包
                        if (!ItemStackEquals(old_seeContents.get(4), copyOfSeeContents.get(4))) {
                            conflictFlags[4] = true;
                            seeToPlayer = true;
                            offhand = copyOfSeeContents.get(4);
                        }
                        // 左手位置 玩家背包 同步到 seeContent
                        if (!ItemStackEquals(old_seeContents.get(4), copyOfOffhand)) {
                            if (PluginConfig.debug) {
                                Bukkit.getConsoleSender().sendMessage(old_seeContents.get(4) == null ? "null" : old_seeContents.get(4).toString());
                                Bukkit.getConsoleSender().sendMessage(copyOfOffhand == null ? "null" : copyOfOffhand.toString());
                            }
                            if (conflictFlags[4]) { // 发现数据冲突
                                closeInventory(seeInventory);
                                NeoTccInvService.operatingInv.remove(player.getName());
                                NeoTccInvService.operatingInvNumber.remove(player.getName());
                                return;
                            }
                            else {
                                playerToSee = true;
                                seeContents.set(4, copyOfOffhand);
                            }
                        }

                        for (int i = 9; i < 45; i++) {
                            // 背包 seeContent 同步到 玩家背包
                            if (!ItemStackEquals(old_seeContents.get(i), copyOfSeeContents.get(i))) {
                                conflictFlags[i] = true;
                                seeToPlayer = true;
                                contents.set(i - 9, copyOfSeeContents.get(i));
                            }

                            // 背包 玩家背包 同步到 seeContent
                            if (!ItemStackEquals(old_seeContents.get(i), copyOfContents.get(i - 9))) {
                                if (conflictFlags[i]) { // 发现数据冲突
                                    closeInventory(seeInventory);
                                    NeoTccInvService.operatingInv.remove(player.getName());
                                    NeoTccInvService.operatingInvNumber.remove(player.getName());
                                    return;
                                }
                                else {
                                    playerToSee = true;
                                    seeContents.set(i, copyOfContents.get(i - 9));
                                }
                            }
                        }

                        ItemStack[] res = new ItemStack[45];
                        for (int i = 0; i < 45; i++) {
                            res[i] = seeContents.get(i);
                        }
                        if (seeToPlayer) {
                            if (PluginConfig.debug) Bukkit.getConsoleSender().sendMessage("seeToPlayer");
                            PlayerInventory inv = player.getInventory();
                            inv.setContents(Arrays.copyOfRange(res, 9, 45));
                            inv.setArmorContents(Arrays.copyOfRange(res, 0, 4));
                            inv.setItemInOffHand(res[4]);
                        }
                        if (playerToSee) {
                            if (PluginConfig.debug) Bukkit.getConsoleSender().sendMessage("playerToSee");
                            seeInventory.setContents(res);
                        }

                        old_seeContents_map.put(player.getUniqueId(), seeContents);
                    }
                } else {    // 如果玩家不在线，则同步到数据库
                    OfflinePlayer offlinePlayer = Bukkit.getServer().getOfflinePlayer(name);
                    Inventory seeInventory = NeoTccInvService.operatingInv.get(offlinePlayer.getName());
                    if (seeInventory == null) return;
                    ItemStack[] seeContents_ = seeInventory.getContents();

                    ArrayList<ItemStack> seeContents = new ArrayList<>();
                    for (int i = 0; i < 45; i++) {
                        if (i < seeContents_.length) {
                            seeContents.add(seeContents_[i]);
                        } else {
                            seeContents.add(null);
                        }
                    }
                    if (!old_seeContents_map.containsKey(offlinePlayer.getUniqueId())) {
                        old_seeContents_map.put(offlinePlayer.getUniqueId(), seeContents);
                    } else {
                        boolean changed = false;
                        ArrayList<ItemStack> old_seeContents = old_seeContents_map.get(offlinePlayer.getUniqueId());
                        for (int i = 0; i < 45; i++) {
                            if (!ItemStackEquals(old_seeContents.get(i), seeContents.get(i))) {
                                changed = true;
                            }
                        }

                        if (changed) {
                            Thread th = new Thread(() -> {
                                currentInventoryMapper.update(offlinePlayer.getUniqueId(), ItemStackBase64Converter.ItemStackArrayToBase64(seeContents_));
                            });

                            th.start();
                        }

                        old_seeContents_map.put(offlinePlayer.getUniqueId(), seeContents);
                    }
                }
            }
        } catch (RuntimeException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String sStackTrace = sw.toString();
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + sStackTrace);
        }
    }

    private static void syncEnderChest() {
        try {
            for (String name : NeoTccInvService.operatingEnderChest.keySet()) {
                Player player = Bukkit.getServer().getPlayer(name);
                // 如果玩家在线，则开始同步
                if (player != null) {
                    Inventory seeEnderChest = NeoTccInvService.operatingEnderChest.get(player.getName());
                    ItemStack[] seeEnderChest_ = seeEnderChest.getContents();
                    Inventory enderChest = player.getEnderChest();
                    ItemStack[] enderChest_ = enderChest.getContents();

                    // 将所有数据统一为同一长度
                    ArrayList<ItemStack> seeEnderChest_contents = new ArrayList<>();
                    for (int i = 0; i < 27; i++) {
                        if (i < seeEnderChest_.length) {
                            seeEnderChest_contents.add(seeEnderChest_[i]);
                        } else {
                            seeEnderChest_contents.add(null);
                        }
                    }

                    ArrayList<ItemStack> enderChest_contents = new ArrayList<>();
                    for (int i = 0; i < 27; i++) {
                        if (i < enderChest_.length) {
                            enderChest_contents.add(enderChest_[i]);
                        } else {
                            enderChest_contents.add(null);
                        }
                    }
                    ArrayList<ItemStack> copyOfEnderChest_contents = new ArrayList<>(enderChest_contents);

                    boolean seeToPlayer = false;
                    boolean playerToSee = false;
                    if (!old_enderChest_map.containsKey(player.getUniqueId())) {
                        old_enderChest_map.put(player.getUniqueId(), seeEnderChest_contents);
                    } else {
                        ArrayList<ItemStack> seeEnderChest_contents_old = old_enderChest_map.get(player.getUniqueId());
                        boolean[] conflictFlags = new boolean[27];
                        for (int i = 0; i < 27; i++) {
                            if (!ItemStackEquals(seeEnderChest_contents_old.get(i), seeEnderChest_contents.get(i))) {
                                conflictFlags[i] = true;
                                seeToPlayer = true;
                                enderChest_contents.set(i, seeEnderChest_contents.get(i));
                            }

                            if (!ItemStackEquals(seeEnderChest_contents_old.get(i), copyOfEnderChest_contents.get(i))) {
                                if (conflictFlags[i]) { // 发现数据冲突
                                    closeInventory(seeEnderChest);
                                    NeoTccInvService.operatingEnderChest.remove(player.getName());
                                    NeoTccInvService.operatingEnderChestNumber.remove(player.getName());
                                    return;
                                }
                                else {
                                    playerToSee = true;
                                    seeEnderChest_contents.set(i, copyOfEnderChest_contents.get(i));
                                }
                            }
                        }

                        ItemStack[] res = new ItemStack[27];
                        for (int i = 0; i < 27; i++) {
                            res[i] = seeEnderChest_contents.get(i);
                        }
                        if (seeToPlayer) {
                            if (PluginConfig.debug) Bukkit.getConsoleSender().sendMessage("enderChestSeeToPlayer");
                            player.getEnderChest().setContents(res);
                        }
                        if (playerToSee) {
                            if (PluginConfig.debug) Bukkit.getConsoleSender().sendMessage("enderChestPlayerToSee");
                            seeEnderChest.setContents(res);
                        }

                        old_enderChest_map.put(player.getUniqueId(), seeEnderChest_contents);
                    }
                } else {    // 如果玩家不在线，则同步到数据库
                    OfflinePlayer offlinePlayer = Bukkit.getServer().getOfflinePlayer(name);
                    Inventory seeEnderChest = NeoTccInvService.operatingEnderChest.get(offlinePlayer.getName());
                    if (seeEnderChest == null) return;
                    ItemStack[] seeEnderChest_ = seeEnderChest.getContents();

                    // 将所有数据统一为同一长度
                    ArrayList<ItemStack> seeEnderChest_contents = new ArrayList<>();
                    for (int i = 0; i < 27; i++) {
                        if (i < seeEnderChest_.length) {
                            seeEnderChest_contents.add(seeEnderChest_[i]);
                        } else {
                            seeEnderChest_contents.add(null);
                        }
                    }

                    if (!old_enderChest_map.containsKey(offlinePlayer.getUniqueId())) {
                        old_enderChest_map.put(offlinePlayer.getUniqueId(), seeEnderChest_contents);
                    } else {
                        boolean changed = false;
                        ArrayList<ItemStack> seeEnderChest_contents_old = old_enderChest_map.get(offlinePlayer.getUniqueId());
                        for (int i = 0; i < 27; i++) {
                            if (!ItemStackEquals(seeEnderChest_contents_old.get(i), seeEnderChest_contents.get(i))) {
                                changed = true;
                                break;
                            }
                        }

                        if (changed) {
                            Thread th = new Thread(() -> {
                                currentEnderChestMapper.update(offlinePlayer.getUniqueId(), ItemStackBase64Converter.ItemStackArrayToBase64(seeEnderChest_));
                            });

                            th.start();
                        }

                        old_seeContents_map.put(offlinePlayer.getUniqueId(), seeEnderChest_contents);
                    }
                }
            }
        } catch (RuntimeException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String sStackTrace = sw.toString();
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + sStackTrace);
        }
    }

    private static void closeInventory(Inventory inventory) {
        // Copy to avoid ConcurrentModificationException
        List<HumanEntity> viewers = new ArrayList<>(inventory.getViewers());

        for (HumanEntity viewer : viewers) {
            if (viewer instanceof Player) {
                ((Player) viewer).closeInventory();
            }
        }
    }

    private static boolean ItemStackEquals(ItemStack a, ItemStack b) {
        if(a == null) {
            return b == null || b.toString().equals("ItemStack{AIR x 0}");
        }
        if(b == null) {
            return a.toString().equals("ItemStack{AIR x 0}");
        }

        return a.equals(b);
    }
}
