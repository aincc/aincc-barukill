package com.baru.barukill.common.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectView
{
	/**
	 * 
	 * @since 1.0.0
	 * @return the view's id
	 */
	int id() default 0;
}
