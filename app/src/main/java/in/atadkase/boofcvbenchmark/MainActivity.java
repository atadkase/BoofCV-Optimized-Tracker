package in.atadkase.boofcvbenchmark;

//Android imports
import android.Manifest;
import android.app.Activity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.Type;
import android.util.Log;
import android.support.v4.app.ActivityCompat;
import android.widget.TextView;

import static android.content.ContentValues.TAG;
import static android.renderscript.Element.I32;
import static android.renderscript.Type.createX;


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

import boofcv.alg.tracker.circulant.CirculantTrackerFloat;
import boofcv.core.image.ConvertImage;

import boofcv.factory.tracker.FactoryTrackerObjectAlgs;
import boofcv.struct.image.InterleavedU8;
import boofcv.struct.image.GrayU8;
import georegression.geometry.UtilPolygons2D_F32;
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
    public native void stringFromJNI();
    private native void initOpenCL(String openCLProgramText);
    private native void shutdownOpenCL();
//    private Allocation mInAllocation;
//    private Allocation mOutAllocation;
//    private RenderScript mRS;
//    private ScriptC_test tscript;
//
//    int[] a_array = {0,1,2,3,4,5,6,7,8,9};
//    int[] b_array = {9,8,7,6,5,4,3,2,1,0};
//    int[] c_array = new int[10];






    @Override
    protected void onCreate(Bundle savedInstanceState) {

        FileIO FIO = new FileIO();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FIO.verifyStoragePermissions(this);
        TextView tv = (TextView) findViewById(R.id.sample_text);
        //tv.setText(stringFromJNI());

        long timeJ = System.nanoTime();
        stringFromJNI();
        long timeN = System.nanoTime();
        System.out.println("Time required is: "+(timeN-timeJ)*1e-6);
        Frame_Converter fc = new Frame_Converter();

        tv.setText(NE10RunTest());

        //createScript();

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
            //TrackerObjectQuadFloat<GrayU8> tracker = FactoryTrackerObjectQuad.circulantFloat(null, GrayU8.class);
            List<Quadrilateral_F32> history = new ArrayList<>();


            CirculantTrackerF32<GrayU8> circTracker = CircTrackerObjectAlgs.circulantFloat(null, GrayU8.class);

            //circTracker.CreateScriptIDP(imageHeight*imageWidth,this);

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
                    Rectangle2D_F32 rect = new Rectangle2D_F32();
                    UtilPolygons2D_F32.bounding(location, rect);
                    int width = (int)(rect.p1.x-rect.p0.x);
                    int height = (int)(rect.p1.y - rect.p0.y);
                    circTracker.initialize(gray,(int)rect.p0.x, (int)rect.p0.y, width, height);
                    //tracker.initialize(gray, location);
                } else {
                    //visible = tracker.process(gray, location);
                    // Expanding Tracker.process here
                    circTracker.performTracking(gray);
                    RectangleLength2D_F32 r = circTracker.getTargetLocation();

                    if( r.x0 >= gray.width || r.y0 >= gray.height ) {
                        visible= false;
                    }
                    else if( r.x0+r.width < 0 || r.y0+r.height < 0 ) {
                        visible = false;
                    }
                    else {
                        float x0 = r.x0;
                        float y0 = r.y0;
                        float x1 = r.x0 + r.width;
                        float y1 = r.y0 + r.height;

                        location.a.x = x0;
                        location.a.y = y0;
                        location.b.x = x1;
                        location.b.y = y0;
                        location.c.x = x1;
                        location.c.y = y1;
                        location.d.x = x0;
                        location.d.y = y1;
                        visible = true;
                    }
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
            initOpenCL(getOpenCLProgram());
            Log.i("OpenCL", "OpenCL Working! Congrats!");


            circTracker.runFFT();
                    shutdownOpenCL();
            Log.i("AndroidBasic", "Exiting backgroundThread");

        } catch (Exception exception) {
            Log.e("1", "Grabber Exception"+exception);
        }





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
            InputStream stream = getAssets().open("FFT2D.cl");
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

//    public void createScript()
//    {
//        System.out.println("RenderScript Started !@%^&*#@%#^@&!(%#^@#%&^@%!*&%*@!#%^!#(");
//        mRS = RenderScript.create(this);
//
//        Type takla = createX(mRS, I32(mRS), 1);
//        mInAllocation = Allocation.createSized(mRS,I32(mRS),10);
//        mOutAllocation = Allocation.createSized(mRS,I32(mRS),10);
//        mInAllocation.copy1DRangeFrom(0,10,a_array);
//        mOutAllocation.copy1DRangeFrom(0,10,b_array);
//        tscript = new ScriptC_test(mRS);
//
//        tscript.forEach_root(mInAllocation,mOutAllocation);
//        mOutAllocation.copy1DRangeTo(0,10,c_array);
//
//        System.out.println("Taklu Haiwan Jindabaad");
//        for(int i=0;i<10; i++)
//            System.out.println(c_array[i]);
//    }




}
