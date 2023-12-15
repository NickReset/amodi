package social.godmode.util;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;

@UtilityClass
public class ReflexUtil {

    public static Object getField(String name, Object from) {
        try {
            Field field = from.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(from);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
