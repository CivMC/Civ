package vg.civcraft.mc.civduties.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.nbt.CompoundTag;
import vg.civcraft.mc.civduties.CivDuties;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.nbt.NbtUtils;

public class DatabaseManager {

    private CivDuties plugin;
    private ManagedDatasource db;

    private Map<UUID, PlayerData> playersDataCache = new ConcurrentHashMap<>();

    public DatabaseManager(ManagedDatasource db) {
        this.plugin = CivDuties.getInstance();
        this.db = db;
        registerMigrations();
        db.updateDatabase();
    }

    private void registerMigrations() {
        db.registerMigration(1, false,
            "create table if not exists DutiesPlayerData(uuid varchar(36) not null,entity blob, "
                + "serverName varchar(256) not null, tierName varchar(256) not null, primary key (uuid));");
    }

    public void savePlayerData(UUID uuid, CompoundTag compound, String serverName, String tierName) {
        try (Connection conn = db.getConnection();
             PreparedStatement addPlayerData = conn.prepareStatement(
                 "insert into DutiesPlayerData(uuid, entity, serverName, tierName) values(?,?,?,?);")) {
            addPlayerData.setString(1, uuid.toString());
            addPlayerData.setBytes(2, NbtUtils.toBytes(compound));
            addPlayerData.setString(3, serverName);
            addPlayerData.setString(4, tierName);
            addPlayerData.execute();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to insert player data " + e.toString());
        }
    }

    public PlayerData getPlayerData(UUID uuid) {
        if (playersDataCache.containsKey(uuid)) {
            return playersDataCache.get(uuid);
        }
        try (Connection conn = db.getConnection();
             PreparedStatement getPlayerData = conn
                 .prepareStatement("select * from DutiesPlayerData where uuid = ?")) {
            getPlayerData.setString(1, uuid.toString());
            try (ResultSet rs = getPlayerData.executeQuery()) {
                if (rs.next()) {
                    CompoundTag compound = NbtUtils.fromBytes(rs.getBytes("entity"));
                    String server = rs.getString("serverName");
                    String tierName = rs.getString("tierName");
                    PlayerData data = new PlayerData(compound, server, tierName);
                    playersDataCache.put(uuid, data);
                    return data;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to retrieve player data " + e.toString());
        }
        return null;
    }

    public void removePlayerData(UUID uuid) {
        playersDataCache.remove(uuid);
        try (Connection conn = db.getConnection();
             PreparedStatement removePlayerData = conn
                 .prepareStatement("delete from DutiesPlayerData where uuid = ?")) {
            removePlayerData.setString(1, uuid.toString());
            removePlayerData.execute();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to remove player data " + e.toString());
        }
    }
}
