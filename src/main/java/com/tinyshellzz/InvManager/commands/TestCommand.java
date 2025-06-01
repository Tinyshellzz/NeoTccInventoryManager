package com.tinyshellzz.InvManager.commands;

import com.tinyshellzz.InvManager.utils.ItemStackBase64Converter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class TestCommand implements TabExecutor {
    String player_full_inventory = null;

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(!(commandSender instanceof Player)) {
            return false;
        }

        Player player = (Player) commandSender;
        PlayerInventory inv = player.getInventory();
        if(player_full_inventory == null) {
            ItemStack[] full = new ItemStack[41]; // 36 + 4 + 1 (offhand)
            System.arraycopy(inv.getContents(), 0, full, 0, 36);
            System.arraycopy(inv.getArmorContents(), 0, full, 36, 4);
            full[40] = inv.getItemInOffHand();
            player_full_inventory = ItemStackBase64Converter.ItemStackArrayToBase64(full);
        } else {
            ItemStack[] full = ItemStackBase64Converter.Base64ToItemStackArray(player_full_inventory);
            inv.setContents(Arrays.copyOfRange(full, 0, 36));
            inv.setArmorContents(Arrays.copyOfRange(full, 36, 40));
            inv.setItemInOffHand(full[40]);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return List.of();
    }
}
