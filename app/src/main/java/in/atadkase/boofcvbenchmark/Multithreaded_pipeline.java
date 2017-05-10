package in.atadkase.boofcvbenchmark;

import android.os.Process;

import boofcv.alg.tracker.circulant.state;
import boofcv.struct.image.ImageGray;

/**
 * Created by shreedutt_ch on 5/10/17.
 */

public class Multithreaded_pipeline<T extends ImageGray<T>> {




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






}
