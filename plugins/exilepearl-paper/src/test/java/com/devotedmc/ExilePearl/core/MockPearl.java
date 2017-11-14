package com.devotedmc.ExilePearl.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.mockito.Mockito;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.PearlType;
import com.devotedmc.ExilePearl.PlayerProvider;
import com.devotedmc.ExilePearl.broadcast.BroadcastListener;
import com.devotedmc.ExilePearl.holder.PearlHolder;

public class MockPearl implements ExilePearl {
	
	private PlayerProvider nameProvider;
	private UUID playerId;
	private UUID killedBy;
	private int pearlId;
	private Location loc;
	private Date pearledOn;
	private PearlType pearlType;
	private Date lastOnline;
	private int health;
	private boolean freedOffline;
	
	public MockPearl(final PlayerProvider nameProvider, final UUID playerId, final UUID killedBy, int pearlId, Location loc) {
		this.nameProvider = nameProvider;
		this.playerId = playerId;
		this.killedBy = killedBy;
		this.pearlId = pearlId;
		this.loc = loc;
		this.health = 10;
		this.pearledOn = new Date();
		this.lastOnline = new Date();
		this.pearlType = PearlType.EXILE;
	}

	@Override
	public String getItemName() {
		return "Mock Pearl";
	}

	@Override
	public UUID getPlayerId() {
		return playerId;
	}

	@Override
	public int getPearlId() {
		return pearlId;
	}

	@Override
	public Player getPlayer() {
		return null;
	}

	@Override
	public PearlType getPearlType() {
		return pearlType;
	}

	@Override
	public void setPearlType(PearlType pearlType) {
		this.pearlType = pearlType;
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
		return nameProvider.getRealPlayerName(playerId);
	}

	@Override
	public PearlHolder getHolder() {
		return null;
	}

	@Override
	public void setHolder(Player p) {
		loc = p.getPlayer().getLocation();
	}

	@Override
	public void setHolder(Block b) {
		loc = b.getLocation();
	}

	@Override
	public void setHolder(Item item) {
		loc = item.getLocation();
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
	public UUID getKillerId() {
		return killedBy;
	}

	@Override
	public String getKillerName() {
		return nameProvider.getRealPlayerName(killedBy);
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
		List<String> lore = new ArrayList<String>();
		lore.add(getItemName());
		lore.add(getPlayerName());
		lore.add(getPlayerId().toString());
		ItemStack is = mock(ItemStack.class);
		when(is.getType()).thenReturn(Material.ENDER_PEARL);
		when(is.getAmount()).thenReturn(1);
		
		ItemMeta im = Mockito.mock(ItemMeta.class);
		final String playerName = getPlayerName();
		when(im.getDisplayName()).thenReturn(playerName);
		when(im.getLore()).thenReturn(lore);
		when(is.getItemMeta()).thenReturn(im);
		return is;
	}

	@Override
	public boolean verifyLocation() {
		return true;
	}

	@Override
	public boolean validateItemStack(ItemStack is) {
		if (is.getItemMeta() == null) {
			return false;
		}
		
		ItemMeta im = is.getItemMeta();
		ItemMeta other = createItemStack().getItemMeta();
		
		if (im.getDisplayName() != other.getDisplayName()) {
			return false;
		}
		
		List<String> lore1 = im.getLore();
		List<String> lore2 = other.getLore();
		return lore1.equals(lore2);
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
            .append(pearlType)
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
				.append(pearlType, other.pearlType)
				.isEquals();
    }

	@Override
	public void performBroadcast() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addBroadcastListener(BroadcastListener bcast) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeBroadcastListener(Object bcast) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isBroadcastingTo(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setKillerId(UUID killerId) {
		this.killedBy = killerId;
	}
	
	@Override
	public Date getLastOnline() {
		return this.lastOnline;
	}

	@Override
	public void setLastOnline(Date online) {
		this.lastOnline = online;
	}

	@Override
	public boolean isSummoned() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setSummoned(boolean summoned) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Location getReturnLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setReturnLocation(Location loc) {
		// TODO Auto-generated method stub
		
	}
}
