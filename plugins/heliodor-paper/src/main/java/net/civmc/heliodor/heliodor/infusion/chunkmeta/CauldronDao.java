package net.civmc.heliodor.heliodor.infusion.chunkmeta;

import net.civmc.heliodor.HeliodorPlugin;
import net.civmc.heliodor.heliodor.infusion.InfusionManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.CacheState;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.XZWCoord;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.BlockBasedChunkMeta;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.table.TableBasedBlockChunkMeta;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.table.TableBasedDataObject;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.table.TableStorageEngine;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CauldronDao extends TableStorageEngine<CauldronInfusion> {

    private final InfusionManager infusionManager;

    public CauldronDao(Logger logger, ManagedDatasource db, InfusionManager infusionManager) {
        super(logger, db);
        this.infusionManager = infusionManager;
    }

    @Override
    public TableBasedDataObject getForLocation(int x, int y, int z, short worldID, short pluginID) {
        throw new IllegalStateException("Chunk not loaded");
    }

    @Override
    public Collection<XZWCoord> getAllDataChunks() {
        List<XZWCoord> result = new ArrayList<>();
        try (Connection insertConn = db.getConnection();
             PreparedStatement selectChunks = insertConn.prepareStatement(
                 "SELECT chunk_x, chunk_z, world_id FROM cauldrons GROUP BY chunk_x, chunk_z, world_id");
             ResultSet rs = selectChunks.executeQuery()) {
            while (rs.next()) {
                int chunkX = rs.getInt(1);
                int chunkZ = rs.getInt(2);
                short worldID = rs.getShort(3);
                result.add(new XZWCoord(chunkX, chunkZ, worldID));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to select populated chunks from db: ", e);
        }
        return result;
    }

    @Override
    public boolean stayLoaded() {
        return false;
    }

    @Override
    public void registerMigrations() {
        db.registerMigration(1, false, """
            CREATE TABLE IF NOT EXISTS cauldrons (chunk_x INT NOT NULL, chunk_z INT NOT NULL,
            world_id SMALLINT UNSIGNED NOT NULL,
            x_offset TINYINT UNSIGNED NOT NULL, y SMALLINT NOT NULL, z_offset TINYINT UNSIGNED NOT NULL,
            charge INT NOT NULL, max_charge INT NOT NULL,
            INDEX chunk (chunk_x, chunk_z, world_id),
            INDEX pos (x_offset, y, z_offset, world_id),
            CONSTRAINT loc UNIQUE (chunk_x,chunk_z,x_offset,y,z_offset,world_id))
            """);
    }

    @Override
    public void insert(CauldronInfusion data, XZWCoord coord) {
        try (Connection connection = db.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO cauldrons (chunk_x, chunk_z, world_id, x_offset, y, z_offset, charge, max_charge)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """);
            statement.setInt(1, coord.getX());
            statement.setInt(2, coord.getZ());
            statement.setInt(3, coord.getWorldID());
            statement.setByte(4, (byte) BlockBasedChunkMeta.modulo(data.getLocation().getBlockX()));
            statement.setShort(5, (short) data.getLocation().getBlockY());
            statement.setByte(6, (byte) BlockBasedChunkMeta.modulo(data.getLocation().getBlockZ()));
            statement.setInt(7, data.getCharge());
            statement.setInt(8, data.getMaxCharge());
            statement.execute();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to update cauldron in db: ", e);
        }
    }

    @Override
    public void update(CauldronInfusion data, XZWCoord coord) {
        try (Connection connection = db.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("""
                UPDATE cauldrons SET charge = ?, max_charge = ?
                WHERE chunk_x = ? AND chunk_z = ? AND world_id = ? AND x_offset = ? AND y = ? AND z_offset = ?
                """);
            statement.setInt(1, data.getCharge());
            statement.setInt(2, data.getMaxCharge());
            statement.setInt(3, coord.getX());
            statement.setInt(4, coord.getZ());
            statement.setShort(5, coord.getWorldID());
            statement.setByte(6, (byte) BlockBasedChunkMeta.modulo(data.getLocation().getBlockX()));
            statement.setShort(7, (short) data.getLocation().getBlockY());
            statement.setByte(8, (byte) BlockBasedChunkMeta.modulo(data.getLocation().getBlockZ()));
            statement.execute();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to update cauldron in db: ", e);
        }
    }

    @Override
    public void delete(CauldronInfusion data, XZWCoord coord) {
        try (Connection connection = db.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("""
                DELETE FROM cauldrons
                WHERE chunk_x = ? AND chunk_z = ? AND world_id = ? AND x_offset = ? AND y = ? AND z_offset = ?
                """);
            statement.setInt(1, coord.getX());
            statement.setInt(2, coord.getZ());
            statement.setShort(3, coord.getWorldID());
            statement.setByte(4, (byte) BlockBasedChunkMeta.modulo(data.getLocation().getBlockX()));
            statement.setShort(5, (short) data.getLocation().getBlockY());
            statement.setByte(6, (byte) BlockBasedChunkMeta.modulo(data.getLocation().getBlockZ()));
            statement.execute();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to delete cauldron from db: ", e);
        }
    }

    @Override
    public void fill(TableBasedBlockChunkMeta<CauldronInfusion> chunkData, Consumer<CauldronInfusion> insertFunction) {
        int preMultipliedX = chunkData.getChunkCoord().getX() * 16;
        int preMultipliedZ = chunkData.getChunkCoord().getZ() * 16;
        World world = chunkData.getChunkCoord().getWorld();
        List<CauldronInfusion> toUpdate = new ArrayList<>();
        try (Connection connection = db.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("""
                SELECT x_offset, y, z_offset, charge, max_charge
                FROM cauldrons
                WHERE chunk_x = ? and chunk_z = ? and world_id = ?
                """);
            statement.setInt(1, chunkData.getChunkCoord().getX());
            statement.setInt(2, chunkData.getChunkCoord().getZ());
            statement.setShort(3, chunkData.getChunkCoord().getWorldID());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    int xOffset = rs.getByte(1);
                    int x = xOffset + preMultipliedX;
                    int y = rs.getShort(2);
                    int zOffset = rs.getByte(3);
                    int z = zOffset + preMultipliedZ;
                    Location location = new Location(world, x, y, z);
                    int charge = rs.getInt(4);
                    int maxCharge = rs.getInt(5);
                    CauldronInfusion infusion = new CauldronInfusion(location, false, charge, maxCharge);
                    toUpdate.add(infusion);
                    insertFunction.accept(infusion);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to load cauldron from db: ", e);
        }
        Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(HeliodorPlugin.class), () -> {
            for (CauldronInfusion infusion : toUpdate) {
                if (infusion.getCacheState() == CacheState.DELETED) {
                    continue;
                }
                if (!this.infusionManager.addInfusion(infusion)) {
                    JavaPlugin.getPlugin(HeliodorPlugin.class).getChunkMetaView().remove(infusion.getLocation());
                }
            }
        });
    }
}
