package io.github.legacymoddingmc.unimixins.common.config;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

class PropertyCollection {

    // We use a TreeMap so keys are automatically sorted
    private Map<String, PropertyToken> properties = new TreeMap<>();

    public static PropertyCollection fromFile(File file) {
        PropertyCollection coll = new PropertyCollection();
        coll.readPropertyTokens(file);
        return coll;
    }

    private void readPropertyTokens(File file) {
        List<String> comment = new ArrayList<>();
        String prop = "";

        List<String> keyOrder = new ArrayList<>();

        if (file.exists()) {
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

                        PropertyToken token = new PropertyToken(comment, prop);
                        properties.put(token.getKey(), token);

                        keyOrder.add(token.getKey());

                        comment.clear();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void writeToFile(File file) {
        writePropertyTokens(file);
    }

    private void writePropertyTokens(File file) {
        boolean dirty = properties.values().stream().anyMatch(PropertyToken::isDirty);

        if(dirty) {
            file.getParentFile().mkdirs();
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                for (Map.Entry<String, PropertyToken> e : properties.entrySet()) {
                    PropertyToken token = e.getValue();

                    for (String commentLine : token.getComment()) {
                        bw.write("# " + commentLine + "\n");
                    }
                    bw.write(token.getKey() + "=" + token.getValue() + "\n\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public PropertyToken get(String key) {
        return properties.get(key);
    }

    public void put(String key, PropertyToken token) {
        PropertyToken old = properties.get(key);
        if (!token.equals(old)) {
            properties.put(key, token);
            token.setDirty();
        }
    }
}
