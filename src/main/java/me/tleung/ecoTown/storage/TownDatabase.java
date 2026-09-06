package me.tleung.ecoTown.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.tleung.ecoTown.EcoTown;
import me.tleung.ecoTown.data.Town;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TownDatabase {
    private final HikariDataSource dataSource;
    private final EcoTown plugin;

    public TownDatabase(EcoTown plugin) {
        this.plugin = plugin;
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        File dbFile = new File(plugin.getDataFolder(), "ecotown.db");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setDriverClassName("org.sqlite.JDBC");
        config.setMaximumPoolSize(1);
        this.dataSource = new HikariDataSource(config);
        createTables();
    }

    private void createTables() {
        String sqlTowns = "CREATE TABLE IF NOT EXISTS towns (name VARCHAR(32) PRIMARY KEY, mayor VARCHAR(36) NOT NULL, spawn_location VARCHAR(255))";
        String sqlClaims = "CREATE TABLE IF NOT EXISTS claims (chunk_key VARCHAR(64) PRIMARY KEY, town_name VARCHAR(32) NOT NULL)";
        String sqlMembers = "CREATE TABLE IF NOT EXISTS town_members (town_name VARCHAR(32), player_uuid VARCHAR(36), PRIMARY KEY(town_name, player_uuid))";

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt1 = conn.prepareStatement(sqlTowns);
                 PreparedStatement stmt2 = conn.prepareStatement(sqlClaims);
                 PreparedStatement stmt3 = conn.prepareStatement(sqlMembers)) {
                stmt1.execute();
                stmt2.execute();
                stmt3.execute();
            }
            // 防呆：自動幫舊版本的 SQLite 加上 spawn_location 欄位
            try (PreparedStatement stmtAlter = conn.prepareStatement("ALTER TABLE towns ADD COLUMN spawn_location VARCHAR(255)")) {
                stmtAlter.execute();
            } catch (SQLException ignored) {
                // 如果欄位已經存在會報錯，我們直接忽略它即可
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("無法建立 EcoTown 資料表: " + e.getMessage());
        }
    }

    public void saveTownSync(Town town) {
        String sql = "INSERT INTO towns (name, mayor) VALUES (?, ?) ON CONFLICT(name) DO NOTHING";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, town.getName());
            stmt.setString(2, town.getMayor().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("城鎮寫入錯誤: " + e.getMessage());
        }
    }

    public void updateSpawnSync(String townName, String spawnLocation) {
        String sql = "UPDATE towns SET spawn_location = ? WHERE name = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, spawnLocation);
            stmt.setString(2, townName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("傳送點寫入錯誤: " + e.getMessage());
        }
    }

    public void claimChunkSync(String chunkKey, String townName) {
        String sql = "INSERT INTO claims (chunk_key, town_name) VALUES (?, ?) ON CONFLICT(chunk_key) DO UPDATE SET town_name=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, chunkKey);
            stmt.setString(2, townName);
            stmt.setString(3, townName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("領地寫入錯誤: " + e.getMessage());
        }
    }

    public void unclaimChunkSync(String chunkKey) {
        String sql = "DELETE FROM claims WHERE chunk_key = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, chunkKey);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("解除領地錯誤: " + e.getMessage());
        }
    }

    public void deleteTownSync(String townName) {
        String sql1 = "DELETE FROM towns WHERE name = ?";
        String sql2 = "DELETE FROM claims WHERE town_name = ?";
        String sql3 = "DELETE FROM town_members WHERE town_name = ?";
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt1 = conn.prepareStatement(sql1);
                 PreparedStatement stmt2 = conn.prepareStatement(sql2);
                 PreparedStatement stmt3 = conn.prepareStatement(sql3)) {
                stmt1.setString(1, townName); stmt1.executeUpdate();
                stmt2.setString(1, townName); stmt2.executeUpdate();
                stmt3.setString(1, townName); stmt3.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                plugin.getLogger().warning("解散城鎮錯誤: " + e.getMessage());
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("解散城鎮連線錯誤: " + e.getMessage());
        }
    }

    public void addMemberSync(String townName, UUID playerUuid) {
        String sql = "INSERT INTO town_members (town_name, player_uuid) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, townName);
            stmt.setString(2, playerUuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("鎮民寫入錯誤: " + e.getMessage());
        }
    }

    public Map<String, Town> loadAllTownsSync() {
        Map<String, Town> loadedTowns = new HashMap<>();
        String sqlTowns = "SELECT name, mayor, spawn_location FROM towns";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlTowns);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String name = rs.getString("name");
                UUID mayor = UUID.fromString(rs.getString("mayor"));
                Town town = new Town(name, mayor);
                town.setSpawnLocation(rs.getString("spawn_location"));
                loadedTowns.put(name, town);
            }
        } catch (SQLException e) {}

        String sqlClaims = "SELECT chunk_key, town_name FROM claims";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlClaims);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Town town = loadedTowns.get(rs.getString("town_name"));
                if (town != null) town.addChunk(rs.getString("chunk_key"));
            }
        } catch (SQLException e) {}

        String sqlMembers = "SELECT town_name, player_uuid FROM town_members";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlMembers);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Town town = loadedTowns.get(rs.getString("town_name"));
                if (town != null) town.addMember(UUID.fromString(rs.getString("player_uuid")));
            }
        } catch (SQLException e) {}

        return loadedTowns;
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) dataSource.close();
    }
}