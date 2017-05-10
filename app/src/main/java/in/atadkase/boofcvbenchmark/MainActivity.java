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
import static boofcv.struct.image.ImageDataType.U8;


//Java imports

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.text.SimpleDateFormat;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import java.nio.ByteBuffer;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

//BoofCV imports
import boofcv.abst.tracker.TrackerObjectQuadFloat;
import boofcv.core.encoding.ConvertNV21;
import boofcv.core.image.ConvertImage;
import boofcv.struct.image.GrayI8;
import boofcv.struct.image.InterleavedU8;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;
import georegression.struct.shapes.Quadrilateral_F32;
import georegression.struct.shapes.Quadrilateral_F64;
import boofcv.abst.tracker.TrackerObjectQuad;
import boofcv.factory.tracker.FactoryTrackerObjectQuad;



public class MainActivity extends Activity {
    String SrcPath = "/storage/emulated/0/imag/wildcat_robot.mp4";
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("nativeOCL");
        System.loadLibrary("NE10_test_demo");
    }

    public native String NE10RunTest();

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


    public static void convert(Frame input, InterleavedU8 output, boolean swapOrder) {
        output.setNumBands(input.imageChannels);
        output.reshape(input.imageWidth, input.imageHeight);

        int N = output.width * output.height * output.numBands;

        ByteBuffer buffer = (ByteBuffer) input.image[0];
        if (buffer.limit() != N) {
            throw new IllegalArgumentException("Unexpected buffer size. " + buffer.limit() + " vs " + N);
        }

        buffer.position(0);
        buffer.get(output.data, 0, N);

        if (input.imageChannels == 3 && swapOrder) {
            swapRgbBands(output.data, output.width, output.height, output.numBands);
        }
    }


    public static void swapRgbBands(byte[] data, int width, int height, int numBands) {

        int N = width * height * numBands;

        if (numBands == 3) {
            for (int i = 0; i < N; i += 3) {
                int k = i + 2;

                byte r = data[i];
                data[i] = data[k];
                data[k] = r;
            }
        } else {
            throw new IllegalArgumentException("Support more bands");
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);
        TextView tv = (TextView) findViewById(R.id.sample_text);
        //tv.setText(stringFromJNI());

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
                    convert(frame, interleaved, true);   //convert frame to interleavedU8
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
            //**************************************************************************
            //**************************************************************************
            //**************************************************************************
            //Done with processing, now write the summary file!.

            System.out.println("Finished the processing!!!!!******************************************************************************************************************");

            double fps_Video = totalFrames / (totalVideo * 1e-9);
            double fps_RGB_GRAY = totalFrames / (totalRGB_GRAY * 1e-9);
            double fps_Tracker = totalFrames / (totalTracker * 1e-9);
            String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
            System.out.printf("Summary: video %6.1f RGB_GRAY %6.1f Tracker %6.1f  Faults %d\n",
                    fps_Video, fps_RGB_GRAY, fps_Tracker, totalFaults);

            BufferedWriter out = null;
            try {
                FileWriter fstream = new FileWriter("/storage/emulated/0/imag/summary.txt", true);   // append to file
                out = new BufferedWriter(fstream);
                String summaryString = timeStamp + " Video: " + numberFormat.format(fps_Video)
                        + " RGB_GRAY: " + numberFormat.format(fps_RGB_GRAY) + " Tracker: "
                        + numberFormat.format(fps_Tracker) + " Faults: " +
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
            //**************************************************************************
            //**************************************************************************
            //**************************************************************************
            //**************************************************************************
            //Save history to a file!!!
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

            Log.d("[FRAMES]", "Frames = " + counter);

//            tracker.update_section_times();
//            double section1_time = tracker.getSection1_time();
//            double section2_time = tracker.getSection2_time();
//            double section3_time = tracker.getSection3_time();
//            double section4_time = tracker.getSection4_time();
//            double section5_time = tracker.getSection5_time();
//            double section6_time = tracker.getSection6_time();
//
//            double section1_FPS = totalFrames/(section1_time*1e-9);
//            double section2_FPS = totalFrames/(section2_time*1e-9);
//            double section3_FPS = totalFrames/(section3_time*1e-9);
//            double section4_FPS = totalFrames/(section4_time*1e-9);
//            double section5_FPS = totalFrames/(section5_time*1e-9);
//            double section6_FPS = totalFrames/(section6_time*1e-9);
//
//
//            Log.i("[FPS]","Section 1 FPS="+totalFrames/(section1_time*1e-9));
//            Log.i("[FPS]","Section 2 FPS="+totalFrames/(section2_time*1e-9));
//            Log.i("[FPS]","Section 3 FPS="+totalFrames/(section3_time*1e-9));
//            Log.i("[FPS]","Section 4 FPS="+totalFrames/(section4_time*1e-9));
//            Log.i("[FPS]","Section 5 FPS="+totalFrames/(section5_time*1e-9));
//            Log.i("[FPS]","Section 6 FPS="+totalFrames/(section6_time*1e-9));
//
//
//
//            Log.i("[FPS-Time]","Section 1 Time="+1/section1_FPS);
//            Log.i("[FPS-Time]","Section 2 Time="+1/section2_FPS);
//            Log.i("[FPS-Time]","Section 3 Time="+1/section3_FPS);
//            Log.i("[FPS-Time]","Section 4 Time="+1/section4_FPS);
//            Log.i("[FPS-Time]","Section 5 Time="+1/section5_FPS);
//            Log.i("[FPS-Time]","Section 6 Time="+1/section6_FPS);
//
//            Log.i("[FPS-TIME]","Tracker Time="+1/fps_Tracker);

        } catch (Exception exception) {
            Log.e("1", "Grabber Exception");
        }

        initOpenCL(getOpenCLProgram());
        Log.i("OpenCL", "OpenCL Working! Congrats!");
        shutdownOpenCL();
        Log.i("AndroidBasic", "Exiting backgroundThread");



    }

    public native String stringFromJNI();

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

    private native void initOpenCL(String openCLProgramText);

    private native void shutdownOpenCL();

//    public void benchmarkNV21() {
//        byte data[] = new byte[1024*1024*3];
//        //GrayU8 gray = new GrayU8(1024,1024);
//        Planar<GrayU8> gray= new Planar<GrayU8>(GrayU8.class,1024,1024,3);
//        for(int i = 0; i<1024*1024*3; i++)
//        {
//            data[i] = (byte)(i%255);
//        }
//        double totalTime =0;
//        double time0 = System.nanoTime();  //Start the first timer
//        for (long i = 0; i < 1000; i++) {
//
//            ConvertNV21.nv21ToMsRgb_U8(data, 1024, 1024,gray);
//        }
//        double time1 = System.nanoTime();  //Start the first timer
//        totalTime = time1 - time0;
//        double fps_Gray = 1000 / (totalTime * 1e-9);
//        System.out.println("FPS of NV21->Gray "+ fps_Gray);
//
//    }

}
