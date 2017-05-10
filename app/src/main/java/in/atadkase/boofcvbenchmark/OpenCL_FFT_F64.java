package in.atadkase.boofcvbenchmark;

import boofcv.abst.transform.fft.DiscreteFourierTransform;
import boofcv.alg.transform.fft.DiscreteFourierTransformOps;
import boofcv.alg.transform.fft.GeneralPurposeFFT_F64_2D;
import boofcv.struct.image.GrayF64;
import boofcv.struct.image.InterleavedF64;

/**
 * Created by ashu on 5/10/17.
 */

public class OpenCL_FFT_F64 implements DiscreteFourierTransform<GrayF64,InterleavedF64> {

    // previous size of input image
    private int prevWidth = -1;
    private int prevHeight = -1;

    // performs the FFT
    private GeneralPurposeFFT_F64_2D alg;

    // storage for temporary results
    private InterleavedF64 tmp = new InterleavedF64(1,1,2);

    // if true then it can modify the input images
    private boolean modifyInputs = false;


    @Override
    public void forward(GrayF64 image, InterleavedF64 transform ) {

    }

    @Override
    public void inverse(InterleavedF64 transform, GrayF64 image ) {

    }

    /**
     * Declare the algorithm if the image size has changed
     */
    private void checkDeclareAlg(GrayF64 image) {

    }

    @Override
    public void setModifyInputs(boolean modify) {
        this.modifyInputs = modify;
    }

    @Override
    public boolean isModifyInputs() {
        return modifyInputs;
    }
}
