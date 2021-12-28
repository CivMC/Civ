package vg.civcraft.mc.civmodcore.commands;

import co.aikar.commands.BaseCommand;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * This class should be used when you can't use {@link co.aikar.commands.annotation.CommandAlias}.
 */
public abstract class NamedCommand extends BaseCommand {

	@SuppressWarnings("deprecation")
	public NamedCommand(@Nonnull final String command) {
		super(Objects.requireNonNull(command));
	}

}
