/**
 * Created by Aleksey Terzi
 */

package com.aleksey.castlegates.engine;

import com.aleksey.castlegates.database.SqlDatabase;
import com.aleksey.castlegates.types.*;
import com.aleksey.castlegates.utils.DataWorker;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class StorageManager {
    private Map<BlockCoord, Gearblock> _gearblocks;
    private DataWorker _dataWorker;

    public void init(SqlDatabase db) throws SQLException {
        _dataWorker = new DataWorker(db);

        _gearblocks = _dataWorker.load();

        _dataWorker.startThread();
    }

    public void close() {
        if(_dataWorker != null) {
            _dataWorker.close();
            _dataWorker = null;
        }
    }

    public Gearblock getGearblock(BlockCoord location) {
        return _gearblocks.get(location);
    }

    public boolean hasGearblock(BlockCoord location) {
        return _gearblocks.containsKey(location);
    }

    public void addGearblock(BlockCoord location) {
        Gearblock gearblock = new Gearblock(location);

        _gearblocks.put(location, gearblock);

        _dataWorker.addChangedGearblock(gearblock);
    }

    public void removeGearblock(Gearblock gearblock) {
        _gearblocks.remove(gearblock.getCoord());

        gearblock.setRemoved();

        _dataWorker.addChangedGearblock(gearblock);
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

        _dataWorker.addChangedLink(link);
    }

    public void removeLink(GearblockLink link) {
        link.setRemoved();

        if(link.getGearblock1() != null) {
            link.getGearblock1().setLink(null);
        }

        if(link.getGearblock2() != null) {
            link.getGearblock2().setLink(null);
        }

        _dataWorker.addChangedLink(link);
    }

    public void setLinkBlocks(GearblockLink link, List<BlockState> blocks) {
        link.setBlocks(blocks);
        _dataWorker.addChangedLink(link);
    }

    public void setLinkBroken(Gearblock gearblock) {
        gearblock.getLink().setBroken(gearblock);
        _dataWorker.addChangedLink(gearblock.getBrokenLink());
    }

    public void setGearblockTimer(Gearblock gearblock, Integer timer, TimerOperation timerOperation, TimerMode timerMode) {
        gearblock.setTimer(timer, timerOperation, timerMode);

        _dataWorker.addChangedGearblock(gearblock);
    }

    public void clearGearblockTimer(Gearblock gearblock) {
        gearblock.setTimer(null, null, null);

        _dataWorker.addChangedGearblock(gearblock);
    }
}