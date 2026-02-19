package net.minelink.ctplus.nms;

import com.google.common.collect.ImmutableMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.util.Map;
import java.util.UUID;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minelink.ctplus.compat.base.NpcIdentity;
import net.minelink.ctplus.compat.base.NpcNameGeneratorFactory;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class NpcPlayer extends ServerPlayer {

    private NpcIdentity identity;

    public NpcPlayer(MinecraftServer server, ServerLevel world, GameProfile profile, ClientInformation clientInformation) {
        super(server, world, profile, clientInformation);
    }

    public NpcIdentity getNpcIdentity() {
        return identity;
    }

    public static NpcPlayer valueOf(Player player) {
        MinecraftServer minecraftServer = MinecraftServer.getServer();
        ServerLevel worldServer = ((CraftWorld) player.getWorld()).getHandle();
        ClientInformation clientInformation = ((CraftPlayer) player).getHandle().clientInformation();

        ImmutableMultimap.Builder<String, Property> builder = ImmutableMultimap.builder();
        for (Map.Entry<String, Property> entry : ((CraftPlayer) player).getProfile().properties().entries()) {
            builder.put(entry.getKey(), entry.getValue());
        }
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), NpcNameGeneratorFactory.getNameGenerator().generate(player), new PropertyMap(builder.build()));

        NpcPlayer npcPlayer = new NpcPlayer(minecraftServer, worldServer, gameProfile, clientInformation);
        npcPlayer.identity = new NpcIdentity(player);

        new NpcPlayerConnection(npcPlayer);

        return npcPlayer;
    }
}
