package net.minelink.ctplus.nms;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minelink.ctplus.compat.base.NpcIdentity;
import net.minelink.ctplus.compat.base.NpcNameGeneratorFactory;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class NpcPlayer extends ServerPlayer {

    private NpcIdentity identity;
    public NpcPlayer(MinecraftServer server, ServerLevel world, GameProfile profile) {
        super(server, world, profile);
    }

    public NpcIdentity getNpcIdentity() {
        return identity;
    }

    public static NpcPlayer valueOf(Player player) {
        MinecraftServer minecraftServer = MinecraftServer.getServer();
        ServerLevel worldServer = ((CraftWorld) player.getWorld()).getHandle();
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), NpcNameGeneratorFactory.getNameGenerator().generate(player));

        for (Map.Entry<String, Property> entry: ((CraftPlayer) player).getProfile().getProperties().entries()) {
            gameProfile.getProperties().put(entry.getKey(), entry.getValue());
        }

        NpcPlayer npcPlayer = new NpcPlayer(minecraftServer, worldServer, gameProfile);
        npcPlayer.identity = new NpcIdentity(player);

        new NpcPlayerConnection(npcPlayer);

        return npcPlayer;
    }
}
