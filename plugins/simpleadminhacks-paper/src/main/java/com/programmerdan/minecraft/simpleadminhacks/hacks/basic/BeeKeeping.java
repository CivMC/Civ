package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.google.common.base.Strings;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_16_R3.TileEntity;
import net.minecraft.server.v1_16_R3.TileEntityBeehive;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.serialization.NBTCompound;
import vg.civcraft.mc.civmodcore.util.MoreCollectionUtils;

public final class BeeKeeping extends BasicHack {

	private static final String BEES_LIST_KEY = "Bees";
	private static final String BEE_DATA_KEY = "EntityData";
	private static final String BEE_NAME_KEY = "CustomName";
	private static final Set<Material> HIVE_MATERIALS = Set.of(Material.BEE_NEST, Material.BEEHIVE);

	public BeeKeeping(final SimpleAdminHacks plugin, final BasicHackConfig config) {
		super(plugin, config);
	}

	public static BasicHackConfig generate(final SimpleAdminHacks plugin, final ConfigurationSection config) {
		return new BasicHackConfig(plugin, config);
	}

	// ------------------------------------------------------------
	// Functionality
	// ------------------------------------------------------------

	@EventHandler(ignoreCancelled = true)
	public void showHiveDetails(final PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		final ItemStack held = event.getItem();
		if (ItemUtils.isValidItem(held)) {
			return;
		}
		final Block block = Objects.requireNonNull(event.getClickedBlock());
		if (!HIVE_MATERIALS.contains(block.getType())) {
			return;
		}
		final TileEntityBeehive beehive = getBeeHive(block);
		if (beehive.isEmpty()) {
			player.sendMessage(ChatColor.GOLD + "There aren't any bees in that hive.");
			return;
		}
		final List<Bee> bees = getBeesFromHive(beehive);
		final int numberOfUnnamed = MoreCollectionUtils.numberOfMatches(bees, Bee::isNameless);
		bees.removeIf(Bee::isNameless);
		// Start building response
		final BaseComponent response = ChatUtils.textComponent("", ChatColor.GOLD);
		final Iterator<Bee> nameIterator = bees.iterator();
		boolean doneFirstElement = false;
		while (nameIterator.hasNext()) {
			final Bee bee = nameIterator.next();
			if (nameIterator.hasNext() || numberOfUnnamed > 0) {
				if (doneFirstElement) {
					response.addExtra(", ");
				}
			}
			else if (numberOfUnnamed == 0) {
				response.addExtra(", and ");
			}
			response.addExtra(bee.name);
			doneFirstElement = true;
		}
		if (numberOfUnnamed > 0) {
			if (bees.isEmpty()) {
				response.addExtra("There are " + numberOfUnnamed + " bees");
			}
			else {
				response.addExtra(", and " + numberOfUnnamed + " others are");
			}
		}
		else {
			response.addExtra(" are");
		}
		if (beehive.isSedated()) {
			response.addExtra(" sedated");
		}
		else {
			response.addExtra(" happily buzzing");
		}
		response.addExtra(" in that hive.");
		player.sendMessage(response);
	}

	private static TileEntityBeehive getBeeHive(@Nonnull final Block block) {
		final CraftBlock craftBlock = (CraftBlock) block;
		final CraftWorld craftWorld = craftBlock.getCraftWorld();
		final WorldServer worldServer = craftWorld.getHandle();
		final TileEntity tileEntity = worldServer.getTileEntity(craftBlock.getPosition());
		return (TileEntityBeehive) Objects.requireNonNull(tileEntity);
	}

	private static List<Bee> getBeesFromHive(@Nonnull final TileEntityBeehive hive) {
		final NBTCompound nbt = new NBTCompound();
		hive.save(nbt.getRAW()); // Serialise onto the NBT compound
		return Stream.of(nbt.getCompoundArray(BEES_LIST_KEY))
				.map(bee -> bee.getCompound(BEE_DATA_KEY))
				.map(Bee::new)
				.collect(Collectors.toCollection(ArrayList::new));
	}

	// ------------------------------------------------------------
	// Bee Data Class
	// ------------------------------------------------------------

	private static final class Bee {

		public final BaseComponent name;

		public Bee(@Nonnull final NBTCompound nbt) {
			// Parse name
			final String rawName = nbt.getString(BEE_NAME_KEY);
			if (Strings.isNullOrEmpty(rawName)) {
				this.name = null;
			}
			else {
				final BaseComponent componentName = ComponentSerializer.parse(rawName)[0];
				this.name = ChatUtils.isNullOrEmpty(componentName) ? null : componentName;
			}
		}

		public boolean isNameless() {
			return this.name == null;
		}

	}

}
