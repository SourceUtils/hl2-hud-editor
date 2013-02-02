package com.timepath.tf2.hudeditor.loaders.test;

import com.timepath.tf2.hudeditor.loaders.BinaryVDF;
import com.timepath.tf2.hudeditor.util.Utils;
import java.io.IOException;

/**
 *
 * @author timepath
 */
public class BinaryVDFTest {
    
    public static void main(String... args) throws IOException {
        System.out.println(new BinaryVDF(Utils.locateSteamAppsDirectory() + "../appcache/appinfo.vdf"));
//        System.out.println(new BinaryVDF(Utils.locateSteamAppsDirectory() + "../appcache/packageinfo.vdf"));
    }
    
}