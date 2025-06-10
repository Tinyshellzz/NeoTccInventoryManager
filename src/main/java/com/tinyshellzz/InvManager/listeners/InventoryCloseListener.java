package com.tinyshellzz.InvManager.listeners;

import com.tinyshellzz.InvManager.config.PluginConfig;
import com.tinyshellzz.InvManager.entities.MCPlayer;
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

import static com.tinyshellzz.InvManager.ObjectPool.*;

public class InventoryCloseListener implements Listener {
    @EventHandler
    public void InventoryClose(InventoryCloseEvent event) {
        String title = event.getView().getTitle();
        HumanEntity p = event.getPlayer();
        if(!p.isOp() && !p.hasPermission("NeoTccInv.use")) {
            return;
        }

        if(title.startsWith("EditInv ")) {
            String target = title.replace("EditInv ", "").toLowerCase();

            NeoTccInvService.editingInvNumber.put(target, NeoTccInvService.editingInvNumber.get(target) - 1);
            if(NeoTccInvService.editingInvNumber.get(target) == 0) {

                ItemStack[] contents = NeoTccInvService.editingInv.get(target).getContents();
                if(PluginConfig.debug) Bukkit.getConsoleSender().sendMessage(target);

                Player player = Bukkit.getPlayer(target);
                if (player != null) {   // 玩家在线
                    PlayerInventory inv = player.getInventory();
                    inv.setContents(Arrays.copyOfRange(contents, 9, 45));
                    inv.setArmorContents(Arrays.copyOfRange(contents, 0, 4));
                    inv.setItemInOffHand(contents[4]);
                } else {
                    MCPlayer userByName = mcPlayerMapper.get_user_by_name(target);

                    ItemStack[] full = new ItemStack[41]; // 36 + 4 + 1 (offhand)
                    System.arraycopy(Arrays.copyOfRange(contents, 9, 45), 0, full, 0, 36);
                    System.arraycopy(Arrays.copyOfRange(contents, 0, 4), 0, full, 36, 4);
                    full[40] = contents[4];
                    String contents_ = ItemStackBase64Converter.ItemStackArrayToBase64(full);

                    currentInventoryMapper.update(userByName.uuid, contents_);
                    mcPlayerMapper.update_shutdown_by_uuid(userByName.uuid, -1);    // 让更改生效
                }

                if(PluginConfig.debug) Bukkit.getConsoleSender().sendMessage("保存更改的背包内容 " + target);
                NeoTccInvService.editingInv.remove(target);
            }
        } else if(title.startsWith("EditEnder ")) {
            String target = title.replace("EditEnder ", "").toLowerCase();

            NeoTccInvService.editingEnderChestNumber.put(target, NeoTccInvService.editingEnderChestNumber.get(target) - 1);
            if(NeoTccInvService.editingEnderChestNumber.get(target) == 0) {
                ItemStack[] contents = NeoTccInvService.editingEnderChest.get(target).getContents();
                Player player = Bukkit.getPlayer(target);
                if (player != null) {   // 玩家在线
                    player.getEnderChest().setContents(contents);
                } else {
                    MCPlayer userByName = mcPlayerMapper.get_user_by_name(target);

                    currentEnderChestMapper.update(userByName.uuid, ItemStackBase64Converter.ItemStackArrayToBase64(contents));
                    mcPlayerMapper.update_shutdown_by_uuid(userByName.uuid, -1);    // 让更改生效
                }

                if(PluginConfig.debug) Bukkit.getConsoleSender().sendMessage("保存更改的末影箱内容 " + target);
                NeoTccInvService.editingEnderChest.remove(target);
            }
        } else if(title.startsWith("Viewing ")) {
            String target = title.replace("Viewing ", "").toLowerCase();
            NeoTccInvService.operatingInvNumber.put(target, NeoTccInvService.operatingInvNumber.get(target) - 1);
            if(NeoTccInvService.operatingInvNumber.get(target) == 0) {
                NeoTccInvService.operatingInv.remove(target);
            }
        } else if(title.startsWith("ViewEnder ")) {
            String target = title.replace("ViewEnder ", "").toLowerCase();
            NeoTccInvService.operatingEnderChestNumber.put(target, NeoTccInvService.operatingEnderChestNumber.get(target) - 1);
            if(NeoTccInvService.operatingEnderChestNumber.get(target) == 0) {
                NeoTccInvService.operatingEnderChest.remove(target);
            }
        }

    }
}
