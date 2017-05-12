#include <jni.h>
#include <string>
#include <omp.h>

#include <stdio.h>
#include <stdlib.h>

#include "NE10.h"
#include "test_funcs.h"
#include "unit_test_common.h"


float imgDP(float* x);

extern "C"
JNIEXPORT void JNICALL
Java_in_atadkase_boofcvbenchmark_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    //std::string hello = "Hello from C++";
    return ;//env->NewStringUTF(hello.c_str());
}


extern "C"
JNIEXPORT jstring JNICALL
Java_in_atadkase_boofcvbenchmark_MainActivity_NE10RunTest(JNIEnv *env,
                                                jobject thiz)
{
    static int test_count = 0;

    void (*test_funcs[])(void) = {test_abs, test_addc, test_add, test_divc,
                                  test_div, test_dot, test_len, test_mlac,
                                  test_mla, test_mulc, test_mul, test_normalize,
                                  test_rsbc, test_setc, test_subc, test_sub,
                                  test_addmat, test_detmat, test_identitymat,
                                  test_invmat, test_mulmat, test_mulcmatvec,
                                  test_submat, test_transmat,
                                  test_fir, test_fir_decimate,
                                  test_fir_interpolate, test_fir_lattice,
                                  test_fir_sparse, test_iir_lattice};

    while (test_count < (sizeof(test_funcs) / sizeof(void (*)(void)))) {
        /* ne10_log_buffer is a global buffer which contain test's result
         * ne10_log_buffer's position need be setup first
         */
        ne10_log_buffer_ptr = ne10_log_buffer;
        *ne10_log_buffer_ptr++ = '[';

        (*test_funcs[test_count])();
        ++test_count;

        /* ne10_log_buffer_ptr is updated in test_funcs */
        --ne10_log_buffer_ptr;
        *ne10_log_buffer_ptr = ']';

        return ((env)->NewStringUTF(ne10_log_buffer));
    }
    return NULL;
}


extern "C"
JNIEXPORT float JNICALL
Java_in_atadkase_boofcvbenchmark_CirculantTrackerF32_IDP(JNIEnv *env,jobject callingObject, jfloatArray Gray, jint height, jint width)
{
    float sum = 0;
    int N = height*width;
    float* data = env->GetFloatArrayElements(Gray, 0);
#pragma omp parallel for reduction(+ : sum)
    for(int i=0; i<N; ++i)
    {
        sum += data[i]*data[i];
    }
    return sum;
}

float* xf, *yf, *xy, *xyf, *tempReal1;

int currentWorkRegionSize = -1;


extern "C"
JNIEXPORT void JNICALL
Java_in_atadkase_boofcvbenchmark_CirculantTrackerF32_InitDGK(int workRegionSize)
{
//        xf = new float[workRegionSize][workRegionSize][2];
//        yf = new float[workRegionSize][workRegionSize][2];
//        xyf = new float[workRegionSize][workRegionSize][2];
//        xy = new float[workRegionSize][workRegionSize];
//        tempReal1 = new float[workRegionSize][workRegionSize];
//        currentWorkRegionSize = workRegionSize;
}
extern "C"
JNIEXPORT jfloatArray JNICALL
Java_in_atadkase_boofcvbenchmark_CirculantTrackerF32_DGK(JNIEnv *env,jobject callingObject, jfloat sigma,
jfloatArray xptr, jfloatArray yptr, jint xwidth, jint xheight, jint ywidth, jint yheight, jint workRegionSize)
{
//    float * x = env->GetFloatArrayElements(xptr, 0);
//    float * y = env->GetFloatArrayElements(yptr,0);
//    int xnumElements = xwidth*xheight;
//    int ynumElements = ywidth*yheight;
//
//    //fft_fwd(x,xf, xnumElements);
//    float xx = imgDP(x, xnumElements);
//
//    //fft_fwd(y, yf, ynumElements);
//    float yy = imgDP(y, ynumElements);


    // cross-correlation term in Fourier domain
    //elementMultConjB(xf,yf,xyf);
    // convert to spatial domain
    //fft.inverse(xyf,xy);
    //circshift(xy,tmpReal1);

    // calculate gaussian response for all positions
    //gaussianKernel(xx, yy, tmpReal1, sigma, k);





//    env->ReleaseFloatArrayElements(xptr, x, 0);
//    env->ReleaseFloatArrayElements(yptr, y, 0);
}


