package vg.civcraft.mc.civmodcore.gluing;

import io.papermc.paper.plugin.configuration.PluginMeta;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.PluginClassLoader;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.events.EventUtils;
import vg.civcraft.mc.civmodcore.utilities.CivLogger;
import vg.civcraft.mc.civmodcore.utilities.MoreClassUtils;

@ApiStatus.Internal
public final class GlueManager implements Listener {
    // TODO: PaperMC has deprecated plugin.getDescription(), but its replacement, plugin.getPluginMeta(), is marked
    //       as unstable ಠ_ಠ

    private final Logger logger = CivLogger.getLogger(getClass());
    private final String requiredDependency;

    public GlueManager(
        final @NotNull CivModCorePlugin plugin
    ) {
        this.requiredDependency = plugin.getName();
    }

    @EventHandler
    private void registerOwnGlues(
        final @NotNull PluginEnableEvent event
    ) {
        final PluginMeta pluginMeta = event.getPlugin().getPluginMeta();
        final Set<String> dependencies = getAllDependencies(pluginMeta);

        // Plugin MUST include "CivModCore" as a dependency
        if (!dependencies.contains(this.requiredDependency)) {
            return;
        }

        // Get the glue classes the plugin has declared
        final Map<String, DependencyGlueHolder> glues = parseGlues(event.getPlugin(), dependencies, this.logger);

        // Enable glues for plugins that are already initialised
        for (final Plugin alreadyLoadedPlugin : Bukkit.getPluginManager().getPlugins()) {
            if (alreadyLoadedPlugin.isEnabled()) {
                final DependencyGlueHolder glue = glues.get(alreadyLoadedPlugin.getName());
                if (glue != null) {
                    glue.enable();
                }
            }
        }

        EventUtils.registerListener(event.getPlugin(), new Listener() {
            @EventHandler
            private void onDependencyEnable(
                final @NotNull PluginEnableEvent dependencyEvent
            ) {
                final DependencyGlueHolder glue = glues.get(dependencyEvent.getPlugin().getName());
                if (glue != null) {
                    glue.enable();
                }
            }

            @EventHandler
            private void onDependencyDisable(
                final @NotNull PluginDisableEvent dependencyEvent
            ) {
                final DependencyGlueHolder glue = glues.get(dependencyEvent.getPlugin().getName());
                if (glue != null) {
                    glue.disable();
                }
            }
        });
    }

    private static @NotNull Set<String> getAllDependencies(
        final @NotNull PluginMeta pluginMeta
    ) {
        final var dependencies = new HashSet<String>();
        dependencies.addAll(pluginMeta.getPluginDependencies());
        dependencies.addAll(pluginMeta.getPluginSoftDependencies());
        return Set.copyOf(dependencies);
    }

    private static final String GLUES_RESOURCE_PATH = "glues.properties";
    private static final Pattern PLUGIN_NAME_PATTERN = Pattern.compile("[a-zA-Z0-9_.\\-]+");

    private static @NotNull Map<String, DependencyGlueHolder> parseGlues(
        final @NotNull Plugin plugin,
        final @NotNull Set<String> dependencies,
        final @NotNull Logger logger
    ) {
        final var config = new Properties();
        final InputStream raw = plugin.getResource(GLUES_RESOURCE_PATH);
        if (raw != null) {
            try {
                config.load(new InputStreamReader(raw));
            }
            catch (final IOException thrown) {
                logger.log(Level.WARNING, "Could not read [" + GLUES_RESOURCE_PATH + "]!", thrown);
            }
        }
        final var glues = new HashMap<String, DependencyGlueHolder>();
        for (final Object key : config.keySet()) {
            final String glueTarget = key.toString();
            if (!PLUGIN_NAME_PATTERN.matcher(glueTarget).matches()) {
                logger.warning("Glue target [" + glueTarget + "] is not a valid plugin name!");
                continue;
            }
            if (!dependencies.contains(glueTarget)) {
                logger.warning("Glue target [" + glueTarget + "] is not declared within plugin.yml's 'depend' or 'softdepend' lists!");
                continue;
            }
            final String glueClass = config.getProperty(glueTarget, null);
            if (glueClass == null) {
                logger.warning("Glue for target [" + glueTarget + "] is null!");
                continue;
            }
            if (!MoreClassUtils.isValidClassPath(glueClass)) {
                logger.warning("Glue for target [" + glueTarget + "] is not a valid Java class path! [" + glueClass + "]");
                continue;
            }
            glues.put(glueTarget, new DependencyGlueHolder(plugin, glueClass));
        }
        return glues;
    }

    private static class DependencyGlueHolder {
        private final Logger logger;
        private final PluginClassLoader classLoader;
        private final String glueClassName;
        private DependencyGlue glue;

        private DependencyGlueHolder(
            final @NotNull Plugin ownerPlugin,
            final @NotNull String glueClassName
        ) {
            this.logger = CivLogger.getLogger(ownerPlugin.getClass(), getClass());
            this.classLoader = (PluginClassLoader) ownerPlugin.getClass().getClassLoader();
            this.glueClassName = glueClassName;
        }

        public void enable() {
            if (this.glue == null) {
                final Class<?> glueClass;
                try {
                    glueClass = Class.forName(this.glueClassName, true, this.classLoader);
                }
                catch (final ClassNotFoundException thrown) {
                    this.logger.warning("Could not find glue class [" + this.glueClassName + "]! Skipping.");
                    return;
                }
                if (!DependencyGlue.class.isAssignableFrom(glueClass)) {
                    this.logger.warning("Glue class [" + this.glueClassName + "] does not implement DependencyGlue! Skipping.");
                    return;
                }
                final Constructor<?> glueConstructor;
                try {
                    glueConstructor = glueClass.getDeclaredConstructor();
                }
                catch (final NoSuchMethodException thrown) {
                    this.logger.warning("Glue class [" + this.glueClassName + "] does not have a default constructor! Skipping.");
                    return;
                }
                try {
                    this.glue = (DependencyGlue) glueConstructor.newInstance();
                }
                catch (final IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException | ExceptionInInitializerError thrown) {
                    this.logger.log(Level.WARNING, "Glue class [" + this.glueClassName + "] could not be constructed! Skipping.", thrown);
                    return;
                }
            }
            try {
                this.glue.enable();
            }
            catch (final RuntimeException thrown) {
                this.logger.log(Level.WARNING, "Glue class [" + this.glueClassName + "] errored while enabling!", thrown);
                return;
            }
            this.logger.info("Glued [" + this.glueClassName + "]");
        }

        public void disable() {
            if (this.glue != null) {
                try {
                    this.glue.disable();
                }
                catch (final Exception thrown) {
                    this.logger.log(Level.WARNING, "Glue class [" + this.glueClassName + "] errored while disabling!", thrown);
                }
                this.glue = null;
            }
        }
    }
}
