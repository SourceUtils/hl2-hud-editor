package com.timepath.tf2.loaders.test;

import com.timepath.tf2.loaders.Blob;
import java.io.File;

/**
 *
 * @author timepath
 */
public class BlobTest {
    
    public static void main(String... args) {
        System.out.println(new Blob(new File("/home/timepath/.local/share/Steam/AppUpdateStats.blob")).toString());
//        System.out.println(new Blob(new File("/home/timepath/.local/share/Steam/ClientRegistry.blob")).toString());
    }
    
}
