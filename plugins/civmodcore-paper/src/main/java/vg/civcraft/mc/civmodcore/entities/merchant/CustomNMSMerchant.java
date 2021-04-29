package vg.civcraft.mc.civmodcore.entities.merchant;

import io.papermc.paper.event.player.PlayerTradeEvent;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityExperienceOrb;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.EntityVillager;
import net.minecraft.server.v1_16_R3.EntityVillagerAbstract;
import net.minecraft.server.v1_16_R3.EntityVillagerTrader;
import net.minecraft.server.v1_16_R3.MerchantRecipe;
import net.minecraft.server.v1_16_R3.MerchantRecipeList;
import net.minecraft.server.v1_16_R3.World;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftVillager;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftMerchantCustom;
import org.bukkit.entity.ExperienceOrb;

public class CustomNMSMerchant extends CraftMerchantCustom.MinecraftMerchant {

	private static final Field TRADES_FIELD;

	static {
		TRADES_FIELD = FieldUtils.getField(CraftMerchantCustom.MinecraftMerchant.class, "trades", true);
		FieldUtils.removeFinalModifier(TRADES_FIELD);
	}

	CustomNMSMerchant(final CustomBukkitMerchant merchant, final Component title) {
		super(title);
		this.craftMerchant = Objects.requireNonNull(merchant);
	}

	/**
	 * @return Returns the raw offers object (which is mutable) via reflection.
	 */
	public MerchantRecipeList getRawOffers() {
		try {
			return (MerchantRecipeList) FieldUtils.readField(TRADES_FIELD, this, true);
		}
		catch (final IllegalAccessException exception) {
			throw new RuntimeException(exception);
		}
	}

	/**
	 * This is based heavily on {@link EntityVillagerAbstract#a(MerchantRecipe)}. This method is called
	 * by NMS when a trade is being purchased.
	 */
	@Override
	public void a(final MerchantRecipe trade) {
		if (getTrader() instanceof EntityPlayer) {
			final var trader = (EntityPlayer) getTrader();
			final var villager = createDisposableVillager(trader.getWorld());
			final var event = new PlayerTradeEvent(
					trader.getBukkitEntity(),
					villager, // Have to spawn a useless villager because this is @Nonnull
					trade.asBukkit(),
					true,  // reward xp?
					true); // should increase uses?
			event.callEvent();
			killDisposableVillager(villager);
			if (event.isCancelled()) {
				return;
			}
			final var eventTrade = event.getTrade();
			if (event.willIncreaseTradeUses()) {
				eventTrade.setUses(eventTrade.getUses() + 1);
			}
			if (event.isRewardingExp() && eventTrade.hasExperienceReward()) {
				/** Based on {@link EntityVillagerTrader#b(MerchantRecipe)} */
				final int xp = 3 + Entity.SHARED_RANDOM.nextInt(4);
				final var world = trader.getWorld();
				world.addEntity(new EntityExperienceOrb(
						world, trader.locX(), trader.locY() + 0.5d, trader.locZ(),
						xp, ExperienceOrb.SpawnReason.VILLAGER_TRADE, trader, null));
			}
			return;
		}
		super.a(trade);
	}

	// ------------------------------------------------------------
	// Unfortunate but necessary villager management
	// ------------------------------------------------------------

	private static CraftVillager createDisposableVillager(final World world) {
		final var villager = new CraftVillager(world.getServer(),
				new EntityVillager(EntityTypes.VILLAGER, world));
		villager.setAI(false);
		villager.setGravity(false);
		villager.setCanPickupItems(false);
		villager.setSilent(true);
		villager.setVillagerExperience(0);
		villager.setInvisible(true);
		villager.setRecipes(Collections.emptyList());
		return villager;
	}

	private static void killDisposableVillager(final CraftVillager villager) {
		villager.getHandle().dead = true;
		villager.getHandle().setHealth(0);
		villager.getHandle().shouldBeRemoved = true;
	}

}
