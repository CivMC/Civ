package vg.civcraft.mc.civmodcore.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import vg.civcraft.mc.civmodcore.annotations.CivConfig;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CivConfigs {

	CivConfig[] value();

}