float imgDP(float* x, int N)
{
    float sum = 0;
    #pragma omp parallel for reduction(+ : sum)
    for(int i=0; i<N; ++i)
    {
        sum += x[i]*x[i];
    }
    return sum;
}


//static void circshift(float* a, float* b, int w2, int h2 ) {
//    int w2 = a.width/2;
//    int h2 = b.height/2;
//
//    for( int y = 0; y < a.height; y++ ) {
//        int yy = (y+h2)%a.height;
//
//        for( int x = 0; x < a.width; x++ ) {
//            int xx = (x+w2)%a.width;
//
//            b.set( xx , yy , a.get(x,y));
//        }
//    }
//
//}
void circshift(float* a, float* b , int aheight, int awidth, int bheight, int astart, int bstart, int astride, int bstride);
void gaussianKernel(float xx , float yy , float* xy , int xywidth, int xyheight, int xystride,
                    int xystart, float sigma  , float* output );

//
extern "C"
JNIEXPORT jfloatArray JNICALL
Java_in_atadkase_boofcvbenchmark_CirculantTrackerF32_LearningFused(JNIEnv *env,jobject callingObject, jfloatArray xyptr, jint xyheight, jint xywidth, jint tmpreal1width,
                                                         jfloat xx, jfloat yy, jfloat sigma, jint workRegionSize,jint xystartindex, jint tempstartindex, jint xystride, jint tempstride)
{

    float sum = 0;
    float* data = env->GetFloatArrayElements(xyptr, 0);
    int N = (int)xyheight*(int)xywidth;
    float* temp = (float*)malloc(sizeof(float)*N);
    float out[N];
    jfloatArray result=env->NewFloatArray(N);
    circshift(data, temp, (int)xyheight, (int)xywidth, (int)tmpreal1width, (int)xystartindex,
              (int)tempstartindex, (int)xystride, (int)tempstride);
    gaussianKernel((float)xx, (float)yy, temp, (int)xywidth, (int)xyheight, (int)tempstride,
                   (int)tempstartindex, (float)sigma, out);
    (*env).SetFloatArrayRegion(result, 0, N, out);
    free(temp);
    return result;

}

 void circshift(float* a, float* b , int aheight, int awidth, int bheight, int astart, int bstart, int astride, int bstride) {


    int w2 = awidth/2;
    int h2 = bheight/2;
#pragma omp parallel for
    for( int y = 0; y < aheight; y++ ) {
        int yy = (y+h2)%aheight;
        #pragma omp parallel for
        for( int x = 0; x < awidth; x++ ) {
            int xx = (x+w2)%awidth;

            b[bstart+xx +bstride*yy] = a[astart+x+astride*y];
        }
    }

}


void gaussianKernel(float xx , float yy , float* xy , int xywidth, int xyheight, int xystride,
                    int xystart, float sigma  , float* output ) {
    float sigma2 = sigma*sigma;
    float N = xywidth*xyheight;
#pragma omp parallel for
    for( int y = 0; y < xyheight; y++ ) {
        int index = xystart + y*xystride;
    #pragma omp parallel for
        for( int x = 0; x < xywidth; x++) {

            // (xx + yy - 2 * xy) / numel(x)
            float value = (xx + yy - 2*xy[index])/N;

            float v =(float) exp(- fmaxf(0, value) / sigma2);

            output[index] = v;
            index++;
        }
    }
}