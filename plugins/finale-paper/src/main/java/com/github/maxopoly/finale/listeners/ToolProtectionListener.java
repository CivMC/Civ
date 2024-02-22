package com.github.maxopoly.finale.listeners;

import com.github.maxopoly.finale.external.FinaleSettingManager;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

public class ToolProtectionListener implements Listener {
	
	private FinaleSettingManager settingMan;
	
	public ToolProtectionListener(FinaleSettingManager settingMan) {
		this.settingMan = settingMan;
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent e) {
		if (!settingMan.useToolProtection(e.getPlayer().getUniqueId())) {
			return;
		}
		ItemStack is = e.getPlayer().getInventory().getItemInMainHand();
		if (is.getEnchantments().isEmpty()) {
			return;
		}
		Damageable meta = ItemUtils.getDamageable(is);
		if (meta == null) {
			return;
		}
		int health = is.getType().getMaxDurability() - meta.getDamage();
		if (health <= settingMan.getToolProtectionThreshhold(e.getPlayer().getUniqueId())) {
			for(int i = 0; i < 5; i++) {
				e.getPlayer().sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Your tool is almost broken");
			}
			e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 10, 4));
		}
	}

}
