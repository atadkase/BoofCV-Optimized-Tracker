package in.atadkase.boofcvbenchmark;


import boofcv.abst.tracker.ConfigCirculantTrackerFloat;

import boofcv.alg.interpolate.InterpolatePixelS;

import boofcv.alg.tracker.circulant.CirculantTrackerFloat;

import boofcv.core.image.border.BorderType;
import boofcv.factory.interpolate.FactoryInterpolation;

import boofcv.struct.image.ImageGray;


/**
 * Created by ashu on 5/10/17.
 */

public class CircTrackerObjectAlgs {

        public static <T extends ImageGray<T>>
        CirculantTrackerF32<T> circulantFloat(ConfigCirculantTrackerFloat config , Class<T> imageType) {
            if( config == null )
                config = new ConfigCirculantTrackerFloat();

            InterpolatePixelS<T> interp = FactoryInterpolation.bilinearPixelS(imageType, BorderType.EXTENDED);

            return new CirculantTrackerF32<>(
                    config.output_sigma_factor,config.sigma,config.lambda,config.interp_factor,
                    config.padding,
                    config.workSpace,
                    config.maxPixelValue,interp);
        }



}
