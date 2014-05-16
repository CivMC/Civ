package com.valadian.bergecraft.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.valadian.bergecraft.annotations.OptType;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Bergification {
  public String opt();
  public String def() default "";
  public OptType type() default OptType.Bool;
}
