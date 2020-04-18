package vg.civcraft.mc.civmodcore.command;

import co.aikar.commands.BaseCommand;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public abstract class AikarCommand extends BaseCommand {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface TabComplete {
        String value();
        boolean async() default false;
    }

}
