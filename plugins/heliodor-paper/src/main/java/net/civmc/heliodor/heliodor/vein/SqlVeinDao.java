package net.civmc.heliodor.heliodor.vein;

import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import java.util.List;
import java.util.Map;

public class SqlVeinDao implements VeinDao {

    private final ManagedDatasource source;

    public SqlVeinDao(ManagedDatasource source) {
        this.source = source;
    }

    public void registerMigrations() {
        source.registerMigration(2, false, """
            CREATE TABLE IF NOT EXISTS veins (
            type VARCHAR(64) NOT NULL,
            spawned_at TIMESTAMP NOT NULL,
            world VARCHAR(64) NOT NULL,
            radius INT NOT NULL,
            x INT NOT NULL,
            y INT NOT NULL,
            z INT NOT NULL,
            offset_x INT NOT NULL,
            offset_y INT NOT NULL,
            offset_z INT NOT NULL,
            blocks_available_estimate INT NOT NULL,
            blocks_mined INT NOT NULL,
            discovered BOOL NOT NULL,
            ores INT NOT NULL,
            INDEX chunk (chunk_x, chunk_z, world_id),
            INDEX pos (x_offset, y, z_offset, world_id),
            CONSTRAINT loc UNIQUE (chunk_x,chunk_z,x_offset,y,z_offset,world_id))
            """);
    }

    @Override
    public List<String> getSpawnableTypes(Map<String, Integer> spawnFrequencyMinutes, Map<String, Integer> maxSpawns) {
        return null;
    }
}
