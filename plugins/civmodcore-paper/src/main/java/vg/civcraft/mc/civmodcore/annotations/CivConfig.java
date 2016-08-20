package vg.civcraft.mc.civmodcore.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import vg.civcraft.mc.civmodcore.annotations.CivConfigType;

/**
 * If using {@link CivConfigType#Object} set default to be a valid YAML string, with
 * a single top-level element "default"
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CivConfig {

	String name();

	String def() default "";

	CivConfigType type() default CivConfigType.Bool;

}
