package vg.civcraft.mc.civmodcore.entities.merchant;

import io.papermc.paper.event.player.PlayerTradeEvent;
import java.lang.reflect.Field;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftMerchant;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftMerchantCustom;
import vg.civcraft.mc.civmodcore.util.CivLogger;

/**
 * This is an alternative to {@link Bukkit#createMerchant(Component)} that re-adds
 * {@link PlayerTradeEvent} emissions. Just do: {@code new CustomBukkitMerchant(Component)}
 */
public class CustomBukkitMerchant extends CraftMerchantCustom {

	private static final CivLogger LOGGER;
	private static final Field MERCHANT_FIELD;

	static {
		LOGGER = CivLogger.getLogger(CustomBukkitMerchant.class);
		MERCHANT_FIELD = FieldUtils.getField(CraftMerchant.class, "merchant", true);
		FieldUtils.removeFinalModifier(MERCHANT_FIELD);
	}

	public CustomBukkitMerchant(final Component title) {
		super(title);
		final var nmsMerchant = new CustomNMSMerchant(this, title);
		try {
			FieldUtils.writeField(MERCHANT_FIELD, this, nmsMerchant, true);
		}
		catch (final IllegalAccessException exception) {
			LOGGER.log(Level.SEVERE,
					"Could not re-set merchant to [" + nmsMerchant + "]",
					exception);
		}
	}

	public CustomNMSMerchant getNMSMerchant() {
		return (CustomNMSMerchant) super.getMerchant();
	}

	/**
	 * Use {@link #getNMSMerchant()} instead. The only reason why this is being kept is because
	 * the super constructor calls this function and expects this type, not {@link CustomNMSMerchant}.
	 */
	@Deprecated
	@Override
	public CraftMerchantCustom.MinecraftMerchant getMerchant() {
		return super.getMerchant();
	}

	@Override
	public String toString() {
		/** Stolen from {@link Object#toString()} because the super version is garbage */
		return getClass().getName() + "@" + Integer.toHexString(hashCode());
	}

}
