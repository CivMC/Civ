package com.github.igotyou.FactoryMod.repairManager;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.utility.LoggingUtils;
import java.util.Map.Entry;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;

public class PercentageHealthRepairManager implements IRepairManager {
	private int health;
	private Factory factory;
	private long breakTime;
	private int maximumHealth;
	private int damageAmountPerDecayIntervall;
	private long gracePeriod;

	public PercentageHealthRepairManager(int initialHealth, int maximumHealth, long breakTime, int damageAmountPerDecayIntervall, long gracePeriod) {
		this.health = initialHealth;
		this.maximumHealth = maximumHealth;
		this.breakTime = breakTime;
		this.damageAmountPerDecayIntervall = damageAmountPerDecayIntervall;
		this.gracePeriod = gracePeriod;
	}

	public boolean atFullHealth() {
		return health >= maximumHealth;
	}

	public int getMaximumHealth() {
		return maximumHealth;
	}

	public int getDamageAmountPerDecayIntervall() {
		return damageAmountPerDecayIntervall;
	}

	public long getGracePeriod() {
		return gracePeriod;
	}

	public boolean inDisrepair() {
		return health <= 0;
	}

	public void setFactory(Factory factory) {
		this.factory = factory;
	}

	public String getHealth() {
		return String.valueOf(health / (maximumHealth / 100)) + "." + (health % (maximumHealth / 100) + " %");
	}

	public void repair(int amount) {
		health = Math.min(health + amount, maximumHealth);
		breakTime = 0;
	}

	public void breakIt() {
		health = 0;
		if (breakTime == 0) {
			breakTime = System.currentTimeMillis();
		}
		FactoryMod.getInstance().getServer().getScheduler()
				.scheduleSyncDelayedTask(FactoryMod.getInstance(), ()->{
						if (factory.getMultiBlockStructure().relevantBlocksDestroyed()) {
							LoggingUtils.log(factory.getLogData() + " removed because blocks were destroyed");
							FactoryMod.getInstance().getManager().removeFactory(factory);
							returnStuff(factory);
						}
				});
	}

	public int getRawHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public static void returnStuff(Factory factory) {
		double rate = FactoryMod.getInstance().getManager().getEgg(factory.getName()).getReturnRate();
		if (rate == 0.0) {
			return;
		}
		for (Entry<ItemStack, Integer> items : FactoryMod.getInstance().getManager().getTotalSetupCost(factory).getEntrySet()) {
			int returnAmount = (int) (items.getValue() * rate);
			ItemMap im = new ItemMap();
			im.addItemAmount(items.getKey(), returnAmount);
			for (ItemStack is : im.getItemStackRepresentation()) {
				factory.getMultiBlockStructure().getCenter().getWorld()
						.dropItemNaturally(factory.getMultiBlockStructure().getCenter(), is);
			}
		}
	}

	public long getBreakTime() {
		return breakTime;
	}

	public void setBreakTime(long breakTime) {
		this.breakTime = breakTime;
	}
}
