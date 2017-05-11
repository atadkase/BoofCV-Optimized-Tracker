package in.atadkase.boofcvbenchmark;

import boofcv.core.encoding.ConvertNV21;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;

/**
 * Created by ashu on 5/10/17.
 */

public class benchMarkNV21 {

        public void runbenchmarkNV21() {
        byte data[] = new byte[1024*1024*3];
        Planar<GrayU8> gray= new Planar<GrayU8>(GrayU8.class,1024,1024,3);
        for(int i = 0; i<1024*1024*3; i++)
        {
            data[i] = (byte)(i%255);
        }
        double totalTime =0;
        double time0 = System.nanoTime();  //Start the first timer
        for (long i = 0; i < 1000; i++) {

            ConvertNV21.nv21TPlanarRgb_U8(data, 1024, 1024,gray);
        }
        double time1 = System.nanoTime();  //Start the first timer
        totalTime = time1 - time0;
        double fps_Gray = 1000 / (totalTime * 1e-9);
        System.out.println("FPS of NV21->Gray "+ fps_Gray);

    }
}
