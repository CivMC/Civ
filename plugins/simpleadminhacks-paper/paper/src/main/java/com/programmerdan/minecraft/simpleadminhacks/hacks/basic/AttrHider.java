package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.destroystokyo.paper.MaterialTags;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import com.programmerdan.minecraft.simpleadminhacks.framework.utilities.PacketManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;

public final class AttrHider extends BasicHack {

	public static final String BYPASS_PERMISSION = "attrhider.bypass";

	private final PacketManager packets = new PacketManager();

	@AutoLoad
	private boolean hideItemMeta;

	@AutoLoad
	private boolean hideEffects;

	@AutoLoad
	private boolean hideHealth;

	public AttrHider(final SimpleAdminHacks plugin, final BasicHackConfig config) {
		super(plugin, config);
	}

	@Override
	public void onEnable() {
		super.onEnable();
		if (!this.hideItemMeta) {
			this.packets.addAdapter(new PacketAdapter(this.plugin, PacketType.Play.Server.ENTITY_EQUIPMENT) {
				@Override
				public void onPacketSending(final PacketEvent event) {
					final PacketContainer packet = event.getPacket();
					if (event.getPlayer().hasPermission(BYPASS_PERMISSION)) {
						return;
					}
					final var slots = packet.getSlotStackPairLists();
					final var slotItemPairs = slots.read(0);
					for (final Pair<EnumWrappers.ItemSlot, ItemStack> slotItemPair : slotItemPairs) {
						final ItemStack item = slotItemPair.getSecond();
						if (item == null || !shouldBeObfuscated(item.getType())) {
							continue;
						}
						final ItemMeta meta = item.getItemMeta();
						final ItemStack fakeItem = item.clone();
						if (meta != null) {
							final ItemMeta fakeMeta = fakeItem.getItemMeta();
							if (meta instanceof LeatherArmorMeta) {
								final LeatherArmorMeta baseLeatherMeta = (LeatherArmorMeta) meta;
								final LeatherArmorMeta fakeLeatherMeta = (LeatherArmorMeta) fakeMeta;
								fakeLeatherMeta.setColor(baseLeatherMeta.getColor());
							}
							if (meta instanceof PotionMeta) {
								final PotionMeta basePotionMeta = (PotionMeta) meta;
								final PotionMeta fakePotionMeta = (PotionMeta) fakeMeta;
								final PotionData basePotion = basePotionMeta.getBasePotionData();
								final PotionData fakePotion = new PotionData(basePotion.getType());
								fakePotionMeta.setBasePotionData(fakePotion);
							}
							if (meta instanceof Damageable) {
								final Damageable fakeDamageable = (Damageable) fakeMeta;
								fakeDamageable.setDamage(0);
							}
							if (meta.hasEnchants()) {
								fakeMeta.addEnchant(Enchantment.DURABILITY, 1, true);
							}
							fakeItem.setItemMeta(fakeMeta);
							slotItemPair.setSecond(fakeItem); // Set item
						}
					}
					slots.write(0, slotItemPairs);
				}
			});
		}
		if (this.hideEffects) {
			this.packets.addAdapter(new PacketAdapter(this.plugin, PacketType.Play.Server.ENTITY_EFFECT) {
				@Override
				public void onPacketSending(final PacketEvent event) {
					final PacketContainer packet = event.getPacket();
					final Player player = event.getPlayer();
					if (player.hasPermission(BYPASS_PERMISSION)) {
						return;
					}
					final PacketContainer cloned = packet.deepClone();
					final StructureModifier<Integer> ints = cloned.getIntegers();
					if (player.getEntityId() == ints.read(0)) {
						return;
					}
					// set amplifier to 0
					cloned.getBytes().write(1, (byte) 0);
					// set duration to 0
					ints.write(1, 0);
					// The packet data is shared between events, but the event
					// instance is exclusive to THIS sending of the packet
					event.setPacket(cloned);
				}
			});
		}
		if (this.hideHealth) {
			this.packets.addAdapter(new PacketAdapter(this.plugin, PacketType.Play.Server.ENTITY_METADATA) {
				@Override
				public void onPacketSending(final PacketEvent event) {
					final PacketContainer packet = event.getPacket();
					final Player player = event.getPlayer();
					if (player.hasPermission(BYPASS_PERMISSION)) {
						return;
					}
					final Entity entity = packet.getEntityModifier(event).read(0);
					if (!(entity instanceof LivingEntity)
							|| player.getEntityId() == entity.getEntityId()
							|| entity.getPassengers().contains(player)) {
						return;
					}
					final PacketContainer cloned = packet.deepClone();
					for (final WrappedWatchableObject object : cloned.getWatchableCollectionModifier().read(0)) {
						// Read the 8th field as a float as that's the living entity's health
						// https://wiki.vg/Entity_metadata#Living_Entity
						if (object.getIndex() == 9) {
							if ((float) object.getValue() > 0) {
								object.setValue(1f); // Half a heart
							}
						}
					}
					// The packet data is shared between events, but the event
					// instance is exclusive to THIS sending of the packet
					event.setPacket(cloned);
				}
			});
		}
	}

	@Override
	public void onDisable() {
		this.packets.removeAllAdapters();
		super.onDisable();
	}

	private static boolean shouldBeObfuscated(final Material material) {
		return MaterialTags.HELMETS.isTagged(material)
				|| MaterialTags.CHEST_EQUIPPABLE.isTagged(material)
				|| MaterialTags.LEGGINGS.isTagged(material)
				|| MaterialTags.BOOTS.isTagged(material)
				|| MaterialTags.SWORDS.isTagged(material)
				|| MaterialTags.AXES.isTagged(material)
				|| MaterialTags.PICKAXES.isTagged(material)
				|| MaterialTags.SHOVELS.isTagged(material)
				|| MaterialTags.HOES.isTagged(material)
				|| material == Material.FIREWORK_ROCKET
				|| material == Material.WRITTEN_BOOK
				|| material == Material.ENCHANTED_BOOK
				|| material == Material.POTION
				|| material == Material.LINGERING_POTION
				|| material == Material.SPLASH_POTION;
	}
	
}
