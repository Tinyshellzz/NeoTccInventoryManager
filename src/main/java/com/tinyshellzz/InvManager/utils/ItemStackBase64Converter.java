package com.tinyshellzz.InvManager.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

public class ItemStackBase64Converter {
    private static String itemStackToBase64(ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static ItemStack itemStackFromBase64(String data) {
        try {
            byte[] bytes = Base64.getDecoder().decode(data);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            return item;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将ItemStack数组序列化为字符串
     * @param items
     * @return
     */
    public static String ItemStackArrayToBase64(ItemStack[] items) {
        StringBuilder ret = new StringBuilder();

        for (ItemStack item : items) {
            ret.append(itemStackToBase64(item));
            ret.append(",");
        }

        if (ret.length() > 0) {
            ret.deleteCharAt(ret.length() - 1);
        }
        return ret.toString();
    }

    /**
     * 将字符串反序列化为ItemStack数组
     * @param data
     * @return
     */
    public static ItemStack[] Base64ToItemStackArray(String data) {
        String[] split = data.split(",");
        ItemStack[] items = new ItemStack[split.length];

        for (int i = 0; i < split.length; i++) {
            items[i] = itemStackFromBase64(split[i]);
        }

        return items;
    }

    public static String PlayerInvToBase64(Player player) {
        PlayerInventory inv = player.getInventory();
        ItemStack[] full = new ItemStack[41]; // 36 + 4 + 1 (offhand)
        System.arraycopy(inv.getContents(), 0, full, 0, 36);
        System.arraycopy(inv.getArmorContents(), 0, full, 36, 4);
        full[40] = inv.getItemInOffHand();
        return ItemStackBase64Converter.ItemStackArrayToBase64(full);
    }

    public static void setBase64AsPlayerInv(Player player, String contents) {
        PlayerInventory inv = player.getInventory();

        ItemStack[] full = ItemStackBase64Converter.Base64ToItemStackArray(contents);
        inv.setContents(Arrays.copyOfRange(full, 0, 36));
        inv.setArmorContents(Arrays.copyOfRange(full, 36, 40));
        inv.setItemInOffHand(full[40]);
    }
}
