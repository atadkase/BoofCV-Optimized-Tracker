//
// Created by ashu on 5/7/17.
//


#include <jni.h>
#include <string>
#include <android/log.h>
#include <cstring>
#include <sstream>
#include <vector>
#include <cassert>
#include <sys/time.h>
#include <math.h>
#include <cl_platform.h>
#include "../includes/cl.h"
#include "../includes/cl_platform.h"

// Commonly-defined shortcuts for LogCat output from native C applications.
#define  LOG_TAG    "AndroidBasic"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)



extern "C"
JNIEXPORT jstring JNICALL
Java_in_atadkase_boofcvbenchmark_MainActivity_string1FromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}


/* Container for all OpenCL-specific objects used in the sample.
 *
 * The container consists of the following parts:
 *   - Regular OpenCL objects, used in almost each
 *     OpenCL application.
 *   - Specific OpenCL objects - buffers, used in this
 *     particular sample.
 *
 * For convenience, collect all objects in one structure.
 * Avoid global variables and make easier the process of passing
 * all arguments in functions.
 */
struct OpenCLObjects
{
    // Regular OpenCL objects:
    cl_platform_id platform;
    cl_device_id device;
    cl_context context;
    cl_command_queue queue;
    cl_program program;
    cl_kernel kernel;

    // Objects that are specific for this sample.
    bool isInputBufferInitialized;
    cl_mem inputBuffer;
    cl_mem outputBuffer;
};

// Hold all OpenCL objects.
OpenCLObjects openCLObjects;


/* This function helps to create informative messages in
 * case when OpenCL errors occur. The function returns a string
 * representation for an OpenCL error code.
 * For example, "CL_DEVICE_NOT_FOUND" instead of "-1".
 */
const char* opencl_error_to_str (cl_int error)
{
#define CASE_CL_CONSTANT(NAME) case NAME: return #NAME;

    // Suppose that no combinations are possible.
    switch(error)
    {
        CASE_CL_CONSTANT(CL_SUCCESS)
        CASE_CL_CONSTANT(CL_DEVICE_NOT_FOUND)
        CASE_CL_CONSTANT(CL_DEVICE_NOT_AVAILABLE)
        CASE_CL_CONSTANT(CL_COMPILER_NOT_AVAILABLE)
        CASE_CL_CONSTANT(CL_MEM_OBJECT_ALLOCATION_FAILURE)
        CASE_CL_CONSTANT(CL_OUT_OF_RESOURCES)
        CASE_CL_CONSTANT(CL_OUT_OF_HOST_MEMORY)
        CASE_CL_CONSTANT(CL_PROFILING_INFO_NOT_AVAILABLE)
        CASE_CL_CONSTANT(CL_MEM_COPY_OVERLAP)
        CASE_CL_CONSTANT(CL_IMAGE_FORMAT_MISMATCH)
        CASE_CL_CONSTANT(CL_IMAGE_FORMAT_NOT_SUPPORTED)
        CASE_CL_CONSTANT(CL_BUILD_PROGRAM_FAILURE)
        CASE_CL_CONSTANT(CL_MAP_FAILURE)
        CASE_CL_CONSTANT(CL_MISALIGNED_SUB_BUFFER_OFFSET)
        CASE_CL_CONSTANT(CL_EXEC_STATUS_ERROR_FOR_EVENTS_IN_WAIT_LIST)
        CASE_CL_CONSTANT(CL_INVALID_VALUE)
        CASE_CL_CONSTANT(CL_INVALID_DEVICE_TYPE)
        CASE_CL_CONSTANT(CL_INVALID_PLATFORM)
        CASE_CL_CONSTANT(CL_INVALID_DEVICE)
        CASE_CL_CONSTANT(CL_INVALID_CONTEXT)
        CASE_CL_CONSTANT(CL_INVALID_QUEUE_PROPERTIES)
        CASE_CL_CONSTANT(CL_INVALID_COMMAND_QUEUE)
        CASE_CL_CONSTANT(CL_INVALID_HOST_PTR)
        CASE_CL_CONSTANT(CL_INVALID_MEM_OBJECT)
        CASE_CL_CONSTANT(CL_INVALID_IMAGE_FORMAT_DESCRIPTOR)
        CASE_CL_CONSTANT(CL_INVALID_IMAGE_SIZE)
        CASE_CL_CONSTANT(CL_INVALID_SAMPLER)
        CASE_CL_CONSTANT(CL_INVALID_BINARY)
        CASE_CL_CONSTANT(CL_INVALID_BUILD_OPTIONS)
        CASE_CL_CONSTANT(CL_INVALID_PROGRAM)
        CASE_CL_CONSTANT(CL_INVALID_PROGRAM_EXECUTABLE)
        CASE_CL_CONSTANT(CL_INVALID_KERNEL_NAME)
        CASE_CL_CONSTANT(CL_INVALID_KERNEL_DEFINITION)
        CASE_CL_CONSTANT(CL_INVALID_KERNEL)
        CASE_CL_CONSTANT(CL_INVALID_ARG_INDEX)
        CASE_CL_CONSTANT(CL_INVALID_ARG_VALUE)
        CASE_CL_CONSTANT(CL_INVALID_ARG_SIZE)
        CASE_CL_CONSTANT(CL_INVALID_KERNEL_ARGS)
        CASE_CL_CONSTANT(CL_INVALID_WORK_DIMENSION)
        CASE_CL_CONSTANT(CL_INVALID_WORK_GROUP_SIZE)
        CASE_CL_CONSTANT(CL_INVALID_WORK_ITEM_SIZE)
        CASE_CL_CONSTANT(CL_INVALID_GLOBAL_OFFSET)
        CASE_CL_CONSTANT(CL_INVALID_EVENT_WAIT_LIST)
        CASE_CL_CONSTANT(CL_INVALID_EVENT)
        CASE_CL_CONSTANT(CL_INVALID_OPERATION)
        CASE_CL_CONSTANT(CL_INVALID_GL_OBJECT)
        CASE_CL_CONSTANT(CL_INVALID_BUFFER_SIZE)
        CASE_CL_CONSTANT(CL_INVALID_MIP_LEVEL)
        CASE_CL_CONSTANT(CL_INVALID_GLOBAL_WORK_SIZE)
        CASE_CL_CONSTANT(CL_INVALID_PROPERTY)

        default:
            return "UNKNOWN ERROR CODE";
    }

#undef CASE_CL_CONSTANT
}

