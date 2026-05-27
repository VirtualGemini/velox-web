package com.velox.module.system.id.frontend;

import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SystemFrontendIdReflectionTransformer {

    private final SystemFrontendIdCodecSupport codecSupport;

    public SystemFrontendIdReflectionTransformer(SystemFrontendIdCodecSupport codecSupport) {
        this.codecSupport = codecSupport;
    }

    public Object encode(Object value) {
        if (!codecSupport.isEnabled()) {
            return value;
        }
        transformValue(value, null, true, new IdentityHashMap<>());
        return value;
    }

    public Object decode(Object value) {
        if (!codecSupport.isEnabled()) {
            return value;
        }
        transformValue(value, null, false, new IdentityHashMap<>());
        return value;
    }

    private Object transformValue(Object value, String fieldName, boolean encode, IdentityHashMap<Object, Boolean> visited) {
        if (value == null) {
            return null;
        }
        if (value instanceof String stringValue) {
            if (!codecSupport.isIdentifierName(fieldName)) {
                return stringValue;
            }
            return encode
                    ? codecSupport.encodeIdentifier(stringValue)
                    : codecSupport.decodeIdentifier(stringValue);
        }
        if (value instanceof Collection<?> collection) {
            transformCollection(collection, fieldName, encode, visited);
            return value;
        }
        if (value instanceof Map<?, ?> map) {
            transformMap(map, encode, visited);
            return value;
        }
        Class<?> valueType = value.getClass();
        if (valueType.isArray()) {
            transformArray(value, fieldName, encode, visited);
            return value;
        }
        if (isTerminalType(valueType) || visited.containsKey(value)) {
            return value;
        }
        visited.put(value, Boolean.TRUE);
        transformFields(value, valueType, encode, visited);
        return value;
    }

    private void transformCollection(
            Collection<?> collection,
            String fieldName,
            boolean encode,
            IdentityHashMap<Object, Boolean> visited
    ) {
        if (!(collection instanceof List<?> list)) {
            for (Object element : collection) {
                transformValue(element, fieldName, encode, visited);
            }
            return;
        }
        for (int index = 0; index < list.size(); index++) {
            Object element = list.get(index);
            Object transformed = transformValue(element, fieldName, encode, visited);
            if (element instanceof String && transformed instanceof String transformedString) {
                ((List<Object>) list).set(index, transformedString);
            }
        }
    }

    private void transformMap(Map<?, ?> map, boolean encode, IdentityHashMap<Object, Boolean> visited) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = entry.getKey();
            String fieldName = key instanceof String stringKey ? stringKey : null;
            Object value = entry.getValue();
            Object transformed = transformValue(value, fieldName, encode, visited);
            if (fieldName != null && value instanceof String && transformed instanceof String transformedString) {
                ((Map<Object, Object>) map).put(key, transformedString);
            }
        }
    }

    private void transformArray(Object array, String fieldName, boolean encode, IdentityHashMap<Object, Boolean> visited) {
        int length = Array.getLength(array);
        for (int index = 0; index < length; index++) {
            Object element = Array.get(array, index);
            Object transformed = transformValue(element, fieldName, encode, visited);
            if (element instanceof String && transformed instanceof String transformedString) {
                Array.set(array, index, transformedString);
            }
        }
    }

    private void transformFields(
            Object target,
            Class<?> targetType,
            boolean encode,
            IdentityHashMap<Object, Boolean> visited
    ) {
        Class<?> currentType = targetType;
        while (currentType != null && currentType != Object.class) {
            for (Field field : currentType.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                    continue;
                }
                field.setAccessible(true);
                try {
                    Object fieldValue = field.get(target);
                    Object transformed = transformValue(fieldValue, field.getName(), encode, visited);
                    if (fieldValue instanceof String && transformed instanceof String transformedString) {
                        field.set(target, transformedString);
                    }
                } catch (IllegalAccessException ignored) {
                    // best effort; skip inaccessible fields
                }
            }
            currentType = currentType.getSuperclass();
        }
    }

    private boolean isTerminalType(Class<?> valueType) {
        return valueType.isPrimitive()
                || Number.class.isAssignableFrom(valueType)
                || CharSequence.class.isAssignableFrom(valueType)
                || Boolean.class == valueType
                || Character.class == valueType
                || Enum.class.isAssignableFrom(valueType)
                || Temporal.class.isAssignableFrom(valueType)
                || valueType.getName().startsWith("java.")
                || valueType.getName().startsWith("javax.")
                || valueType.getName().startsWith("jakarta.");
    }
}
