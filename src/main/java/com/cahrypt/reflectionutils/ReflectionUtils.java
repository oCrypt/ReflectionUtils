package com.cahrypt.reflectionutils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public final class ReflectionUtils {

    /**
     * Checks whether a field is static and final.
     * @param field The field to check.
     * @return Whether the field is static and final.
     */
    public static boolean isStaticFinal(Field field) {
        return field.toGenericString().contains("static final");
    }

    /**
     * Creates a new instance of a class.
     * @param clazz The class.
     * @param objects The class parameters.
     * @param <T> The type of the class.
     * @return The class instance.
     */
    public static <T> T newInstance(Class<T> clazz, Object... objects) {
        List<Class<?>> constructArgs = new ArrayList<>();

        for (Object arg : objects) {
            constructArgs.add(arg.getClass());
        }

        T object = null;

        try {
            object = clazz.getDeclaredConstructor(constructArgs.toArray(Class<?>[]::new)).newInstance(objects);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return object;
    }

    /**
     * Creates a new instance of a class.
     * @param clazz The class.
     * @param args The class parameters.
     * @param <T> The type of the class.
     * @return The class instance.
     */
    public static <T> T newInstance(Class<T> clazz, List<Object> args) {
        return newInstance(clazz, args.toArray());
    }

    /**
     * Creates a new instance of a class.
     * @param clazz The class.
     * @param <T> The type of the class.
     * @return The class instance.
     */
    public static <T> T newInstance(Class<T> clazz) {
        return newInstance(clazz, new Object[0]);
    }

    /**
     * Invokes an object's method.
     * @param method The method to invoke.
     * @param object The object to invoke the method on.
     * @param args The arguments to pass to the method.
     */
    public static void invoke(Method method, Object object, Object... args) {
        try {
            method.invoke(object, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Invokes an object's method.
     * @param method The method to invoke.
     * @param object The object to invoke the method on.
     * @param args The arguments to pass to the method.
     */
    public static void invoke(Method method, Object object, List<Object> args) {
        invoke(method, object, args.toArray());
    }

    /**
     * Invokes an object's method with no parameters.
     * @param method The method to invoke.
     * @param object The object to invoke the method on.
     */
    public static void invoke(Method method, Object object) {
        invoke(method, object, new Object[0]);
    }

    /**
     * Consumes all classes in a package.
     * @param classLoader The class loader to use.
     * @param packagePath The package path.
     * @param baseType The base type of the classes to consume.
     * @param consumer The consumer.
     * @param <T> The type of the classes to consume.
     */
    public static <T> void consumeClasses(ClassLoader classLoader, String packagePath, Class<T> baseType, Consumer<Class<? extends T>> consumer) {
        File packageFile = new File("src" + File.separator + "main" + File.separator + "java" + File.separator + packagePath.replace(".", File.separator));

        if (!packageFile.exists()) {
            throw new IllegalArgumentException("Package path does not exist: " + packagePath);
        }

        if (!packageFile.isDirectory()) {
            throw new IllegalArgumentException("Package path is not a directory: " + packagePath);
        }

        File[] classFiles = packageFile.listFiles();

        if (classFiles == null) {
            throw new IllegalArgumentException("Package path is empty: " + packagePath);
        }

        for (File classFile : classFiles) {
            String className = classFile.getName();

            if (!className.endsWith(".java")) {
                System.out.println("Skipping non-class file: " + className);
                continue;
            }

            className = className.substring(0, className.length() - 5);

            Class<?> clazz;

            try {
                clazz = Class.forName(packagePath + "." + className, true, classLoader);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                continue;
            }

            if (!baseType.isAssignableFrom(clazz)) {
                continue;
            }

            consumer.accept(clazz.asSubclass(baseType));
        }
    }

    /**
     * Consumes all classes in a package.
     * @param packagePath The package path.
     * @param baseType The base type of the classes to consume.
     * @param consumer The consumer.
     * @param <T> The type of the classes to consume.
     */
    public static <T> void consumeClasses(String packagePath, Class<T> baseType, Consumer<Class<? extends T>> consumer) {
        consumeClasses(ReflectionUtils.class.getClassLoader(), packagePath, baseType, consumer);
    }

    /**
     * Gets all classes in a package.
     * @param packagePath The package path.
     * @param baseType The base type of the classes to get.
     * @param <T> The type of the classes to get.
     * @return The classes.
     */
    public static <T> Set<Class<? extends T>> getClasses(String packagePath, Class<T> baseType) {
        Set<Class<? extends T>> classes = new HashSet<>();

        consumeClasses(packagePath, baseType, clazz -> classes.add(clazz));

        return classes;
    }

    /**
     * Creates instances of all classes in a package.
     * @param packagePath The package path.
     * @param baseType The base type of the classes to create instances of.
     * @param args The class parameters.
     */
    public static void createInstances(String packagePath, Class<?> baseType, List<Object> args) {
        consumeInstances(packagePath, baseType, object -> {}, args);
    }

    /**
     * Creates and consumes instances of all classes in a package.
     * @param packagePath The package path.
     * @param baseType The base type of the classes to create instances of.
     * @param consumer The consumer.
     * @param args The class parameters.
     * @param <T> The type of the classes to create instances of.
     */
    public static <T> void consumeInstances(String packagePath, Class<T> baseType, Consumer<T> consumer, List<Object> args) {
        consumeClasses(packagePath, baseType, clazz -> {
            T object = newInstance(clazz, args);
            consumer.accept(object);
        });
    }

    /**
     * Creates and gets instances of all classes in a package.
     * @param packagePath The package path.
     * @param baseType The base type of the classes to create instances of.
     * @param args The class parameters.
     * @param <T> The type of the classes to create instances of.
     * @return The instances.
     */
    public static <T> Set<T> getInstances(String packagePath, Class<T> baseType, List<Object> args) {
        Set<T> instances = new HashSet<>();

        consumeInstances(packagePath, baseType, instances::add, args);

        return instances;
    }
}
