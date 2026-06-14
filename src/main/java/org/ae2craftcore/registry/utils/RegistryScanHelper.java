package org.ae2craftcore.registry.utils;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.ae2craftcore.Ae2craftcore;

import java.lang.annotation.Annotation;

public final class RegistryScanHelper {

    private RegistryScanHelper() {
    }

    public static ModFileScanData getScanData() {
        return ModList.get().getModFileById(Ae2craftcore.MODID).getFile().getScanResult();
    }

    @SafeVarargs
    public static ObjectOpenHashSet<String> findAnnotatedClasses(Class<? extends Annotation>... annotations) {
        var scanData = getScanData();
        var result = new ObjectOpenHashSet<String>();
        var names = new ObjectOpenHashSet<String>();
        for (var a : annotations) names.add(a.getName());
        for (var data : scanData.getAnnotations()) {
            if (names.contains(data.annotationType().getClassName())) result.add(data.clazz().getClassName());
        }
        return result;
    }

    public static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            Ae2craftcore.LOGGER.error("RegistryScanHelper: Failed to load class: {}", className);
            return null;
        }
    }
}