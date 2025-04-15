#include <jni.h>
#include <string>
#include <android/log.h>

#include "toolkit.h"

extern "C" JNIEXPORT jstring JNICALL
Java_com_henrythasler_sheetmusic_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    vrv::Toolkit tk(false);
    __android_log_print(ANDROID_LOG_INFO, "Verovio", "%s", "test");
    std::string hello = "Verovio " + tk.GetVersion();
//    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}