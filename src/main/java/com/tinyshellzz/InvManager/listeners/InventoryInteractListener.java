package com.tinyshellzz.InvManager.listeners;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class InventoryInteractListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity whoClicked = event.getWhoClicked();
        String title = event.getView().getTitle();
        if(title.startsWith("Viewing ") || title.startsWith("ViewEnder ") || title.startsWith("History ")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        HumanEntity whoClicked = event.getWhoClicked();
        String title = event.getView().getTitle();
        if(title.startsWith("Viewing ") || title.startsWith("ViewEnder ") || title.startsWith("History ")) {
            event.setCancelled(true);
        }
    }
}
