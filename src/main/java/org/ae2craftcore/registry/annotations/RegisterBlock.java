package org.ae2craftcore.registry.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(RegisterBlock.List.class)
public @interface RegisterBlock {
    String name();

    float strength() default 1.0f;

    float resistance() default -1.0f;

    String sound() default "metal";

    boolean requiresCorrectTool() default false;

    boolean noOcclusion() default false;

    boolean dynamicShape() default false;

    int lightLevel() default 0;

    float friction() default 0.6f;

    boolean hasItem() default true;

    int tier() default -1;

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface List {
        RegisterBlock[] value();
    }
}