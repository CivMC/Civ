package net.minelink.ctplus;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class NpcManager {

    private final CombatTagPlus plugin;

    private final Map<UUID, Npc> spawnedNpcs = new HashMap<>();

    private static boolean doHasParticles = false;

    static {
        try {
            PotionEffect.class.getMethod("hasParticles");
            doHasParticles = true;
        } catch (NoSuchMethodException ignored) {

        }
    }

    public NpcManager(CombatTagPlus plugin) {
        this.plugin = plugin;
    }

    public Npc spawn(Player player) {
        // Do nothing if player already has a NPC
        Npc npc = getSpawnedNpc(player.getUniqueId());
        if (npc != null) return null;

        // Spawn fake player entity
        npc = new Npc(plugin.getNpcPlayerHelper(), plugin.getNpcPlayerHelper().spawn(player));
        spawnedNpcs.put(player.getUniqueId(), npc);

        Player entity = npc.getEntity();

        entity.setCanPickupItems(false);

        // Copy player data to fake player
        entity.setGameMode(player.getGameMode());
        entity.setHealth(player.getHealth());
        entity.setHealthScale(player.getHealthScale());
        entity.setTotalExperience(player.getTotalExperience());
        entity.setFoodLevel(player.getFoodLevel());
        entity.setExhaustion(player.getExhaustion());
        entity.setSaturation(player.getSaturation());
        entity.setFireTicks(player.getFireTicks());

        copyInventory(player, entity);
        copyPotionEffects(player, entity);

        // Play a nice little effect indicating the NPC was spawned
        if (plugin.getSettings().playEffect()) {
            Location l = entity.getLocation();
            l.getWorld().playEffect(l, Effect.MOBSPAWNER_FLAMES, 0, 64);
            l.getWorld().playSound(l, Sound.EXPLODE, 0.9F, 0);
        }

        return npc;
    }

    private void copyInventory(Player from, Player to) {
        ItemStack[] contents = from.getInventory().getContents();
        ItemStack[] armorContents = from.getInventory().getArmorContents();

        // Clone inventory contents
        for (int i = 0; i < contents.length; ++i) {
            ItemStack item = contents[i];
            if (item != null) {
                contents[i] = item.clone();
            }
        }

        // Clone player equipment
        for (int i = 0; i < armorContents.length; ++i) {
            ItemStack item = armorContents[i];
            if (item != null) {
                armorContents[i] = item.clone();
            }
        }

        to.getInventory().setContents(contents);
        to.getInventory().setArmorContents(armorContents);

        // Send equipment packets to nearby players
        if (plugin.getNpcPlayerHelper().isNpc(to)) {
            plugin.getNpcPlayerHelper().updateEquipment(to);
        }
    }

    private void copyPotionEffects(Player from, Player to) {
        for (PotionEffect e : from.getActivePotionEffects()) {
            PotionEffect effect;

            // 1.8+ vs 1.7
            if (doHasParticles) {
                effect = new PotionEffect(e.getType(), e.getDuration(), e.getAmplifier(), e.isAmbient(), e.hasParticles());
            } else {
                effect = new PotionEffect(e.getType(), e.getDuration(), e.getAmplifier(), e.isAmbient());
            }

            to.addPotionEffect(effect);
        }
    }

    public void despawn(Npc npc) {
        // Do nothing if NPC isn't spawned or if it's a different NPC
        Npc other = getSpawnedNpc(npc.getIdentity().getId());
        if (other == null || other != npc) return;

        // Call NPC despawn event
        NpcDespawnEvent event = new NpcDespawnEvent(npc);
        Bukkit.getPluginManager().callEvent(event);

        // Remove the NPC entity from the world
        plugin.getNpcPlayerHelper().despawn(npc.getEntity());
        spawnedNpcs.remove(npc.getIdentity().getId());
    }

    public Npc getSpawnedNpc(UUID playerId) {
        return spawnedNpcs.get(playerId);
    }

    public boolean npcExists(UUID playerId) {
        return spawnedNpcs.containsKey(playerId);
    }

}
