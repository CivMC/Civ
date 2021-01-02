package com.programmerdan.minecraft.banstick.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * A list of exclusions for a particular player.
 * 
 * @author Maxopoly
 *
 */
public final class BSExclusions {

    private BSPlayer forPlayer;
    private List<BSExclusion> exclusionList;

    private BSExclusions() {
    }

    /**
     * Get the exclusions list for a particular player.
     * 
     * @param player The player to retrieve, as a BSPlayer
     * @return a new BSExclusions for this player.
     */
    public static BSExclusions onlyFor(BSPlayer player) {
        BSExclusions exclusions = new BSExclusions();
        exclusions.forPlayer = player;
        return exclusions;
    }

    /**
     * @return a new list of exclusions (clone)
     */
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
        Set<Long> pids = new HashSet<>();
        for (BSExclusion excl : getAll()) {
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
        Set<BSPlayer> pids = new HashSet<>();
        for (BSExclusion excl : getAll()) {
            pids.add(excl.getFirstPlayer());
            pids.add(excl.getSecondPlayer());
        }
        pids.remove(forPlayer);
        return pids;
    }

    public boolean hasExclusionWith(BSPlayer player) {
        return getExclusionWith(player) != null;
    }

    /**
     * @param player to retrieve mutual exclusions
     * @return an exclusion that covers given player and this BSExclusions' player, 
     *     or null if none found.
     */
    public BSExclusion getExclusionWith(BSPlayer player) {
    	if (forPlayer.getId() == player.getId()) {
    		return null;
    	}
        for (BSExclusion excl : getAll()) {
            if (excl.getFirstPlayerID() == player.getId() || excl.getSecondPlayerID() == player.getId()) {
                return excl;
            }
        }
        return null;
    }

    /**
     * Removes a particular exclusion from the exclusion list.
     * @param excl the exclusion to remove. Removes by object reference
     */
    public void remove(BSExclusion excl) {
    	if (exclusionList == null) {
            fill();
        }
        exclusionList.remove(excl);
    }

    /**
     * Resets and refills the exclusion list.
     */
    private void fill() {
        exclusionList = new ArrayList<>();
        exclusionList.addAll(BSExclusion.byPlayer(forPlayer).values());
    }

    /**
     * Adds a new exclusion to the exclusion list. Does not check if already there.
     * 
     * @param excl the exclusion to add. Adds by object reference
     */
    public void addNew(BSExclusion excl) {
        if (exclusionList == null) {
            fill();
        }
        this.exclusionList.add(excl);

    }

    /**
     * @return the size of the Exclusions list.
     */
    public int getOrdinality() {
        if (exclusionList == null) {
        	fill();
        }
        return this.exclusionList.size();
    }
}
