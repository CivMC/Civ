package vg.civcraft.mc.namelayer.config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NameConfig {

	public String name();
	public String def() default "";
	public NameConfigType type() default NameConfigType.String;
}
