package org.ae2craftcore.registry.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(RegisterItem.List.class)
public @interface RegisterItem {
    String name();

    int stacksTo() default 64;

    int durability() default 0;

    boolean fireResistant() default false;

    String tier() default "";

    String rarity() default "COMMON";

    String craftRemainder() default "";

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface List {
        RegisterItem[] value();
    }
}