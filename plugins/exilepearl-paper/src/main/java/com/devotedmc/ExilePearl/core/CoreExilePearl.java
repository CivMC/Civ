package com.devotedmc.ExilePearl.core;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.Lang;
import com.devotedmc.ExilePearl.PearlType;
import com.devotedmc.ExilePearl.broadcast.BroadcastListener;
import com.devotedmc.ExilePearl.event.PearlMovedEvent;
import com.devotedmc.ExilePearl.holder.BlockHolder;
import com.devotedmc.ExilePearl.holder.EntityHolder;
import com.devotedmc.ExilePearl.holder.HolderVerifyResult;
import com.devotedmc.ExilePearl.holder.ItemHolder;
import com.devotedmc.ExilePearl.holder.PearlHolder;
import com.devotedmc.ExilePearl.holder.PlayerHolder;
import com.devotedmc.ExilePearl.storage.PearlUpdateStorage;
import com.google.common.base.Preconditions;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import vg.civcraft.mc.civmodcore.util.TextUtil;

/**
 * Instance of a player who is imprisoned in an exile pearl
 * @author Gordon
 */
final class CoreExilePearl implements ExilePearl {
	private static final int HOLDER_COUNT = 5;
	private static final int DEFAULT_HEALTH = 10;

	// The player provider instance
	private final ExilePearlApi pearlApi;

	// The storage instance
	private final PearlUpdateStorage storage;

	private final UUID playerId;
	private final int pearlId;
	private UUID killedBy;
	private final Set<BroadcastListener> bcastListeners = new HashSet<BroadcastListener>();

	private PearlType pearlType;
	private Date pearledOn;
	private Date lastSeen;
	private LinkedBlockingDeque<PearlHolder> holders;
	private boolean freedOffline;
	private int health;
	private boolean storageEnabled;
	private boolean summoned;
	private Location returnLoc;

	/**
	 * Creates a new prison pearl instance
	 * @param playerId The pearled player id
	 * @param holder The holder instance
	 */
	public CoreExilePearl(final ExilePearlApi pearlApi, final PearlUpdateStorage storage, 
			final UUID playerId, final UUID killedBy, int pearlId, final PearlHolder holder) {
		Preconditions.checkNotNull(pearlApi, "pearlApi");
		Preconditions.checkNotNull(storage, "storage");
		Preconditions.checkNotNull(playerId, "playerId");
		Preconditions.checkNotNull(killedBy, "killedBy");
		Preconditions.checkNotNull(holder, "holder");

		this.pearlApi = pearlApi;
		this.storage = storage;
		this.playerId = playerId;
		this.pearlId = pearlId;
		this.killedBy = killedBy;
		this.pearledOn = new Date();
		this.lastSeen = new Date();
		this.pearlType = PearlType.EXILE;
		this.holders = new LinkedBlockingDeque<PearlHolder>();
		this.holders.add(holder);
		this.health = DEFAULT_HEALTH;
		storageEnabled = false;
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
		return pearlApi.getPlayer(playerId);
	}


	@Override
	public PearlType getPearlType() {
		return pearlType;
	}


	@Override
	public void setPearlType(PearlType pearlType) {
		this.pearlType = pearlType;

		if(storageEnabled) {
			storage.updatePearlType(this);
		}
	}


	@Override
	public Date getPearledOn() {
		return this.pearledOn;
	}


	@Override
	public void setPearledOn(Date pearledOn) {
		Preconditions.checkNotNull(pearledOn, "pearledOn");
		checkPearlValid();

		this.pearledOn = pearledOn;
	}


	@Override
	public String getPlayerName() {
		String name = pearlApi.getRealPlayerName(playerId);
		if (name == null) {
			name = "Unknown player";
		}
		return name;
	}


	@Override
	public PearlHolder getHolder() {
		return holders.peekLast();
	}


	public void setHolder(PearlHolder holder) {
		Preconditions.checkNotNull(holder, "holder");
		setHolderInternal(holder);
	}


	@Override
	public void setHolder(Player player) {
		Preconditions.checkNotNull(player, "player");
		setHolderInternal(new PlayerHolder(player));
	}


	@Override
	public void setHolder(Block block) {
		Preconditions.checkNotNull(block, "block");
		setHolderInternal(new BlockHolder(block));
	}

	@Override
	public void setHolder(Entity entity) {
		Preconditions.checkNotNull(entity, "entity");
		setHolderInternal(new EntityHolder(entity));
	}

