package com.timepath.tf2.hudedit.util;

/**
 *
 * @author andrew
 */
public class Property {

    private String key;
    private String value;
    private String info;
    
    public Property() {
    	
    }

    public Property(String key, String value, String info) {
        this.key = key;
        this.value = value;
        this.info = info;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    public void setValue(Object value) {
        this.setValue(value.toString());
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return key + ":" + value + ":" + info;
    }

}