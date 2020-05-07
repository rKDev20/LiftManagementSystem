package core;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {
    public static boolean verbose = false;

    public static void Log(String TAG, String message) {
        Log(TAG, message, false);
    }

    public static void Log(String TAG, String message, boolean verbose) {
        if (!Util.verbose||verbose)
            print(TAG,message);
    }
    private static void print(String TAG, String message){
        SimpleDateFormat dtf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        Date now = new Date();
        String time = dtf.format(now);
        System.out.println(time + "  " + TAG + " : " + message);
    }
}
