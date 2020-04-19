package vg.civcraft.mc.civmodcore.command;

import static vg.civcraft.mc.civmodcore.command.AikarCommand.TabComplete;
import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.CommandCompletions.CommandCompletionHandler;
import com.google.common.base.Strings;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang.reflect.FieldUtils;
import org.bukkit.Material;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.api.MaterialAPI;
import vg.civcraft.mc.civmodcore.util.TextUtil;

public abstract class AikarCommandManager {

    private final ACivMod plugin;
    private final BukkitCommandManager manager;

    public AikarCommandManager(ACivMod plugin) {
        this.plugin = plugin;
        this.manager = new BukkitCommandManager(plugin);
        registerCommands();
        CommandCompletions<BukkitCommandCompletionContext> completions = this.manager.getCommandCompletions();
        completions.registerAsyncCompletion("materials", context ->
                Arrays.stream(Material.values()).
                        map(Enum::name).
                        filter((name) -> TextUtil.startsWith(name, context.getInput())).
                        collect(Collectors.toCollection(ArrayList::new)));
        completions.registerAsyncCompletion("itemMaterials", context ->
                Arrays.stream(Material.values()).
                        filter(MaterialAPI::isValidItemMaterial).
                        map(Enum::name).
                        filter((name) -> TextUtil.startsWith(name, context.getInput())).
                        collect(Collectors.toCollection(ArrayList::new)));
        registerCompletions(completions);
    }

    public abstract void registerCommands();

    public void registerCompletions(CommandCompletions<BukkitCommandCompletionContext> manager) {

    }

    protected final void registerCommand(AikarCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Could not register that command: the command was null.");
        }
        this.plugin.info("Command [" + command.getClass().getSimpleName() + "] registered.");
        this.manager.registerCommand(command);
        for (Method method : command.getClass().getDeclaredMethods()) {
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            if (method.getParameterCount() > 1) {
                continue;
            }
            TabComplete tabComplete = method.getAnnotation(TabComplete.class);
            if (tabComplete == null) {
                continue;
            }
            if (Strings.isNullOrEmpty(tabComplete.value())) {
                continue;
            }
            if (tabComplete.async()) {
                this.manager.getCommandCompletions().registerAsyncCompletion(tabComplete.value(), (context) ->
                        runCommandCompletion(context, command, tabComplete.value(), method));
            }
            else {
                this.manager.getCommandCompletions().registerCompletion(tabComplete.value(), (context) ->
                        runCommandCompletion(context, command, tabComplete.value(), method));
            }
            this.plugin.info("Command Completer [" + tabComplete.value() + "] registered.");
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> runCommandCompletion(BukkitCommandCompletionContext context, AikarCommand command,
                                                    String id, Method method) {
        if (method == null) {
            this.plugin.warning("Could not tab complete [@" + id + "]: the handler was null!");
            return Collections.emptyList();
        }
        try {
            method.setAccessible(true);
            switch (method.getParameterCount()) {
                case 0:
                    return (List<String>) method.invoke(command);
                case 1:
                    return (List<String>) method.invoke(command, context);
                default:
                    throw new UnsupportedOperationException("Unsupported number of parameters.");
            }
        }
        catch (Exception exception) {
            this.plugin.warning("Could not tab complete [@" + id + "]: an error with the handler!", exception);
        }
        return Collections.emptyList();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void reset() {
        this.manager.unregisterCommands();
        CommandCompletions<BukkitCommandCompletionContext> completions = this.manager.getCommandCompletions();
        try {
            Map<String, CommandCompletionHandler> completionMap = (HashMap<String, CommandCompletionHandler>)
                    FieldUtils.readField(completions, "completionMap", true);
            completionMap.clear();
        }
        catch (Exception ignored) {
            this.plugin.warning("Could not reset Aikar command completions.");
        }
        try {
            Map<Class, String> defaultCompletions = (HashMap<Class, String>)
                    FieldUtils.readField(completions, "defaultCompletions", true);
            defaultCompletions.clear();
        }
        catch (Exception ignored) {
            this.plugin.warning("Could not reset Aikar's default command completions.");
        }
        this.plugin.info("Aikar commands reset.");
    }

    public BukkitCommandManager getInternalManager() {
        return this.manager;
    }

}
