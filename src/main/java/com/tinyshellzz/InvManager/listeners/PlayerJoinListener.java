package com.tinyshellzz.InvManager.listeners;

import com.tinyshellzz.InvManager.config.PluginConfig;
import com.tinyshellzz.InvManager.entities.MCPlayer;
import com.tinyshellzz.InvManager.services.NeoTccInvService;
import com.tinyshellzz.InvManager.utils.MyUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.tinyshellzz.InvManager.ObjectPool.*;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        MCPlayer userByUuid = mcPlayerMapper.get_user_by_uuid(player.getUniqueId().toString());
        plugin.getServer().getAsyncScheduler().runNow(plugin, () -> {
            if(userByUuid != null) {
                // 0和-2代表非正常重启, -1代表正常离线, 1代表正常重启
                // 正常重启且切换服务器, 以及离线玩家, 可以从数据库加载背包
                if (userByUuid.shutdown == -1 || (userByUuid.server_id != PluginConfig.server_id && userByUuid.shutdown == 1)) {
                    currentInventoryMapper.load_current_content(player);
                    currentEnderChestMapper.load_current_content(player);
    
                    if (!currentInventoryMapper.exists(player.getUniqueId())) {
                        currentInventoryMapper.insert(player);
                    }
                    if (!currentEnderChestMapper.exists(player.getUniqueId())) {
                        currentEnderChestMapper.insert(player);
                    }
                }
            }
    
            String target = player.getName().toLowerCase();
            if(NeoTccInvService.editingInv.containsKey(target)) {
                ItemStack[] contents = NeoTccInvService.editingInv.get(target).getContents();
                PlayerInventory inv = player.getInventory();
                inv.setContents(Arrays.copyOfRange(contents, 9, 45));
                inv.setArmorContents(Arrays.copyOfRange(contents, 0, 4));
                inv.setItemInOffHand(contents[4]);
    
                MyUtil.closeInventory(NeoTccInvService.editingInv.get(target));
                NeoTccInvService.editingInv.remove(target);
                NeoTccInvService.editingInvNumber.remove(target);
            }
            if(NeoTccInvService.editingEnderChest.containsKey(target)) {
                ItemStack[] contents = NeoTccInvService.editingEnderChest.get(target).getContents();
                player.getEnderChest().setContents(contents);
    
                MyUtil.closeInventory(NeoTccInvService.editingEnderChest.get(target));
                NeoTccInvService.editingEnderChest.remove(target);
                NeoTccInvService.editingEnderChestNumber.remove(target);
            }
    
            mcPlayerMapper.update_player_name(player);      // shutdown 重置为0
        });
    }
}
