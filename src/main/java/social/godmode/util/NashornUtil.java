package social.godmode.util;

import lombok.experimental.UtilityClass;
import org.openjdk.nashorn.api.scripting.JSObject;
import social.godmode.nashorn.JavaScriptEngine;

import java.util.List;

@UtilityClass
public class NashornUtil {

    public static JSObject convertListToJSObject(JavaScriptEngine engine, List<?> list) {
        JSObject object = (JSObject) engine.eval("new Array()");

        for (int i = 0; i < list.size(); i++) object.setSlot(i, list.get(i));

        return object;
    }
}