/* The following macro is used after each OpenCL call
 * to check if OpenCL error occurs. In the case when ERR != CL_SUCCESS
 * the macro forms an error message with OpenCL error code mnemonic,
 * puts it to LogCat, and returns from a caller function.
 *
 * The approach helps to implement consistent error handling tactics
 * because it is important to catch OpenCL errors as soon as
 * possible to avoid missing the origin of the problem.
 *
 * You may chose a different way to do that. The macro is
 * simple and context-specific as it assumes you use it in a function
 * that doesn't have a return value, so it just returns in the end.
 */
#define SAMPLE_CHECK_ERRORS(ERR)                                                      \
    if(ERR != CL_SUCCESS)                                                             \
    {                                                                                 \
        LOGE                                                                          \
        (                                                                             \
            "OpenCL error with code %s happened in file %s at line %d. Exiting.\n",   \
            opencl_error_to_str(ERR), __FILE__, __LINE__                              \
        );                                                                            \
                                                                                      \
        return;                                                                       \
    }





void initOpenCL
        (
                JNIEnv* env,
                jobject thisObject,
                jstring openCLProgramText,
                cl_device_type primary_device_type,    // try to use this device type first
                cl_device_type secondary_device_type,  // use this device type if primary one is unavailable
                OpenCLObjects& openCLObjects
        )
{
    /*
     * This function picks and creates all necessary OpenCL objects
     * to be used at each filter iteration. The objects are:
     * OpenCL platform, device, context, command queue, program,
     * and kernel.
     *
     * Almost all of these steps need to be performed in all
     * OpenCL applications before the actual compute kernel calls
     * are performed.
     *
     * For convenience, in this application all basic OpenCL objects
     * are stored in the OpenCLObjects structure,
     * so, this function populates fields of this structure,
     * which is passed as parameter openCLObjects.
     * Consider reviewing the fields before going further.
     * The structure definition is in the beginning of this file.
     */

    using namespace std;

    // Will be used at each effect iteration,
    // and means that you haven't yet initialized
    // the inputBuffer object.
    openCLObjects.isInputBufferInitialized = false;

    // The following variable stores return codes for all OpenCL calls.
    // In the code it is used with the SAMPLE_CHECK_ERRORS macro defined
    // before this function.
    cl_int err = CL_SUCCESS;

    /* -----------------------------------------------------------------------
     * Step 1: Query for all available OpenCL platforms on the system.
     * Enumerate all platforms and pick one based on its name or
     * device type it contains.
     */

    cl_uint num_of_platforms = 0;
    // Get total number of the available platforms.
    err = clGetPlatformIDs(0, 0, &num_of_platforms);
    SAMPLE_CHECK_ERRORS(err);
    LOGD("Number of available platforms: %u", num_of_platforms);

    vector<cl_platform_id> platforms(num_of_platforms);
    // Get IDs for all platforms.
    err = clGetPlatformIDs(num_of_platforms, &platforms[0], 0);
    SAMPLE_CHECK_ERRORS(err);

    // Two ways of selecting of an OpenCL platform are implemented.
    // The first way is searching for the platform by name. It is useful
    // if you target for a specific OpenCL implementation. To enable
    // this way, assign non-empty string to the following variable
    // required_platform_subname, which defines sub-string for platform
    // name matching:
    string required_platform_subname = ""; // e.g. assign to "Intel"

    // If the string above is empty, the second way of platform selection
    // works instead of searching by name.
    // This way enumerates all platforms and for each platform, it searches
    // for devices of specific types (primary_device_type and
    // secondary_device_type). The resulting platform IDs will be assigned to
    // the following variables (-1 means not found):
    int
            primary_platform_index = num_of_platforms,        // for primary_device_type
            secondary_platform_index = num_of_platforms;        // for secondary_device_type


    LOGD("OpenCL platform names:");

    cl_uint selected_platform_index = num_of_platforms;

    for(cl_uint i = 0; i < num_of_platforms; ++i)
    {
        // Get the length for the i-th platform name.
        size_t platform_name_length = 0;
        err = clGetPlatformInfo(
                platforms[i],
                CL_PLATFORM_NAME,
                0,
                0,
                &platform_name_length
        );
        SAMPLE_CHECK_ERRORS(err);

        // Get the name itself for the i-th platform.
        vector<char> platform_name_buffer(platform_name_length);
        err = clGetPlatformInfo(
                platforms[i],
                CL_PLATFORM_NAME,
                platform_name_length,
                &platform_name_buffer[0],
                0
        );
        SAMPLE_CHECK_ERRORS(err);

        string platform_name = &platform_name_buffer[0];
        string selection_marker;    // additional message will be printed to log

        if(!required_platform_subname.empty())
        {
            // The fist way of platform selection: by name.

            // Decide if the found i-th platform is the required one.
            // In this example only the first match is selected,
            // while the rest matches are ignored.
            if(
                    platform_name.find(required_platform_subname) &&
                    selected_platform_index == num_of_platforms // have not selected yet
                    )
            {
                selected_platform_index = i;
                selection_marker = "  [Selected]";
                // Do not exit here and continue the enumeration to see all available platforms,
            }
        }
        else
        {
            // The second way of platform selection: by device type

            // Get the number of devices of primary_device_type
            cl_uint num_devices = 0;
            err = clGetDeviceIDs(platforms[i], primary_device_type, 0, 0, &num_devices);
            // Do not check with SAMPLE_CHECK_ERRORS here, because err may contain
            // CL_DEVICE_NOT_FOUND which is processed below

            if(err != CL_DEVICE_NOT_FOUND)
            {
                // Handle all other type of errors from clGetDeviceIDs here
                SAMPLE_CHECK_ERRORS(err);
                assert(num_devices > 0);

                if(primary_platform_index == num_of_platforms)
                {
                    primary_platform_index = i;
                    selection_marker = "  [Primary]";
                }
            }
            else
            {
                // Get the number of devices of secondary_device_type
                // (similarly to primary_device_type).
                err = clGetDeviceIDs(platforms[i], secondary_device_type, 0, 0, &num_devices);
                // Do not check with SAMPLE_CHECK_ERRORS here, because err may contain
                // CL_DEVICE_NOT_FOUND which is processed below

                if(err != CL_DEVICE_NOT_FOUND)
                {
                    // Handle all other type of errors from clGetDeviceIDs here
                    SAMPLE_CHECK_ERRORS(err);
                    assert(num_devices > 0);

                    if(secondary_platform_index == num_of_platforms)
                    {
                        secondary_platform_index = i;
                        selection_marker = "  [Secondary]";
                    }
                }
            }
        }

        // Print platform name with an optional selection marker
        LOGD("    [%u] %s", i, (platform_name + selection_marker).c_str());
    }

    if(required_platform_subname.empty())
    {
        // Select between primary and secondary device type

        if(primary_platform_index != num_of_platforms)
        {
            selected_platform_index = primary_platform_index;
        }
        else if(secondary_platform_index != num_of_platforms)
        {
            selected_platform_index = secondary_platform_index;
        }
    }

    if(selected_platform_index == num_of_platforms)
    {
        LOGE("There is no found a suitable OpenCL platform");
        return;
    }

    openCLObjects.platform = platforms[selected_platform_index];

    /* -----------------------------------------------------------------------
     * Step 2: Create context with a device of the specified type.
     * Primary device type is passed as function argument required_device_type.
     * If the program cannot create device with primary device type, it tries
     * to create context with secondary device type. It can happens for example,
     * if primary device type is GPU and you run the program on the emulator,
     * which doesn't have GPU, but has only CPU device type. This is an example
     * of the flexibility you can program in your code to avoid crashes on
     * diverse hardware.
     */

    cl_context_properties context_props[] = {
            CL_CONTEXT_PLATFORM,
            cl_context_properties(openCLObjects.platform),
            0
    };

    openCLObjects.context =
            clCreateContextFromType
                    (
                            context_props,
                            primary_device_type,
                            0,
                            0,
                            &err
                    );

    if(err == CL_DEVICE_NOT_FOUND)
    {
        LOGE
        (
                "There is no primary device type available.\n"
                        "Fall back to the secondary device type."
        );

        // The same call of clCreateContextFromType
        // with secondary_device_type.

        openCLObjects.context =
                clCreateContextFromType
                        (
                                context_props,
                                secondary_device_type,
                                0,
                                0,
                                &err
                        );
    }

    SAMPLE_CHECK_ERRORS(err);

    /* -----------------------------------------------------------------------
     * Step 3: Query for OpenCL device that was used for context creation.
     */

    err = clGetContextInfo
            (
                    openCLObjects.context,
                    CL_CONTEXT_DEVICES,
                    sizeof(openCLObjects.device),
                    &openCLObjects.device,
                    0
            );
    SAMPLE_CHECK_ERRORS(err);

    /* -----------------------------------------------------------------------
     * Step 4: Create OpenCL program from its source code.
     * Use program source code passed from Java side of the application.
     * It comes as a jstring. First convert jstring with OpenCL program text
     * to a regular usable char[], as it is processed incorrectly
     * if represented as char*.
     */

    const char* openCLProgramTextNative = env->GetStringUTFChars(openCLProgramText, 0);
    LOGD("OpenCL program text:\n%s", openCLProgramTextNative);

    // After obtaining a regular C string, call clCreateProgramWithSource
    // to create an OpenCL program object.

    openCLObjects.program =
            clCreateProgramWithSource
                    (
                            openCLObjects.context,
                            1,
                            &openCLProgramTextNative,
                            0,
                            &err
                    );

    SAMPLE_CHECK_ERRORS(err);

    /* -----------------------------------------------------------------------
     * Step 5: Build the program.
     * During creation a program is not built. Call the build function explicitly.
     * This example utilizes the create-build sequence, still other options are applicable,
     * for example, when a program consists of several parts, some of which are libraries.
     * Consider using clCompileProgram and clLinkProgram as alternatives.
     * Also consider looking into a dedicated chapter in the OpenCL specification
     * for more information on applicable alternatives and options.
     */

    err = clBuildProgram(openCLObjects.program, 0, 0, 0, 0, 0);

    if(err == CL_BUILD_PROGRAM_FAILURE)
    {
        size_t log_length = 0;
        err = clGetProgramBuildInfo(
                openCLObjects.program,
                openCLObjects.device,
                CL_PROGRAM_BUILD_LOG,
                0,
                0,
                &log_length
        );
        SAMPLE_CHECK_ERRORS(err);

        vector<char> log(log_length);

        err = clGetProgramBuildInfo(
                openCLObjects.program,
                openCLObjects.device,
                CL_PROGRAM_BUILD_LOG,
                log_length,
                &log[0],
                0
        );
        SAMPLE_CHECK_ERRORS(err);

        LOGE
        (
                "Error happened during the build of OpenCL program.\nBuild log:%s",
                &log[0]
        );

        return;
    }

    /* -----------------------------------------------------------------------
     * Step 6: Extract kernel from the built program.
     * An OpenCL program consists of kernels. Each kernel can be called (enqueued) from
     * the host part of an application.
     * First create a kernel to call it from the existing program.
     * Creating a kernel via clCreateKernel is similar to obtaining an entry point of a specific function
     * in an OpenCL program.
     */

    openCLObjects.kernel = clCreateKernel(openCLObjects.program, "FFT2DRadix2", &err);
    SAMPLE_CHECK_ERRORS(err);

    /* -----------------------------------------------------------------------
     * Step 7: Create command queue.
     * OpenCL kernels are enqueued for execution to a particular device through
     * special objects called command queues. Command queue provides ordering
     * of calls and other OpenCL commands.
     * This sample uses a simple in-order OpenCL command queue that doesn't
     * enable execution of two kernels in parallel on a target device.
     */

    openCLObjects.queue =
            clCreateCommandQueue
                    (
                            openCLObjects.context,
                            openCLObjects.device,
                            0,    // Creating queue properties, refer to the OpenCL specification for details.
                            &err
                    );
    SAMPLE_CHECK_ERRORS(err);

    // -----------------------------------------------------------------------

    LOGD("initOpenCL finished successfully");

    env->ReleaseStringUTFChars(openCLProgramText, openCLProgramTextNative);
}


