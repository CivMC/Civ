package com.programmerdan.minecraft.banstick.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class BSExclusions {

    private BSExclusions() {
    }

    private BSPlayer forPlayer;

    private List<BSExclusion> exclusionList;

    public static BSExclusions onlyFor(BSPlayer player) {
        BSExclusions exclusions = new BSExclusions();
        exclusions.forPlayer = player;
        return exclusions;
    }

    public List<BSExclusion> getAll() {
        if (exclusionList == null) {
            fill();
        }
        return new LinkedList<>(exclusionList);
    }

    /**
     * @return Set containing player ids of all players this one has an exclusion with
     */
    public Set<Long> getExcludedPlayerIDs() {
        Set <Long> pids = new HashSet<>();
        for(BSExclusion excl : getAll()) {
            pids.add(excl.getFirstPlayerID());
            pids.add(excl.getSecondPlayerID());
        }
        pids.remove(forPlayer.getId());
        return pids;
    }

    /**
     * @return Set containing all players this one has an exclusion with
     */
    public Set<BSPlayer> getExcludedPlayers() {
        Set <BSPlayer> pids = new HashSet<>();
        for(BSExclusion excl : getAll()) {
            pids.add(excl.getFirstPlayer());
            pids.add(excl.getSecondPlayer());
        }
        pids.remove(forPlayer);
        return pids;
    }

    public boolean hasExclusionWith(BSPlayer player) {
        return getExclusionWith(player) != null;
    }

    public BSExclusion getExclusionWith(BSPlayer player) {
        for (BSExclusion excl : getAll()) {
            if (excl.getFirstPlayerID() == player.getId() || excl.getSecondPlayerID() == player.getId()) {
                return excl;
            }
        }
        return null;
    }

    public void remove(BSExclusion excl) {
    	if (exclusionList == null) {
            fill();
        }
        exclusionList.remove(excl);
    }

    private void fill() {
        exclusionList = new ArrayList<>();
        exclusionList.addAll(BSExclusion.byPlayer(forPlayer).values());
    }

    public void addNew(BSExclusion excl) {
        if (exclusionList == null) {
            fill();
        }
        this.exclusionList.add(excl);

    }

    public int getOrdinality() {
        return getAll().size();
    }
}
