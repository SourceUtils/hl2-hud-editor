package com.timepath.tf2.hudedit.properties;

import java.io.File;

/**
 *
 * @author andrew
 */
public class HudFile {

    public final Object obj;

    public HudFile(File obj) {
        this.obj = obj;
    }

    @Override
    public String toString() {
        if(obj instanceof File) {
            return ((File) obj).getName();
        }
        return obj.getClass() + ":" + (String) obj;
    }

}