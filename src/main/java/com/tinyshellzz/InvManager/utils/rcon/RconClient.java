package com.tinyshellzz.InvManager.utils.rcon;

import java.io.IOException;
import java.util.logging.Level;

public class RconClient {
    private String server_ip;  // Replace with your server IP
    private int rcon_port;           // Use the port from server.properties
    private String rcon_password;

    public RconClient(String server_ip, int rcon_port, String rcon_password) {
        this.server_ip = server_ip;
        this.rcon_port = rcon_port;
        this.rcon_password = rcon_password;
    }

    /**
     * usage: sendRconCommand("kick player_name");
     * @param command
     */
    public void sendRconCommand(String command) {
        // Attempt a connection
        ServerAPI api = ServerAPI.get();
        try {
            api.connect(server_ip, rcon_port);
        } catch (IOException e) {
            MyLogger.getLogger("net.ddns.jsonet.rcon").log(Level.SEVERE, "Failed to establish connection to " + server_ip + ":" + rcon_port, e);
            System.exit(1);
        }

        // Authenticate
        try {
            int reqId = api.authenticate(rcon_password);
            ServerAPI.Packet p = api.parsePacket();
            if (p.getRequestID() == -1 && p.getRequestID() != reqId) {
                // Auth failed
                MyLogger.getLogger("net.ddns.jsonet.rcon").severe("Auth failed: Invalid passphrase");
                System.exit(1);
            }
        } catch (IOException e) {
            MyLogger.getLogger("net.ddns.jsonet.rcon").log(Level.SEVERE, "Failed to authenticate with " + server_ip + ":" + rcon_port, e);
            System.exit(1);
        }
        MyLogger.getLogger("net.ddns.jsonet.rcon").log(Level.INFO, "Authentication successful");


        // TODO this won't allow commands to contain and instances of cmdDelimiter

        issueCommand(command);
    }

    private void issueCommand(String cmd) {
        ServerAPI api = ServerAPI.get();
        int requestId;
        String resp = "";
        try {
            if(cmd.startsWith("/")) {
                cmd = cmd.substring(1);
            }
            requestId = api.sendCommand(cmd);
        } catch (IOException e) {
            MyLogger.getLogger("net.ddns.jsonet.rcon").log(Level.SEVERE, "Failed to send command '"+cmd+"'", e);
        }

        // 无法获取返回数据
//        try {
//            ServerAPI.Packet p = api.parsePacket();
//            if (p.getRequestID() == requestId) {
//                resp = new String(p.getRawData(), Charsets.UTF_8);
//            }
//        } catch (IOException e) {
//            MyLogger.getLogger("net.ddns.jsonet.rcon").log(Level.SEVERE, "Failed to parse response packet", e);
//        }
    }
}
