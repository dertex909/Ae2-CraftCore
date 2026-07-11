/*
 * Ae2 CraftCore
 * Copyright (C) 2026 dertex909
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

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