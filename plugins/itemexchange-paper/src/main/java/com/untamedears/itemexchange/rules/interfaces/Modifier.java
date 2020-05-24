package com.untamedears.itemexchange.rules.interfaces;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jetbrains.annotations.Contract;

/**
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Modifier {

	/**
	 *
	 */
	@Contract("-> !null")
	String slug();

	/**
	 *
	 */
	int order() default Integer.MAX_VALUE;

}
