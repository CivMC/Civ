package vg.civcraft.mc.namelayer.config.annotations;

public @interface NameConfig {

	public String name();
	public String def() default "";
	public NameConfigType type() default NameConfigType.String;
}
