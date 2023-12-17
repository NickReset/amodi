package social.godmode.util;

import lombok.experimental.UtilityClass;
import social.godmode.script.JavaScriptEngine;

import java.util.List;

@UtilityClass
public class ScriptUtil {

    public static Object toJSList(JavaScriptEngine engine, List<?> list) {
        return engine.getContext().asValue(list);
    }
}
