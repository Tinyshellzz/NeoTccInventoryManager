package com.tinyshellzz.InvManager.listeners;

import com.tinyshellzz.InvManager.config.PluginConfig;
import com.tinyshellzz.InvManager.services.NeoTccInvService;
import com.tinyshellzz.InvManager.utils.ItemStackBase64Converter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static com.tinyshellzz.InvManager.ObjectPool.currentEnderChestMapper;
import static com.tinyshellzz.InvManager.ObjectPool.currentInventoryMapper;

public class InventoryCloseListener implements Listener {
    @EventHandler
    public void InventoryClose(InventoryCloseEvent event) {
        String title = event.getView().getTitle();
        HumanEntity p = event.getPlayer();
        if(!p.isOp() && !p.hasPermission("NeoTccInv.use")) {
            return;
        }

        if(title.startsWith("NeoTccInv: Viewing ")) {
            String target = title.substring(19);

            if(NeoTccInvService.operatingInv.containsKey(target)) {
                ItemStack[] contents = NeoTccInvService.operatingInv.get(target).getContents();
                if(PluginConfig.debug) Bukkit.getConsoleSender().sendMessage(target);

                Player player = Bukkit.getPlayer(target);
                if (player != null) {
                    PlayerInventory inv = player.getInventory();
                    inv.setContents(Arrays.copyOfRange(contents, 9, 45));

                    @Nullable ItemStack[] armorContents = inv.getArmorContents();
                    armorContents[3] = contents[0];
                    armorContents[2] = contents[1];
                    armorContents[1] = contents[2];
                    armorContents[0] = contents[3];
                    inv.setArmorContents(armorContents);

                    inv.setItemInOffHand(contents[4]);
                } else {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(target);

                    ItemStack[] full = new ItemStack[41]; // 36 + 4 + 1 (offhand)
                    System.arraycopy(Arrays.copyOfRange(contents, 9, 45), 0, full, 0, 36);
                    System.arraycopy(Arrays.copyOfRange(contents, 0, 4), 0, full, 36, 4);
                    full[40] = contents[4];
                    String contents_ = ItemStackBase64Converter.ItemStackArrayToBase64(full);

                    currentInventoryMapper.update(offlinePlayer.getUniqueId(), contents_);
                }
            }

            NeoTccInvService.operatingInvNumber.put(target, NeoTccInvService.operatingInvNumber.get(target) - 1);
            if(NeoTccInvService.operatingInvNumber.get(target) == 0) {
                NeoTccInvService.operatingInv.remove(target);
            }
        } else if(title.startsWith("NeoTccInv: EnderChest ")) {
            String target = title.replace("NeoTccInv: EnderChest ", "");
            if(NeoTccInvService.operatingEnderChest.containsKey(target)) {
                ItemStack[] contents = NeoTccInvService.operatingEnderChest.get(target).getContents();
                Player player = Bukkit.getPlayer(target);
                if (player != null) {
                    player.getEnderChest().setContents(contents);
                } else {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(target);

                    currentEnderChestMapper.update(offlinePlayer.getUniqueId(), ItemStackBase64Converter.ItemStackArrayToBase64(contents));
                }
            }

            NeoTccInvService.operatingEnderChestNumber.put(target, NeoTccInvService.operatingEnderChestNumber.get(target) - 1);
            if(NeoTccInvService.operatingEnderChestNumber.get(target) == 0) {
                NeoTccInvService.operatingEnderChest.remove(target);
            }
        }

    }
}
