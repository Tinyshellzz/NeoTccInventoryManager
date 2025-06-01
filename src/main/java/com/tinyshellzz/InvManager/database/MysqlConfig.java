package com.tinyshellzz.InvManager.database;

import com.tinyshellzz.InvManager.config.PluginConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class MysqlConfig {
    public static Connection connect() throws SQLException {
        String database = String.format("jdbc:mysql://%s:%s/%s", PluginConfig.db_host, PluginConfig.db_port, PluginConfig.db_database);
        Connection conn = DriverManager.getConnection(database, PluginConfig.db_user, PluginConfig.db_passwd);
        conn.setAutoCommit(false);
        return conn;
    }
}
