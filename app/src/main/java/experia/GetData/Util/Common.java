package experia.GetData.Util;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * Created by Le Van Hoang on 2014/05/15.
 */
public class Common {

    public static String fileName = "log.txt";

    public static boolean writeToFile(String fileName, String text) {
        boolean result;
        try {
            File external = Environment.getExternalStorageDirectory();
            String sdcardPath = external.getPath();
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/logger/" + fileName);

            File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/logger");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter filewriter = new FileWriter(file, true);
            BufferedWriter out = new BufferedWriter(filewriter);

            out.write(text);

            out.close();
            filewriter.close();
            result = true;
        } catch (Exception e) {
            android.util.Log.d("failed to save file", e.toString());
            result = false;
        }
        return result;
    }
}
