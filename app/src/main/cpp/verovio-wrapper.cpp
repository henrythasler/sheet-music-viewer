#include <jni.h>
#include <string>
#include <android/log.h>

#include "toolkit.h"


/**
 * replaces all occurrence of a string sequence with another in the given string
 * from: https://stackoverflow.com/a/3418285
 * @param str string
 * @param from string sequence that shall be replaced
 * @param to replacement string sequence
 * @return true if the replacement was done; false if the sequence is not found
 */
void replaceAll(std::string& str, const std::string& from, const std::string& to) {
    if(from.empty())
        return;
    size_t start_pos = 0;
    while((start_pos = str.find(from, start_pos)) != std::string::npos) {
        str.replace(start_pos, from.length(), to);
        start_pos += to.length(); // In case 'to' contains 'from', like replacing 'x' with 'yx'
    }
}

vrv::Toolkit tk(false);

extern "C" JNIEXPORT jstring JNICALL
Java_com_henrythasler_sheetmusic_VerovioViewModel_getVersion(
        JNIEnv* env,
        jobject /* this */) {
    return env->NewStringUTF(tk.GetVersion().c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_henrythasler_sheetmusic_VerovioViewModel_setDataPath(JNIEnv *env, jobject thiz, jstring verovioDataPath) {
    const char *nativePath = env->GetStringUTFChars(verovioDataPath, nullptr);
    tk.SetResourcePath(nativePath);
    env->ReleaseStringUTFChars(verovioDataPath, nativePath);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_henrythasler_sheetmusic_VerovioViewModel_renderData(JNIEnv *env, jobject thiz, jstring data) {
    const char* mei_data = env->GetStringUTFChars(data, nullptr);

//    __android_log_print(ANDROID_LOG_DEBUG, "Verovio", "%X %X %X %X", mei_data[0x422], mei_data[0x423], mei_data[0x424], mei_data[0x425]);

    std::string svg_data = tk.RenderData(mei_data, R"({
        "breaks": "auto",
        "adjustPageHeight": true,
        "adjustPageWidth": true,
        "smuflTextFont": "none",
        "header": "none",
        "footer": "none",
        "lyricSize": 3,
        "multiRestStyle": "block",
        "svgFormatRaw": true,
        "header": "auto",
        "pageMarginLeft": 50,
        "pageMarginRight": 50,
        "pageMarginTop": 10,
        "pageMarginBottom": 10,
}
)");

    // workaround for https://github.com/rism-digital/verovio/issues/4038
//    replaceAll(svg_data,"overflow=\"inherit\"", "overflow=\"visible\"");

    env->ReleaseStringUTFChars(data, mei_data);
    return env->NewStringUTF(svg_data.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_henrythasler_sheetmusic_VerovioViewModel_renderToTimemap(JNIEnv *env, jobject thiz, jstring data) {
    const char* mei_data = env->GetStringUTFChars(data, nullptr);

    if(tk.SetOptions(R"({
        "breaks": "none",
}
)")) {
        tk.LoadData(mei_data);
        std::string time_map = tk.RenderToTimemap("");
        env->ReleaseStringUTFChars(data, mei_data);
        return env->NewStringUTF(time_map.c_str());
    }
    return env->NewStringUTF("");
}