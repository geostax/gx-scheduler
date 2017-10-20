package com.geostax.scheduler.core.executor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * annotation for task handler
 * A task handler can be triggered by Quartz scheduler to achieve a user-specified task
 * 
 * @author Phil XIAO 2016-5-17 21:06:49
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface TaskHandler {

	String value() default "";

}
