package uk.protonull.civ.protocollib;

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
import java.util.Set;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * This allows plugins to unobtrusively modify item metas before being sent over the network. Any changes made to the
 * item metas do not contaminate their original items.
 */
public abstract class NetworkItemMetaTransformer extends PacketAdapter {
	@FunctionalInterface
	private interface PacketHandler {
		void handle(
			@NotNull PacketContainer container,
			@NotNull ItemProcessor itemProcessor,
			@NotNull Player recipient
		);
	}

	// Only add handlers for packets where the items are shown to players. Entity equipment need not apply.
	private static final Map<PacketType, PacketHandler> HANDLERS = ImmutableMap.<PacketType, PacketHandler>builder()
		.put(PacketType.Play.Server.WINDOW_ITEMS, NetworkItemMetaTransformer::handleWindowItems)
		.put(PacketType.Play.Server.SET_SLOT, NetworkItemMetaTransformer::handleSetSlot)
		.put(PacketType.Play.Server.OPEN_WINDOW_MERCHANT, NetworkItemMetaTransformer::handleMerchantOffers)
		.build();

	public NetworkItemMetaTransformer(
		final @NotNull Plugin plugin,
		final @NotNull ListenerPriority priority
	) {
		super(
			plugin,
			priority,
			Set.copyOf(HANDLERS.keySet())
		);
	}

	@Override
	public final void onPacketSending(
		final @NotNull PacketEvent event
	) {
		if (event.isCancelled()) {
			return;
		}
		final PacketHandler handler = HANDLERS.get(event.getPacketType());
		if (handler != null) {
			handler.handle(
				event.getPacket(),
				this::processItem,
				event.getPlayer()
			);
		}
	}

	@FunctionalInterface
	private interface ItemProcessor {
		boolean process(
			@NotNull Material material,
			int amount,
			@NotNull ItemMeta meta,
			@NotNull Player recipient
		);
	}

	protected abstract boolean processItem(
		@NotNull Material material,
		int amount,
		@NotNull ItemMeta meta,
		@NotNull Player recipient
	);

	// ============================================================
	// Handlers
	// ============================================================

	private static void handleWindowItems(
		final @NotNull PacketContainer container,
		final @NotNull ItemProcessor itemProcessor,
		final @NotNull Player recipient
	) {
		final ClientboundContainerSetContentPacket handle; // For reference

		container.getItemListModifier().modify(0, (items) -> {
			processItems(items.listIterator(), itemProcessor, recipient);
			return items;
		});
		container.getItemModifier().modify(0, (original) -> {
			return processItem(original, itemProcessor, recipient).orElse(original);
		});
	}

	private static void handleSetSlot(
		final @NotNull PacketContainer container,
		final @NotNull ItemProcessor itemProcessor,
		final @NotNull Player recipient
	) {
		final ClientboundContainerSetSlotPacket handle; // For reference

		container.getItemModifier().modify(0, (original) -> {
			return processItem(original, itemProcessor, recipient).orElse(original);
		});
	}

	private static void handleMerchantOffers(
		final @NotNull PacketContainer container,
		final @NotNull ItemProcessor itemProcessor,
		final @NotNull Player recipient
	) {
		final ClientboundMerchantOffersPacket handle; // For reference

		container.getMerchantRecipeLists().modify(0, (offers) -> {
			for (final var iter = offers.listIterator(); iter.hasNext();) {
				final MerchantRecipe originalOffer = iter.next();

				final List<ItemStack> ingredients = originalOffer.getIngredients();
				processItems(ingredients.listIterator(), itemProcessor, recipient);

				final Optional<ItemStack> result = processItem(originalOffer.getResult(), itemProcessor, recipient);
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
		final @NotNull ListIterator<ItemStack> iter,
		final @NotNull ItemProcessor itemProcessor,
		final @NotNull Player recipient
	) {
		while (iter.hasNext()) {
			processItem(iter.next(), itemProcessor, recipient).ifPresent(iter::set);
		}
	}

	private static Optional<ItemStack> processItem(
		ItemStack item,
		final @NotNull ItemProcessor itemProcessor,
		final @NotNull Player recipient
	) {
		if (item == null) {
			return Optional.empty();
		}
		final Material material = item.getType();
		if (material == Material.AIR) {
			return Optional.empty();
		}
		item = item.clone();
		final ItemMeta meta = item.getItemMeta();
		if (meta == null) { // Shouldn't happen, but just in case
			return Optional.empty();
		}
		if (!itemProcessor.process(material, item.getAmount(), meta, recipient)) {
			return Optional.empty();
		}
		item.setItemMeta(meta);
		return Optional.of(item);
	}
}
