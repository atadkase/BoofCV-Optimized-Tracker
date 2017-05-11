package in.atadkase.boofcvbenchmark;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import georegression.struct.shapes.Quadrilateral_F32;

import java.text.SimpleDateFormat;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;


/**
 * Created by ashu on 5/10/17.
 */

public class FileIO {

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "verifyStoragePermissions: ");
        }
    }


    public void history_writer(List<Quadrilateral_F32> history, String timeStamp) {
        BufferedWriter out = null;
        try {
            FileWriter fstream = new FileWriter("/storage/emulated/0/imag/history." + timeStamp + ".txt", true);   // append to file
            out = new BufferedWriter(fstream);
            for (Quadrilateral_F32 history_loc : history) {
                out.write("a:" + history_loc.a.x + " " + history_loc.a.y + "\n" +
                        "b:" + history_loc.b.x + " " + history_loc.b.y + "\n" +
                        "c:" + history_loc.c.x + " " + history_loc.c.y + "\n" +
                        "d:" + history_loc.d.x + " " + history_loc.d.y + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception ex) {/*ignore*/}
            }
        }
    }

    public void summary_writer(double fpsVideo, double fpsRGB, double fpsTracker,int totalFaults, String timeStamp)
    {
        DecimalFormat numberFormat = new DecimalFormat("#.000000");
        BufferedWriter out = null;
        try {
            FileWriter fstream = new FileWriter("/storage/emulated/0/imag/summary.txt", true);   // append to file
            out = new BufferedWriter(fstream);
            String summaryString = timeStamp + " Video: " + numberFormat.format(fpsVideo)
                    + " RGB_GRAY: " + numberFormat.format(fpsRGB) + " Tracker: "
                    + numberFormat.format(fpsTracker) + " Faults: " +
                    totalFaults + "\n";
            out.write(summaryString);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception ex) {/*ignore*/}
            }

        }
    }
}
