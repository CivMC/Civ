package vg.civcraft.mc.civmodcore.api;

import io.protonull.utilities.Equals;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public final class SpawnEggAPI {

	private static final Map<Material, EntityType> spawnEggs = new HashMap<Material, EntityType>() {{
		put(Material.BAT_SPAWN_EGG, EntityType.BAT);
		put(Material.BLAZE_SPAWN_EGG, EntityType.BLAZE);
		put(Material.CAT_SPAWN_EGG, EntityType.CAT);
		put(Material.CAVE_SPIDER_SPAWN_EGG, EntityType.CAVE_SPIDER);
		put(Material.CHICKEN_SPAWN_EGG, EntityType.CHICKEN);
		put(Material.COD_SPAWN_EGG, EntityType.COD);
		put(Material.COW_SPAWN_EGG, EntityType.COW);
		put(Material.CREEPER_SPAWN_EGG, EntityType.CREEPER);
		put(Material.DOLPHIN_SPAWN_EGG, EntityType.DOLPHIN);
		put(Material.DONKEY_SPAWN_EGG, EntityType.DONKEY);
		put(Material.DROWNED_SPAWN_EGG, EntityType.DROWNED);
		put(Material.ELDER_GUARDIAN_SPAWN_EGG, EntityType.ELDER_GUARDIAN);
		put(Material.ENDERMAN_SPAWN_EGG, EntityType.ENDERMAN);
		put(Material.ENDERMITE_SPAWN_EGG, EntityType.ENDERMITE);
		put(Material.EVOKER_SPAWN_EGG, EntityType.EVOKER);
		put(Material.FOX_SPAWN_EGG, EntityType.FOX);
		put(Material.GHAST_SPAWN_EGG, EntityType.GHAST);
		put(Material.GUARDIAN_SPAWN_EGG, EntityType.GUARDIAN);
		put(Material.HORSE_SPAWN_EGG, EntityType.HORSE);
		put(Material.HUSK_SPAWN_EGG, EntityType.HUSK);
		put(Material.LLAMA_SPAWN_EGG, EntityType.LLAMA);
		put(Material.MAGMA_CUBE_SPAWN_EGG, EntityType.MAGMA_CUBE);
		put(Material.MOOSHROOM_SPAWN_EGG, EntityType.MUSHROOM_COW);
		put(Material.MULE_SPAWN_EGG, EntityType.MULE);
		put(Material.OCELOT_SPAWN_EGG, EntityType.OCELOT);
		put(Material.PANDA_SPAWN_EGG, EntityType.PANDA);
		put(Material.PARROT_SPAWN_EGG, EntityType.PARROT);
		put(Material.PHANTOM_SPAWN_EGG, EntityType.PHANTOM);
		put(Material.PIG_SPAWN_EGG, EntityType.PIG);
		put(Material.PILLAGER_SPAWN_EGG, EntityType.PILLAGER);
		put(Material.POLAR_BEAR_SPAWN_EGG, EntityType.POLAR_BEAR);
		put(Material.PUFFERFISH_SPAWN_EGG, EntityType.PUFFERFISH);
		put(Material.RABBIT_SPAWN_EGG, EntityType.RABBIT);
		put(Material.RAVAGER_SPAWN_EGG, EntityType.RAVAGER);
		put(Material.SALMON_SPAWN_EGG, EntityType.SALMON);
		put(Material.SHEEP_SPAWN_EGG, EntityType.SHEEP);
		put(Material.SHULKER_SPAWN_EGG, EntityType.SHULKER);
		put(Material.SILVERFISH_SPAWN_EGG, EntityType.SILVERFISH);
		put(Material.SKELETON_HORSE_SPAWN_EGG, EntityType.SKELETON_HORSE);
		put(Material.SKELETON_SPAWN_EGG, EntityType.SKELETON);
		put(Material.SLIME_SPAWN_EGG, EntityType.SLIME);
		put(Material.SPIDER_SPAWN_EGG, EntityType.SPIDER);
		put(Material.SQUID_SPAWN_EGG, EntityType.SQUID);
		put(Material.STRAY_SPAWN_EGG, EntityType.STRAY);
		put(Material.TRADER_LLAMA_SPAWN_EGG, EntityType.TRADER_LLAMA);
		put(Material.TROPICAL_FISH_SPAWN_EGG, EntityType.TROPICAL_FISH);
		put(Material.TURTLE_SPAWN_EGG, EntityType.TURTLE);
		put(Material.VEX_SPAWN_EGG, EntityType.VEX);
		put(Material.VILLAGER_SPAWN_EGG, EntityType.VILLAGER);
		put(Material.VINDICATOR_SPAWN_EGG, EntityType.VINDICATOR);
		put(Material.WANDERING_TRADER_SPAWN_EGG, EntityType.WANDERING_TRADER);
		put(Material.WITCH_SPAWN_EGG, EntityType.WITCH);
		put(Material.WITHER_SKELETON_SPAWN_EGG, EntityType.WITHER_SKELETON);
		put(Material.WOLF_SPAWN_EGG, EntityType.WOLF);
		put(Material.ZOMBIE_HORSE_SPAWN_EGG, EntityType.ZOMBIE_HORSE);
		put(Material.ZOMBIE_PIGMAN_SPAWN_EGG, EntityType.PIG_ZOMBIE);
		put(Material.ZOMBIE_SPAWN_EGG, EntityType.ZOMBIE);
		put(Material.ZOMBIE_VILLAGER_SPAWN_EGG, EntityType.ZOMBIE_VILLAGER);
	}};

	public static boolean isSpawnEgg(Material material) {
		if (material == null) {
			return false;
		}
		return spawnEggs.containsKey(material);
	}

	public static EntityType getEntityType(Material material) {
		if (material == null) {
			return null;
		}
		return spawnEggs.get(material);
	}

	public static Material getSpawnEgg(EntityType entityType) {
		if (entityType == null) {
			return null;
		}
		for (Map.Entry<Material, EntityType> entry : spawnEggs.entrySet()) {
			if (Equals.notNull(entityType, entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

}
