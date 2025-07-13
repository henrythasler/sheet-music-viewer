#include <jni.h>
#include <string>
#include <numeric>
#include <android/log.h>

#include "toolkit.h"


/**
 * @brief Replaces all occurrences of a substring within a string with another substring.
 *
 * This function searches for all occurrences of the substring `from` in the string `str`
 * and replaces them with the substring `to`. If `from` is empty, the function does nothing.
 * from: https://stackoverflow.com/a/3418285
 *
 * @param str  The string in which to perform replacements. Modified in place.
 * @param from The substring to search for and replace.
 * @param to   The substring to replace each occurrence of `from`.
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

/**
 * @brief Joins a vector of strings into a single string with a specified separator.
 *
 * This function concatenates all elements of the input vector, inserting the given
 * separator string between each element. If the input vector is empty, an empty
 * string is returned. If the vector contains only one element, that element is returned.
 *
 * @param input      The vector of strings to join.
 * @param separator  The string to insert between each element.
 * @return           A single string with all elements joined by the separator.
 */
inline std::string joinStrings(std::vector<std::string> input, const std::string& separator) {
    if(input.empty()) {
        return "";
    }
    else if (input.size() == 1) {
        return input.front();
    }

    std::stringstream out;
    out << input.front();
    for_each(input.begin() + 1, input.end(), [&out, &separator] (const std::string& element) { out << separator << element; });
    return out.str();
}


// Creates an instance of the verovio Toolkit without initializing the GUI.
// The 'false' parameter disables GUI-related features, making it suitable for headless or backend processing.
// 'tk' is the Toolkit object used for MEI/MusicXML rendering and manipulation.
vrv::Toolkit tk(false);


/**
 * JNI function to retrieve the version string from the Verovio toolkit.
 *
 * This function is called from Java via the JNI interface. It invokes the
 * `GetVersion()` method on the `tk` object (an instance of the Verovio toolkit)
 * and returns the resulting version string as a Java `String`.
 *
 * @param env   Pointer to the JNI environment.
 * @param this  Reference to the calling Java object (unused).
 * @return      A Java string containing the Verovio toolkit version.
 */
extern "C" JNIEXPORT jstring JNICALL
Java_com_henrythasler_sheetmusic_VerovioViewModel_tkGetVersion(JNIEnv* env,jobject /* this */) {
    return env->NewStringUTF(tk.GetVersion().c_str());
}

/**
 * Sets the resource path for the Verovio toolkit from the Java layer.
 *
 * This JNI function is called from Java to set the resource path used by the Verovio toolkit.
 * It receives a Java string representing the path, converts it to a UTF-8 C string,
 * and passes it to the toolkit's SetResourcePath method. The result of the operation
 * is returned as a jboolean to the Java caller.
 *
 * @param env   Pointer to the JNI environment.
 * @param thiz  Reference to the calling Java object.
 * @param path  Java string containing the resource path to set.
 * @return      JNI_TRUE if the resource path was set successfully, JNI_FALSE otherwise.
 */
