package com.tinyshellzz.InvManager.database;

import com.tinyshellzz.InvManager.config.PluginConfig;
import com.tinyshellzz.InvManager.utils.ItemStackBase64Converter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class HistoryInventoryMapper {
    public HistoryInventoryMapper() {
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = MysqlConfig.connect();
            stmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS history_inv (" +
                    "player_uuid Char(36)," +
                    "`time` BIGINT," +
                    "contents LONGTEXT," +
                    "ender LONGTEXT," +
                    "UNIQUE KEY (`time`, player_uuid)" +
                    ") ENGINE=InnoDB CHARACTER SET=utf8;");
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[NeoTccInventoryRecover]HistoryInventoryMapper.HistoryInventoryMapper:" + e.getMessage());
        } finally {
            try {
                if(stmt != null) stmt.close();
                if(rs != null) rs.close();
                if(conn != null) conn.close();
            } catch (SQLException e) {
            }
        }
    }

    private void insert(UUID player_uuid, String contents, String ender) {
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = MysqlConfig.connect();
            stmt = conn.prepareStatement("INSERT INTO history_inv VALUES (?, ?, ?, ?)");
            stmt.setString(1, player_uuid.toString());
            stmt.setLong(2, System.currentTimeMillis() / 1000L);
            stmt.setString(3, contents);
            stmt.setString(4, ender);
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[NeoTccInventoryRecover]HistoryInventoryMapper.insert:" + e.getMessage());
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (rs != null) rs.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
            }
        }
    }

    public void insert(Player player) {
        String contents = ItemStackBase64Converter.PlayerInvToBase64(player);
        String ender = ItemStackBase64Converter.ItemStackArrayToBase64(player.getEnderChest().getContents());
        insert(player.getUniqueId(), contents, ender);
    }

    private String[] get_contents_older_than(UUID player_uuid, long unix_time_stamp) {
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet rs = null;
        String[] ret = null;

        try {
            conn = MysqlConfig.connect();
            conn.commit();
            stmt = conn.prepareStatement("SELECT * FROM history_inv where player_uuid = ? AND `time` > ? LIMIT 1 OFFSET 0");
            stmt.setString(1, player_uuid.toString());
            stmt.setLong(2, unix_time_stamp);
            rs = stmt.executeQuery();

            if(rs.next()) {
                ret = new String[2];
                ret[0] = rs.getString("contents");
                ret[1] = rs.getString("ender");
            }
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[NeoTccInventoryRecover]HistoryInventoryMapper.get:" + e.getMessage());
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

    public String[] get_contents_before_x_seconds(UUID player_uuid, int seconds) {
        return get_contents_older_than(player_uuid, System.currentTimeMillis() / 1000L - seconds);
    }

    private void delete_data_older_than(long unix_time_stamp) {
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = MysqlConfig.connect();
            stmt = conn.prepareStatement("DELETE FROM co_block WHERE time<?  LIMIT 100000");
            stmt.setLong(1, unix_time_stamp);
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "HistoryInventoryMapper.delete_data_older_than:" + e.getMessage());
        } finally {
            try {
                if(stmt != null) stmt.close();
                if(rs != null) rs.close();
                if(conn != null) conn.close();
            } catch (SQLException e) {
            }
        }
    }

    public void clean_data_older_then(int day) {
        delete_data_older_than(System.currentTimeMillis() / 1000L - 86400L * PluginConfig.clean_data_older_than);
    }
}
