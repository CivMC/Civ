package vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.table;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.CacheState;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.BlockBasedChunkMeta;

public abstract class TableBasedBlockChunkMeta<D extends TableBasedDataObject>
    extends BlockBasedChunkMeta<TableBasedDataObject, TableStorageEngine<D>> {

    private static final Logger CHUNK_META_LOGGER = LogManager.getLogger("Chunk meta");

    private final List<D> modifiedEntries = new ArrayList<>();

    public TableBasedBlockChunkMeta(boolean isNew, TableStorageEngine<D> storage) {
        super(isNew, storage);
    }

    public void reportChange(D data) {
        synchronized (modifiedEntries) {
            modifiedEntries.add(data);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void put(int x, int y, int z, TableBasedDataObject blockData, boolean isNew) {
        super.put(x, y, z, blockData, isNew);
        if (isNew) {
            synchronized (modifiedEntries) {
                modifiedEntries.add((D) blockData);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void remove(TableBasedDataObject blockData) {
        super.remove(blockData);
        blockData.setCacheState(CacheState.DELETED);
        //this may look weird, but is what happens if the data was NEW previously, never written to the
        //db and doesn't need to be deleted from there either
        if (blockData.getCacheState() != CacheState.NORMAL) {
            synchronized (modifiedEntries) {
                modifiedEntries.add((D) blockData);
            }
        }
    }

    @Override
    protected TableBasedDataObject remove(int x, int y, int z) {
        TableBasedDataObject data = super.remove(x, y, z);
        if (data != null) {
            data.setCacheState(CacheState.DELETED);
        }
        return data;
    }

    @Override
    public void insert() {
        CHUNK_META_LOGGER.debug("Inserting at " + chunkCoord);
        List<D> datas;
        synchronized (modifiedEntries) {
            datas = new ArrayList<>(modifiedEntries);
            modifiedEntries.clear();
        }
        for (D data : datas) {
            switch (data.getCacheState()) {
                case NORMAL:
                    continue;
                case MODIFIED:
                    storage.update(data, chunkCoord);
                    break;
                case NEW:
                    storage.insert(data, chunkCoord);
                    break;
                case DELETED:
                    storage.delete(data, chunkCoord);
            }
            data.setCacheState(CacheState.NORMAL);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void delete() {
        iterateAll(d -> {
            storage.delete((D) d, chunkCoord);
        });
    }

    @Override
    public void update() {
        insert();
    }

    @Override
    public void populate() {
        storage.fill(this, data -> {
            Location loc = data.getLocation();
            put(modulo(loc.getBlockX()), loc.getBlockY(), modulo(loc.getBlockZ()), data, false);
        });
    }

}
