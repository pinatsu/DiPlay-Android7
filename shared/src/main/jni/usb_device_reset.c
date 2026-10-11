#include <errno.h>
#include <jni.h>
#include <linux/usbdevice_fs.h>
#include <sys/ioctl.h>

JNIEXPORT jint JNICALL
Java_com_shilapi_xcertplay_transport_UsbDeviceResetNative_reset(
        JNIEnv *env, jobject receiver, jint file_descriptor) {
    (void) env;
    (void) receiver;
    if (file_descriptor < 0) return EINVAL;

    int result;
    do {
        result = ioctl(file_descriptor, USBDEVFS_RESET, NULL);
    } while (result < 0 && errno == EINTR);
    return result == 0 ? 0 : errno;
}
