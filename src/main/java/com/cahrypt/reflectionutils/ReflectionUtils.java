package com.cahrypt.reflectionutils;

import java.io.File;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class ReflectionUtils {

    /**
     * Get all the fields of a class matching a predicate.
     * @param clazz The class to get the fields from.
     * @param predicate The predicate to match the fields against.
     * @return A set of fields matching the predicate.
     */
    public static Set<Field> getAllFields(Class<?> clazz, Predicate<Field> predicate) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(predicate)
                .collect(Collectors.toSet());
    }

    /**
     * Get all the methods of a class matching a predicate.
     * @param clazz The class to get the methods from.
     * @param predicate The predicate to match the methods against.
     * @return A set of methods matching the predicate.
     */
    public static Set<Method> getAllMethods(Class<?> clazz, Predicate<Method> predicate) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(predicate)
                .collect(Collectors.toSet());
    }

    /**
     * Get all the constructors of a class matching a predicate.
     * @param clazz The class to get the constructors from.
     * @param predicate The predicate to match the constructors against.
     * @return A set of constructors matching the predicate.
     */
    public static Set<Constructor<?>> getAllConstructors(Class<?> clazz, Predicate<Constructor<?>> predicate) {
        return Arrays.stream(clazz.getDeclaredConstructors())
                .filter(predicate)
                .collect(Collectors.toSet());
    }

    /**
     * Get all the fields of a class.
     * @param clazz The class to get the fields from.
     * @return A set of fields.
     */
    public static Set<Field> getAllFields(Class<?> clazz) {
        return getAllFields(clazz, field -> true);
    }

    /**
     * Get all the methods of a class.
     * @param clazz The class to get the methods from.
     * @return A set of methods.
     */
    public static Set<Method> getAllMethods(Class<?> clazz) {
        return getAllMethods(clazz, method -> true);
    }

    /**
     * Get all the constructors of a class.
     * @param clazz The class to get the constructors from.
     * @return A set of constructors.
     */
    public static Set<Constructor<?>> getAllConstructors(Class<?> clazz) {
        return getAllConstructors(clazz, constructor -> true);
    }

    /**
     * Consume a field which is ensured to be accessible to the accessor.
     * @param obj The object to consume.
     * @param accessor The object accessing the field.
     * @param consumer The consumer.
     */
    public static <T extends AccessibleObject> void consumeAccessibleObject(T obj, Object accessor, Consumer<T> consumer) {
        boolean accessible = obj.canAccess(accessor);

        if (!accessible) {
            obj.setAccessible(true);
        }

        consumer.accept(obj);

        if (!accessible) {
            obj.setAccessible(false);
        }
    }

    /**
     * Consume a field which is ensured to be accessible to the accessor.
     * @param fieldName The field name.
     * @param clazz The class where the field is situated.
     * @param accessor The object accessing the field.
     * @param consumer The consumer.
     */
    public static void consumeAccessibleField(String fieldName, Class<?> clazz, Object accessor, Consumer<Field> consumer) {
        Field field;

        try {
            field = clazz.getField(fieldName);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return;
        }

        consumeAccessibleObject(field, accessor, consumer);
    }

    /**
     * Checks whether a member is static and final.
     * @param member The member to check.
     * @return Whether the member is static and final.
     */
    public static boolean isStaticFinal(Member member) {
        return isStatic(member) && isFinal(member);
    }

    /**
     * Checks whether a member is public.
     * @param member The member to check.
     * @return Whether the member is public.
     */
    public static boolean isPublic(Member member) {
        return hasModifier(member, Modifier.PUBLIC);
    }

    /**
     * Checks whether a member is protected.
     * @param member The member to check.
     * @return Whether the member is protected.
     */
    public static boolean isProtected(Member member) {
        return hasModifier(member, Modifier.PROTECTED);
    }

    /**
     * Checks whether a member is private.
     * @param member The member to check.
     * @return Whether the member is private.
     */
    public static boolean isPrivate(Member member) {
        return hasModifier(member, Modifier.PRIVATE);
    }

    /**
     * Checks whether a member is static.
     * @param member The member to check.
     * @return Whether the member is static.
     */
    public static boolean isStatic(Member member) {
        return hasModifier(member, Modifier.STATIC);
    }

    /**
     * Checks whether a member is final.
     * @param member The member to check.
     * @return Whether the member is final.
     */
    public static boolean isFinal(Member member) {
        return hasModifier(member, Modifier.FINAL);
    }

    /**
     * Checks whether a member has a specific name prefix.
     * @param member The member to check.
     * @param prefix The prefix to check for.
     * @return Whether the member has the prefix.
     */
    public static boolean hasPrefix(String prefix, Member member) {
        return member.getName().startsWith(prefix);
    }

    /**
     * Checks whether a member has a specific modifier.
     * @param member The member to check.
     * @param modifier The modifier to check for (in string form).
     * @return Whether the member has the modifier.
     */
    public static boolean hasModifier(Member member, String modifier) {
        return getModifierString(member).contains(modifier);
    }

    /**
     * Checks whether a member has a specific modifier.
     * @param member The member to check.
     * @param modifier The modifier to check for (in integer form).
     * @return Whether the member has the modifier.
     */
    public static boolean hasModifier(Member member, int modifier) {
        return hasModifier(member, Modifier.toString(modifier));
    }

    /**
     * Gets the modifier string of a member.
     * @param member The member to get the modifier string from.
     * @return The modifier string.
     */
    public static String getModifierString(Member member) {
        return Modifier.toString(member.getModifiers());
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
