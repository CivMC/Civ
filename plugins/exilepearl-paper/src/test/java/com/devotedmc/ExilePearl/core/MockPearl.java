package com.devotedmc.ExilePearl.core;

import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.PearlPlayer;
import com.devotedmc.ExilePearl.PlayerNameProvider;
import com.devotedmc.ExilePearl.holder.PearlHolder;

public class MockPearl implements ExilePearl {
	
	private PlayerNameProvider nameProvider;
	private UUID playerId;
	private UUID killedBy;
	private Location loc;
	private Date pearledOn;
	private int health;
	private boolean freedOffline;
	
	public MockPearl(final PlayerNameProvider nameProvider, final UUID playerId, final UUID killedBy, Location loc) {
		this.nameProvider = nameProvider;
		this.playerId = playerId;
		this.killedBy = killedBy;
		this.loc = loc;
		this.health = 10;
		this.pearledOn = new Date();
	}

	@Override
	public String getItemName() {
		return "Mock Pearl";
	}

	@Override
	public UUID getUniqueId() {
		return playerId;
	}

	@Override
	public PearlPlayer getPlayer() {
		return null;
	}

	@Override
	public Date getPearledOn() {
		return pearledOn;
	}

	@Override
	public void setPearledOn(Date pearledOn) {
		this.pearledOn = pearledOn;
	}

	@Override
	public String getPlayerName() {
		return nameProvider.getName(playerId);
	}

	@Override
	public PearlHolder getHolder() {
		return null;
	}

	@Override
	public void setHolder(PearlPlayer p) {
		loc = p.getLocation();
	}

	@Override
	public void setHolder(Block b) {
		loc = b.getLocation();
	}

	@Override
	public void setHolder(Location l) {
		loc = l;
	}

	@Override
	public int getHealth() {
		return health;
	}

	@Override
	public Integer getHealthPercent() {
		return null;
	}

	@Override
	public void setHealth(int health) {
		this.health = health;
	}

	@Override
	public Location getLocation() {
		return loc;
	}

	@Override
	public UUID getKillerUniqueId() {
		return killedBy;
	}

	@Override
	public String getKillerName() {
		return null;
	}

	@Override
	public String getLocationDescription() {
		return null;
	}

	@Override
	public boolean getFreedOffline() {
		return freedOffline;
	}

	@Override
	public void setFreedOffline(boolean freedOffline) {
		this.freedOffline = freedOffline;
	}

	@Override
	public ItemStack createItemStack() {
		return null;
	}

	@Override
	public boolean verifyLocation() {
		return true;
	}

	@Override
	public boolean validateItemStack(ItemStack is) {
		return false;
	}

	@Override
	public void enableStorage() {		
	}
	
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31) // two randomly chosen prime numbers
            .append(playerId)
            .append(killedBy)
            .append(getLocation())
            .append(health)
            .append(pearledOn)
            .append(freedOffline)
            .toHashCode();
    }
	
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MockPearl other = (MockPearl) o;

		return new EqualsBuilder()
				.append(playerId, other.playerId)
				.append(killedBy, other.killedBy)
				.append(getLocation(), other.getLocation())
				.append(health, other.health)
				.append(pearledOn, other.pearledOn)
				.append(freedOffline, other.freedOffline)
				.isEquals();
    }

}
