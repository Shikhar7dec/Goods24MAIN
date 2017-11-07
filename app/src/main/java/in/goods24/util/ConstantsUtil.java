package in.goods24.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.annotation.Immutable;

/**
 * Created by Shikhar on 10/21/2017.
 */

public class ConstantsUtil {
    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final int WIDTH = 512;
    public static final int HEIGHT = 1024;
    public static final int IMGDIVFACTOR = 128;
    public static String tempImgFilePath ="/storage/emulated/0/temp.webp";
    public static final Map<String,String> USERMAP = createMap();
    private static Map<String,String> createMap(){
        Map<String,String> result =new HashMap<>();
        result.put("1","Admin");
        result.put("2","SMUser");
        result.put("3","Runner");
        result.put("4","Distributor");
        result.put("5","User");
        return Collections.unmodifiableMap(result);
    }
}
