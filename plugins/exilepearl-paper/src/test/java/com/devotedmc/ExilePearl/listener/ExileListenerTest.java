package com.devotedmc.ExilePearl.listener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.NullArgumentException;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.block.Hopper;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.LingeringPotion;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Wolf;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.junit.Before;
import org.junit.Test;

import com.devotedmc.ExilePearl.BrewHandler;
import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.ExileRule;
import com.devotedmc.ExilePearl.config.PearlConfig;
import com.devotedmc.ExilePearl.event.PlayerPearledEvent;
import com.google.common.base.Function;

@SuppressWarnings("deprecation")
public class ExileListenerTest {

	private ExilePearlApi pearlApi;
	private PearlConfig config;
	private ExileListener dut;

	final UUID uid = UUID.randomUUID();
	final Player player = mock(Player.class);
	final ExilePearl pearl = mock(ExilePearl.class);

	// Used for damage events
	final Map<DamageModifier, Double> modifiers = new HashMap<DamageModifier, Double>();
	final Map<DamageModifier, Function<Double, Double>> modifierFunctions = new HashMap<DamageModifier, Function<Double, Double>>();

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		config = mock(PearlConfig.class);

		pearlApi = mock(ExilePearlApi.class);
		when(pearlApi.getPearlConfig()).thenReturn(config);

		dut = new ExileListener(pearlApi);

		when(player.getUniqueId()).thenReturn(uid);

