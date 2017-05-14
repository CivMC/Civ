/**
 * Created by Aleksey Terzi
 */

package com.aleksey.castlegates.engine;

import com.aleksey.castlegates.CastleGates;
import com.aleksey.castlegates.database.SqlDatabase;
import com.aleksey.castlegates.types.*;
import com.aleksey.castlegates.utils.DataWorker;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class StorageManager {
    private Map<BlockCoord, Gearblock> gearblocks;
    private DataWorker dataWorker;

    public void init(SqlDatabase db) throws SQLException {
        this.dataWorker = new DataWorker(db, CastleGates.getConfigManager().getLogChanges());
        this.gearblocks = this.dataWorker.load();

        CastleGates.getPluginLogger().log(Level.INFO, "Loaded " + this.gearblocks.size() + " gearblocks");

        this.dataWorker.startThread();
    }

    public void close() {
        if(this.dataWorker != null) {
            this.dataWorker.close();
        }
    }

    public Gearblock getGearblock(BlockCoord location) {
        return this.gearblocks.get(location);
    }

    public boolean hasGearblock(BlockCoord location) {
        return this.gearblocks.containsKey(location);
    }

    public void addGearblock(BlockCoord location) {
        Gearblock gearblock = new Gearblock(location);

        this.gearblocks.put(location, gearblock);

        this.dataWorker.addChangedGearblock(gearblock);
    }

    public void removeGearblock(Gearblock gearblock) {
        this.gearblocks.remove(gearblock.getCoord());

        gearblock.setRemoved();

        this.dataWorker.addChangedGearblock(gearblock);
    }

    public void addLink(Gearblock gearblock1, Gearblock gearblock2) {
        GearblockLink link;

        if(gearblock1.getBrokenLink() != null) {
            link = gearblock1.getBrokenLink();
            link.setRestored(gearblock2);
        }
        else if(gearblock2.getBrokenLink() != null) {
            link = gearblock2.getBrokenLink();
            link.setRestored(gearblock1);
        }
        else {
            link = new GearblockLink(gearblock1, gearblock2);
        }

        gearblock1.setLink(link);
        gearblock2.setLink(link);

        this.dataWorker.addChangedLink(link);
    }

    public void removeLink(GearblockLink link) {
        link.setRemoved();

        if(link.getGearblock1() != null) {
            link.getGearblock1().setLink(null);
        }

        if(link.getGearblock2() != null) {
            link.getGearblock2().setLink(null);
        }

        this.dataWorker.addChangedLink(link);
    }

    public void setLinkBlocks(GearblockLink link, List<BlockState> blocks) {
        link.setBlocks(blocks);
        this.dataWorker.addChangedLink(link);
    }

    public void setLinkBroken(Gearblock gearblock) {
        gearblock.getLink().setBroken(gearblock);
        this.dataWorker.addChangedLink(gearblock.getBrokenLink());
    }

    public void setGearblockTimer(Gearblock gearblock, Integer timer, TimerOperation timerOperation) {
        gearblock.setTimer(timer, timerOperation);

        this.dataWorker.addChangedGearblock(gearblock);
    }

    public void clearGearblockTimer(Gearblock gearblock) {
        gearblock.setTimer(null, null);

        this.dataWorker.addChangedGearblock(gearblock);
    }
}