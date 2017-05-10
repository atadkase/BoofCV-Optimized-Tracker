package in.atadkase.boofcvbenchmark;

/**
 * Created by shreedutt_ch on 5/10/17.
 */

public class Multithread_test implements Runnable{

        public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        System.out.print("The new thread id is:");
        System.out.println(android.os.Process.getThreadPriority(android.os.Process.myTid()));
        System.out.println("This is from a different thread boys!!! going into spin loop!");

    }

}
