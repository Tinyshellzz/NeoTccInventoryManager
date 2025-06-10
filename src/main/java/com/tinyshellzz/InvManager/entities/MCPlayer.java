package com.tinyshellzz.separatedLootChest.entity;

import java.util.UUID;

public class MCPlayer {
    public String name;
    public UUID uuid;
    public int loot_chest_opened;

    public MCPlayer(String name, UUID uuid, int loot_chest_opened) {
        this.name = name.toLowerCase();
        this.uuid = uuid;
        this.loot_chest_opened = loot_chest_opened;
    }
}
