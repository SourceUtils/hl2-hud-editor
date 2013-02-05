package com.timepath.tf2.loaders.test;

import com.timepath.tf2.loaders.BinaryVDF;
import com.timepath.tf2.hudeditor.util.Utils;
import java.io.IOException;

/**
 *
 * @author timepath
 */
public class BinaryVDFTest {
    
    public static void main(String... args) throws IOException {
//        System.out.println(new BinaryVDF(Utils.locateSteamAppsDirectory() + "../appcache/packageinfo.vdf"));
        BinaryVDF bvdf = new BinaryVDF(Utils.locateSteamAppsDirectory() + "../appcache/appinfo.vdf");
        System.out.println(bvdf);
    }
    
}