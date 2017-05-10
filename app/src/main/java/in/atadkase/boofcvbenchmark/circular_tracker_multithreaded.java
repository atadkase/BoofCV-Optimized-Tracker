package in.atadkase.boofcvbenchmark;

/**
 * Created by shreedutt_ch on 5/10/17.
 */
import android.os.Process;

import boofcv.alg.tracker.circulant.state;

import boofcv.abst.feature.detect.peak.SearchLocalPeak;
import boofcv.abst.transform.fft.DiscreteFourierTransform;
import boofcv.alg.interpolate.InterpolatePixelS;
import boofcv.alg.misc.PixelMath;
import boofcv.alg.tracker.circulant.state;
import boofcv.alg.transform.fft.DiscreteFourierTransformOps;
import boofcv.factory.feature.detect.peak.FactorySearchLocalPeak;
import boofcv.misc.BoofMiscOps;
import boofcv.struct.image.GrayF64;
import boofcv.struct.image.ImageGray;
import boofcv.struct.image.InterleavedF64;
import georegression.struct.shapes.RectangleLength2D_F32;

import java.util.Random;

public class circular_tracker_multithreaded<T extends ImageGray<T>> {


    public state<T> initial_state;

    public circular_tracker_multithreaded(double output_sigma_factor, double sigma, double lambda, double interp_factor,
                                          double padding ,
                                          int workRegionSize ,
                                          double maxPixelValue,
                                          InterpolatePixelS<T> interp ) {
        if( workRegionSize < 3 )
            throw new IllegalArgumentException("Minimum size of work region is 3 pixels.");

        this.initial_state.output_sigma_factor = output_sigma_factor;
        this.initial_state.sigma = sigma;
        this.initial_state.lambda = lambda;
        this.initial_state.interp_factor = interp_factor;
        this.initial_state.maxPixelValue = maxPixelValue;
        this.initial_state.interp = interp;

        this.initial_state.padding = padding;
        this.initial_state.workRegionSize = workRegionSize;

        resizeImages(workRegionSize);
        computeCosineWindow(this.initial_state.cosine);
        computeGaussianWeights(this.initial_state.workRegionSize);

        this.initial_state.localPeak.setImage(this.initial_state.response);
    }

    class pipe1 implements Runnable {

        public state<T> loc_state;

        public void initialize_state(state<T> received_state){
            this.loc_state = received_state;
        }


        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);

            //Wait till there is an element in queue!

            //seperate into image and state.
            initialize_state(); //SEND enqueued state to update to local state.

            //perform the tasks of this pipe using the image and state.
            circ_tracker.get_subwindow(gray,circ_tracker.templateNew);






















            System.out.print("The new thread id is:");
            System.out.println(android.os.Process.getThreadPriority(android.os.Process.myTid()));
            System.out.println("This is from a different thread boys!!! going into spin loop!");

        }
    }
    class pipe2 implements Runnable {
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);

            System.out.print("The new thread id is:");
            System.out.println(android.os.Process.getThreadPriority(android.os.Process.myTid()));
            System.out.println("This is from a different thread boys!!! going into spin loop!");

        }
    }
    class pipe3 implements Runnable {
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);

            System.out.print("The new thread id is:");
            System.out.println(android.os.Process.getThreadPriority(android.os.Process.myTid()));
            System.out.println("This is from a different thread boys!!! going into spin loop!");

        }
    }
    class pipe4 implements Runnable {
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);

            System.out.print("The new thread id is:");
            System.out.println(android.os.Process.getThreadPriority(android.os.Process.myTid()));
            System.out.println("This is from a different thread boys!!! going into spin loop!");

        }
    }
    class pipe5 implements Runnable {
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);

            System.out.print("The new thread id is:");
            System.out.println(android.os.Process.getThreadPriority(android.os.Process.myTid()));
            System.out.println("This is from a different thread boys!!! going into spin loop!");

        }
    }
    class pipe6 implements Runnable {
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);

            System.out.print("The new thread id is:");
            System.out.println(android.os.Process.getThreadPriority(android.os.Process.myTid()));
            System.out.println("This is from a different thread boys!!! going into spin loop!");

        }
    }
    class pipe7 implements Runnable {
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);

            System.out.print("The new thread id is:");
            System.out.println(android.os.Process.getThreadPriority(android.os.Process.myTid()));
            System.out.println("This is from a different thread boys!!! going into spin loop!");

        }
    }


    public void resizeImages( int workRegionSize ) {
        this.initial_state.templateNew.reshape(workRegionSize, workRegionSize);
        this.initial_state.template.reshape(workRegionSize, workRegionSize);
        this.initial_state.cosine.reshape(workRegionSize,workRegionSize);
        this.initial_state.k.reshape(workRegionSize,workRegionSize);
        this.initial_state.kf.reshape(workRegionSize,workRegionSize);
        this.initial_state.alphaf.reshape(workRegionSize,workRegionSize);
        this.initial_state.newAlphaf.reshape(workRegionSize,workRegionSize);
        this.initial_state.response.reshape(workRegionSize,workRegionSize);
        this.initial_state.tmpReal0.reshape(workRegionSize,workRegionSize);
        this.initial_state.tmpReal1.reshape(workRegionSize,workRegionSize);
        this.initial_state.tmpFourier0.reshape(workRegionSize,workRegionSize);
        this.initial_state.tmpFourier1.reshape(workRegionSize,workRegionSize);
        this.initial_state.tmpFourier2.reshape(workRegionSize,workRegionSize);
        this.initial_state.gaussianWeight.reshape(workRegionSize,workRegionSize);
        this.initial_state.gaussianWeightDFT.reshape(workRegionSize,workRegionSize);
    }

    public static void computeCosineWindow( GrayF64 cosine ) {
        double cosX[] = new double[ cosine.width ];
        for( int x = 0; x < cosine.width; x++ ) {
            cosX[x] = 0.5*(1 - Math.cos( 2.0*Math.PI*x/(cosine.width-1) ));
        }
        for( int y = 0; y < cosine.height; y++ ) {
            int index = cosine.startIndex + y*cosine.stride;
            double cosY = 0.5*(1 - Math.cos( 2.0*Math.PI*y/(cosine.height-1) ));
            for( int x = 0; x < cosine.width; x++ ) {
                cosine.data[index++] = cosX[x]*cosY;
            }
        }
    }
    public void computeGaussianWeights( int width ) {
        // desired output (gaussian shaped), bandwidth proportional to target size
        double output_sigma = Math.sqrt(width*width) * this.initial_state.output_sigma_factor;

        double left = -0.5/(output_sigma*output_sigma);

        int radius = width/2;

        for( int y = 0; y < this.initial_state.gaussianWeight.height; y++ ) {
            int index = this.initial_state.gaussianWeight.startIndex + y*this.initial_state.gaussianWeight.stride;

            double ry = y-radius;

            for( int x = 0; x < width; x++ ) {
                double rx = x-radius;

                this.initial_state.gaussianWeight.data[index++] = Math.exp(left * (ry * ry + rx * rx));
            }
        }

        this.initial_state.fft.forward(this.initial_state.gaussianWeight,this.initial_state.gaussianWeightDFT);
    }


















}
