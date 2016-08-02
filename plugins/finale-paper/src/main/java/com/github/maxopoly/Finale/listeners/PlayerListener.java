package com.github.maxopoly.finale.listeners;

import net.minecraft.server.v1_10_R1.NBTTagCompound;
import net.minecraft.server.v1_10_R1.NBTTagInt;
import net.minecraft.server.v1_10_R1.NBTTagList;
import net.minecraft.server.v1_10_R1.NBTTagString;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.TippedArrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import com.github.maxopoly.finale.FinaleManager;

public class PlayerListener implements Listener {

	private FinaleManager manager;

	public PlayerListener(FinaleManager manager) {
		this.manager = manager;
	}

	@EventHandler
	public void playerLogin(PlayerJoinEvent e) {
		if (manager.isAttackSpeedEnabled()) {;
			// Set attack speed
			AttributeInstance attr = e.getPlayer().getAttribute(
				Attribute.GENERIC_ATTACK_SPEED);
			if (attr != null) {
				attr.setBaseValue(manager.getAttackSpeed());
			}
		}
		if (manager.isRegenHandlerEnabled()) {
			// Register login for custom health regen
			manager.getPassiveRegenHandler().registerPlayer(
					e.getPlayer().getUniqueId());
		}
	}

	@EventHandler
	public void healthRegen(EntityRegainHealthEvent e) {
		if (!manager.isRegenHandlerEnabled()) return;
		if (e.getEntityType() != EntityType.PLAYER) {
			return;
		}
		if (e.getRegainReason() == RegainReason.SATIATED
				&& manager.getPassiveRegenHandler().blockPassiveHealthRegen()) {
			e.setCancelled(true);
			return;
		}
		if (e.getRegainReason() == RegainReason.EATING && manager.getPassiveRegenHandler().blockFoodHealthRegen()) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void inventoryClick(InventoryClickEvent e) {
	    ItemStack is = e.getCurrentItem();
	    if (is == null) {
		return;
	    }
	    Integer adjustedDamage = manager.getAdjustAttackDamage(is.getType());
	    if (adjustedDamage == null) {
		return;
	    }
	    net.minecraft.server.v1_10_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(is);
	    NBTTagCompound compound = (nmsStack.hasTag()) ? nmsStack.getTag() : new NBTTagCompound();
	    NBTTagList modifiers = new NBTTagList();
	    NBTTagCompound damage = new NBTTagCompound();
	    damage.set("AttributeName", new NBTTagString("generic.attackDamage"));
	    damage.set("Name", new NBTTagString("generic.attackDamage"));
	    damage.set("Amount", new NBTTagInt(adjustedDamage));
	    damage.set("Slot", new NBTTagString("mainhand"));
	    modifiers.add(damage);
	    compound.set("AttributeModifiers", modifiers);
	    nmsStack.setTag(compound);
	    ItemStack result = CraftItemStack.asBukkitCopy(nmsStack);
	    e.setCurrentItem(result);
	}
	
	//@EventHandler
	public void arrowHit(EntityDamageByEntityEvent e) {
		if (!(e.getEntity() instanceof LivingEntity)) {
			return;
		}
		if (e.getDamager().getType() == EntityType.TIPPED_ARROW) {
			return;
		}
	}

}
