package com.programmerdan.minecraft.simpleadminhacks.autoload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AutoLoad {
	
	//Thanks Angelia

	/**
	 * The identifier used for this option when reading it from the hacks config yaml section. If
	 * this is not set, the variable name will be used
	 * 
	 * @return Identifier used for this option or an empty string if the variable
	 *         name should be used
	 */
	String id() default "";

	/**
	 * Is the parameter required? If set to true, an exception will be thrown if no
	 * default is specified and the option is not specified in the config
	 * 
	 * @return Whether this option is required
	 */
	boolean isRequired() default true;
	
}
