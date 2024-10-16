package vg.civcraft.mc.civmodcore.commands;

import co.aikar.commands.BaseCommand;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>This annotation, when used on a method on an {@link BaseCommand} extending class will be automatically
 * registered as a tab completion.</p>
 *
 * <p>The return type of the method MUST, <i>MUST</i> be {@code List<String>}, otherwise it will fail.</p>
 *
 * <p>The method is permitted to have 0-1 parameters, but that parameter MUST, <i>MUST</i> be of type
 * {@code BukkitCommandCompletionContext}, otherwise it will fail.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TabComplete {
	String value();

	boolean async() default false;
}