void shutdownOpenCL (OpenCLObjects& openCLObjects)
{
    /* Release all OpenCL objects.
     * This is a regular sequence of calls to deallocate
     * all created OpenCL resources in bootstrapOpenCL.
     *
     * You can call these deallocation procedures in the middle
     * of your application execution (not at the end) if you don't
     * need OpenCL runtime any more.
     * Use deallocation, for example, to free memory or recreate
     * OpenCL objects with different parameters.
     *
     * Calling deallocation in the end of application
     * execution might be not so useful, as upon killing
     * an application, which is a common thing in the Android OS,
     * all OpenCL resources are deallocated automatically.
     */

    cl_int err = CL_SUCCESS;

    if(openCLObjects.isInputBufferInitialized)
    {
        err = clReleaseMemObject(openCLObjects.inputBuffer);
        SAMPLE_CHECK_ERRORS(err);
    }

    err = clReleaseKernel(openCLObjects.kernel);
    SAMPLE_CHECK_ERRORS(err);

    err = clReleaseProgram(openCLObjects.program);
    SAMPLE_CHECK_ERRORS(err);

    err = clReleaseCommandQueue(openCLObjects.queue);
    SAMPLE_CHECK_ERRORS(err);

    err = clReleaseContext(openCLObjects.context);
    SAMPLE_CHECK_ERRORS(err);

    /* There is no procedure to deallocate OpenCL devices or
     * platforms as both are not created at the startup,
     * but queried from the OpenCL runtime.
     */
}























