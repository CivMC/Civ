package com.github.maxopoly.finale.combat;

import com.github.maxopoly.finale.Finale;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAnimation;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class AsyncPacketHandler implements Listener, PacketListener {

    private final CombatConfig cc;

    public AsyncPacketHandler(CombatConfig cc) {
        this.cc = cc;

        Bukkit.getPluginManager().registerEvents(this, Finale.getPlugin());
    }

    private final Set<UUID> isDigging = Sets.newConcurrentHashSet();
    private final Map<UUID, Long> lastRemovals = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastStartBreaks = new ConcurrentHashMap<>();

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        PacketTypeCommon packetType = event.getPacketType();

        CPSHandler cpsHandler = Finale.getPlugin().getManager().getCPSHandler();
        if (packetType == PacketType.Play.Client.INTERACT_ENTITY) {
            Player attacker = event.getPlayer();
            World world = attacker.getWorld();

            ServerLevel level = ((CraftWorld) world).getHandle();

            WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);

            if (packet.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                return;
            }
            event.setCancelled(true);
            new BukkitRunnable() {

                @Override
                public void run() {
                    CraftEntity entity = level.getEntity(packet.getEntityId()).getBukkitEntity();
                    Damageable target = entity instanceof Damageable ? (Damageable) entity : null;

                    if (target == null || target.isDead() || target.isInvulnerable() ||
                        !world.getUID().equals(target.getWorld().getUID()) || !(target instanceof LivingEntity)) {
                        DamageSources damageSources = ((CraftWorld) world).getHandle().damageSources();
                        entity.getHandle().hurt(damageSources.playerAttack(((CraftPlayer) attacker).getHandle()), (float) ((CraftPlayer) attacker).getHandle().getAttribute(Attributes.ATTACK_DAMAGE).getValue());
                        return;
                    }

                    double distanceSquared = attacker.getLocation().distanceSquared(target.getLocation());

                    if (distanceSquared > (cc.getMaxReach() * cc.getMaxReach())) {
                        return;
                    }

                    if (cpsHandler.getCPS(attacker.getUniqueId()) >= cc.getCPSLimit()) {
                        attacker.sendMessage(ChatColor.RED + "You've hit CPS limit of " + cc.getCPSLimit() + "!");
                        return;
                    }

                    if (attacker.getGameMode() == GameMode.SPECTATOR) {
                        attacker.setSpectatorTarget(target);
                    } else {
                        CombatUtil.attack(attacker, ((CraftEntity) target).getHandle());
                    }
                }
            }.runTask(Finale.getPlugin());
        } else if (packetType == PacketType.Play.Client.ENTITY_ACTION) {
            WrapperPlayClientEntityAction packet = new WrapperPlayClientEntityAction(event);
            Player player = event.getPlayer();
            WrapperPlayClientEntityAction.Action playerAction = packet.getAction();
            SprintHandler sprintHandler = Finale.getPlugin().getManager().getSprintHandler();
            if (playerAction == WrapperPlayClientEntityAction.Action.START_SPRINTING) {
                sprintHandler.startSprinting(player);
            } else if (playerAction == WrapperPlayClientEntityAction.Action.STOP_SPRINTING) {
                sprintHandler.stopSprinting(player);
            }
        } else if (packetType == PacketType.Play.Client.ANIMATION) {
            Player attacker = event.getPlayer();
            WrapperPlayClientAnimation packet = new WrapperPlayClientAnimation(event);
            InteractionHand hand = packet.getHand();
            if (hand == InteractionHand.MAIN_HAND && !isDigging.contains(attacker.getUniqueId())) {
                Block targetBlock = attacker.getTargetBlockExact(4);
                if (targetBlock != null && targetBlock.getType() != Material.AIR) {
                    return;
                }
                cpsHandler.updateClicks(attacker);
            }
        } else if (packetType == PacketType.Play.Client.PLAYER_DIGGING) {
            Player attacker = event.getPlayer();

            WrapperPlayClientPlayerDigging packet = new WrapperPlayClientPlayerDigging(event);
            DiggingAction digType = packet.getAction();
            if (digType == DiggingAction.STAB) {
                // no spears
                event.setCancelled(true);
                return;
            }

            if (attacker.getGameMode() != GameMode.SURVIVAL) {
                return;
            }

            Vector3i position = packet.getBlockPosition();
            if (digType == DiggingAction.START_DIGGING) {
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
            } else if (digType == DiggingAction.CANCELLED_DIGGING || digType == DiggingAction.FINISHED_DIGGING) {
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
