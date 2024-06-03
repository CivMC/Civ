package vg.civcraft.mc.civmodcore.inventory.items.network;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

/**
 * This allows plugins to unobtrusively modify item metas before being sent over the network. Any changes made to the
 * item metas do not contaminate their original items.
 */
@ApiStatus.Internal
public final class NetworkItemMetaTransformer extends PacketAdapter {
	@FunctionalInterface
	private interface PacketHandler {
		void handle(
			@NotNull PacketContainer container
		);
	}

	// Only add handlers for packets where the items are shown to players. Entity equipment need not apply.
	private static final Map<PacketType, PacketHandler> HANDLERS = ImmutableMap.<PacketType, PacketHandler>builder()
		.put(PacketType.Play.Server.WINDOW_ITEMS, NetworkItemMetaTransformer::handleWindowItems)
		.put(PacketType.Play.Server.SET_SLOT, NetworkItemMetaTransformer::handleSetSlot)
		.put(PacketType.Play.Server.OPEN_WINDOW_MERCHANT, NetworkItemMetaTransformer::handleMerchantOffers)
		.build();

	public NetworkItemMetaTransformer(
		final @NotNull CivModCorePlugin plugin
	) {
		super(
			plugin,
			ListenerPriority.LOWEST,
			List.copyOf(HANDLERS.keySet())
		);
	}

	@Override
	public void onPacketSending(
		final @NotNull PacketEvent event
	) {
		if (event.isCancelled()) {
			return;
		}
		final PacketHandler handler = HANDLERS.get(event.getPacketType());
		if (handler != null) {
			handler.handle(event.getPacket());
		}
	}

	// ============================================================
	// Handlers
	// ============================================================

	private static void handleWindowItems(
		final @NotNull PacketContainer container
	) {
		final ClientboundContainerSetContentPacket handle; // For reference

		container.getItemListModifier().modify(0, (items) -> {
			processItems(items.listIterator());
			return items;
		});
		container.getItemModifier().modify(0, (original) -> {
			return processItem(original).orElse(original);
		});
	}

	private static void handleSetSlot(
		final @NotNull PacketContainer container
	) {
		final ClientboundContainerSetSlotPacket handle; // For reference

		container.getItemModifier().modify(0, (original) -> {
			return processItem(original).orElse(original);
		});
	}

	private static void handleMerchantOffers(
		final @NotNull PacketContainer container
	) {
		final ClientboundMerchantOffersPacket handle; // For reference

		container.getMerchantRecipeLists().modify(0, (offers) -> {
			for (final var iter = offers.listIterator(); iter.hasNext();) {
				final MerchantRecipe originalOffer = iter.next();

				final List<ItemStack> ingredients = originalOffer.getIngredients();
				processItems(ingredients.listIterator());

				final Optional<ItemStack> result = processItem(originalOffer.getResult());
				if (result.isPresent()) {
					// Can't just change the result, so we need to reconstruct the recipe
					// ALWAYS USE THE MOST COMPREHENSIVE CONSTRUCTOR
					final var newOffer = new MerchantRecipe(
						result.get(),
						originalOffer.getUses(),
						originalOffer.getMaxUses(),
						originalOffer.hasExperienceReward(),
						originalOffer.getVillagerExperience(),
						originalOffer.getPriceMultiplier(),
						originalOffer.getDemand(),
						originalOffer.getSpecialPrice(),
						originalOffer.shouldIgnoreDiscounts()
					);
					newOffer.setIngredients(ingredients);
					iter.set(newOffer);
				}
			}
			return offers;
		});
	}

	// ============================================================
	// Helpers
	// ============================================================

	private static void processItems(
		final @NotNull ListIterator<ItemStack> iter
	) {
		while (iter.hasNext()) {
			processItem(iter.next()).ifPresent(iter::set);
		}
	}

	private static Optional<ItemStack> processItem(
		ItemStack item
	) {
		if (ItemUtils.isEmptyItem(item)) {
			return Optional.empty();
		}
		item = item.clone();
		final ItemMeta meta = item.getItemMeta();
		if (meta == null) { // Shouldn't happen, but just in case
			return Optional.empty();
		}
		final var event = new NetworkItemEvent(meta);
		event.callEvent();
		if (!event.hasMetaBeenUpdated()) {
			return Optional.empty();
		}
		item.setItemMeta(event.meta);
		return Optional.of(item);
	}
}
