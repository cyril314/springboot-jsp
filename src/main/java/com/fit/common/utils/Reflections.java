package com.fit.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.lang.reflect.*;

public class Reflections {

    private static final String SETTER_PREFIX = "set";
    private static final String GETTER_PREFIX = "get";
    private static final String CGLIB_CLASS_SEPARATOR = "$$";
    private static Logger logger = LoggerFactory.getLogger(Reflections.class);

    public Reflections() {
    }

    public static Object invokeGetter(Object obj, String propertyName) {
        Object object = obj;
        String[] var6;
        int var5 = (var6 = propertyName.split(".")).length;

        for (int var4 = 0; var4 < var5; ++var4) {
            String name = var6[var4];
            String getterMethodName = "get" + StringUtil.capitalize(name);
            object = invokeMethod(object, getterMethodName, new Class[0], new Object[0]);
        }

        return object;
    }

    public static void invokeSetter(Object obj, String propertyName, Object value) {
        Object object = obj;
        String[] names = propertyName.split(".");

        for (int i = 0; i < names.length; ++i) {
            String setterMethodName;
            if (i < names.length - 1) {
                setterMethodName = "get" + StringUtil.capitalize(names[i]);
                object = invokeMethod(object, setterMethodName, new Class[0], new Object[0]);
            } else {
                setterMethodName = "set" + StringUtil.capitalize(names[i]);
                invokeMethodByName(object, setterMethodName, new Object[]{value});
            }
        }

    }

    public static Object getFieldValue(Object obj, String fieldName) {
        Field field = getAccessibleField(obj, fieldName);
        if (field == null) {
            throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + obj + "]");
        } else {
            Object result = null;

            try {
                result = field.get(obj);
            } catch (IllegalAccessException var5) {
                logger.error("不可能抛出的异常{}", var5.getMessage());
            }

            return result;
        }
    }

    public static void setFieldValue(Object obj, String fieldName, Object value) {
        Field field = getAccessibleField(obj, fieldName);
        if (field == null) {
            throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + obj + "]");
        } else {
            try {
                field.set(obj, value);
            } catch (IllegalAccessException var5) {
                logger.error("不可能抛出的异常:{}", var5.getMessage());
            }

        }
    }

    public static Object invokeMethod(Object obj, String methodName, Class<?>[] parameterTypes, Object[] args) {
        Method method = getAccessibleMethod(obj, methodName, parameterTypes);
        if (method == null) {
            throw new IllegalArgumentException("Could not find method [" + methodName + "] on target [" + obj + "]");
        } else {
            try {
                return method.invoke(obj, args);
            } catch (Exception var6) {
                throw convertReflectionExceptionToUnchecked(var6);
            }
        }
    }

    public static Object invokeMethodByName(Object obj, String methodName, Object[] args) {
        Method method = getAccessibleMethodByName(obj, methodName);
        if (method == null) {
            throw new IllegalArgumentException("Could not find method [" + methodName + "] on target [" + obj + "]");
        } else {
            try {
                return method.invoke(obj, args);
            } catch (Exception var5) {
                throw convertReflectionExceptionToUnchecked(var5);
            }
        }
    }

    public static Field getAccessibleField(Object obj, String fieldName) {
        Assert.notNull(obj, "object can't be null");
        Assert.hasLength(fieldName, "fieldName can't be blank");
        Class superClass = obj.getClass();

        while (superClass != Object.class) {
            try {
                Field field = superClass.getDeclaredField(fieldName);
                makeAccessible(field);
                return field;
            } catch (NoSuchFieldException var4) {
                superClass = superClass.getSuperclass();
            }
        }

        return null;
    }

    public static Method getAccessibleMethod(Object obj, String methodName, Class... parameterTypes) {
        Assert.notNull(obj, "object can\'t be null");
        Assert.hasLength(methodName, "methodName can't be blank");
        Class searchType = obj.getClass();

        while (searchType != Object.class) {
            try {
                Method method = searchType.getDeclaredMethod(methodName, parameterTypes);
                makeAccessible(method);
                return method;
            } catch (NoSuchMethodException var5) {
                searchType = searchType.getSuperclass();
            }
        }

        return null;
    }

    public static Method getAccessibleMethodByName(Object obj, String methodName) {
        Assert.notNull(obj, "object can't be null");
        Assert.hasLength(methodName, "methodName can't be blank");

        for (Class searchType = obj.getClass(); searchType != Object.class; searchType = searchType.getSuperclass()) {
            Method[] methods = searchType.getDeclaredMethods();
            Method[] var7 = methods;
            int var6 = methods.length;

            for (int var5 = 0; var5 < var6; ++var5) {
                Method method = var7[var5];
                if (method.getName().equals(methodName)) {
                    makeAccessible(method);
                    return method;
                }
            }
        }

        return null;
    }

    public static void makeAccessible(Method method) {
        if ((!Modifier.isPublic(method.getModifiers()) || !Modifier.isPublic(method.getDeclaringClass().getModifiers())) && !method.isAccessible()) {
            method.setAccessible(true);
        }

    }

    public static void makeAccessible(Field field) {
        if ((!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers()) || Modifier.isFinal(field.getModifiers())) && !field.isAccessible()) {
            field.setAccessible(true);
        }

    }

    public static <T> Class<T> getClassGenricType(Class clazz) {
        return getClassGenricType(clazz, 0);
    }

    public static Class getClassGenricType(Class clazz, int index) {
        Type genType = clazz.getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            logger.warn(clazz.getSimpleName() + "\'s superclass not ParameterizedType");
            return Object.class;
        } else {
            Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
            if (index < params.length && index >= 0) {
                if (!(params[index] instanceof Class)) {
                    logger.warn(clazz.getSimpleName() + " not set the actual class on superclass generic parameter");
                    return Object.class;
                } else {
                    return (Class) params[index];
                }
            } else {
                logger.warn("Index: " + index + ", Size of " + clazz.getSimpleName() + "\'s Parameterized Type: " + params.length);
                return Object.class;
            }
        }
    }

    public static Class<?> getUserClass(Object instance) {
        Assert.notNull(instance, "Instance must not be null");
        Class clazz = instance.getClass();
        if (clazz != null && clazz.getName().contains("$$")) {
            Class superClass = clazz.getSuperclass();
            if (superClass != null && !Object.class.equals(superClass)) {
                return superClass;
            }
        }

        return clazz;
    }

    public static RuntimeException convertReflectionExceptionToUnchecked(Exception e) {
        return (RuntimeException) (!(e instanceof IllegalAccessException) && !(e instanceof IllegalArgumentException) && !(e instanceof NoSuchMethodException) ? (e instanceof InvocationTargetException ? new RuntimeException(((InvocationTargetException) e).getTargetException()) : (e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException("Unexpected Checked Exception.", e))) : new IllegalArgumentException(e));
    }
}

