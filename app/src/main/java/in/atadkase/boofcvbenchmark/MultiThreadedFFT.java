package in.atadkase.boofcvbenchmark;

import org.jtransforms.fft.FloatFFT_2D;

import boofcv.abst.transform.fft.DiscreteFourierTransform;
import boofcv.alg.transform.fft.DiscreteFourierTransformOps;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.InterleavedF32;
import pl.edu.icm.jlargearrays.ConcurrencyUtils;

/**
 * Created by ashu on 5/11/17.
 */

public class MultiThreadedFFT
        implements DiscreteFourierTransform<GrayF32,InterleavedF32>
{
    // previous size of input image
    private int prevWidth = -1;
    private int prevHeight = -1;

    // performs the FFT
    private FloatFFT_2D alg;

    // storage for temporary results
    private InterleavedF32 tmp = new InterleavedF32(1,1,2);

    // if true then it can modify the input images
    private boolean modifyInputs = false;

    @Override
    public void forward(GrayF32 image, InterleavedF32 transform ) {
        DFTOpsMultithreaded.checkImageArguments(image,transform);
        if( image.isSubimage() || transform.isSubimage() )
            throw new IllegalArgumentException("Subimages are not supported");

        checkDeclareAlg(image);



        int N = image.width*image.height;
        System.arraycopy(image.data,0,transform.data,0,N);

        // the transform over writes the input data
        alg.realForwardFull(transform.data);
    }

    @Override
    public void inverse(InterleavedF32 transform, GrayF32 image ) {
        DFTOpsMultithreaded.checkImageArguments(image,transform);
        if( image.isSubimage() || transform.isSubimage() )
            throw new IllegalArgumentException("Subimages are not supported");

        checkDeclareAlg(image);

        // If he user lets us, modify the transform
        InterleavedF32 workImage;
        if(modifyInputs) {
            workImage = transform;
        } else {
            tmp.reshape(transform.width,transform.height);
            tmp.setTo(transform);
            workImage = tmp;
        }

        alg.complexInverse(workImage.data, true);

        // copy the real portion.  imaginary should be zeros
        int N = image.width*image.height;
        for( int i = 0; i < N; i++ ) {
            image.data[i] = workImage.data[i*2];
        }
    }

    /**
     * Declare the algorithm if the image size has changed
     */
    private void checkDeclareAlg(GrayF32 image) {
        if( prevWidth != image.width || prevHeight != image.height ) {
            prevWidth = image.width;
            prevHeight = image.height;
            alg = new FloatFFT_2D(image.height,image.width);
        }
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
