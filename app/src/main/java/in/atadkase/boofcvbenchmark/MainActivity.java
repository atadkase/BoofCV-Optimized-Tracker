package in.atadkase.boofcvbenchmark;

//Android imports
import android.Manifest;
import android.app.Activity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.support.v4.app.ActivityCompat;
import android.widget.TextView;

import static android.content.ContentValues.TAG;



//Java imports

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.text.SimpleDateFormat;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;


import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

//BoofCV imports
import boofcv.abst.tracker.TrackerObjectQuadFloat;

import boofcv.core.image.ConvertImage;

import boofcv.struct.image.InterleavedU8;
import boofcv.struct.image.GrayU8;
import georegression.struct.shapes.Rectangle2D_F32;
import georegression.struct.shapes.RectangleLength2D_F32;
import georegression.struct.shapes.Quadrilateral_F32;

import boofcv.abst.tracker.TrackerObjectQuad;
import boofcv.factory.tracker.FactoryTrackerObjectQuad;



public class MainActivity extends Activity {
    String SrcPath = "/storage/emulated/0/imag/wildcat_robot.mp4";
    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("nativeOCL");
        System.loadLibrary("NE10_test_demo");
    }
    public native String NE10RunTest();
    public native String stringFromJNI();
    private native void initOpenCL(String openCLProgramText);
    private native void shutdownOpenCL();








    @Override
    protected void onCreate(Bundle savedInstanceState) {

        FileIO FIO = new FileIO();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FIO.verifyStoragePermissions(this);
        TextView tv = (TextView) findViewById(R.id.sample_text);
        //tv.setText(stringFromJNI());

        Frame_Converter fc = new Frame_Converter();

        tv.setText(NE10RunTest());



        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(SrcPath);
        try {
            grabber.start();
            long time_vid = grabber.getLengthInTime();
            Log.d("[TIME_VID]", "Time is " + time_vid);
            Frame frame = new Frame();

            int imageWidth = grabber.getImageWidth();
            int imageHeight = grabber.getImageHeight();

            FrameGrabber.ImageMode imageFormat = grabber.getImageMode();
            int numBands = 1;
            if (imageFormat == FrameGrabber.ImageMode.COLOR) {
                numBands = 3;
            }

            GrayU8 gray = new GrayU8(1, 1);
            InterleavedU8 interleaved = new InterleavedU8(imageWidth, imageHeight, numBands);
            Quadrilateral_F32 location = new Quadrilateral_F32(211.0f, 162.0f, 326.0f, 153.0f, 335.0f, 258.0f, 215.0f, 249.0f);
            TrackerObjectQuadFloat<GrayU8> tracker = FactoryTrackerObjectQuad.circulantFloat(null, GrayU8.class);
            DecimalFormat numberFormat = new DecimalFormat("#.000000");
            List<Quadrilateral_F32> history = new ArrayList<>();

            long totalVideo = 0;
            long totalRGB_GRAY = 0;
            long totalTracker = 0;
            int totalFaults = 0;
            int totalFrames = 0;
            boolean visible = false;
            long counter = 0;
            long time0, time1, time2, time3;


            gray.reshape(imageWidth, imageHeight);

            for (long i = 0; i < grabber.getLengthInFrames(); i++) {
                counter++;
                time0 = System.nanoTime();  //Start the first timer

                try {
                    frame = grabber.grabImage();
                    if (frame == null)
                        break;
                } catch (Exception e) {
                    Log.e("EXCEPTION", "Grab image exception");
                }

                time1 = System.nanoTime();   //Frame Grabbed checkpoint

                try {
                    fc.convert(frame, interleaved, true);   //convert frame to interleavedU8
                } catch (Exception e) {
                    Log.e("EXCEPTION", "Convert exception", e);
                }

                ConvertImage.average(interleaved, gray);  //Convert interleaved to gray

                time2 = System.nanoTime();  //Frame conversion to BoofCV checkpoint

                if (i == 0) {   //Initializer code
                    tracker.initialize(gray, location);
                } else {
                    visible = tracker.process(gray, location);
                }

                time3 = System.nanoTime();   //Processing done checkpoint

                history.add(location.copy());
                totalVideo += time1 - time0;
                totalRGB_GRAY += time2 - time1;
                totalTracker += time3 - time2;

                totalFrames++;
                if (!visible)
                    totalFaults++;

            }
            grabber.stop();

            //**************************************************************************
            //Done with processing, now write the summary file!.

            System.out.println("Finished the processing!!!!!******************************************************************************************************************");

            double fps_Video = totalFrames / (totalVideo * 1e-9);
            double fps_RGB_GRAY = totalFrames / (totalRGB_GRAY * 1e-9);
            double fps_Tracker = totalFrames / (totalTracker * 1e-9);
            String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
            System.out.printf("Summary: video %6.1f RGB_GRAY %6.1f Tracker %6.1f  Faults %d\n",
                    fps_Video, fps_RGB_GRAY, fps_Tracker, totalFaults);

            FIO.summary_writer(fps_Video,fps_RGB_GRAY,fps_Tracker,totalFaults,timeStamp);

            //**************************************************************************
            //Save history to a file!!!

            FIO.history_writer(history, timeStamp);


            Log.d("[FRAMES]", "Frames = " + counter);

        } catch (Exception exception) {
            Log.e("1", "Grabber Exception");
        }

        initOpenCL(getOpenCLProgram());
        Log.i("OpenCL", "OpenCL Working! Congrats!");
        shutdownOpenCL();
        Log.i("AndroidBasic", "Exiting backgroundThread");



    }



    private String getOpenCLProgram() {
        /* OpenCL program text is stored in a separate file in
         * assets directory. Here you need to load it as a single
         * string.
         *
         * In fact, the program may be directly built into
         * native source code where OpenCL API is used,
         * it is useful for short kernels (few lines) because it doesn't
         * involve loading code and you don't need to pass it from Java to
         * native side.
         */

        try {
            StringBuilder buffer = new StringBuilder();
            InputStream stream = getAssets().open("VectorAdd.cl");
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String s;

            while ((s = reader.readLine()) != null) {
                buffer.append(s);
                buffer.append("\n");
            }

            reader.close();
            return buffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";

    }





}
