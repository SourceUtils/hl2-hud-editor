package com.timepath.util.file;

import java.io.File;
import java.util.logging.Logger;

/**
 *
 * @author timepath
 */
public class FileUtils {

    private static final Logger LOG = Logger.getLogger(FileUtils.class.getName());

    private FileUtils() {
    }

    public static void chmod777(File file) {
        file.setReadable(true, false);
        file.setWritable(true, false);
        file.setExecutable(true, false);
    }

}
