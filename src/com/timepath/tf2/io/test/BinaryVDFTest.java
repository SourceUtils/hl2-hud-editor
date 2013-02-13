package com.timepath.tf2.io.test;

import com.timepath.tf2.hudeditor.Utils;
import com.timepath.tf2.io.BinaryVDF;
import java.io.IOException;
import java.util.logging.Logger;

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

    private static final Logger LOG = Logger.getLogger(BinaryVDFTest.class.getName());
}