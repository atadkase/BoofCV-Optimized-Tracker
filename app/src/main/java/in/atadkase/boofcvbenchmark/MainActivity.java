package in.atadkase.boofcvbenchmark;

//Android imports
import android.Manifest;
import android.app.Activity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.support.v4.app.ActivityCompat;


import static android.content.ContentValues.TAG;


//Java imports

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
import boofcv.alg.tracker.circulant.CirculantTracker;
import boofcv.factory.tracker.FactoryTrackerObjectAlgs;

import boofcv.core.image.ConvertImage;
import boofcv.struct.image.InterleavedU8;
import boofcv.struct.image.GrayU8;
import georegression.geometry.UtilPolygons2D_F64;
import georegression.struct.shapes.Quadrilateral_F64;
import boofcv.abst.tracker.TrackerObjectQuad;
import boofcv.factory.tracker.FactoryTrackerObjectQuad;
import georegression.struct.shapes.Rectangle2D_F64;
import georegression.struct.shapes.RectangleLength2D_F32;


public class MainActivity extends Activity {
    String SrcPath="/storage/emulated/0/wildcat_robot.mp4";
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

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

        if (permission!= PackageManager.PERMISSION_GRANTED)
        {
            Log.d(TAG, "verifyStoragePermissions: ");
        }
    }


    public static void convert(Frame input , InterleavedU8 output , boolean swapOrder ) {
        output.setNumBands(input.imageChannels);
        output.reshape(input.imageWidth,input.imageHeight);

        int N = output.width*output.height*output.numBands;

        ByteBuffer buffer = (ByteBuffer)input.image[0];
        if( buffer.limit() != N ) {
            throw new IllegalArgumentException("Unexpected buffer size. "+buffer.limit()+" vs "+N);
        }

        buffer.position(0);
        buffer.get(output.data,0,N);

        if( input.imageChannels == 3 && swapOrder ) {
            swapRgbBands(output.data,output.width,output.height,output.numBands);
        }
    }


    public static void swapRgbBands( byte []data, int width , int height , int numBands ) {

        int N = width*height*numBands;

        if( numBands == 3  ) {
            for (int i = 0; i < N; i+=3) {
                int k = i+2;

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
        //setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(SrcPath);
        System.out.println("No problem here!!!");
//        try
//        {
        try {
            grabber.start();
        }catch (Exception exception)
        {
            Log.e("1", "Grabber Exception");
        }
            long time_vid = grabber.getLengthInTime();
            Log.d("[TIME_VID]", "Time is "+ time_vid);
            Frame frame = new Frame();

            int imageWidth = grabber.getImageWidth();
            int imageHeight = grabber.getImageHeight();

            FrameGrabber.ImageMode imageFormat = grabber.getImageMode();
            int numBands=1;
            if(imageFormat == FrameGrabber.ImageMode.COLOR)
            {
                numBands = 3;
            }

            GrayU8 gray = new GrayU8(1,1);
            InterleavedU8 interleaved = new InterleavedU8(imageWidth, imageHeight, numBands);
            Quadrilateral_F64 location = new Quadrilateral_F64(211.0,162.0,326.0,153.0,335.0,258.0,215.0,249.0);
            TrackerObjectQuad<GrayU8> tracker = FactoryTrackerObjectQuad.circulant(null, GrayU8.class);
            DecimalFormat numberFormat = new DecimalFormat("#.000000");
            List<Quadrilateral_F64> history = new ArrayList<>();

            CirculantTracker<GrayU8> circ_tracker = FactoryTrackerObjectAlgs.circulant(null,GrayU8.class);

            long totalVideo=0;
            long totalRGB_GRAY = 0;
            long totalTracker = 0;
            int totalFaults = 0;
            int totalFrames = 0;
            boolean visible = false;
            long counter = 0;
            long time0,time1,time2,time3;


            gray.reshape(imageWidth,imageHeight);
            System.out.println("No problem here2!!!");


            for(long i = 0; i<grabber.getLengthInFrames(); i++)
            {
                counter++;
                time0 = System.nanoTime();  //Start the first timer

                try {
                    frame = grabber.grabImage();

                    if(frame== null)
                        break;
                }catch (Exception e)
                {
                    Log.e("EXCEPTION","Grab image exception");
                }

                time1 = System.nanoTime();   //Frame Grabbed checkpoint

                try {
                    convert(frame, interleaved, false);   //convert frame to interleavedU8
                }catch (Exception e)
                {
                    Log.e("EXCEPTION","Convert exception" ,e);
                }

                ConvertImage.average(interleaved,gray);  //Convert interleaved to gray

                time2 = System.nanoTime();  //Frame conversion to BoofCV checkpoint

                if(i==0){   //Initializer code


                   //tracker.initialize(gray, location);

                    //****************************************

                    Rectangle2D_F64 rect = new Rectangle2D_F64();
                    UtilPolygons2D_F64.bounding(location, rect);

                    int width = (int)(rect.p1.x - rect.p0.x);
                    int height = (int)(rect.p1.y - rect.p0.y);

                    circ_tracker.initialize(gray,(int)rect.p0.x,(int)rect.p0.y,width,height);

                }
                else{

                    //****************************************START OF CODE IN UI************************************
                    tracker.process(gray,location);
                    //********************************************************************************

                    circ_tracker.performTracking(gray);

                    //*******************************************************************************


                        RectangleLength2D_F32 r = circ_tracker.getTargetLocation();

                        if( r.x0 >= gray.width || r.y0 >= gray.height )
                            visible = false;
                        else if( r.x0+r.width < 0 || r.y0+r.height < 0 )
                            visible = false;
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

                //System.out.println("No problem here3!!!");
                time3 = System.nanoTime();   //Processing done checkpoint

                history.add( location.copy() );
                totalVideo += time1-time0;
                totalRGB_GRAY += time2-time1;
                totalTracker += time3-time2;

                totalFrames++;
                if( !visible )
                    totalFaults++;

            }
            try {
                grabber.stop();
            }catch (Exception exception)
        {
            Log.e("1", "Grabber Exception");
        }
            //**************************************************************************
            //**************************************************************************
            //**************************************************************************
            //**************************************************************************
            //Done with processing, now write the summary file!.

        System.out.println("No problem here4!!");

            System.out.println("Finished the processing!!!!!******************************************************************************************************************");

            double fps_Video = totalFrames/(totalVideo*1e-9);
            double fps_RGB_GRAY = totalFrames/(totalRGB_GRAY*1e-9);
            double fps_Tracker = totalFrames/(totalTracker*1e-9);
            String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
            System.out.printf("Summary: video %6.1f RGB_GRAY %6.1f Tracker %6.1f  Faults %d\n",
                    fps_Video,fps_RGB_GRAY,fps_Tracker,totalFaults);

            BufferedWriter out = null;
            try
            {
                FileWriter fstream = new FileWriter("/storage/emulated/0/summary.txt", true);   // append to file
                out = new BufferedWriter(fstream);
                String summaryString = timeStamp+ " Video: "+ numberFormat.format(fps_Video)
                        +" RGB_GRAY: "+numberFormat.format(fps_RGB_GRAY)+ " Tracker: "
                        + numberFormat.format(fps_Tracker)+ " Faults: "+
                        totalFaults+"\n";
                out.write(summaryString);
            }
            catch (IOException e)
            {
                System.err.println("Error: " + e.getMessage());
            }
            finally
            {
                if(out != null) {
                    try {
                        out.close();
                    }
                    catch (Exception ex) {/*ignore*/}
                }

            }
            //**************************************************************************
            //**************************************************************************
            //**************************************************************************
            //**************************************************************************
            //Save history to a file!!!
            try
            {
                FileWriter fstream = new FileWriter("/storage/emulated/0/history."+timeStamp+".txt", true);   // append to file
                out = new BufferedWriter(fstream);
                for( Quadrilateral_F64 history_loc : history ) {
                    out.write("a:"+history_loc.a.x+" "+history_loc.a.y+"\n"+
                            "b:"+history_loc.b.x+" "+history_loc.b.y+"\n"+
                            "c:"+history_loc.c.x+" "+history_loc.c.y+"\n"+
                            "d:"+history_loc.d.x+" "+history_loc.d.y+"\n");
                }
            }
            catch (IOException e)
            {
                System.err.println("Error: " + e.getMessage());
            }
            finally
            {
                if(out != null) {
                    try {
                        out.close();
                    }
                    catch (Exception ex) {/*ignore*/}
                }

            }

            Log.d("[FRAMES]", "Frames = "+ counter);
//
//        }catch (Exception exception)
//        {
//            Log.e("1", "Grabber Exception");
//        }

    }

}
