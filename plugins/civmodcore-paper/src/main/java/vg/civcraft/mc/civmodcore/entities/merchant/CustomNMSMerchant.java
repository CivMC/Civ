package vg.civcraft.mc.civmodcore.entities.merchant;

import io.papermc.paper.event.player.PlayerTradeEvent;
import java.lang.reflect.Field;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityExperienceOrb;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.EntityVillagerAbstract;
import net.minecraft.server.v1_16_R3.EntityVillagerTrader;
import net.minecraft.server.v1_16_R3.MerchantRecipe;
import net.minecraft.server.v1_16_R3.MerchantRecipeList;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftMerchantCustom;
import org.bukkit.entity.AbstractVillager;
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
			final var event = new PlayerTradeEvent(
					trader.getBukkitEntity(),
					(AbstractVillager) getCraftMerchant(),
					trade.asBukkit(),
					true,	// reward xp?
					true);	// should increase uses?
			event.callEvent();
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

}
