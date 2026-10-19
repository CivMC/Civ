package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ServerLinks;

public final class ServerLinksHack extends BasicHack {

    @AutoLoad
    private String discordUrl;

    @AutoLoad
    private String patreonUrl;

    private final List<ServerLinks.ServerLink> links;

    public ServerLinksHack(final SimpleAdminHacks plugin, final BasicHackConfig config) {
        super(plugin, config);
        this.links = new ArrayList<>();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        addLink("Discord", this.discordUrl);
        addLink("Patreon", this.patreonUrl);
    }

    @Override
    public void onDisable() {
        for (final ServerLinks.ServerLink link : this.links) {
            Bukkit.getServer().getServerLinks().removeLink(link);
        }
        this.links.clear();
        super.onDisable();
    }

    private void addLink(final String name, final String url) {
        if (url == null || url.isBlank()) {
            this.plugin.warning("ServerLinksHack is missing a URL for " + name + '.');
            return;
        }
        this.links.add(Bukkit.getServer().getServerLinks().addLink(Component.text(name), URI.create(url)));
    }

}
