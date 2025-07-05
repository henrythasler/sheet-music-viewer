#include <jni.h>
#include <string>
#include <numeric>
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

extern "C" JNIEXPORT jstring JNICALL
Java_com_henrythasler_sheetmusic_VerovioViewModel_renderData(JNIEnv *env, jobject thiz, jstring data, float fontScale) {
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