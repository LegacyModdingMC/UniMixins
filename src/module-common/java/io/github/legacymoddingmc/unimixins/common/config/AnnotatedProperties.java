package io.github.legacymoddingmc.unimixins.common.config;

import java.io.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.*;

public class AnnotatedProperties {
    public static void load(File file, Class<?> cls) {
        PropertyCollection properties = PropertyCollection.fromFile(file);

        try {
            for (Field f : cls.getFields()) {
                if (f.isAnnotationPresent(ConfigString.class)) {
                    ConfigString ann = f.getAnnotation(ConfigString.class);
                    List<String> comment = new ArrayList<>(Arrays.asList(ann.com().split("\n")));
                    String def = ann.def();
                    String cat = ann.cat();

                    comment.removeIf(s -> s.startsWith("[default:"));
                    comment.add("[default: " + def + "]");

                    String key = cat + "." + f.getName();

                    PropertyToken prop = properties.get(key);
                    if (prop == null) {
                        prop = new PropertyToken(comment, key, def);
                        prop.setDirty();
                    } else {
                        prop.setComment(comment);
                    }

                    setFieldValue(f, prop.getValue());

                    properties.put(key, prop);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        properties.writeToFile(file);
    }

    private static void setFieldValue(Field f, String value) throws Exception {
        if(f.getType() == boolean.class) {
            f.set(null, Boolean.parseBoolean(value));
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ConfigString {

        /** Default value */
        String def();
        /** Comment */
        String com() default "";
        /** Category (the property name gets prefixed with this) */
        String cat();

    }
}
