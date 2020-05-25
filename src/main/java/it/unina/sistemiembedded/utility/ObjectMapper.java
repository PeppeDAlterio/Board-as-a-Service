package it.unina.sistemiembedded.utility;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generic object mapper using destination class's default values
 * @author Gisueppe D'Alterio
 */
public class ObjectMapper {

    private static final HashSet<String> GETTER_PREFIXES_SET = new HashSet<>(Arrays.asList("get", "is"));

    private static String SETTER_PREFIX = "set";

    /**
     * Configure prefixes in getter methods. Defaults are "get" and "is".
     * @param getterPrefixes Collection String getter prefixes, case sensitive
     */
    synchronized public static void configureGetterPrefixes(Collection<String> getterPrefixes) {
        GETTER_PREFIXES_SET.clear();
        for (String getterPrefix : getterPrefixes) {
            GETTER_PREFIXES_SET.add(getterPrefix.trim());
        }
    }

    /**
     * Configure prefix for setter methods. Default is "set"
     * @param setterPrefix String setter prefix, case sensitive
     */
    synchronized public static void configureSettersPrefix(String setterPrefix) {
        SETTER_PREFIX = setterPrefix.trim();
    }

    /**
     * Object mapper from a source instance to the destination instance.
     * Values passed as skipArgs will be skipped and destination's class default will be used instead.
     * Only fields with public getters and setters will be mapped.
     * @param source S source instance to map into another object
     * @param destinationInstance D instance to refresh data from source
     * @param skipArgs String[] list of args to skip while mapping. Please use the same name of the field (it's not case-sensitive anyway)
     * @param <S> Source class
     * @param <D> Destination class
     * @return D new instance of destination class with source data
     */
    public static <S, D> D mapOnInstance(S source, D destinationInstance, String ... skipArgs) {

        HashSet<String> skipSet = Arrays.stream(skipArgs)
                .map(String::toLowerCase)
                .collect(Collectors.toCollection(HashSet::new));

        for (Method method : source.getClass().getDeclaredMethods()) {

            String getterFieldName = null;


            for (String s : GETTER_PREFIXES_SET) {
                if(method.getName().startsWith(s)) {
                    getterFieldName = method.getName().substring(s.length());
                    break;
                }
            }

            // Not a getter
            if(getterFieldName == null) continue;

            // Field to skip
            if(skipSet.contains(getterFieldName.toLowerCase())) continue;

            try {
                Object getValue = method.invoke(source);
                if(getValue!=null) {
                    destinationInstance.getClass().getMethod(SETTER_PREFIX + getterFieldName, getValue.getClass())
                            .invoke(destinationInstance, method.invoke(source));
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalArgumentException("Incompatible source and destination: " + source.getClass() + ", " + destinationInstance.getClass());
            } catch (NoSuchMethodException e) {

                // Maybe using an interface? E.g. setSet is expecting a Set and not ad HashSet

                try {
                    destinationInstance.getClass().getMethod(SETTER_PREFIX + getterFieldName, method.getReturnType())
                            .invoke(destinationInstance, method.invoke(source));
                } catch (IllegalAccessException|InvocationTargetException e2) {
                    throw new IllegalArgumentException("Incompatible source and destination: " + source.getClass() + ", " + destinationInstance.getClass());
                } catch (NoSuchMethodException ignored) {}

            }


        }

        return destinationInstance;

    }

    /**
     * Object mapper from a source instance to a destination class new instance.
     * Values passed as skipArgs will be skipped and destination's class default will be used instead.
     * Destination class MUST have an empty constructor and only fields with public getters and setters will be mapped.
     * @param source S source instance to map into another object
     * @param destination Class destination class
     * @param skipArgs String[] list of args to skip while mapping. Please use the same name of the field (it's not case-sensitive anyway)
     * @param <S> Source class
     * @param <D> Destination class
     * @return D new instance of destination class with source data
     */
    public static <S, D> D map(S source, Class<D> destination, String ... skipArgs) {

        D mappedObject;

        try {
            mappedObject = destination.getConstructor().newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("No empty constructor in destination class: " + destination);
        }

        return mapOnInstance(source, mappedObject, skipArgs);

    }

    /**
     * Object mapper from a collection of source instances to a destination list of class new instances.
     * Values passed as skipArgs will be skipped and destination's class default will be used instead.
     * Destination class MUST have an empty constructor and only fields with public getters and setters will be mapped.
     * @param source Collection collection of source instances to map into destination list
     * @param destination Class destination class
     * @param skipArgs String[] list of args to skip while mapping. Please use the same name of the field (it's not case-sensitive anyway)
     * @param <S> Source class
     * @param <D> Destination class
     * @return List list with new instances of destination class from source data
     */
    public static <S, D> List<D> listMap(Collection<S> source, Class<D> destination, String ... skipArgs) {

        ArrayList<D> destinationList = new ArrayList<>(source.size());
        for (S s : source) {
            destinationList.add(map(s, destination, skipArgs));
        }

        return destinationList;

    }

    /**
     * Object mapper from a collection of source instances to a destination list of class new instances.
     * Values passed as skipArgs will be skipped and destination's class default will be used instead.
     * Destination class MUST have an empty constructor and only fields with public getters and setters will be mapped.
     * This implementation uses an HashSet
     * @param source Collection collection of source instances to map into destination set
     * @param destination Class destination class
     * @param skipArgs String[] list of args to skip while mapping. Please use the same name of the field (it's not case-sensitive anyway)
     * @param <S> Source class
     * @param <D> Destination class
     * @return Set set with new instances of destination class from source data
     */
    public static <S, D> Set<D> setMap(Collection<S> source, Class<D> destination, String ... skipArgs) {

        Set<D> destinationList = new HashSet<>(source.size());
        for (S s : source) {
            destinationList.add(map(s, destination, skipArgs));
        }

        return destinationList;

    }

}

