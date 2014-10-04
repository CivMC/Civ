package com.untamedears.JukeAlert.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.GroupManager;
import com.untamedears.citadel.entity.Faction;
import com.untamedears.citadel.entity.FactionMember;
import com.untamedears.citadel.entity.Moderator;
import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.manager.ConfigManager;
import com.untamedears.JukeAlert.util.IgnoreList;

// An Iterator that produces the Players associated with a specific Citadel
//  group. First the founder, then the moderators, and lastly the members.
//  A maximum number of players to return can be configured. Also a maximum
//  Player distance from a specific location can be configured. Each instance
//  of this class can only be iterated over once and once iteration starts
//  the configurable values can't be changed.
public class OnlineGroupMembers implements Iterable<Player>, Iterator<Player> {
    public static OnlineGroupMembers get(String groupName) {
        return new OnlineGroupMembers(groupName);
    }

    private GroupManager manager_;
    private String groupName_;
    private Iterator<Moderator> mods_iter_ = null;
    private Iterator<FactionMember> member_iter_ = null;
    private Player next_ = null;
    private int state_ = 0;
    private int returnedCount_ = 0;
    private Double maxDistance_;
    private int maxPlayers_;
    private Location referenceLocation_ = null;
    private boolean alreadyIterating_ = false;
	private Set<UUID> skipList_= null;

    public OnlineGroupMembers(String groupName) {
        manager_ = Citadel.getGroupManager();
        groupName_ = groupName;

        ConfigManager config = JukeAlert.getInstance().getConfigManager();
        maxDistance_ = config.getMaxAlertDistanceAll();
        maxPlayers_ = config.getMaxPlayerAlertCount();
    }

    public OnlineGroupMembers maxPlayers(Integer value) {
        if (alreadyIterating_) {
            throw new UnsupportedOperationException();
        }
        if (value != null) {
            maxPlayers_ = value;
        }
        return this;
    }

    public OnlineGroupMembers maxDistance(Double value) {
        if (alreadyIterating_) {
            throw new UnsupportedOperationException();
        }
        if (value != null) {
            maxDistance_ = value;
        }
        return this;
    }

    public OnlineGroupMembers reference(Location loc) {
        if (alreadyIterating_) {
            throw new UnsupportedOperationException();
        }
        referenceLocation_ = loc;
        return this;
    }
    
    public OnlineGroupMembers skipList(Set<UUID> list) {

        if (alreadyIterating_) {
            throw new UnsupportedOperationException();
        }
        skipList_ = list;
    	
		return this;
    	
    }

    @Override  // Iterator<Player>
    public Player next() {
        startIterating();
        if (next_ == null) {
            throw new NoSuchElementException();
        }
        Player retval = next_;
        next_ = getNextPlayer();
        return retval;
    }

    @Override  // Iterator<Player>
    public boolean hasNext() {
        startIterating();
        return next_ != null;
    }

    @Override  // Iterator<Player>
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override  // Iterable<Player>
    public Iterator<Player> iterator() {
        if (alreadyIterating_) {
            throw new UnsupportedOperationException();
        }
        startIterating();
        return this;
    }

    private void startIterating() {
        if (!alreadyIterating_) {
            alreadyIterating_ = true;
            next_ = getNextPlayer();
        }
    }

    private Player getFounder() {
        Faction group = manager_.getGroup(groupName_);
        if (group != null) {
            return Bukkit.getPlayerExact(group.getFounderName());
        }
        return null;
    }

    private Player getNextModerator() {
        if (mods_iter_ == null) {
            Set<Moderator> mods = manager_.getModeratorsOfGroup(groupName_);
            mods_iter_ = mods.iterator();
        }
        while (mods_iter_.hasNext()) {
            Moderator mod = mods_iter_.next();
            Player player = Bukkit.getPlayerExact(mod.getPlayerName());
            if (player != null) {
                return player;
            }
        }
        mods_iter_ = null;
        return null;
    }

    private Player getNextMember() {
        if (member_iter_ == null) {
            Set<FactionMember> members = manager_.getMembersOfGroup(groupName_);
            member_iter_ = members.iterator();
        }
        while (member_iter_.hasNext()) {
            FactionMember member = member_iter_.next();
            Player player = Bukkit.getPlayerExact(member.getPlayerName());
            if (player != null) {
                return player;
            }
        }
        member_iter_ = null;
        return null;
    }

    private Player getPlayerByState() {
        Player player = null;
        if (state_ <= 0) {
            player = getFounder();
            state_ = 1;
        } else if (state_ == 1) {
            player = getNextModerator();
            if (player == null) {
                state_ = 2;
            }
        } else if (state_ == 2) {
            player = getNextMember();
            if (player == null) {
                state_ = 3;
            }
        }
        return player;
    }

    private boolean inFinalState() {
        return state_ >= 3;
    }

    // If the player is out of range return null, otherwise return the player.
    private Player outOfRange(Player player) {
        if (player == null) {
            return null;
        }
        if (referenceLocation_ != null
                && maxDistance_ != null
                && referenceLocation_.distance(player.getLocation()) > maxDistance_) {
            return null;
        }
        return player;
    }

    // If the player is ignoring this group return null, otherwise return the player.
    private Player playerIgnoringGroup(Player player) {
        if (player == null) {
            return null;
        }
        final UUID accountId = player.getUniqueId();
        if (IgnoreList.doesPlayerIgnoreAll(accountId)) {
            return null;
        }
        if (skipList_ != null && skipList_.contains(accountId)) {
            return null;
        }
        return player;
    }

    private Player getNextPlayer() {
        if (returnedCount_ >= maxPlayers_) {
            return null;
        }
        Player player;
        do {
            player = getPlayerByState();
            if (inFinalState()) {
                returnedCount_ = maxPlayers_;
                return null;
            }
            player = outOfRange(player);
            player = playerIgnoringGroup(player);
        } while (player == null);
        ++returnedCount_;
        return player;
    }
}
