package com.github.maxopoly.finale.combat;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import com.comphenix.protocol.wrappers.EnumWrappers.Hand;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerDigType;
import com.comphenix.protocol.wrappers.WrappedEnumEntityUseAction;
import com.github.maxopoly.finale.Finale;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.core.BlockPos;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftLivingEntity;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class AsyncPacketHandler extends PacketAdapter implements Listener {

	private CombatConfig cc;
	
	public AsyncPacketHandler(CombatConfig cc) {
		super(Finale.getPlugin(), ListenerPriority.HIGH, PacketType.Play.Client.USE_ENTITY, PacketType.Play.Client.ARM_ANIMATION, PacketType.Play.Client.BLOCK_DIG, PacketType.Play.Client.ENTITY_ACTION);

		this.cc = cc;
		
		Bukkit.getPluginManager().registerEvents(this, Finale.getPlugin());
	}

	private Set<UUID> attackedThisTick = Sets.newConcurrentHashSet();
	private Set<UUID> isDigging = Sets.newConcurrentHashSet();
	private Map<UUID, Long> lastRemovals = new HashMap<>();
	private Map<UUID, Long> lastStartBreaks = new HashMap<>();
	
	@Override
	public void onPacketReceiving(PacketEvent event) {
		PacketType packetType = event.getPacketType();

		CPSHandler cpsHandler = Finale.getPlugin().getManager().getCPSHandler();
		if (packetType == PacketType.Play.Client.USE_ENTITY) {
			Player attacker = event.getPlayer();
			World world = attacker.getWorld();

			PacketContainer packet = event.getPacket();
			StructureModifier<WrappedEnumEntityUseAction> actions = packet.getEnumEntityUseActions();
			EntityUseAction action = actions.read(0).getAction();

			if (action != EntityUseAction.ATTACK) {
				return;
			}
			event.setCancelled(true);
			new BukkitRunnable() {

				@Override
				public void run() {
					Entity target = packet.getEntityModifier(event).read(0);
					if (target == null || target.isDead() || target.isInvulnerable() ||
							!world.getUID().equals(target.getWorld().getUID())) {
						return;
					}

					double distanceSquared = attacker.getLocation().distanceSquared(target.getLocation());

					if (distanceSquared > (cc.getMaxReach() * cc.getMaxReach())) {
						return;
					}

					if (cpsHandler.getCPS(attacker.getUniqueId()) >= cc.getCPSLimit()) {
						attacker.sendMessage(ChatColor.RED + "You've hit CPS limit of " + cc.getCPSLimit() + "!");
						return;
					}

					CombatUtil.attack(attacker, ((CraftEntity) target).getHandle());
				}
			}.runTask(Finale.getPlugin());
		} else if (packetType == PacketType.Play.Client.ENTITY_ACTION) {
			Player player = event.getPlayer();
			PacketContainer packet = event.getPacket();
			EnumWrappers.PlayerAction playerAction = packet.getPlayerActions().read(0);
			SprintHandler sprintHandler = Finale.getPlugin().getManager().getSprintHandler();
			if (playerAction == EnumWrappers.PlayerAction.START_SPRINTING) {
				sprintHandler.startSprinting(player);
			} else if (playerAction == EnumWrappers.PlayerAction.STOP_SPRINTING) {
				sprintHandler.stopSprinting(player);
			}
		} else if (packetType == PacketType.Play.Client.ARM_ANIMATION) {
			Player attacker = event.getPlayer();
			PacketContainer packet = event.getPacket();
			Hand hand = packet.getHands().getValues().get(0);
			if (hand == Hand.MAIN_HAND && !isDigging.contains(attacker.getUniqueId())) {
				Block targetBlock = attacker.getTargetBlock(4);
				if (targetBlock != null && targetBlock.getType() != Material.AIR) {
					return;
				}
				cpsHandler.updateClicks(attacker);
			}
		} else if (packetType == PacketType.Play.Client.BLOCK_DIG) {
			Player attacker = event.getPlayer();

			if (attacker.getGameMode() != GameMode.SURVIVAL) {
				return;
			}

			PacketContainer packet = event.getPacket();
			BlockPosition position = packet.getBlockPositionModifier().getValues().get(0);
			PlayerDigType digType = packet.getPlayerDigTypes().getValues().get(0);
			if (digType == PlayerDigType.START_DESTROY_BLOCK) {
				Block block = attacker.getWorld().getBlockAt(position.getX(), position.getY(), position.getZ());
				if (block.getType() == Material.BEDROCK || block.getType() == Material.BARRIER && !isDigging.contains(attacker.getUniqueId())) {
					isDigging.add(attacker.getUniqueId());
					cpsHandler.updateClicks(attacker);
					return;
				}

				float strength = ((CraftWorld) block.getWorld()).getHandle().getBlockState((new BlockPos(position.getX(), position.getY(), position.getZ()))).destroySpeed;

				long lastStartBreak = lastStartBreaks.getOrDefault(attacker.getUniqueId(), 0L);
				long timeSinceBreak = (System.currentTimeMillis() - lastStartBreak);
				lastStartBreaks.put(attacker.getUniqueId(), System.currentTimeMillis());
				if (strength > 0) {
					long lastRemoval = lastRemovals.getOrDefault(attacker.getUniqueId(), 0L);
					long timeSinceRemoval = (System.currentTimeMillis() - lastRemoval);

					if (isDigging.contains(attacker.getUniqueId())) {
						return;
					}
					isDigging.add(attacker.getUniqueId());
					if (timeSinceRemoval >= 48 && timeSinceBreak > 51) {
						cpsHandler.updateClicks(attacker);
					}
				}
			} else if (digType == PlayerDigType.ABORT_DESTROY_BLOCK || digType == PlayerDigType.STOP_DESTROY_BLOCK){
				isDigging.remove(attacker.getUniqueId());
				lastRemovals.put(attacker.getUniqueId(), System.currentTimeMillis());
			}
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		
		if (isDigging.contains(player.getUniqueId())) {
			isDigging.remove(player.getUniqueId());
		}
		
		if (lastRemovals.containsKey(player.getUniqueId())) {
			lastRemovals.remove(player.getUniqueId());
		}
		
		if (lastStartBreaks.containsKey(player.getUniqueId())) {
			lastStartBreaks.remove(player.getUniqueId());
		}
	}
}
