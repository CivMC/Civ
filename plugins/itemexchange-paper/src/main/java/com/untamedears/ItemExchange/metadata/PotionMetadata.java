package com.untamedears.ItemExchange.metadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.untamedears.ItemExchange.utility.ExchangeRule;

public class PotionMetadata implements AdditionalMetadata {
	private static final String[] numerals = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
	
	private List<PotionEffect> effects;
	
	public PotionMetadata(PotionMeta meta) {
		effects.addAll(meta.getCustomEffects());
	}
	
	private PotionMetadata() {
		
	}
	
	@Override
	public String serialize() {
		StringBuilder serialized = new StringBuilder();
		
		Iterator<PotionEffect> iterator = effects.iterator();
		
		while(iterator.hasNext()) {
			PotionEffect effect = iterator.next();
			
			serialized.append(effect.getType().getName()).append(ExchangeRule.tertiarySpacer).append(effect.getAmplifier()).append(ExchangeRule.tertiarySpacer).append(effect.getDuration()).append(ExchangeRule.tertiarySpacer).append(effect.isAmbient());
			
			if(iterator.hasNext())
				serialized.append(ExchangeRule.secondarySpacer);
		}
		
		return serialized.toString();
	}

	@Override
	public boolean matches(ItemStack item) {
		if(item.hasItemMeta()) {
			ItemMeta meta = item.getItemMeta();
			
			if(meta instanceof PotionMeta) {
				PotionMeta potionMeta = (PotionMeta) meta;
				
				return potionMeta.getCustomEffects().equals(effects);
			}
		}
		
		return false;
	}

	@Override
	public String getDisplayedInfo() {
		StringBuilder info = new StringBuilder();
		
		info.append(ChatColor.DARK_AQUA).append("Potion effects: ");
		
		if(effects.size() > 0) {
			Iterator<PotionEffect> iterator = effects.iterator();
			
			while(iterator.hasNext()) {
				PotionEffect effect = iterator.next();
				
				String name = effect.getType().getName().toLowerCase().replaceAll("_", " ");
				
				info.append(name).append(" ");
				
				int level = effect.getAmplifier();
				
				if(level - 1 < numerals.length)
					info.append(numerals[level - 1]);
				else
					info.append(level);
				
				info.append(" - ");
				
				info.append(effect.getDuration() / 20 + "s");
				
				if(iterator.hasNext())
					info.append('\n');
			}
		}
		else {
			info.append(ChatColor.AQUA).append("<none>");
		}
		
		return info.toString();
	}
	
	public static PotionMetadata deserialize(String s) {
		PotionMetadata metadata = new PotionMetadata();
		
		metadata.effects = new ArrayList<PotionEffect>();
		
		String[] effects = s.split(ExchangeRule.secondarySpacer);
		
		for(String effect : effects) {
			String[] parts = effect.split(ExchangeRule.tertiarySpacer);
			
			PotionEffectType type = PotionEffectType.getByName(parts[0]);
			int amplifier = Integer.parseInt(parts[1]);
			int duration = Integer.parseInt(parts[2]);
			boolean ambient = Boolean.parseBoolean(parts[3]);

			metadata.effects.add(new PotionEffect(type, duration, amplifier, ambient));
		}

		return metadata;
	}
}
