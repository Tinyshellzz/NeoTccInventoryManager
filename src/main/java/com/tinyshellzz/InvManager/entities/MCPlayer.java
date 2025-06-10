package com.tinyshellzz.InvManager.entities;

import java.util.UUID;

public class MCPlayer {
    public String name;
    public UUID uuid;
    public int shutdown;    // 0和-2代表非正常重启, -1代表正常离线, 1代表正常重启
    public int server_id;

    public MCPlayer(String name, UUID uuid, int shutdown, int server_id) {
        this.name = name.toLowerCase();
        this.uuid = uuid;
        this.shutdown = shutdown;
        this.server_id = server_id;
    }
}
