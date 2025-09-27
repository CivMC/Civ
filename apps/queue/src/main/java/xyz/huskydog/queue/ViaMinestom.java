package xyz.huskydog.queue;
/*
 * This file is part of ViaLimbo.
 *
 * Copyright (C) 2020 - 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2020 - 2025. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import net.lenni0451.classtransform.TransformerManager;
import net.lenni0451.classtransform.additionalclassprovider.GuavaClassPathProvider;
import net.lenni0451.classtransform.mixinstranslator.MixinsTranslator;
import net.lenni0451.classtransform.utils.tree.IClassProvider;
import net.lenni0451.optconfig.ConfigLoader;
import net.lenni0451.optconfig.provider.ConfigProvider;
import net.minestom.server.MinecraftServer;
import net.raphimc.viaproxy.ViaProxy;
import net.raphimc.viaproxy.protocoltranslator.ProtocolTranslator;
import net.raphimc.viaproxy.protocoltranslator.viaproxy.ViaProxyConfig;
import net.raphimc.viaproxy.util.ClassLoaderPriorityUtil;

// heavily inspired by https://github.com/LOOHP/ViaLimbo
public class ViaMinestom {

    public void onEnable() {
        // MinecraftServer.LOGGER.info("[ViaMinestom] Starting");
        try {
            // ServerProperties serverProperties = Limbo.getInstance().getServerProperties();
            String ip = Config.HOST;
            int port = Config.PORT;

            MinecraftServer.getSchedulerManager().execute(() -> {
                int minestomPort = MinecraftServer.getServer().getPort();
                String minestomHost = MinecraftServer.getServer().getAddress();
                startViaProxy(ip, port, Main.MC_VERSION, minestomHost, minestomPort);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void onDisable() {
        stopViaProxy();
    }

    private void startViaProxy(String ip, int port, String minecraftVersion, String minestomHost, int minestomPort) {
        try {
            MinecraftServer.LOGGER.info("[ViaMinestom] Initializing ViaProxy " + ViaProxy.VERSION + " (" + ViaProxy.IMPL_VERSION + ")");

            IClassProvider classProvider = new GuavaClassPathProvider();
            TransformerManager transformerManager = new TransformerManager(classProvider);
            transformerManager.addTransformerPreprocessor(new MixinsTranslator());
            transformerManager.addTransformer("net.raphimc.viaproxy.injection.mixins.**");

            ConfigLoader<ViaProxyConfig> configLoader = new ConfigLoader<>(ViaProxyConfig.class);
            configLoader.getConfigOptions().setResetInvalidOptions(true).setRewriteConfig(true).setCommentSpacing(1);

            // create the data folder if not exist
            Main.getCwd().mkdirs();
            Field cwdField = ViaProxy.class.getDeclaredField("CWD");
            cwdField.setAccessible(true);
            cwdField.set(null, Main.getCwd());

            // blank out viaproxy config
            ViaProxyConfig config = configLoader.load(ConfigProvider.memory("", s -> {
            })).getConfigInstance();
            Field configField = ViaProxy.class.getDeclaredField("CONFIG");
            configField.setAccessible(true);
            configField.set(null, config);

            // set viaproxy config options
            config.setBindAddress(new InetSocketAddress(ip, port));
            config.setTargetAddress(new InetSocketAddress(minestomHost, minestomPort));
            config.setTargetVersion(ProtocolVersion.getClosest(minecraftVersion));
            // config.setPassthroughBungeecordPlayerInfo(bungeecord);
            config.setAllowLegacyClientPassthrough(true);
            config.setAuthMethod(ViaProxyConfig.AuthMethod.NONE); // TODO: VELOCITY support https://github.com/KiyoNetcat/ViaProxyVelocity

            Method loadNettyMethod = ViaProxy.class.getDeclaredMethod("loadNetty");
            loadNettyMethod.setAccessible(true);
            loadNettyMethod.invoke(null);
            ClassLoaderPriorityUtil.loadOverridingJars();
            ProtocolTranslator.init();
            ViaProxy.startProxy();

            MinecraftServer.LOGGER.info("[ViaMinestom] ViaProxy listening on /" + ip + ":" + port);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void stopViaProxy() {
        ViaProxy.stopProxy();
        MinecraftServer.LOGGER.info("[ViaMinestom] ViaProxy Shutdown");
    }

}
