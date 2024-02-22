package com.github.maxopoly.finale.combat;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import com.comphenix.protocol.wrappers.EnumWrappers.Hand;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerDigType;
import com.comphenix.protocol.wrappers.WrappedEnumEntityUseAction;
import com.github.maxopoly.finale.Finale;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class AsyncPacketHandler extends PacketAdapter implements Listener {

	private CombatConfig cc;
	
	public AsyncPacketHandler(CombatConfig cc) {
		super(Finale.getPlugin(), ListenerPriority.HIGH, PacketType.Play.Client.USE_ENTITY, PacketType.Play.Client.ARM_ANIMATION, PacketType.Play.Client.BLOCK_DIG);

		this.cc = cc;
		
		Bukkit.getPluginManager().registerEvents(this, Finale.getPlugin());
	}
	
	private Set<UUID> isDigging = Sets.newConcurrentHashSet();
	private Map<UUID, Long> lastRemovals = new ConcurrentHashMap<>();
	private Map<UUID, Long> lastStartBreaks = new ConcurrentHashMap<>();
	
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
					Entity entity = packet.getEntityModifier(event).read(0);
					Damageable target = entity instanceof Damageable ? (Damageable) entity : null;

					if (target == null || target.isDead() || target.isInvulnerable() ||
							!world.getUID().equals(target.getWorld().getUID()) || !(target instanceof LivingEntity)) {
						if (entity instanceof CraftEntity craftEntity){
							craftEntity.getHandle().hurt(DamageSource.playerAttack(((CraftPlayer) attacker).getHandle()), (float) ((CraftPlayer) attacker).getHandle().getAttribute(Attributes.ATTACK_DAMAGE).getValue());
						}
						return;
					}

					LivingEntity entityTarget = (LivingEntity) target;

					double distanceSquared = attacker.getLocation().distanceSquared(target.getLocation());

					if (distanceSquared > (cc.getMaxReach() * cc.getMaxReach())) {
						return;
					}

					if (cpsHandler.getCPS(attacker.getUniqueId()) >= cc.getCPSLimit()) {
						attacker.sendMessage(ChatColor.RED + "You've hit CPS limit of " + cc.getCPSLimit() + "!");
						return;
					}

					CombatUtil.attack(attacker, ((CraftLivingEntity) entityTarget).getHandle());
				}
			}.runTask(Finale.getPlugin());
		} else if (packetType == PacketType.Play.Client.ARM_ANIMATION) {
			Player attacker = event.getPlayer();
			PacketContainer packet = event.getPacket();
			Hand hand = packet.getHands().getValues().get(0);
			if (hand == Hand.MAIN_HAND && !isDigging.contains(attacker.getUniqueId())) {
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
		
		isDigging.remove(player.getUniqueId());
		lastRemovals.remove(player.getUniqueId());
		lastStartBreaks.remove(player.getUniqueId());
	}
}
