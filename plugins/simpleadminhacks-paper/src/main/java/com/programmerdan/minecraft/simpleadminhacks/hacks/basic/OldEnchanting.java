package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import com.programmerdan.minecraft.simpleadminhacks.framework.utilities.PacketManager;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import net.minecraft.world.inventory.ContainerEnchantTable;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftInventoryView;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.projectiles.ProjectileSource;
import vg.civcraft.mc.civmodcore.entities.EntityUtils;
import vg.civcraft.mc.civmodcore.inventory.InventoryUtils;
import vg.civcraft.mc.civmodcore.inventory.RecipeManager;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.world.WorldUtils;

public final class OldEnchanting extends BasicHack {

	private static final ItemStack LAPIS_ITEM = new ItemStack(Material.LAPIS_LAZULI, 64);
	private static final ItemStack EMERALD_ITEM = new ItemStack(Material.EMERALD, 1);
	private static final Random RANDOM = new SecureRandom();

	private final PacketManager packets;
	private final ShapelessRecipe emeraldToExp;
	private final ShapedRecipe expToEmerald;
	private final Field enchantingTableRandomiser;
	private final Map<EntityType, Double> entityExpDropModifiers;

	@AutoLoad
	private boolean hideEnchants;

	@AutoLoad
	private boolean fillLapis;

	@AutoLoad
	private boolean randomiseEnchants;

	@AutoLoad
	private boolean fixEnchantCosts;

	@AutoLoad
	private double experienceModifier;

	@AutoLoad
	private double lootModifier;

	@AutoLoad
	private boolean emeraldCrafting;

	@AutoLoad
	private boolean emeraldLeveling;

	@AutoLoad
	private boolean disableGrindExp;

	@AutoLoad
	private boolean preventOrbExp;

	@AutoLoad
	private boolean directBottleExp;

	@AutoLoad
	private int maxRepairCost;

	@AutoLoad
	private int expPerBottle;

	@AutoLoad
	private boolean allowExpRecovery;

	@AutoLoad
	private boolean disableEnchantedBookCreation;

	@AutoLoad
	private boolean disableEnchantedBookUsage;

	public OldEnchanting(final SimpleAdminHacks plugin, final BasicHackConfig config) {
		super(plugin, config);
		// Setup packet manager
		this.packets = new PacketManager();
		// Recipe that crafts Bottles o' Enchanting from Emeralds
		this.emeraldToExp = new ShapelessRecipe(
				new NamespacedKey(plugin, "emeraldToBottle"),
				new ItemStack(Material.EXPERIENCE_BOTTLE, 9));
		this.emeraldToExp.addIngredient(Material.EMERALD);
		// Recipe that crafts Emeralds from Bottles o' Enchanting
		this.expToEmerald = new ShapedRecipe(
				new NamespacedKey(plugin, "bottleToEmerald"),
				EMERALD_ITEM);
		this.expToEmerald.shape("xxx", "xxx", "xxx");
		this.expToEmerald.setIngredient('x', Material.EXPERIENCE_BOTTLE);
		// Setup enchantment randomiser
		Field randomiser = null;
		try {
			randomiser = FieldUtils.getDeclaredField(ContainerEnchantTable.class, "p", true);
			final Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(randomiser, randomiser.getModifiers() & ~Modifier.FINAL);
		}
		catch (final Exception exception) {
			plugin.warning("[OldEnchanting] An exception was thrown while trying to reflect the enchanting " +
					"table's randomiser field.", exception);
		}
		this.enchantingTableRandomiser = randomiser;
		// Setup entity xp modifiers
		this.entityExpDropModifiers = new HashMap<>();
	}

