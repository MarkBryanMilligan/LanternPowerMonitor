package com.lanternsoftware.util.dao.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DBSerializable {
    String name() default "";
    String seq() default "";
    CaseFormat caseFormat() default CaseFormat.SNAKE;
    DBIndex[] indexes() default {};
    boolean autogen() default true;
}
