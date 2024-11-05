package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import io.papermc.paper.event.entity.EntityInsideBlockEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Hoglin;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PortalConnect extends BasicHack {

    @AutoLoad
    private String server;

    @AutoLoad
    private String world;

    private final Map<UUID, Long> onCooldown = new HashMap<>();

    public PortalConnect(SimpleAdminHacks plugin, BasicHackConfig config) {
        super(plugin, config);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void on(EntityInsideBlockEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (event.getBlock().getType() != Material.END_PORTAL) {
            return;
        }
        if (!event.getBlock().getWorld().getName().equals(world)) {
            return;
        }

        event.setCancelled(true);

        if (onCooldown.containsKey(player.getUniqueId())) {
            long until = onCooldown.get(player.getUniqueId());
            if (System.currentTimeMillis() <= until) {
                return;
            }
        }

        onCooldown.put(player.getUniqueId(), System.currentTimeMillis() + 10_000);

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);

        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }
}