	@Override
	public void onEnable() {
		super.onEnable();
		if (this.experienceModifier < 0.0d) {
			this.plugin.warning("[OldEnchanting] Experience modifier [" + this.experienceModifier + "] " +
					"is unsupported, defaulting to 0.2");
			this.experienceModifier = 0.2d;
		}
		if (this.lootModifier < 0.0d) {
			this.plugin.warning("[OldEnchanting] Loot modifier [" + this.lootModifier + "] " +
					"is unsupported, defaulting to 1.5");
			this.lootModifier = 1.5d;
		}
		if (this.maxRepairCost < 2 && this.maxRepairCost != -1) {
			this.plugin.warning("[OldEnchanting] Maximum repair cost [" + this.maxRepairCost + "] " +
					"is unsupported, defaulting to 33");
			this.maxRepairCost = 33;
		}
		if (this.expPerBottle < -1) {
			this.plugin.warning("[OldEnchanting] Experience per bottle [" + this.expPerBottle + "] " +
					"is unsupported, defaulting to 10");
			this.expPerBottle = 10;
		}
		{ // @AutoLoad is not sophisticated enough to handle maps
			final ConfigurationSection base = this.config.getBase();
			final ConfigurationSection modifiers = base.getConfigurationSection("entityExpDropModifiers");
			if (modifiers != null) {
				for (final String key : modifiers.getKeys(false)) {
					final EntityType type = EntityUtils.getEntityType(key);
					if (type == null) {
						this.plugin.warning("[OldEnchanting] EntityType [" + key + "] does not exist, skipping.");
						continue;
					}
					double modifier = modifiers.getDouble(key, 1.0d);
					if (modifier < 0.0d) {
						this.plugin.warning("[OldEnchanting] Experience modifier [" + modifier + "] for " +
								"[" + key + "] is unsupported, defaulting to 1.0");
						modifier = 1.0d;
					}
					this.entityExpDropModifiers.put(type, modifier);
				}
			}
		}
		if (this.hideEnchants) {
			this.packets.addAdapter(new PacketAdapter(this.plugin, PacketType.Play.Server.WINDOW_DATA) {
				@Override
				public void onPacketSending(final PacketEvent event) {
					final InventoryType type = event.getPlayer().getOpenInventory().getType();
					if (type == InventoryType.ENCHANTING) {
						final PacketContainer packet = event.getPacket();
						final int property = packet.getIntegers().read(1);
						switch (property) {
							case 3:
							case 4:
							case 5:
							case 6:
								packet.getIntegers().write(2, -1);
								break;
						}
					}
				}
			});
		}
		if (this.emeraldCrafting) {
			RecipeManager.registerRecipe(this.emeraldToExp);
			RecipeManager.registerRecipe(this.expToEmerald);
		}
	}

