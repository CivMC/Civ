package com.untamedears.citadel.entity;

import static com.untamedears.citadel.Utility.toAccountId;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.Openable;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.DbUpdateAction;
import com.untamedears.citadel.SecurityLevel;

import static com.untamedears.citadel.Utility.sendMessage;

/**
 * User: chrisrico
 */
@Table(name="reinforcement")
@Entity
public class PlayerReinforcement implements
        IReinforcement, Comparable<IReinforcement> {

    public static final List<Integer> SECURABLE = new ArrayList<Integer>();
    public static final List<Integer> NON_REINFORCEABLE = new ArrayList<Integer>();
    public static final Map<Integer, Double> MATERIAL_SCALING = new HashMap<Integer, Double>();

    @Id private ReinforcementKey id;
    private int materialId;
    private int durability;
    private SecurityLevel securityLevel;
    private String chunkId;
    private boolean insecure;
    // @Transient == not persisted in the DB
    @Transient private DbUpdateAction dbAction;

    @Version
    @Column(name="version")
    private int dbRowVersion;  // Do not touch

    @Column(name = "name")
    private String ownerName;

    @Column(name="maturation_time")
    private int maturationTime;


    public PlayerReinforcement() {
        this.dbAction = DbUpdateAction.NONE;
    }

    public PlayerReinforcement(
	        Block block,
	        ReinforcementMaterial material,
	        Faction owner,
            SecurityLevel securityLevel) {
        this.id = new ReinforcementKey(block);
        this.materialId = material.getMaterial().getId();
        double baseDurability = (double)material.getStrength();
        double scale = 1.00000001D;
        int blockType = block.getTypeId();
        if (PlayerReinforcement.MATERIAL_SCALING.containsKey(blockType)) {
            scale = PlayerReinforcement.MATERIAL_SCALING.get(blockType);
        }
        this.durability = (int)(baseDurability * scale);
        this.ownerName = owner.getName();
        this.securityLevel = securityLevel;
        this.chunkId = this.id.getChunkId();
        this.dbAction = DbUpdateAction.INSERT;
        if (this.durability < 50) {
            this.maturationTime = 0;
        } else {
            final double interval = Citadel.getConfigManager().getMaturationIntervalD();
            // Maturation time is in minutes since the epoch (Jan 1, 1970)
            double tmpDur = (baseDurability / interval) * 60.0D;
            this.maturationTime = (int)(System.currentTimeMillis() / 60000L + (long)tmpDur);
        }
    }

    private void flagForDbUpdate() {
        if (getDbAction() == DbUpdateAction.NONE) {
            setDbAction(DbUpdateAction.SAVE);
        }
    }

    public void updateFrom(PlayerReinforcement that) {
        setMaterialId(that.getMaterialId());
        setDurability(that.getDurability());
        setOwner(that.getOwner());
        setSecurityLevel(that.getSecurityLevel());
        setMaturationTime(that.getMaturationTime());
        if (getDbAction() == DbUpdateAction.DELETE) {
            setDbAction(DbUpdateAction.SAVE);
        }
    }

    public ReinforcementKey getId() { return id; }
    public void setId(ReinforcementKey id) { this.id = id; }

    // Do not touch
    public int getDbRowVersion() { return this.dbRowVersion; }
    public void setDbRowVersion(int value) { this.dbRowVersion = value; }
    // Do not touch

    public Block getBlock() {
        try {
        	return Bukkit.getServer().getWorld(id.getWorld()).getBlockAt(
                    id.getX(),
                    id.getY(),
                    id.getZ());
        } catch (NullPointerException e) {
        	return null;
        }
    }

    public ReinforcementMaterial getMaterial() {
        return ReinforcementMaterial.get(Material.getMaterial(materialId));
    }

    public int getMaterialId() {
        return materialId;
    }

    public void setMaterialId(int materialId) {
        flagForDbUpdate();
        this.materialId = materialId;
    }

    public int getDurability() {
        return durability;
    }

    public void setDurability(int durability) {
        flagForDbUpdate();
        this.durability = durability;
    }

    public double getScaleFactor() {
        int blockType = this.getBlock().getTypeId();
        if (PlayerReinforcement.MATERIAL_SCALING.containsKey(blockType)) {
            return PlayerReinforcement.MATERIAL_SCALING.get(blockType);
        }
        return 1.0000001D;
    }

    public int getMaxDurability() {
        return this.getMaterial().getStrength();
    }

    public int getScaledMaxDurability() {
        return (int)((double)this.getMaterial().getStrength() * this.getScaleFactor());
    }

    public String getChunkId() {
        return this.chunkId;
    }

    public void setChunkId(String id) {
        this.chunkId = id;
    }

    public SecurityLevel getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(SecurityLevel securityLevel) {
        flagForDbUpdate();
        this.securityLevel = securityLevel;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String newOwner) {
        flagForDbUpdate();
        ownerName = newOwner;
    }

    public Faction getOwner() {
        return Citadel.getGroupManager().getDelegatedGroup(getOwnerName());
    }

    public void setOwner(Faction group) {
        String group_name = null;
        if (group != null) {
            group_name = group.getName();
        }
        setOwnerName(group_name);
    }

    public int getMaturationTime() {
        return this.maturationTime;
    }

    public void setMaturationTime(int time) {
        this.maturationTime = time;
    }

    public boolean isInsecure() {
        return this.insecure;
    }

    public void setInsecure(boolean value) {
        this.insecure = value;
    }

    public void toggleInsecure() {
        this.setInsecure(!this.isInsecure());
    }

    public double getHealth() {
        return (double)durability / ((double)this.getMaterial().getStrength() * this.getScaleFactor());
    }

    public String getHealthText() {
        double health = getHealth();
        if (health > 0.75) {
            return "excellently";
        } else if (health > 0.50) {
            return "well";
        } else if (health > 0.25) {
            return "decently";
        } else {
            return "poorly";
        }
    }

    public String getStatus() {
        String verb;
        if (isSecurable()) {
            verb = "Locked";
        } else {
            verb = "Reinforced";
        }
        return String.format("%s %s with %s",
                verb,
                getHealthText(),
                getMaterial().getMaterial().name());
    }

    public boolean isAccessible(String playerOrAccount) {
        final UUID accountId = toAccountId(playerOrAccount);
        if (accountId == null) {
            return false;
        }
        return isAccessible(accountId, Bukkit.getPlayer(accountId));
    }

    public boolean isAccessible(Player player) {
        return isAccessible(player.getUniqueId(), player);
    }

    private boolean isAccessible(UUID accountId, Player player) {
        final Faction owner = getOwner();
        if (owner == null) {
            Citadel.severe("isAccessible encountered unowned reinforcement: " + toString());
            if (player != null) {
                sendMessage(player, ChatColor.RED,
                    "This reinforcement has an issue. Please send modmail. " + getId().toString());
            }
            return false;
        }
        if (owner.isDisciplined()) {
            return false;
        }
        switch (securityLevel) {
            case PRIVATE:
                return owner.isFounder(accountId);
            case GROUP:
                return owner.isFounder(accountId)
                    || owner.isMember(accountId)
                    || owner.isModerator(accountId);
            case PUBLIC:
            	return true;
        }
        return false;
    }

    public boolean isBypassable(String playerOrAccount) {
        final UUID accountId = toAccountId(playerOrAccount);
        if (accountId == null) {
            return false;
        }
        return isBypassable(accountId, Bukkit.getPlayer(accountId));
    }

    public boolean isBypassable(Player player) {
        return isBypassable(player.getUniqueId(), player);
    }

    private boolean isBypassable(UUID accountId, Player player) {
        final Faction owner = getOwner();
        if (owner == null) {
            Citadel.severe("isBypassable encountered unowned reinforcement: %s" + toString());
            if (player != null) {
                sendMessage(player, ChatColor.RED,
                    "This reinforcement has an issue. Please send modmail. " + getId().toString());
            }
            return false;
        }
        if (owner.isDisciplined()) {
            return false;
        }
        switch (securityLevel) {
            case PRIVATE:
                return owner.isFounder(accountId);
            default:
                return owner.isFounder(accountId) || owner.isModerator(accountId);
        }
    }

    public boolean isSecurable() {
        Block block = getBlock();
        return block.getState() instanceof InventoryHolder
                || block.getState().getData() instanceof Openable
                || SECURABLE.contains(block.getTypeId());
    }

    @Override
    public String toString() {
        return String.format("%s, material: %s, durability: %d", id, getMaterial().getMaterial().name(), durability);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IReinforcement)) return false;

        IReinforcement r = (IReinforcement)o;
        return this.id.equals(r.getId());
    }

    public int compareTo(IReinforcement r) {
    	return this.id.compareTo(r.getId());
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    public DbUpdateAction getDbAction() { return this.dbAction; }
    public void setDbAction(DbUpdateAction value) { this.dbAction = value; }
}
