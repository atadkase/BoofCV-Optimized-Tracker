package in.atadkase.boofcvbenchmark;

import org.bytedeco.javacv.Frame;

import java.nio.ByteBuffer;

import boofcv.struct.image.InterleavedU8;

/**
 * Created by ashu on 5/10/17.
 */

public class Frame_Converter {

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

}