	@Override
	public void setHolder(Item item) {
		Preconditions.checkNotNull(item, "item");
		setHolderInternal(new ItemHolder(item));
	}

	/**
	 * Internal method for updating the holder
	 * @param holder The new holder instance
	 */
	private void setHolderInternal(PearlHolder holder) {
		checkPearlValid();

		// Do nothing if the holder is the same
		if (holder.equals(holders.getLast())) {
			return;
		}

		PearlHolder from = holders.peekLast();
		holders.add(holder);

		// Generate a moved event
		Bukkit.getPluginManager().callEvent(new PearlMovedEvent(this, from, holder));

		if (holders.size() > HOLDER_COUNT) {
			holders.poll();
		}

		if(storageEnabled) {
			storage.updatePearlLocation(this);
		}
	}

    
    /**
     * Gets the pearl health value
     * @return The strength value
     */
	@Override
    public Integer getHealthPercent() {
		return (int)Math.round(((double)health / pearlApi.getPearlConfig().getPearlHealthMaxValue()) * 100);
    }

    
    /**
     * Gets the pearl health value
     * @return The strength value
     */
	@Override
    public int getHealth() {
    	return this.health;
    }
    
    
    /**
     * Sets the pearl heatlh value
     * @param health The health value
     */
	@Override
    public void setHealth(int health) {
		checkPearlValid();

    	if (health < 0) {
    		health = 0;
    	}
    	
    	if (health > pearlApi.getPearlConfig().getPearlHealthMaxValue()) {
    		health = pearlApi.getPearlConfig().getPearlHealthMaxValue();
    	}
    	
    	this.health = health;
    	
		if(storageEnabled) {
			storage.updatePearlHealth(this);
		}
    }

	/**
	 * Gets the pearl location
	 */
	@Override
	public Location getLocation() {
		return this.holders.peekLast().getLocation();
	}


	@Override
	public void setKillerId(UUID killerId) {
		this.killedBy = killerId;
	}


	@Override
	public String getItemName() {
		return pearlType.getTitle();
	}


	@Override
	public UUID getKillerId() {
		return killedBy;
	}


	@Override
	public String getKillerName() {
		String name = pearlApi.getRealPlayerName(killedBy);
		if (name == null) {
			name = "Unknown player";
		}
		return name;
	}


	/**
	 * Gets the name of the current location
	 * @return The string of the current location
	 */
	@Override
	public String getLocationDescription() {
		final Location loc = getHolder().getLocation();
		final Vector vec = loc.toVector();
		final String str = loc.getWorld().getName() + " " + vec.getBlockX() + " " + vec.getBlockY() + " " + vec.getBlockZ();
		return "held by " + getHolder().getName() + " at " + str;
	}


	/**
	 * Gets whether the player was freed offline
	 * @return true if the player was freed offline
	 */
	@Override
	public boolean getFreedOffline() {
		return this.freedOffline;
	}

	/**
	 * Gets whether the player was freed offline
	 * @return true if the player was freed offline
	 */
	@Override
	public void setFreedOffline(boolean freedOffline) {
		checkPearlValid();

		this.freedOffline = freedOffline;

		if (storageEnabled) {
			storage.updatePearlFreedOffline(this);
		}
	}


	/**
	 * Creates an item stack for the pearl
	 * @return The new item stack
	 */
	@Override
	public ItemStack createItemStack() {
		List<String> lore = pearlApi.getLoreProvider().generateLore(this);
		ItemStack is = new ItemStack(Material.ENDER_PEARL, 1);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(this.getPlayerName());
		im.setLore(lore);
		im.addEnchant(Enchantment.DURABILITY, 1, true);
		im.addItemFlags(ItemFlag.HIDE_ENCHANTS);

		PersistentDataContainer container = im.getPersistentDataContainer();
		container.set(pearlApi.getLoreProvider().getExilePearlIdKey(), PersistentDataType.INTEGER, this.pearlId);

		is.setItemMeta(im);
		return is;
	}


	/**
	 * Validates that an item stack is the prison pearl
	 * @param is The item stack
	 * @return true if it checks out
	 */
	public boolean validateItemStack(ItemStack is) {
		Preconditions.checkNotNull(is, "is");

		int pearlId = pearlApi.getLoreProvider().getPearlIdFromItemStack(is);

		if (pearlId == this.pearlId) {
			ItemMeta im = is.getItemMeta();
			im.setLore(pearlApi.getLoreProvider().generateLore(this));

			PersistentDataContainer container = im.getPersistentDataContainer();

			NamespacedKey exilePearlIdKey = pearlApi.getLoreProvider().getExilePearlIdKey();
			if (!container.has(exilePearlIdKey, PersistentDataType.INTEGER)) {
				container.set(exilePearlIdKey, PersistentDataType.INTEGER, this.pearlId);
			}

			is.setItemMeta(im);
			return true;
		}

		return false;
	}


