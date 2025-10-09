package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class ArthropodEggHack extends BasicHack {

    @AutoLoad
    private double eggChance;

    @AutoLoad
    private double lootingChance;

    @AutoLoad
    private boolean removeDrops;

    @AutoLoad
    private List<String> allowedTypes;

    public ArthropodEggHack(SimpleAdminHacks plugin, BasicHackConfig config) {
        super(plugin, config);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player targetPlayer = event.getEntity().getKiller();
        if (null == targetPlayer) {
            return;
        }

        String type = event.getEntity().getType().toString();
        if (allowedTypes == null || !allowedTypes.contains(type)) {
            return;
        }

        // Check for a baby animal
        if (event.getEntity() instanceof Ageable) {
            Ageable ageableEntity = (Ageable) event.getEntity();
            if (!ageableEntity.isAdult()) {
                return;
            }
        }

        // Check the player's currently equipped weapon
        ItemStack handstack = targetPlayer.getEquipment().getItemInMainHand();
        // Get the map of enchantments on that item
        Map<Enchantment, Integer> itemEnchants = handstack.getEnchantments();
        if (itemEnchants.isEmpty()) {
            return;
        }

        // Check if one enchantment is BaneOfArthropods
        if (null == itemEnchants.get(Enchantment.BANE_OF_ARTHROPODS)) {
            return;
        }

        double randomNum = Math.random();
        double levelOfArthropod = handstack.getEnchantmentLevel(Enchantment.BANE_OF_ARTHROPODS);
        double levelOfLooting = handstack.getEnchantmentLevel(Enchantment.LOOTING);

        double targetPercentage = (eggChance * levelOfArthropod) + (lootingChance * levelOfLooting);

        // Check if egg should be spawned
        if (randomNum < targetPercentage) {
            final Material spawnEggMaterial = Bukkit.getItemFactory().getSpawnEgg(event.getEntityType());
            if (spawnEggMaterial == null) {
                this.logger.warning("Could not get the spawn-egg type for [" + event.getEntityType() + "]!");
                return;
            }
            ItemStack item = ItemStack.of(spawnEggMaterial, 1);
            if (removeDrops) {
                event.getDrops().clear();
                event.setDroppedExp(0);
            }
            event.getDrops().add(item);
        }
    }

}
