package uz.audio.helpers;

import android.content.Context;
import android.util.Log;

/**
 * Created by Ankit on 9/13/2016.
 */
public class L{

    private static Context context;
    private static L mInstance;

    public L(Context context) {
        this.context = context;
    }

    public static L getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new L(context);
        }
        return mInstance;
    }

    public void dumpLog(String line){
//
//        File dest_dir = new File(Constants.DOWNLOAD_FILE_DIR+"/");
//        File dest_file = new File(dest_dir, "log.txt");
//
//            Log.d("Dump", "Writing " + line);
//            FileWriter fw = null;
//            try {
//                fw = new FileWriter(dest_file.getAbsoluteFile(), true);
//                BufferedWriter bw = new BufferedWriter(fw);
//                bw.write(line);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }


    }


    public static void m(String dbHelper, String s) {
        Log.d(dbHelper,s);
    }
}