	/**
	 * Verifies the pearl location
	 * @return
	 */
	public boolean verifyLocation() {
		StringBuilder sb = new StringBuilder();

		StringBuilder failure_reason_log = new StringBuilder();

		for (final PearlHolder holder : this.holders) {
			HolderVerifyResult reason = this.verifyHolder(holder);
			if (reason.isValid()) {
				sb.append(String.format("ExilePearl (%s, %s) passed verification for reason '%s'.",
						playerId.toString(), this.getPlayerName(), reason.toString()));
				pearlApi.log(sb.toString());

				return true;
			} else {
				failure_reason_log.append(reason.toString()).append(", ");
			}
		}
		sb.append(String.format("ExilePearl (%s, %s) failed verification for reason '%s'.",
				playerId.toString(), this.getPlayerName(), failure_reason_log.toString()));

		pearlApi.log(sb.toString());
		return false;
	}


	/**
	 * Verifies the holder of a pearl
	 * @param holder The holder to check
	 * @return true if the pearl was found in a valid location
	 */
	private HolderVerifyResult verifyHolder(PearlHolder holder) {
		return holder.validate(this);
	}


	@Override
	public void enableStorage() {
		storageEnabled = true;
	}

	/**
	 * Checks to make sure the pearl being operated on is valid
	 */
	private void checkPearlValid() {
		if (storageEnabled && !pearlApi.isPlayerExiled(playerId)) {
			throw new RuntimeException(String.format("Tried to modify exile pearl for player %s that is no longer valid.", getPlayerName()));
		}
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

        CoreExilePearl other = (CoreExilePearl) o;

		return new EqualsBuilder()
				.append(playerId, other.playerId)
				.append(killedBy, other.killedBy)
				.append(getLocation(), other.getLocation())
				.append(health, other.health)
				.append(pearledOn, other.pearledOn)
				.append(freedOffline, other.freedOffline)
				.isEquals();
    }


	@Override
	public void performBroadcast() {
		Location l = getHolder().getLocation();
		String name = getHolder().getName();		
		TextUtil.msg(getPlayer(), Lang.pearlPearlIsHeld, name, l.getBlockX(), l.getBlockY(), l.getBlockZ(), l.getWorld().getName());

		for(BroadcastListener b : bcastListeners) {
			b.broadcast(this);
		}
	}


	@Override
	public void addBroadcastListener(BroadcastListener bcast) {
		bcastListeners.add(bcast);
	}


	@Override
	public void removeBroadcastListener(Object o) {
		bcastListeners.remove(o);
	}


	@Override
	public boolean isBroadcastingTo(Object o) {
		for(BroadcastListener b : bcastListeners) {
			if (b.contains(o)) {
				return true;
			}
		}
		return false;
	}


	@Override
	public Date getLastOnline() {
		return lastSeen;
	}


	@Override
	public void setLastOnline(Date online) {
		lastSeen = online;

		if (storageEnabled) {
			storage.updatePearlLastOnline(this);
		}
	}

	@Override
	public boolean isSummoned() {
		return summoned;
	}

	@Override
	public void setSummoned(boolean summoned) {
		if(pearlType != PearlType.PRISON) return;
		this.summoned = summoned;
		if (storageEnabled) {
			storage.updatePearlSummoned(this);
		}
	}

	@Override
	public Location getReturnLocation() {
		return returnLoc;
	}

	@Override
	public void setReturnLocation(Location loc) {
		if(pearlType != PearlType.PRISON) return;
		this.returnLoc = loc;
		if(storageEnabled) {
			storage.updateReturnLocation(this);
		}
	}


	@Override
	public double getLongTimeMultiplier() {
		int timer = pearlApi.getPearlConfig().pearlCostMultiplicationTimerDays();
		if (timer <= 0) {
			return 1.0;
		}
		long sinceLastOnline = System.currentTimeMillis() - getLastOnline().getTime();
		double days = TimeUnit.MILLISECONDS.toDays(sinceLastOnline);
		return Math.max(1.0, days / timer);
	}
}