extern "C" JNIEXPORT jboolean JNICALL
Java_com_henrythasler_sheetmusic_VerovioViewModel_tkSetResourcePath(JNIEnv *env, jobject thiz, jstring path) {
    const char *utfPath = env->GetStringUTFChars(path, nullptr);
    auto res = tk.SetResourcePath(utfPath);
    env->ReleaseStringUTFChars(path, utfPath);
    return res;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_henrythasler_sheetmusic_VerovioViewModel_tkSetOptions(JNIEnv *env, jobject thiz, jobjectArray options) {
    jsize length = env->GetArrayLength(options);
    std::vector<std::string> jsonOptions;
    jsonOptions.reserve(length);

    for (jsize i = 0; i < length; ++i) {
        auto option = (jstring) env->GetObjectArrayElement(options, i);
        const char *utfOption = env->GetStringUTFChars(option, nullptr);
        jsonOptions.emplace_back(utfOption);
        env->ReleaseStringUTFChars(option, utfOption);
        env->DeleteLocalRef(option);
    }

    std::string mergedOptions = "{" + joinStrings(jsonOptions, ",") + "}";
//    __android_log_print(ANDROID_LOG_DEBUG, "Verovio", "%s", mergedOptions.c_str());
    return tk.SetOptions(mergedOptions);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_henrythasler_sheetmusic_VerovioViewModel_tkLoadData(JNIEnv *env, jobject thiz, jstring data) {
    const char *utfData = env->GetStringUTFChars(data, nullptr);
    return tk.LoadData(utfData);
    env->ReleaseStringUTFChars(data, utfData);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_henrythasler_sheetmusic_VerovioViewModel_tkGetPageCount(JNIEnv* env,jobject /* this */) {
    return tk.GetPageCount();
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_henrythasler_sheetmusic_VerovioViewModel_tkRenderToSVG(JNIEnv *env, jobject thiz, int pageNo, jboolean xmlDeclaration) {
    return env->NewStringUTF(tk.RenderToSVG(pageNo, xmlDeclaration).c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_henrythasler_sheetmusic_VerovioViewModel_tkRenderToTimemap(JNIEnv *env, jobject thiz, jobjectArray options) {
    jsize length = env->GetArrayLength(options);
    std::vector<std::string> jsonOptions;
    jsonOptions.reserve(length);

    for (jsize i = 0; i < length; ++i) {
        auto option = (jstring) env->GetObjectArrayElement(options, i);
        const char *utfOption = env->GetStringUTFChars(option, nullptr);
        jsonOptions.emplace_back(utfOption);
        env->ReleaseStringUTFChars(option, utfOption);
        env->DeleteLocalRef(option);
    }

    std::string mergedOptions = "{" + joinStrings(jsonOptions, ",") + "}";
    return env->NewStringUTF(tk.RenderToTimemap(mergedOptions).c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_henrythasler_sheetmusic_VerovioViewModel_tkRenderData(JNIEnv *env, jobject thiz, jstring data, float fontScale) {
    const char* mei_data = env->GetStringUTFChars(data, nullptr);

//    __android_log_print(ANDROID_LOG_DEBUG, "Verovio", "%X %X %X %X", mei_data[0x422], mei_data[0x423], mei_data[0x424], mei_data[0x425]);

//    std::string svg_data = tk.RenderData(mei_data, R"({
//        "breaks": "auto",
//        "adjustPageHeight": false,
//        "adjustPageWidth": false,
//        "smuflTextFont": "none",
//        "header": "auto",
//        "footer": "auto",
//        "lyricSize": 4.5,
//        "multiRestStyle": "block",
//        "svgFormatRaw": true,
//        "pageMarginLeft": 50,
//        "pageMarginRight": 50,
//        "pageMarginTop": 10,
//        "pageMarginBottom": 10,
//}
//)");

    // minimal size
//    std::string options = "ss";

    std::vector<std::string> jsonOptions;

    jsonOptions.emplace_back(R"("smuflTextFont": "none")");
    jsonOptions.emplace_back(R"("svgFormatRaw": true)");

    jsonOptions.emplace_back(R"("header": "auto")");
    jsonOptions.emplace_back(R"("footer": "none")");

    jsonOptions.emplace_back(R"("breaks": "auto")");
    jsonOptions.emplace_back(R"("adjustPageWidth": true)");
    jsonOptions.emplace_back(R"("adjustPageHeight": true)");

    jsonOptions.emplace_back(R"("pageMarginLeft": 50)");
    jsonOptions.emplace_back(R"("pageMarginRight": 50)");
    jsonOptions.emplace_back(R"("pageMarginTop": 10)");
    jsonOptions.emplace_back(R"("pageMarginBottom": 10)");

//    jsonOptions.emplace_back(R"("lyricSize": 4.5)");
    jsonOptions.emplace_back(std::format("\"lyricSize\": {}", std::min(8.0, std::max(2.0, 4.5 * fontScale / 100.))));

    jsonOptions.emplace_back(R"("lyricWordSpace": 4.0)");
//    jsonOptions.emplace_back(std::format("\"lyricWordSpace\": {}", std::min(10.0, std::max(1.2, 6.0 * fontScale))));

    jsonOptions.emplace_back(R"("multiRestStyle": "block")");

    auto mergedJsonOptions = "{" + joinStrings(jsonOptions, ",") + "}";
//    __android_log_print(ANDROID_LOG_DEBUG, "Verovio", "%s", mergedJsonOptions.c_str());
    std::string svg_data = tk.RenderData(mei_data, mergedJsonOptions);

//    std::string svg_data = tk.RenderData(mei_data, R"({
//        "breaks": "auto",
//        "adjustPageHeight": true,
//        "adjustPageWidth": true,
//        "smuflTextFont": "none",
//        "header": "auto",
//        "footer": "none",
//        "lyricSize": 4.5,
//        "multiRestStyle": "block",
//        "svgFormatRaw": true,
//        "pageMarginLeft": 50,
//        "pageMarginRight": 50,
//        "pageMarginTop": 10,
//        "pageMarginBottom": 10,
//}
//)");

    // workaround for https://github.com/rism-digital/verovio/issues/4038
//    replaceAll(svg_data,"overflow=\"inherit\"", "overflow=\"visible\"");

    env->ReleaseStringUTFChars(data, mei_data);
    return env->NewStringUTF(svg_data.c_str());
}
