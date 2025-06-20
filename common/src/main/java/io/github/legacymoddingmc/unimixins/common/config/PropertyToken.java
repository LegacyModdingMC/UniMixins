package io.github.legacymoddingmc.unimixins.common.config;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

public class PropertyToken {

    private List<String> comment;
    private String key;
    private String value;
    private boolean dirty;

    public PropertyToken(List<String> comment, String propertyString) {
        this.comment = ImmutableList.copyOf(comment);
        String[] pair = propertyString.split("=");
        if (pair.length != 2) {
            throw new RuntimeException();
        } else {
            key = pair[0];
            value = pair[1];
        }
    }

    public PropertyToken(List<String> comment, String key, String value) {
        this.comment = new ArrayList<>(comment);
        this.key = key;
        this.value = value;
    }

    public List<String> getComment() {
        return comment;
    }

    public void setComment(List<String> comment) {
        if(!comment.equals(this.comment)) {
            this.comment = ImmutableList.copyOf(comment);
            this.dirty = true;
        }
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        if(!key.equals(this.key)) {
            this.key = key;
            this.dirty = true;
        }
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        if(!value.equals(this.value)) {
            this.value = value;
            this.dirty = true;
        }
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty() {
        dirty = true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PropertyToken) {
            PropertyToken o = (PropertyToken) obj;
            return comment.equals(o.comment) && key.equals(o.key) && value.equals(o.value);
        }
        return super.equals(obj);
    }
}