extern "C" void Java_in_atadkase_boofcvbenchmark_MainActivity_initOpenCL
        (
                JNIEnv* env,
                jobject thisObject,
                jstring openCLProgramText
        )
{
    initOpenCL
            (
                    env,
                    thisObject,
                    openCLProgramText,
                    CL_DEVICE_TYPE_GPU,    // primary device type
                    CL_DEVICE_TYPE_CPU,    // secondary device type if primary one is not available
                    openCLObjects
            );
}


extern "C" void Java_in_atadkase_boofcvbenchmark_MainActivity_shutdownOpenCL
        (
                JNIEnv* env,
                jobject thisObject
        )
{
    shutdownOpenCL(openCLObjects);
    LOGD("shutdownOpenCL(openCLObjects) was called");
}


extern "C" void Java_in_atadkase_boofcvbenchmark_CirculantTrackerF32_FFTStep
        (
                JNIEnv* env,
                jobject thisObject,
                jint width,
                jint p,
                jfloatArray inDataPtr,
                jint direction
        )
{
    //LOGD("shutdownOpenCL(openCLObjects) was called");
    int err;
    float * x = env->GetFloatArrayElements(inDataPtr, 0);
//    cl_float2* inData= (cl_float2 *)x;
    int N = pow(2,p);
    cl_float2* inData = (cl_float2*)malloc(N * sizeof(cl_float2));

    for(int i=0; i<N; i++)
    {
        inData[i].s[0] = x[i];
        inData[i].s[1] = 0;
    }



    cl_float2* outData = (cl_float2*) malloc (N * sizeof (cl_float2));
    if(!openCLObjects.isInputBufferInitialized) {
        if (openCLObjects.isInputBufferInitialized) {
            /* If this is not the first time, you need to deallocate the previously
             * allocated buffer as the new buffer will be allocated in
             * the next statements.
             *
             * It is important to remember that unlike Java, there is no
             * garbage collector for OpenCL objects, so deallocate all resources
             * explicitly to avoid running out of memory.
             *
             * It is especially important in case of image buffers,
             * because they are relatively large and even one lost buffer
             * can significantly limit free resources for the application.
             */

            err = clReleaseMemObject(openCLObjects.inputBuffer);
            //SAMPLE_CHECK_ERRORS(err);
        }
        openCLObjects.inputBuffer = clCreateBuffer(openCLObjects.context,
                                                   CL_MEM_READ_ONLY | CL_MEM_USE_HOST_PTR,
                                                   sizeof(float) * N * 2, (void *) inData,
                                                   &err);
        //SAMPLE_CHECK_ERRORS(err);
        openCLObjects.isInputBufferInitialized = true;
    }
    cl_mem outputBuffer =
            clCreateBuffer
                    (
                            openCLObjects.context,
                            CL_MEM_READ_WRITE | CL_MEM_USE_HOST_PTR,
                            sizeof(float) * N* 2,    // Buffer size in bytes, same as the input buffer.
                            (void*)outData,  // Area, above which the buffer is created.
                            &err
                    );
   // SAMPLE_CHECK_ERRORS(err);
    cl_int widthNative = width;
    cl_int pNative = p;
    cl_int directionNative = direction;


    err = clSetKernelArg(openCLObjects.kernel, 0, sizeof(cl_int), &widthNative);
   // SAMPLE_CHECK_ERRORS(err);

    err = clSetKernelArg(openCLObjects.kernel, 1, sizeof(cl_int), &pNative);
    //SAMPLE_CHECK_ERRORS(err);


    err = clSetKernelArg(openCLObjects.kernel, 2, sizeof(openCLObjects.inputBuffer), &openCLObjects.inputBuffer);
   // SAMPLE_CHECK_ERRORS(err);

    err = clSetKernelArg(openCLObjects.kernel, 3, sizeof(outputBuffer), &outputBuffer);
   // SAMPLE_CHECK_ERRORS(err);

    size_t globalSize[2] = { 4, 64 };


    timeval start;
    timeval end;

    gettimeofday(&start, NULL);

    err =
            clEnqueueNDRangeKernel
                    (
                            openCLObjects.queue,
                            openCLObjects.kernel,
                            2,
                            0,
                            globalSize,
                            0,
                            0, 0, 0
                    );
    SAMPLE_CHECK_ERRORS(err);

    err = clFinish(openCLObjects.queue);
    gettimeofday(&end, NULL);
    SAMPLE_CHECK_ERRORS(err);



    /* The start and end timestamps are obtained, now calculate
     * the elapsed time interval in seconds and print it out in
     * LogCat.
     */

    float ndrangeDuration =
            (end.tv_sec + end.tv_usec * 1e-6) - (start.tv_sec + start.tv_usec * 1e-6);

    LOGD("NDRangeKernel time: %f", ndrangeDuration);

}


