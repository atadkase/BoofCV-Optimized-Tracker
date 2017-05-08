LOCAL_PATH := $(call my-dir)
LOCAL_PATH_EXT := $(call my-dir)/../external/
include $(CLEAR_VARS)

LOCAL_MODULE := InverseFilter

LOCAL_CFLAGS += -DANDROID_CL
LOCAL_CFLAGS += -O3 -ffast-math

LOCAL_C_INCLUDES := $(LOCAL_PATH)/../include

LOCAL_SRC_FILES := nativeOCL.cpp
#LOCAL_LDFLAGS += -ljnigraphics
LOCAL_LDLIBS := -llog -ljnigraphics -lOpenCL
LOCAL_LDLIBS += $(LOCAL_PATH)/libOpenCL.so
LOCAL_ARM_MODE := arm

include $(BUILD_SHARED_LIBRARY)