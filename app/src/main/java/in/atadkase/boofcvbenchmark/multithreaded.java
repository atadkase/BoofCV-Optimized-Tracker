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

//    CirculantTrackerF32<T> circ_tracker;
//
//    public multithreaded(CirculantTrackerF32<T> tracker){
//
//        this.circ_tracker = tracker;
//
//    }

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


}
