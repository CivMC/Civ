package net.minelink.ctplus.compat.v1_7_R4;

import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.ItemStack;
import net.minecraft.server.v1_7_R4.MinecraftServer;
import net.minecraft.server.v1_7_R4.NBTCompressedStreamTools;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.NBTTagList;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_7_R4.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_7_R4.WorldNBTStorage;
import net.minecraft.server.v1_7_R4.WorldServer;
import net.minelink.ctplus.compat.api.NpcIdentity;
import net.minelink.ctplus.compat.api.NpcPlayerHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

public final class NpcPlayerHelperImpl implements NpcPlayerHelper {

    private static Method addPlayer;

    private static Method removePlayer;

    static {
        try {
            addPlayer = PacketPlayOutPlayerInfo.class.getMethod("addPlayer", EntityPlayer.class);
            removePlayer = PacketPlayOutPlayerInfo.class.getMethod("removePlayer", EntityPlayer.class);
        } catch (NoSuchMethodException ignored) {

        }
    }

    @Override
    public Player spawn(Player player) {
        NpcPlayer npcPlayer = NpcPlayer.valueOf(player);
        WorldServer worldServer = ((CraftWorld) player.getWorld()).getHandle();
        Location l = player.getLocation();

        npcPlayer.spawnIn(worldServer);
        npcPlayer.setPositionRotation(l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
        npcPlayer.playerInteractManager.a(worldServer);
        npcPlayer.invulnerableTicks = 0;

        for (Object o : MinecraftServer.getServer().getPlayerList().players) {
            if (!(o instanceof EntityPlayer) || o instanceof NpcPlayer) continue;
            if (addPlayer == null) break;

            try {
                ((EntityPlayer) o).playerConnection.sendPacket((Packet) addPlayer.invoke(null, npcPlayer));
            } catch (Exception ignored) {

            }
        }

        worldServer.addEntity(npcPlayer);
        worldServer.getPlayerChunkMap().addPlayer(npcPlayer);

        return npcPlayer.getBukkitEntity();
    }

    @Override
    public void despawn(Player player) {
        EntityPlayer entity = ((CraftPlayer) player).getHandle();

        if (!(entity instanceof NpcPlayer)) {
            throw new IllegalArgumentException();
        }

        for (Object o : MinecraftServer.getServer().getPlayerList().players) {
            if (!(o instanceof EntityPlayer) || o instanceof NpcPlayer) continue;
            if (addPlayer == null) break;

            try {
                ((EntityPlayer) o).playerConnection.sendPacket((Packet) removePlayer.invoke(null, entity));
            } catch (Exception ignored) {

            }
        }

        WorldServer worldServer = MinecraftServer.getServer().getWorldServer(entity.dimension);
        worldServer.removeEntity(entity);
        worldServer.getPlayerChunkMap().removePlayer(entity);
    }

    @Override
    public boolean isNpc(Player player) {
        return ((CraftPlayer) player).getHandle() instanceof NpcPlayer;
    }

    @Override
    public NpcIdentity getIdentity(Player player) {
        if (!isNpc(player)) {
            throw new IllegalArgumentException();
        }

        return ((NpcPlayer) ((CraftPlayer) player).getHandle()).getNpcIdentity();
    }

    @Override
    public void updateEquipment(Player player) {
        EntityPlayer entity = ((CraftPlayer) player).getHandle();

        if (!(entity instanceof NpcPlayer)) {
            throw new IllegalArgumentException();
        }

        Location l = player.getLocation();
        int rangeSquared = 512 * 512;

        for (int i = 0; i < 5; ++i) {
            ItemStack item = entity.getEquipment(i);
            if (item == null) continue;

            Packet packet = new PacketPlayOutEntityEquipment(entity.getId(), i, item);

            for (Object o : entity.world.players) {
                if (!(o instanceof EntityPlayer)) continue;

                EntityPlayer p = (EntityPlayer) o;
                Location loc = p.getBukkitEntity().getLocation();
                if (l.getWorld().equals(loc.getWorld()) && l.distanceSquared(loc) <= rangeSquared) {
                    p.playerConnection.sendPacket(packet);
                }
            }
        }
    }

    @Override
    public void syncOffline(Player player) {
        EntityPlayer entity = ((CraftPlayer) player).getHandle();

        if (!(entity instanceof NpcPlayer)) {
            throw new IllegalArgumentException();
        }

        NpcPlayer npcPlayer = (NpcPlayer) entity;
        NpcIdentity identity = npcPlayer.getNpcIdentity();
        Player p = Bukkit.getPlayer(identity.getId());
        if (p != null && p.isOnline()) return;

        WorldNBTStorage worldStorage = (WorldNBTStorage) ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle().getDataManager();
        NBTTagCompound playerNbt = worldStorage.getPlayerData(identity.getId().toString());
        if (playerNbt == null) return;

        playerNbt.setShort("Air", (short) entity.getAirTicks());
        playerNbt.setFloat("HealF", entity.getHealth());
        playerNbt.setShort("Health", (short) ((int) Math.ceil((double) entity.getHealth())));
        playerNbt.setFloat("AbsorptionAmount", entity.getAbsorptionHearts());
        playerNbt.setInt("XpTotal", entity.expTotal);
        playerNbt.setInt("foodLevel", entity.getFoodData().foodLevel);
        playerNbt.setInt("foodTickTimer", entity.getFoodData().foodTickTimer);
        playerNbt.setFloat("foodSaturationLevel", entity.getFoodData().saturationLevel);
        playerNbt.setFloat("foodExhaustionLevel", entity.getFoodData().exhaustionLevel);
        playerNbt.setShort("Fire", (short) entity.fireTicks);
        playerNbt.set("Inventory", npcPlayer.inventory.a(new NBTTagList()));

        File file1 = new File(worldStorage.getPlayerDir(), identity.getId().toString() + ".dat.tmp");
        File file2 = new File(worldStorage.getPlayerDir(), identity.getId().toString() + ".dat");

        try {
            NBTCompressedStreamTools.a(playerNbt, new FileOutputStream(file1));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save player data for " + identity.getName(), e);
        }

        if ((!file2.exists() || file2.delete()) && !file1.renameTo(file2)) {
            throw new RuntimeException("Failed to save player data for " + identity.getName());
        }
    }

    @Override
    public void createPlayerList(Player player) {
        if (addPlayer == null) return;

        EntityPlayer p = ((CraftPlayer) player).getHandle();

        for (WorldServer worldServer : MinecraftServer.getServer().worlds) {
            for (Object o : worldServer.players) {
                if (!(o instanceof NpcPlayer)) continue;

                NpcPlayer npcPlayer = (NpcPlayer) o;

                try {
                    p.playerConnection.sendPacket((Packet) addPlayer.invoke(null, npcPlayer));
                } catch (Exception ignored) {

                }
            }
        }
    }

    @Override
    public void removePlayerList(Player player) {
        if (addPlayer == null) return;

        EntityPlayer p = ((CraftPlayer) player).getHandle();

        for (WorldServer worldServer : MinecraftServer.getServer().worlds) {
            for (Object o : worldServer.players) {
                if (!(o instanceof NpcPlayer)) continue;

                NpcPlayer npcPlayer = (NpcPlayer) o;

                try {
                    p.playerConnection.sendPacket((Packet) removePlayer.invoke(null, npcPlayer));
                } catch (Exception ignored) {

                }
            }
        }
    }

}