	@Override
	public void onDisable() {
		if (this.fillLapis) {
			for (final Player player : Bukkit.getOnlinePlayers()) {
				final InventoryView inventory = player.getOpenInventory();
				if (inventory.getType() != InventoryType.ENCHANTING) {
					continue;
				}
				inventory.setItem(1, null);
			}
		}
		this.entityExpDropModifiers.clear();
		this.packets.removeAllAdapters();
		if (this.emeraldCrafting) {
			RecipeManager.removeRecipe(this.emeraldToExp);
			RecipeManager.removeRecipe(this.expToEmerald);
		}
		super.onDisable();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDeath(final EntityDeathEvent event) {
		if (this.disableGrindExp) {
			event.setDroppedExp(0);
			return;
		}
		final LivingEntity entity = event.getEntity();
		final EntityType entityType = entity.getType();
		int experience = event.getDroppedExp();
		if (this.entityExpDropModifiers.containsKey(entityType)) {
			experience = applyModifier(experience, this.entityExpDropModifiers.get(entityType));
		}
		else if (entityType != EntityType.PLAYER) {
			experience = applyModifier(experience, this.experienceModifier);
		}
		if (entityType != EntityType.PLAYER) {
			final Player killer = entity.getKiller();
			if (killer != null) {
				final ItemStack held = killer.getInventory().getItemInMainHand();
				if (ItemUtils.isValidItem(held) && held.hasItemMeta()) {
					final ItemMeta meta = held.getItemMeta();
					if (meta.hasEnchant(Enchantment.LOOT_BONUS_MOBS)) {
						final double modifier = this.lootModifier * meta.getEnchantLevel(Enchantment.LOOT_BONUS_MOBS);
						experience = applyModifier(experience, modifier);
					}
				}
			}
		}
		event.setDroppedExp(Math.max(0, experience));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockExp(final BlockExpEvent event) {
		if (this.disableGrindExp) {
			event.setExpToDrop(0);
		}
		else {
			event.setExpToDrop(applyModifier(event.getExpToDrop(), this.experienceModifier));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onFurnaceExp(final FurnaceExtractEvent event) {
		if (this.disableGrindExp) {
			event.setExpToDrop(0);
		}
		else {
			event.setExpToDrop(applyModifier(event.getExpToDrop(), this.experienceModifier));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onFishingExp(final PlayerFishEvent event) {
		if (this.disableGrindExp) {
			event.setExpToDrop(0);
		}
		else {
			event.setExpToDrop(applyModifier(event.getExpToDrop(), this.experienceModifier));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBreedExp(final EntityBreedEvent event) {
		if (this.disableGrindExp) {
			event.setExperience(0);
		}
		else {
			event.setExperience(applyModifier(event.getExperience(), this.experienceModifier));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onMerchantRecipe(final InventoryOpenEvent event) {
		if (!this.disableGrindExp) {
			return;
		}
		final Inventory inventory = event.getInventory();
		if (!(inventory instanceof Merchant)) {
			return;
		}
		for (final MerchantRecipe recipe : ((Merchant) inventory).getRecipes()) {
			recipe.setExperienceReward(false);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onExpChange(final PlayerExpChangeEvent event) {
		if (this.preventOrbExp) {
			event.setAmount(0);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onExpBottle(final ExpBottleEvent event) {
		if (this.expPerBottle != -1) { // If bottle xp is not random
			event.setExperience(this.expPerBottle);
		}
		if (this.directBottleExp) {
			final ThrownExpBottle bottle = event.getEntity();
			final ProjectileSource thrower = bottle.getShooter();
			if (thrower instanceof Player) {
				final Player player = (Player) thrower;
				player.giveExp(event.getExperience());
				// Play the experience sound as no experience orbs will be spawned
				// Credit to Team CoFH for the random pitch generator, see their code below
				// https://github.com/CoFH/ThermalFoundation/blob/1.12/src/main/java/cofh/thermalfoundation/item/tome/ItemTomeExperience.java#L268
				// This is a reasonable use of their "Copy portions of this code for use in other projects." clause.
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1F,
						(RANDOM.nextFloat() - RANDOM.nextFloat()) * 0.35f + 0.9f);
				event.setExperience(0);
				bottle.teleport(player);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onEmeraldExp(final PlayerInteractEvent event) {
		if (!this.emeraldCrafting
				|| !this.emeraldLeveling
				|| this.expPerBottle == 0) {
			return;
		}
		switch (event.getAction()) {
			case RIGHT_CLICK_AIR:
			case RIGHT_CLICK_BLOCK:
				break;
			default:
				return;
		}
		final Player player = event.getPlayer();
		final PlayerInventory inventory = player.getInventory();
		final EquipmentSlot slot = Objects.requireNonNull(event.getHand());
		final ItemStack held = inventory.getItem(slot);
		if (!ItemUtils.isValidItem(held)
				|| !ItemUtils.areItemsSimilar(held, EMERALD_ITEM)) {
			return;
		}
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			final Block clicked = Objects.requireNonNull(event.getClickedBlock());
			if (clicked.getType().isInteractable()) {
				event.setCancelled(true); // Don't give levels if trying to open a chest for example
				return;
			}
		}
		final int amount = held.getAmount();
		if (amount <= 0) {
			return;
		}
		int experience = 0;
		if (this.expPerBottle != -1) { // If bottle xp is not random
			experience = this.expPerBottle * 9;
		}
		else {
			// Simulate nine thrown bottles, which drop between 3-11 experience
			// https://minecraft.gamepedia.com/Bottle_o%27_Enchanting
			for (int i = 0; i < 9; i++) {
				experience += RANDOM.nextInt(11 - 3 + 1) + 3;
			}
		}
		player.giveExp(experience);
		inventory.setItem(slot, ItemUtils.decrementItem(held));
		event.setCancelled(true); // Give the emerald leveling precedence
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerExpRecovery(final PlayerInteractEvent event) {
		if (!this.allowExpRecovery
				|| this.expPerBottle == -1 // If bottle xp is random
				|| this.expPerBottle == 0 // If bottle xp is nothing
				|| event.getAction() != Action.LEFT_CLICK_BLOCK) {
			return;
		}
		final Block block = event.getClickedBlock();
		if (!WorldUtils.isValidBlock(block)
				|| block.getType() != Material.ENCHANTING_TABLE) {
			return;
		}
		final Player player = event.getPlayer();
		final PlayerInventory inventory = player.getInventory();
		final ItemStack held = inventory.getItemInMainHand();
		if (held.getType() != Material.GLASS_BOTTLE) {
			return;
		}
		final int totalExp = computeCurrentXP(player);
		if (totalExp < this.expPerBottle) {
			return;
		}
		createExpBottles(player, totalExp);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPrepareBookEnchant(final PrepareItemEnchantEvent event) {
		if (!this.disableEnchantedBookCreation) {
			return;
		}
		final ItemStack item = event.getItem();
		if (!ItemUtils.isValidItem(item)
				|| item.getType() != Material.BOOK) {
			return;
		}
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPrepareBookUsage(final PrepareAnvilEvent event) {
		if (!this.disableEnchantedBookUsage) {
			return;
		}
		final AnvilInventory inventory = event.getInventory();
		if (inventory.first(Material.ENCHANTED_BOOK) != -1) {
			if (event.getResult() != null) {
				event.setResult(null);
				// This is needed because of client side shenanigans
				Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
					for (final Player viewer : InventoryUtils.getViewingPlayers(inventory)) {
						viewer.updateInventory();
					}
				}, 1L);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPrepareItemEnchant(final PrepareItemEnchantEvent event) {
		final CraftInventoryView view = (CraftInventoryView) event.getView();
		final ContainerEnchantTable table = (ContainerEnchantTable) view.getHandle();
		if (this.randomiseEnchants) {
			try {
				this.enchantingTableRandomiser.set(table, RANDOM);
			}
			catch (final IllegalArgumentException | IllegalAccessException exception) {
				this.plugin.warning("[OldEnchanting] Could not set randomiser!", exception);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onAnvilRepair(final PrepareAnvilEvent event) {
		if (this.maxRepairCost == -1) { // If no maximum
			return;
		}
		final ItemStack result = event.getResult();
		if (!ItemUtils.isValidItem(result)) {
			return;
		}
		ItemUtils.handleItemMeta(result, (Repairable meta) -> {
			final int newRepairCost = this.maxRepairCost - 2;
			if (meta.getRepairCost() < newRepairCost) {
				return false;
			}
			meta.setRepairCost(newRepairCost);
			return true;
		});
		event.setResult(result);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEnchantItem(final EnchantItemEvent event) {
		if (this.fixEnchantCosts) {
			final Player player = event.getEnchanter();
			if (player.getLevel() < event.getExpLevelCost()) {
				event.setCancelled(true);
				return;
			}
			// The level cost of enchantments is always the button index + 1, assuming the server is Bukkit.
			// And so if we want the enchantment to actually cost 30 levels as required by the ExpLevelCost, we need to
			// make up for the difference with a manual level reduction of the remainder.
			final int bukkitLevelCost = event.whichButton() + 1;
			final int levelRemainder = event.getExpLevelCost() - bukkitLevelCost;
			final int newLevel = player.getLevel() - levelRemainder;
			player.setLevel(newLevel);
			// The exp level cost needs to be reduced to the remaining cost, otherwise Bukkit's enchantment process
			// still expects you to have thirty levels on you.
			event.setExpLevelCost(bukkitLevelCost);
		}
		if (this.fillLapis) {
			event.getInventory().setItem(1, LAPIS_ITEM.clone());
		}
	}

	// TODO: This function causes weirdness when you place Lapis Lazuli in the item to enchant slot. It should be
	//    modified to only cancel the event if items are being added to or removed from the consumable slot.
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryClick(final InventoryClickEvent event) {
		if (!this.fillLapis
				|| event instanceof InventoryCreativeEvent) {
			return;
		}
		final Inventory inventory = event.getClickedInventory();
		if (!InventoryUtils.isValidInventory(inventory) || inventory.getType() != InventoryType.ENCHANTING) {
			return;
		}
		final ItemStack currentItem = event.getCurrentItem();
		if (!ItemUtils.isValidItem(currentItem) || !currentItem.isSimilar(LAPIS_ITEM)) {
			return;
		}
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onOpenEnchantingTable(final InventoryOpenEvent event) {
		if (!this.fillLapis) {
			return;
		}
		final Inventory inventory = event.getInventory();
		if (inventory.getType() != InventoryType.ENCHANTING) {
			return;
		}
		event.getInventory().setItem(1, LAPIS_ITEM.clone());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onCloseEnchantingTable(final InventoryCloseEvent event) {
		if (!this.fillLapis) {
			return;
		}
		final Inventory inventory = event.getInventory();
		if (inventory.getType() != InventoryType.ENCHANTING) {
			return;
		}
		inventory.setItem(1, null);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBreakEnchantingTable(final BlockBreakEvent event) {
		if (!this.fillLapis) {
			return;
		}
		final Block block = event.getBlock();
		if (block.getType() != Material.ENCHANTING_TABLE) {
			return;
		}
		block.getDrops().removeIf(drop -> drop.isSimilar(LAPIS_ITEM));
	}

	// ------------------------------------------------------------
	// Do not touch anything below unless you want a headache!
	// ------------------------------------------------------------

	private int applyModifier(int experience, double modifier) {
		return (int) Math.max(0, Math.ceil(experience * modifier));
	}

	private int computeCurrentXP(Player player) {
		float currentLevel = player.getLevel();
		float progress = player.getExp();
		float a = 1f, b = 6f, c = 0f, x = 2f, y = 7f;
		if (currentLevel > 16 && currentLevel <= 31) {
			a = 2.5f;
			b = -40.5f;
			c = 360f;
			x = 5f;
			y = -38f;
		}
		else if (currentLevel >= 32) {
			a = 4.5f;
			b = -162.5f;
			c = 2220f;
			x = 9f;
			y = -158f;
		}
		return (int) Math
				.floor(a * currentLevel * currentLevel + b * currentLevel + c + progress * (x * currentLevel + y));
	}

	private void createExpBottles(Player player, int totalExp) {
		ItemMap inv = new ItemMap(player.getInventory());
		int bottles = inv.getAmount(new ItemStack(Material.GLASS_BOTTLE));
		int xpavailable = totalExp / this.expPerBottle;
		int remove = Math.min(bottles, xpavailable);
		if (remove == 0) {
			return;
		}
		boolean noSpace = false;
		int bottleCount = 0;
		ItemMap removeMap = new ItemMap();
		removeMap.addItemAmount(new ItemStack(Material.GLASS_BOTTLE), remove);
		for (ItemStack is : removeMap.getItemStackRepresentation()) {
			int initialAmount = is.getAmount();
			player.getInventory().removeItem(is);
			is.setType(Material.EXPERIENCE_BOTTLE);
			HashMap<Integer, ItemStack> result = player.getInventory().addItem(is);
			if (!result.isEmpty()) {
				is.setType(Material.GLASS_BOTTLE);
				player.getInventory().addItem(is);
				noSpace = true;
				break;
			}
			else {
				bottleCount += initialAmount;
			}
		}
		if (bottleCount > 0) {
			int endXP = totalExp - bottleCount * this.expPerBottle;
			player.setLevel(0);
			player.setExp(0);
			player.giveExp(endXP);
			player.sendMessage(ChatColor.GREEN + "Created " + bottleCount + " XP bottles.");
		}
		if (noSpace) {
			player.sendMessage(ChatColor.RED + "Not enough space in inventory for all XP bottles.");
		}
	}

}
