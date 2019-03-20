package com.github.maxopoly.finale.combat;

import org.bukkit.World;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import com.github.maxopoly.finale.Finale;

import net.md_5.bungee.api.ChatColor;

public class AsyncPacketHandler extends PacketAdapter {
	
	private double maxReach;
	private int cpsLimit;
	
	public AsyncPacketHandler() {
		super(Finale.getPlugin(), ListenerPriority.HIGH, PacketType.Play.Client.USE_ENTITY);
		
		CombatConfig cc = Finale.getPlugin().getManager().getCombatConfig();
		maxReach = cc.getMaxReach();
		cpsLimit = cc.getCPSLimit();
	}
	
	@Override
	public void onPacketReceiving(PacketEvent event) {
		PacketType packetType = event.getPacketType();
		
		if (packetType != PacketType.Play.Client.USE_ENTITY) return;
		
		event.setCancelled(true);
		
		Player attacker = event.getPlayer();
		World world = attacker.getWorld();
		
		CPSHandler cpsHandler = Finale.getPlugin().getManager().getCPSHandler();
		cpsHandler.updateClicks(attacker);
		
		PacketContainer packet = event.getPacket();
		Entity entity = packet.getEntityModifier(event).read(0);
		Damageable target = entity instanceof Damageable ? (Damageable)entity : null;
		
		if (target == null) return;
		if (target.isDead()) return;
		if (target.isInvulnerable()) return;
		if (!world.getUID().equals(target.getWorld().getUID())) return;
		if (!(target instanceof LivingEntity)) return;
		
		LivingEntity entityTarget = (LivingEntity) target;
		
		StructureModifier<EntityUseAction> actions = packet.getEntityUseActions();
		EntityUseAction action = actions.read(0);
		
		if (action != EntityUseAction.ATTACK) return;
		
		double distanceSquared = attacker.getLocation().distanceSquared(target.getLocation());
		
		if (distanceSquared > (maxReach * maxReach)) return;
		
		if (cpsHandler.getCPS(attacker.getUniqueId()) >= cpsLimit) {
			attacker.sendMessage(ChatColor.RED + "You've hit CPS limit of " + cpsLimit + "!");
			return;
		}
		
		Hit hit = new Hit(attacker, entityTarget);
		Finale.getPlugin().getManager().getCombatRunnable().getHitQueue().add(hit);
	}
}
