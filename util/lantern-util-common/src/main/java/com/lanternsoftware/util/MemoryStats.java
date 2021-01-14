package com.lanternsoftware.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MemoryStats {
    public static long size(Object _o) {
        return size(_o, new HashSet<Integer>());
    }

    private static long size(Object _o, Set<Integer> _counted) {
        int hash = System.identityHashCode(_o);
        if (_counted.contains(hash))
            return 0;
        _counted.add(hash);
        long size = 0;
        for (Field f : allFields(_o.getClass())) {
            if (Modifier.isStatic(f.getModifiers()) || Modifier.isTransient(f.getModifiers()))
                continue;
            f.setAccessible(true);
            try {
                Object child =  f.get(_o);
                if (child != null) {
                    if (f.getType().equals(String.class))
                        size += ((String)child).length();
                    else if (f.getType().equals(Date.class))
                        size += 8;
                    else if (f.getType().equals(Double.class) || f.getType().equals(Double.TYPE))
                        size += 8;
                    else if (f.getType().equals(Float.class) || f.getType().equals(Float.TYPE))
                        size += 4;
                    else if (f.getType().equals(Long.class) || f.getType().equals(Long.TYPE))
                        size += 8;
                    else if (f.getType().equals(Integer.class) || f.getType().equals(Integer.TYPE))
                        size += 4;
                    else if (f.getType().equals(Short.class) || f.getType().equals(Short.TYPE))
                        size += 2;
                    else if (f.getType().equals(Byte.class) || f.getType().equals(Byte.TYPE))
                        size += 1;
                    else if (f.getType().equals(Character.class) || f.getType().equals(Character.TYPE))
                        size += 1;
                    else if (f.getType().equals(Boolean.class) || f.getType().equals(Boolean.TYPE))
                        size += 1;
                    else if (f.getType().equals(byte[].class))
                        size += ((byte[])child).length;
                    else if (f.getType().equals(char[].class))
                        size += ((char[])child).length;
                    else if (Collection.class.isAssignableFrom(f.getType())) {
                        for (Object childElement : ((Collection)child)) {
                            size += size(childElement, _counted);
                        }
                    }
                    else if (Map.class.isAssignableFrom(f.getType())) {
                        Set<Entry<?, ?>> entries = ((Map)child).entrySet();
                        for (Entry<?, ?> childElement : entries) {
                            size += size(childElement.getKey(), _counted);
                            size += size(childElement.getValue(), _counted);
                        }
                    }
                    else
                        size += size(child, _counted);
                }
            } catch (IllegalAccessException _e) {
            }
        }
        return size;
    }

    private static List<Field> allFields(Class _c) {
        if (_c == null)
            return Collections.emptyList();
        List<Field> fields = CollectionUtils.asArrayList(_c.getDeclaredFields());
        fields.addAll(allFields(_c.getSuperclass()));
        return fields;
    }
}