		modifiers.put(DamageModifier.BASE, 1d);
		modifierFunctions.put(DamageModifier.BASE, (Function<Double, Double>)mock(Function.class));
	}

	@Test
	public void testExileListener() {
		// Null arguments throw exceptions
		Throwable e = null;
		try { new ExileListener(null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
	}

	@Test
	public void testOnPlayerPearled() {
		when(config.canPerform(ExileRule.USE_BED)).thenReturn(false);

		PlayerPearledEvent e = new PlayerPearledEvent(pearl);
		dut.onPlayerPearled(e);
		verify(player, times(0)).setBedSpawnLocation(null, true);

		when(pearl.getPlayer()).thenReturn(player);
		dut.onPlayerPearled(e);
		verify(player, times(0)).setBedSpawnLocation(null, true);

		when(player.isOnline()).thenReturn(true);
		when(config.canPerform(ExileRule.USE_BED)).thenReturn(true);
		dut.onPlayerPearled(e);
		verify(player, times(0)).setBedSpawnLocation(null, true);

		when(config.canPerform(ExileRule.USE_BED)).thenReturn(false);
		dut.onPlayerPearled(e);
		verify(player, times(1)).setBedSpawnLocation(null, true);
	}

	@Test
	public void testOnPlayerEnterBed() {
		PlayerBedEnterEvent e = new PlayerBedEnterEvent(player, mock(Block.class));
		when(config.canPerform(ExileRule.USE_BED)).thenReturn(true);
		dut.onPlayerEnterBed(e);
		assertFalse(e.isCancelled());

		e = new PlayerBedEnterEvent(player, mock(Block.class));
		when(config.canPerform(ExileRule.USE_BED)).thenReturn(false);
		dut.onPlayerEnterBed(e);
		assertFalse(e.isCancelled());

		e = new PlayerBedEnterEvent(player, mock(Block.class));
		when(config.canPerform(ExileRule.USE_BED)).thenReturn(true);
		when(pearlApi.isPlayerExiled(uid)).thenReturn(true);
		dut.onPlayerEnterBed(e);
		assertFalse(e.isCancelled());

		e = new PlayerBedEnterEvent(player, mock(Block.class));
		when(config.canPerform(ExileRule.USE_BED)).thenReturn(false);
		dut.onPlayerEnterBed(e);
		assertTrue(e.isCancelled());
	}

	@Test
	public void testOnPearlThrow() {
		final EnderPearl pearl = mock(EnderPearl.class);
		when(pearl.getShooter()).thenReturn(player);

		ProjectileLaunchEvent e = new ProjectileLaunchEvent(pearl);
		when(config.canPerform(ExileRule.THROW_PEARL)).thenReturn(true);
		dut.onPearlThrow(e);
		assertFalse(e.isCancelled());

		e = new ProjectileLaunchEvent(pearl);
		when(config.canPerform(ExileRule.THROW_PEARL)).thenReturn(false);
		dut.onPearlThrow(e);
		assertFalse(e.isCancelled());

		e = new ProjectileLaunchEvent(pearl);
		when(config.canPerform(ExileRule.THROW_PEARL)).thenReturn(true);
		when(pearlApi.isPlayerExiled(uid)).thenReturn(true);
		dut.onPearlThrow(e);
		assertFalse(e.isCancelled());

		e = new ProjectileLaunchEvent(pearl);
		when(config.canPerform(ExileRule.THROW_PEARL)).thenReturn(false);
		dut.onPearlThrow(e);
		assertTrue(e.isCancelled());

		e = new ProjectileLaunchEvent(mock(Projectile.class));
		when(config.canPerform(ExileRule.THROW_PEARL)).thenReturn(false);
		dut.onPearlThrow(e);
		assertFalse(e.isCancelled());
	}

	@Test
	public void testOnPlayerFillBucket() {
		PlayerBucketFillEvent e = new PlayerBucketFillEvent(player, null, null, null, null);
		when(config.canPerform(ExileRule.FILL_BUCKET)).thenReturn(true);
		dut.onPlayerFillBucket(e);
		assertFalse(e.isCancelled());

		e = new PlayerBucketFillEvent(player, null, null, null, null);
		when(config.canPerform(ExileRule.FILL_BUCKET)).thenReturn(false);
		dut.onPlayerFillBucket(e);
		assertFalse(e.isCancelled());

		e = new PlayerBucketFillEvent(player, null, null, null, null);
		when(config.canPerform(ExileRule.FILL_BUCKET)).thenReturn(true);
		when(pearlApi.isPlayerExiled(uid)).thenReturn(true);
		dut.onPlayerFillBucket(e);
		assertFalse(e.isCancelled());

		e = new PlayerBucketFillEvent(player, null, null, null, null);
		when(config.canPerform(ExileRule.FILL_BUCKET)).thenReturn(false);
		dut.onPlayerFillBucket(e);
		assertTrue(e.isCancelled());
	}

	@Test
	public void testOnPlayerMilkCow() {
		ItemStack milkBucket = new ItemStack(Material.MILK_BUCKET, 1);
		PlayerBucketFillEvent e = new PlayerBucketFillEvent(player, null, null, null, milkBucket);
		when(config.canPerform(ExileRule.MILK_COWS)).thenReturn(true);
		dut.onPlayerFillBucket(e);
		assertFalse(e.isCancelled());

		e = new PlayerBucketFillEvent(player, null, null, null, milkBucket);
		when(config.canPerform(ExileRule.MILK_COWS)).thenReturn(false);
		dut.onPlayerFillBucket(e);
		assertFalse(e.isCancelled());

		e = new PlayerBucketFillEvent(player, null, null, null, milkBucket);
		when(config.canPerform(ExileRule.MILK_COWS)).thenReturn(true);
		when(pearlApi.isPlayerExiled(uid)).thenReturn(true);
		dut.onPlayerFillBucket(e);
		assertFalse(e.isCancelled());

		e = new PlayerBucketFillEvent(player, null, null, null, milkBucket);
		when(config.canPerform(ExileRule.MILK_COWS)).thenReturn(false);
		dut.onPlayerFillBucket(e);
		assertTrue(e.isCancelled());
	}

	@Test
	public void testOnPlayerEmptyBucket() {
		PlayerBucketEmptyEvent e = new PlayerBucketEmptyEvent(player, null, null, null, null);
		when(config.canPerform(ExileRule.USE_BUCKET)).thenReturn(true);
		dut.onPlayerEmptyBucket(e);
		assertFalse(e.isCancelled());

		e = new PlayerBucketEmptyEvent(player, null, null, null, null);
		when(config.canPerform(ExileRule.USE_BUCKET)).thenReturn(false);
		dut.onPlayerEmptyBucket(e);
		assertFalse(e.isCancelled());

		e = new PlayerBucketEmptyEvent(player, null, null, null, null);
		when(config.canPerform(ExileRule.USE_BUCKET)).thenReturn(true);
		when(pearlApi.isPlayerExiled(uid)).thenReturn(true);
		dut.onPlayerEmptyBucket(e);
		assertFalse(e.isCancelled());

		e = new PlayerBucketEmptyEvent(player, null, null, null, null);
		when(config.canPerform(ExileRule.USE_BUCKET)).thenReturn(false);
		dut.onPlayerEmptyBucket(e);
		assertTrue(e.isCancelled());
	}

	@Test
	public void testOnPlayerFillCauldron() {
		Block block = mock(Block.class);
		when(block.getType()).thenReturn(Material.CAULDRON);
		PlayerBucketEmptyEvent e = new PlayerBucketEmptyEvent(player, block, null, null, null);
		when(config.canPerform(ExileRule.FILL_CAULDRON)).thenReturn(true);
		dut.onPlayerEmptyBucket(e);
		assertFalse(e.isCancelled());

		e = new PlayerBucketEmptyEvent(player, block, null, null, null);
		when(config.canPerform(ExileRule.FILL_CAULDRON)).thenReturn(false);
		dut.onPlayerEmptyBucket(e);
		assertFalse(e.isCancelled());

		e = new PlayerBucketEmptyEvent(player, block, null, null, null);
		when(config.canPerform(ExileRule.FILL_CAULDRON)).thenReturn(true);
		when(pearlApi.isPlayerExiled(uid)).thenReturn(true);
		dut.onPlayerEmptyBucket(e);
		assertFalse(e.isCancelled());

		e = new PlayerBucketEmptyEvent(player, block, null, null, null);
		when(config.canPerform(ExileRule.FILL_CAULDRON)).thenReturn(false);
		dut.onPlayerEmptyBucket(e);
		assertTrue(e.isCancelled());
	}

	@Test
	public void testOnPlayerInteractBrewing() {
		Block block = mock(Block.class);
		when(block.getType()).thenReturn(Material.BREWING_STAND);

		PlayerInteractEvent e = new PlayerInteractEvent(player, null, null, block, null);
		when(config.canPerform(ExileRule.BREW)).thenReturn(true);
		dut.onPlayerInteract(e);
		assertFalse(e.isCancelled());

		e = new PlayerInteractEvent(player, null, null, block, null);
		when(config.canPerform(ExileRule.BREW)).thenReturn(false);
		dut.onPlayerInteract(e);
		assertFalse(e.isCancelled());

		e = new PlayerInteractEvent(player, null, null, block, null);
		when(config.canPerform(ExileRule.BREW)).thenReturn(true);
		when(pearlApi.isPlayerExiled(uid)).thenReturn(true);
		dut.onPlayerInteract(e);
		assertFalse(e.isCancelled());

		e = new PlayerInteractEvent(player, null, null, block, null);
		when(config.canPerform(ExileRule.BREW)).thenReturn(false);
		dut.onPlayerInteract(e);
		assertTrue(e.isCancelled());
	}

	@Test
	public void testOnPlayerInteractAnvil() {
		Block block = mock(Block.class);
		when(block.getType()).thenReturn(Material.ANVIL);

		PlayerInteractEvent e = new PlayerInteractEvent(player, null, null, block, null);
		when(config.canPerform(ExileRule.USE_ANVIL)).thenReturn(true);
		dut.onPlayerInteract(e);
		assertFalse(e.isCancelled());

		e = new PlayerInteractEvent(player, null, null, block, null);
		when(config.canPerform(ExileRule.USE_ANVIL)).thenReturn(false);
		dut.onPlayerInteract(e);
		assertFalse(e.isCancelled());

		e = new PlayerInteractEvent(player, null, null, block, null);
		when(config.canPerform(ExileRule.USE_ANVIL)).thenReturn(true);
		when(pearlApi.isPlayerExiled(uid)).thenReturn(true);
		dut.onPlayerInteract(e);
		assertFalse(e.isCancelled());

		e = new PlayerInteractEvent(player, null, null, block, null);
		when(config.canPerform(ExileRule.USE_ANVIL)).thenReturn(false);
		dut.onPlayerInteract(e);
		assertTrue(e.isCancelled());
	}

	@Test
	public void testOnPlayerEnchant() {
		Map<Enchantment, Integer> enchants = new HashMap<Enchantment, Integer>();
		EnchantItemEvent e = new EnchantItemEvent(player, null, null, null, 0, enchants, 0);
		when(config.canPerform(ExileRule.ENCHANT)).thenReturn(true);
		dut.onPlayerEnchant(e);
		assertFalse(e.isCancelled());

		e = new EnchantItemEvent(player, null, null, null, 0, enchants, 0);
		when(config.canPerform(ExileRule.ENCHANT)).thenReturn(false);
		dut.onPlayerEnchant(e);
		assertFalse(e.isCancelled());

		e = new EnchantItemEvent(player, null, null, null, 0, enchants, 0);
		when(config.canPerform(ExileRule.ENCHANT)).thenReturn(true);
		when(pearlApi.isPlayerExiled(uid)).thenReturn(true);
		dut.onPlayerEnchant(e);
		assertFalse(e.isCancelled());

		e = new EnchantItemEvent(player, null, null, null, 0, enchants, 0);
		when(config.canPerform(ExileRule.ENCHANT)).thenReturn(false);
		dut.onPlayerEnchant(e);
		assertTrue(e.isCancelled());
	}

	@Test
	public void testOnPlayerDamagePlayer() {
		EntityDamageByEntityEvent e = new EntityDamageByEntityEvent(player, mock(Player.class), null, modifiers, modifierFunctions);
		when(config.canPerform(ExileRule.PVP)).thenReturn(true);
		dut.onPlayerDamage(e);
		assertFalse(e.isCancelled());

		e = new EntityDamageByEntityEvent(player, mock(Player.class), null, modifiers, modifierFunctions);
		when(config.canPerform(ExileRule.PVP)).thenReturn(false);
		dut.onPlayerDamage(e);
		assertFalse(e.isCancelled());

		e = new EntityDamageByEntityEvent(player, mock(Player.class), null, modifiers, modifierFunctions);
		when(config.canPerform(ExileRule.PVP)).thenReturn(true);
		when(pearlApi.isPlayerExiled(uid)).thenReturn(true);
		dut.onPlayerDamage(e);
		assertFalse(e.isCancelled());

		e = new EntityDamageByEntityEvent(player, mock(Player.class), null, modifiers, modifierFunctions);
		when(config.canPerform(ExileRule.PVP)).thenReturn(false);
		dut.onPlayerDamage(e);
		assertTrue(e.isCancelled());
	}

	@Test
	public void testOnPlayerDamagePlayerProjectile() {
		Arrow arrow = mock(Arrow.class);

		EntityDamageByEntityEvent e = new EntityDamageByEntityEvent(arrow, mock(Player.class), null, modifiers, modifierFunctions);
		when(config.canPerform(ExileRule.PVP)).thenReturn(true);
		dut.onPlayerDamage(e);
		assertFalse(e.isCancelled());

		e = new EntityDamageByEntityEvent(arrow, mock(Player.class), null, modifiers, modifierFunctions);
		when(config.canPerform(ExileRule.PVP)).thenReturn(false);
		dut.onPlayerDamage(e);
		assertFalse(e.isCancelled());

		e = new EntityDamageByEntityEvent(arrow, mock(Player.class), null, modifiers, modifierFunctions);
		when(config.canPerform(ExileRule.PVP)).thenReturn(true);
		when(pearlApi.isPlayerExiled(uid)).thenReturn(true);
		dut.onPlayerDamage(e);
		assertFalse(e.isCancelled());

		e = new EntityDamageByEntityEvent(arrow, mock(Player.class), null, modifiers, modifierFunctions);
		when(config.canPerform(ExileRule.PVP)).thenReturn(false);
		dut.onPlayerDamage(e);
		assertFalse(e.isCancelled());

		when(arrow.getShooter()).thenReturn(player);
		dut.onPlayerDamage(e);
		assertTrue(e.isCancelled());

		// Snowball
		Snowball snowball = mock(Snowball.class);
		e = new EntityDamageByEntityEvent(snowball, mock(Player.class), null, modifiers, modifierFunctions);
		when(config.canPerform(ExileRule.PVP)).thenReturn(false);
		dut.onPlayerDamage(e);
		assertFalse(e.isCancelled());

		when(snowball.getShooter()).thenReturn(player);
		dut.onPlayerDamage(e);
		assertTrue(e.isCancelled());

		// Egg
		Egg egg = mock(Egg.class);
		e = new EntityDamageByEntityEvent(egg, mock(Player.class), null, modifiers, modifierFunctions);
		when(config.canPerform(ExileRule.PVP)).thenReturn(false);
		dut.onPlayerDamage(e);
		assertFalse(e.isCancelled());

		when(egg.getShooter()).thenReturn(player);
		dut.onPlayerDamage(e);
		assertTrue(e.isCancelled());

		// Wolf
		Wolf wolf = mock(Wolf.class);
		e = new EntityDamageByEntityEvent(wolf, mock(Player.class), null, modifiers, modifierFunctions);
		when(config.canPerform(ExileRule.PVP)).thenReturn(false);
		dut.onPlayerDamage(e);
		assertFalse(e.isCancelled());

		when(wolf.getOwner()).thenReturn(player);
		dut.onPlayerDamage(e);
		assertTrue(e.isCancelled());

		// FishHook
		FishHook fishHook = mock(FishHook.class);
		e = new EntityDamageByEntityEvent(fishHook, mock(Player.class), null, modifiers, modifierFunctions);
		when(config.canPerform(ExileRule.PVP)).thenReturn(false);
		dut.onPlayerDamage(e);
		assertFalse(e.isCancelled());

		when(fishHook.getShooter()).thenReturn(player);
		dut.onPlayerDamage(e);
		assertTrue(e.isCancelled());

		// Fireball
		Fireball fireball = mock(Fireball.class);
		e = new EntityDamageByEntityEvent(fireball, mock(Player.class), null, modifiers, modifierFunctions);
		when(config.canPerform(ExileRule.PVP)).thenReturn(false);
		dut.onPlayerDamage(e);
		assertFalse(e.isCancelled());

		when(fireball.getShooter()).thenReturn(player);
		dut.onPlayerDamage(e);
		assertTrue(e.isCancelled());
	}

	@Test
	public void testOnPlayerDamagePet() {
		LivingEntity pet = mock(LivingEntity.class);
		when(pet.getCustomName()).thenReturn("pet");

		EntityDamageByEntityEvent e = new EntityDamageByEntityEvent(player, pet, null, modifiers, modifierFunctions);
		when(config.canPerform(ExileRule.KILL_PETS)).thenReturn(true);
		dut.onPlayerDamage(e);
		assertFalse(e.isCancelled());

		e = new EntityDamageByEntityEvent(player, pet, null, modifiers, modifierFunctions);
		when(config.canPerform(ExileRule.KILL_PETS)).thenReturn(false);
		dut.onPlayerDamage(e);
		assertFalse(e.isCancelled());

		e = new EntityDamageByEntityEvent(player, pet, null, modifiers, modifierFunctions);
		when(config.canPerform(ExileRule.KILL_PETS)).thenReturn(true);
		when(pearlApi.isPlayerExiled(uid)).thenReturn(true);
		dut.onPlayerDamage(e);
		assertFalse(e.isCancelled());

		e = new EntityDamageByEntityEvent(player, pet, null, modifiers, modifierFunctions);
		when(config.canPerform(ExileRule.KILL_PETS)).thenReturn(false);
		dut.onPlayerDamage(e);
		assertTrue(e.isCancelled());
	}

	@Test
	public void testOnPlayerDamageMob() {
		List<String> animals = new ArrayList<String>();
		when(config.getProtectedAnimals()).thenReturn(animals);
		dut.loadConfig(config);

		Pig pig = mock(Pig.class);

		EntityDamageByEntityEvent e = new EntityDamageByEntityEvent(player, pig, null, modifiers, modifierFunctions);
		when(config.canPerform(ExileRule.KILL_MOBS)).thenReturn(true);
		dut.onPlayerDamage(e);
		assertFalse(e.isCancelled());

		e = new EntityDamageByEntityEvent(player, pig, null, modifiers, modifierFunctions);
		when(config.canPerform(ExileRule.KILL_MOBS)).thenReturn(false);
		dut.onPlayerDamage(e);
		assertFalse(e.isCancelled());

		e = new EntityDamageByEntityEvent(player, pig, null, modifiers, modifierFunctions);
		when(config.canPerform(ExileRule.KILL_MOBS)).thenReturn(true);
		when(pearlApi.isPlayerExiled(uid)).thenReturn(true);
		dut.onPlayerDamage(e);
		assertFalse(e.isCancelled());

		e = new EntityDamageByEntityEvent(player, pig, null, modifiers, modifierFunctions);
		when(config.canPerform(ExileRule.KILL_MOBS)).thenReturn(false);
		dut.onPlayerDamage(e);
		assertFalse(e.isCancelled());

		animals.add("Pig");
		dut.loadConfig(config);

		e = new EntityDamageByEntityEvent(player, pig, null, modifiers, modifierFunctions);
		dut.onPlayerDamage(e);
		assertTrue(e.isCancelled());
	}

	@Test
	public void testOnPlayerDrinkPotion() {
		// enforce not a brew here.
		BrewHandler bh = mock(BrewHandler.class);
		when(bh.isBrew(any(ItemStack.class))).thenReturn(false);
		when(pearlApi.getBrewHandler()).thenReturn(bh);

		when(config.canPerform(ExileRule.DRINK_BREWS)).thenReturn(false);

		PlayerItemConsumeEvent e = new PlayerItemConsumeEvent(player, new ItemStack(Material.POTION, 1));
		when(config.canPerform(ExileRule.USE_POTIONS)).thenReturn(true);
		dut.onPlayerDrinkPotion(e);
		assertFalse(e.isCancelled());

		e = new PlayerItemConsumeEvent(player, new ItemStack(Material.POTION, 1));
		when(config.canPerform(ExileRule.USE_POTIONS)).thenReturn(false);
		dut.onPlayerDrinkPotion(e);
		assertFalse(e.isCancelled());

		e = new PlayerItemConsumeEvent(player, new ItemStack(Material.POTION, 1));
		when(config.canPerform(ExileRule.USE_POTIONS)).thenReturn(true);
		when(pearlApi.isPlayerExiled(uid)).thenReturn(true);
		dut.onPlayerDrinkPotion(e);
		assertFalse(e.isCancelled());

		e = new PlayerItemConsumeEvent(player, new ItemStack(Material.POTION, 1));
		when(config.canPerform(ExileRule.USE_POTIONS)).thenReturn(false);
		dut.onPlayerDrinkPotion(e);
		assertTrue(e.isCancelled());

		e = new PlayerItemConsumeEvent(player, new ItemStack(Material.BAKED_POTATO, 1));
		dut.onPlayerDrinkPotion(e);
		assertFalse(e.isCancelled());
	}

	@Test
	public void testOnPlayerDrinkBrew() {
		// enforce is a brew here.
		BrewHandler bh = mock(BrewHandler.class);
		when(bh.isBrew(any(ItemStack.class))).thenReturn(true);
		when(pearlApi.getBrewHandler()).thenReturn(bh);

		when(config.canPerform(ExileRule.USE_POTIONS)).thenReturn(false);

		PlayerItemConsumeEvent e = new PlayerItemConsumeEvent(player, new ItemStack(Material.POTION, 1));
		when(config.canPerform(ExileRule.DRINK_BREWS)).thenReturn(true);
		dut.onPlayerDrinkPotion(e);
		assertFalse(e.isCancelled());

		e = new PlayerItemConsumeEvent(player, new ItemStack(Material.POTION, 1));
		when(config.canPerform(ExileRule.DRINK_BREWS)).thenReturn(false);
		dut.onPlayerDrinkPotion(e);
		assertFalse(e.isCancelled());

		e = new PlayerItemConsumeEvent(player, new ItemStack(Material.POTION, 1));
		when(config.canPerform(ExileRule.DRINK_BREWS)).thenReturn(true);
		when(pearlApi.isPlayerExiled(uid)).thenReturn(true);
		dut.onPlayerDrinkPotion(e);
		assertFalse(e.isCancelled());

		e = new PlayerItemConsumeEvent(player, new ItemStack(Material.POTION, 1));
		when(config.canPerform(ExileRule.DRINK_BREWS)).thenReturn(false);
		dut.onPlayerDrinkPotion(e);
		assertTrue(e.isCancelled());

		e = new PlayerItemConsumeEvent(player, new ItemStack(Material.BAKED_POTATO, 1));
		dut.onPlayerDrinkPotion(e);
		assertFalse(e.isCancelled());
	}

	@Test
	public void testOnPlayerThrowPotion() {
		ThrownPotion thrown = mock(ThrownPotion.class);
		when(thrown.getShooter()).thenReturn(player);

		PotionSplashEvent e = new PotionSplashEvent(thrown, null);
		when(config.canPerform(ExileRule.USE_POTIONS)).thenReturn(true);
		dut.onPlayerThrowPotion(e);
		assertFalse(e.isCancelled());

		e = new PotionSplashEvent(thrown, null);
		when(config.canPerform(ExileRule.USE_POTIONS)).thenReturn(false);
		dut.onPlayerThrowPotion(e);
		assertFalse(e.isCancelled());

		e = new PotionSplashEvent(thrown, null);
		when(config.canPerform(ExileRule.USE_POTIONS)).thenReturn(true);
		when(pearlApi.isPlayerExiled(uid)).thenReturn(true);
		dut.onPlayerThrowPotion(e);
		assertFalse(e.isCancelled());

		e = new PotionSplashEvent(thrown, null);
		when(config.canPerform(ExileRule.USE_POTIONS)).thenReturn(false);
		dut.onPlayerThrowPotion(e);
		assertTrue(e.isCancelled());
	}

	@Test
	public void testOnPlayerThrowLingeringPotion() {
		LingeringPotion thrown = mock(LingeringPotion.class);
		when(thrown.getShooter()).thenReturn(player);

		LingeringPotionSplashEvent e = new LingeringPotionSplashEvent(thrown, null);
		when(config.canPerform(ExileRule.USE_POTIONS)).thenReturn(true);
		dut.onPlayerThrowLingeringPotion(e);
		assertFalse(e.isCancelled());

		e = new LingeringPotionSplashEvent(thrown, null);
		when(config.canPerform(ExileRule.USE_POTIONS)).thenReturn(false);
		dut.onPlayerThrowLingeringPotion(e);
		assertFalse(e.isCancelled());

		e = new LingeringPotionSplashEvent(thrown, null);
		when(config.canPerform(ExileRule.USE_POTIONS)).thenReturn(true);
		when(pearlApi.isPlayerExiled(uid)).thenReturn(true);
		dut.onPlayerThrowLingeringPotion(e);
		assertFalse(e.isCancelled());

		e = new LingeringPotionSplashEvent(thrown, null);
		when(config.canPerform(ExileRule.USE_POTIONS)).thenReturn(false);
		dut.onPlayerThrowLingeringPotion(e);
		assertTrue(e.isCancelled());
	}

	@Test
	public void testOnPlayerIgnite() {
		BlockIgniteEvent e = new BlockIgniteEvent(mock(Block.class), IgniteCause.FLINT_AND_STEEL, player, mock(Block.class));
		when(config.canPerform(ExileRule.IGNITE)).thenReturn(true);
		dut.onPlayerIgnite(e);
		assertFalse(e.isCancelled());

		e = new BlockIgniteEvent(mock(Block.class), IgniteCause.FLINT_AND_STEEL, player, mock(Block.class));
		when(config.canPerform(ExileRule.IGNITE)).thenReturn(false);
		dut.onPlayerIgnite(e);
		assertFalse(e.isCancelled());

		e = new BlockIgniteEvent(mock(Block.class), IgniteCause.FLINT_AND_STEEL, player, mock(Block.class));
		when(config.canPerform(ExileRule.IGNITE)).thenReturn(true);
		when(pearlApi.isPlayerExiled(uid)).thenReturn(true);
		dut.onPlayerIgnite(e);
		assertFalse(e.isCancelled());

		e = new BlockIgniteEvent(mock(Block.class), IgniteCause.FLINT_AND_STEEL, player, mock(Block.class));
		when(config.canPerform(ExileRule.IGNITE)).thenReturn(false);
		dut.onPlayerIgnite(e);
		assertTrue(e.isCancelled());
	}

	@Test
	public void testOnBlockBreak() {
		BlockBreakEvent e = new BlockBreakEvent(mock(Block.class), player);
		when(config.canPerform(ExileRule.MINE)).thenReturn(true);
		dut.onBlockBreak(e);
		assertFalse(e.isCancelled());

		e = new BlockBreakEvent(mock(Block.class), player);
		when(config.canPerform(ExileRule.MINE)).thenReturn(false);
		dut.onBlockBreak(e);
		assertFalse(e.isCancelled());

		e = new BlockBreakEvent(mock(Block.class), player);
		when(config.canPerform(ExileRule.MINE)).thenReturn(true);
		when(pearlApi.isPlayerExiled(uid)).thenReturn(true);
		dut.onBlockBreak(e);
		assertFalse(e.isCancelled());

		e = new BlockBreakEvent(mock(Block.class), player);
		when(config.canPerform(ExileRule.MINE)).thenReturn(false);
		dut.onBlockBreak(e);
		assertTrue(e.isCancelled());
	}

	@Test
	public void testOnCollectXp() {
		final int xpAmount = 1;
		PlayerExpChangeEvent e = new PlayerExpChangeEvent(player, xpAmount);
		when(config.canPerform(ExileRule.COLLECT_XP)).thenReturn(true);
		dut.onCollectXp(e);
		assertEquals(e.getAmount(), xpAmount);

		e = new PlayerExpChangeEvent(player, 1);
		when(config.canPerform(ExileRule.COLLECT_XP)).thenReturn(false);
		dut.onCollectXp(e);
		assertEquals(e.getAmount(), xpAmount);

		e = new PlayerExpChangeEvent(player, 1);
		when(config.canPerform(ExileRule.COLLECT_XP)).thenReturn(true);
		when(pearlApi.isPlayerExiled(uid)).thenReturn(true);
		dut.onCollectXp(e);
		assertEquals(e.getAmount(), xpAmount);

		e = new PlayerExpChangeEvent(player, 1);
		when(config.canPerform(ExileRule.COLLECT_XP)).thenReturn(false);
		dut.onCollectXp(e);
		assertEquals(e.getAmount(), 0);
	}

	@Test
	public void testOnPlaceTnt() {
		Block block = mock(Block.class);
		when (block.getType()).thenReturn(Material.TNT);

		BlockPlaceEvent e = new BlockPlaceEvent(block, null, null, null, player, true, null);
		when(config.canPerform(ExileRule.PLACE_TNT)).thenReturn(true);
		dut.onBlockPlaced(e);
		assertFalse(e.isCancelled());

		e = new BlockPlaceEvent(block, null, null, null, player, true, null);
		when(config.canPerform(ExileRule.PLACE_TNT)).thenReturn(false);
		dut.onBlockPlaced(e);
		assertFalse(e.isCancelled());

		e = new BlockPlaceEvent(block, null, null, null, player, true, null);
		when(config.canPerform(ExileRule.PLACE_TNT)).thenReturn(true);
		when(pearlApi.isPlayerExiled(uid)).thenReturn(true);
		dut.onBlockPlaced(e);
		assertFalse(e.isCancelled());

		e = new BlockPlaceEvent(block, null, null, null, player, true, null);
		when(config.canPerform(ExileRule.PLACE_TNT)).thenReturn(false);
		dut.onBlockPlaced(e);
		assertTrue(e.isCancelled());

		when (block.getType()).thenReturn(Material.DIRT);

		e = new BlockPlaceEvent(block, null, null, null, player, true, null);
		dut.onBlockPlaced(e);
		assertFalse(e.isCancelled());
	}

	@Test
	public void testOnInventoryMoveTnt() {
		when(player.getGameMode()).thenReturn(GameMode.SURVIVAL);
		ItemStack item = new ItemStack(Material.TNT, 1);


		Inventory top = mock(Inventory.class);
		when(top.getHolder()).thenReturn(mock(Dropper.class));
		when(top.getSize()).thenReturn(9);

		Inventory bottom = mock(Inventory.class);
		when(bottom.getHolder()).thenReturn(mock(Player.class));
		when(bottom.getSize()).thenReturn(45);

		InventoryView view = mock(InventoryView.class);
		when(view.getPlayer()).thenReturn(player);
		when(view.getTopInventory()).thenReturn(top);
		when(view.getBottomInventory()).thenReturn(bottom);
		when(view.getCursor()).thenReturn(item);
		when(view.getItem(eq(5))).thenReturn(item);
		when(view.getItem(eq(40))).thenReturn(item);

		InventoryClickEvent e = new InventoryClickEvent(view, null, 5, ClickType.LEFT, InventoryAction.PLACE_SOME);
		when(config.canPerform(ExileRule.PLACE_TNT)).thenReturn(true);
		dut.onInventoryClick(e);
		assertFalse(e.isCancelled());

		e = new InventoryClickEvent(view, null, 5, ClickType.LEFT, InventoryAction.PLACE_SOME);
		when(config.canPerform(ExileRule.PLACE_TNT)).thenReturn(false);
		dut.onInventoryClick(e);
		assertFalse(e.isCancelled());

		e = new InventoryClickEvent(view, null, 5, ClickType.LEFT, InventoryAction.PLACE_SOME);
		when(config.canPerform(ExileRule.PLACE_TNT)).thenReturn(true);
		when(pearlApi.isPlayerExiled(uid)).thenReturn(true);
		dut.onInventoryClick(e);
		assertFalse(e.isCancelled());

		e = new InventoryClickEvent(view, null, 5, ClickType.LEFT, InventoryAction.PLACE_SOME);
		when(config.canPerform(ExileRule.PLACE_TNT)).thenReturn(false);
		dut.onInventoryClick(e);
		assertTrue(e.isCancelled());

		e = new InventoryClickEvent(view, null, 5, ClickType.LEFT, InventoryAction.PLACE_SOME);
		when(top.getHolder()).thenReturn(mock(Chest.class));
		dut.onInventoryClick(e);
		assertFalse(e.isCancelled());

		e = new InventoryClickEvent(view, null, 5, ClickType.LEFT, InventoryAction.PLACE_ALL);
		when(top.getHolder()).thenReturn(mock(Hopper.class));
		dut.onInventoryClick(e);
		assertTrue(e.isCancelled());

		e = new InventoryClickEvent(view, null, 5, ClickType.LEFT, InventoryAction.PLACE_ONE);
		when(top.getHolder()).thenReturn(mock(Dispenser.class));
		dut.onInventoryClick(e);
		assertTrue(e.isCancelled());

		e = new InventoryClickEvent(view, null, 5, ClickType.SHIFT_LEFT, InventoryAction.MOVE_TO_OTHER_INVENTORY);
		dut.onInventoryClick(e);
		assertFalse(e.isCancelled());

		e = new InventoryClickEvent(view, null, 40, ClickType.LEFT, InventoryAction.PLACE_SOME);
		dut.onInventoryClick(e);
		assertFalse(e.isCancelled());

		e = new InventoryClickEvent(view, null, 40, ClickType.SHIFT_LEFT, InventoryAction.MOVE_TO_OTHER_INVENTORY);
		when(view.getType()).thenReturn(InventoryType.DISPENSER);
		assertEquals(e.getCurrentItem(), item);
		dut.onInventoryClick(e);
		assertTrue(e.isCancelled());

	}
}
