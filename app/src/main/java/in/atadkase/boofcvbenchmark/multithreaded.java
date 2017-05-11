package in.atadkase.boofcvbenchmark;

import android.os.Process;


import boofcv.alg.misc.PixelMath;
import boofcv.misc.BoofMiscOps;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayU8;

import boofcv.struct.image.ImageGray;

/**
 * Created by shreedutt_ch on 5/11/17.
 */

public class multithreaded<T extends ImageGray<T>>{

    public CirculantTrackerF32<T> circ_tracker;

    public multithreaded(CirculantTrackerF32<T> tracker){

        this.circ_tracker = tracker;

    }

    public int num_Threads=7;

    public class speedup_code1 implements Runnable {

        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);

            int thread_id = (android.os.Process.myTid()) % (num_Threads);
            System.out.println(thread_id);
        }
    }
    public void execute() {

        Thread a = new Thread(new multithreaded.speedup_code1());
        Thread b = new Thread(new multithreaded.speedup_code1());
        Thread c = new Thread(new speedup_code1());
        Thread d = new Thread(new speedup_code1());
        Thread et = new Thread(new speedup_code1());
        Thread f = new Thread(new speedup_code1());
        Thread g = new Thread(new speedup_code1());

        a.start();
        b.start();
        c.start();
        d.start();
        et.start();
        f.start();
        g.start();


    }
    public void get_subwindow_multi(T image , GrayF32 output ) {

        // copy the target region

        circ_tracker.interp.setImage(image);
        int index = 0;



        for( int y = 0; y < circ_tracker.workRegionSize; y++ ) {
            float yy = circ_tracker.regionTrack.y0 + y*circ_tracker.stepY;

            for( int x = 0; x < circ_tracker.workRegionSize; x++ ) {
                float xx = circ_tracker.regionTrack.x0 + x*circ_tracker.stepX;

                if( circ_tracker.interp.isInFastBounds(xx,yy))
                    output.data[index++] = circ_tracker.interp.get_fast(xx,yy);
                else if( BoofMiscOps.checkInside(image, xx, yy))
                    output.data[index++] = circ_tracker.interp.get(xx, yy);
                else {
                    // randomize to make pixels outside the image poorly correlate.  It will then focus on matching
                    // what's inside the image since it has structure
                    output.data[index++] = circ_tracker.rand.nextFloat()*circ_tracker.maxPixelValue;
                }
            }
        }

        // normalize values to be from -0.5 to 0.5
        PixelMath.divide(output, circ_tracker.maxPixelValue, output);
        PixelMath.plus(output, -0.5f, output);
        // apply the cosine window to it
        PixelMath.multiply(output,circ_tracker.cosine,output);
    }




}
