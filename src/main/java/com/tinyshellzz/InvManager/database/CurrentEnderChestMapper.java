package com.tinyshellzz.InvManager.database;

import com.tinyshellzz.InvManager.config.PluginConfig;
import com.tinyshellzz.InvManager.utils.ItemStackBase64Converter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class CurrentEnderChestMapper {
    public CurrentEnderChestMapper() {
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = MysqlConfig.connect();
            stmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS current_ender_chest (" +
                    "player_uuid Char(36)," +
                    "contents LONGTEXT," +
                    "UNIQUE KEY (player_uuid)" +
                    ") ENGINE=InnoDB CHARACTER SET=utf8;");
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[NeoTccInventoryRecover]CurrentEnderChestMapper.CurrentEnderChestMapper:" + e.getMessage());
        } finally {
            try {
                if(stmt != null) stmt.close();
                if(rs != null) rs.close();
                if(conn != null) conn.close();
            } catch (SQLException e) {
            }
        }
    }

    public void insert(UUID player_uuid, String contents) {
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = MysqlConfig.connect();
            stmt = conn.prepareStatement("INSERT INTO current_ender_chest VALUES (?, ?)");
            stmt.setString(1, player_uuid.toString());
            stmt.setString(2, contents);
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[NeoTccInventoryRecover]CurrentEnderChestMapper.insert:" + e.getMessage());
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (rs != null) rs.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
            }
        }
    }

    /**
     * 用于出现问题时，回档
     */
    public ArrayList<String> get_empty_uuids() {
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet rs = null;
        ArrayList<String> ret = new ArrayList<>();

        try {
            conn = MysqlConfig.connect();
            conn.commit();
            stmt = conn.prepareStatement("SELECT * FROM current_ender_chest where cotents = ?");
            stmt.setString(1, "rO0ABXA=,rO0ABXA=,rO0ABXA=,rO0ABXA=,rO0ABXA=,rO0ABXA=,rO0ABXA=,rO0ABXA=,rO0ABXA=,rO0ABXA=,rO0ABXA=,rO0ABXA=,rO0ABXA=,rO0ABXA=,rO0ABXA=,rO0ABXA=,rO0ABXA=,rO0ABXA=,rO0ABXA=,rO0ABXA=,rO0ABXA=,rO0ABXA=,rO0ABXA=,rO0ABXA=,rO0ABXA=,rO0ABXA=,rO0ABXA=");
            rs = stmt.executeQuery();

            while(rs.next()) {
                ret.add(rs.getString(2));
            }
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[NeoTccInventoryRecover]CurrentEnderChestMapper.get_empty_uuids:" + e.getMessage());
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (rs != null) rs.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
            }
        }

        return ret;
    }

    public void insert(Player player) {
        String contents = ItemStackBase64Converter.ItemStackArrayToBase64(player.getEnderChest().getContents());
        insert(player.getUniqueId(), contents);
    }

    public boolean exists(UUID player_uuid) {
        return get(player_uuid) != null;
    }

    public String get(UUID player_uuid) {
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet rs = null;
        String ret = null;

        try {
            conn = MysqlConfig.connect();
            conn.commit();
            stmt = conn.prepareStatement("SELECT * FROM current_ender_chest where player_uuid = ?");
            stmt.setString(1, player_uuid.toString());
            rs = stmt.executeQuery();

            if(rs.next()) {
                ret = rs.getString(2);
            }
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[NeoTccInventoryRecover]CurrentEnderChestMapper.get:" + e.getMessage());
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (rs != null) rs.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
            }
        }

        return ret;
    }

    public void load_current_content(Player player) {
        String contents = get(player.getUniqueId());
        if(contents == null) return;
        player.getEnderChest().setContents(ItemStackBase64Converter.Base64ToItemStackArray(contents));
    }

    public void update(UUID player_uuid, String contents) {
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = MysqlConfig.connect();
            stmt = conn.prepareStatement("UPDATE current_ender_chest SET contents = ? WHERE player_uuid = ?");
            stmt.setString(1, contents);
            stmt.setString(2, player_uuid.toString());
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[NeoTccInventoryRecover]CurrentEnderChestMapper.update:" + e.getMessage());
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (rs != null) rs.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
            }
        }
    }

    public void update(Player player) {
        String contents = ItemStackBase64Converter.ItemStackArrayToBase64(player.getEnderChest().getContents());
        update(player.getUniqueId(), contents);
    }
}
