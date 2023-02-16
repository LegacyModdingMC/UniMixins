package io.github.legacymoddingmc.unimixins.common;

import java.io.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.*;

public class AnnotatedProperties {
    public static void load(File file, Class<?> cls) {
        Map<String, PropertyToken> propertyTokens = readPropertyTokens(file);

        try {
            for (Field f : cls.getFields()) {
                if (f.isAnnotationPresent(ConfigString.class)) {
                    ConfigString ann = f.getAnnotation(ConfigString.class);
                    List<String> comment = Arrays.asList(ann.com().split("\n"));
                    String def = ann.def();
                    String cat = ann.cat();

                    String key = cat + "." + f.getName();

                    PropertyToken prop = propertyTokens.get(key);
                    if (prop == null) {
                        prop = new PropertyToken(comment, key, def);
                    }

                    prop.defaultValue = def;

                    prop.comment.removeIf(s -> s.startsWith("[default:"));
                    prop.comment.add("[default: " + prop.defaultValue + "]");

                    setFieldValue(f, prop.value);

                    propertyTokens.put(key, prop);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        writePropertyTokens(file, propertyTokens);
    }

    private static void setFieldValue(Field f, String value) throws Exception {
        if(f.getType() == boolean.class) {
            f.set(null, Boolean.parseBoolean(value));
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private static void writePropertyTokens(File file, Map<String, PropertyToken> propertyTokens) {
        List<String> keys = new ArrayList<>();
        keys.addAll(propertyTokens.keySet());
        Collections.sort(keys);

        try(BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for(String k : keys) {
                PropertyToken token = propertyTokens.get(k);

                for(String commentLine : token.comment) {
                    bw.write("# " + commentLine + "\n");
                }
                bw.write(token.key + "=" + token.value + "\n\n");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static Map<String, PropertyToken> readPropertyTokens(File file) {
        List<String> comment = new ArrayList<>();
        String prop = "";

        Map<String, PropertyToken> tokens = new HashMap<>();

        if(file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                while (true) {
                    String line = br.readLine();
                    if (line == null) break;
                    if (line.isEmpty()) continue;

                    line = line.trim();

                    if (line.startsWith("#")) {
                        comment.add(line.substring(1).trim());
                    } else {
                        prop = line;

                        PropertyToken token = new PropertyToken(new ArrayList<>(comment), prop);
                        tokens.put(token.key, token);

                        comment.clear();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return tokens;
    }

    private static class PropertyToken {

        public List<String> comment;
        public String key;
        public String value;
        public String defaultValue;

        public PropertyToken(List<String> comment, String propertyString) {
            this.comment = comment;
            String[] pair = propertyString.split("=");
            if(pair.length != 2) {
                throw new RuntimeException();
            } else {
                key = pair[0];
                value = pair[1];
            }
        }

        public PropertyToken(List<String> comment, String key, String value) {
            this.comment = comment;
            this.key = key;
            this.value = value;
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
