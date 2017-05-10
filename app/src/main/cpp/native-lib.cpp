#include <jni.h>
#include <string>

#include <stdio.h>
#include <stdlib.h>

#include "NE10.h"
#include "test_funcs.h"
#include "unit_test_common.h"

extern "C"
JNIEXPORT jstring JNICALL
Java_in_atadkase_boofcvbenchmark_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
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