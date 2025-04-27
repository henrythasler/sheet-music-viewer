#include <jni.h>
#include <string>
#include <android/log.h>

#include "toolkit.h"

extern "C" JNIEXPORT jstring JNICALL
Java_com_henrythasler_sheetmusic_VerovioViewModel_getVersion(
        JNIEnv* env,
        jobject /* this */) {
    vrv::Toolkit tk(false);
    return env->NewStringUTF(tk.GetVersion().c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_henrythasler_sheetmusic_VerovioViewModel_renderData(JNIEnv *env, jobject thiz, jstring path) {
    const char* nativePath = env->GetStringUTFChars(path, nullptr);

    vrv::Toolkit tk(false);
//    __android_log_print(ANDROID_LOG_INFO, "Verovio", "%s", tk.GetResourcePath().c_str());
    tk.SetResourcePath(nativePath);
//    __android_log_print(ANDROID_LOG_INFO, "Verovio", "%s", tk.GetResourcePath().c_str());

    // chord-005
    std::string mei_data = R"(<?xml version="1.0" encoding="UTF-8"?><?xml-model href="https://music-encoding.org/schema/5.1/mei-basic.rng" type="application/xml" schematypens="http://relaxng.org/ns/structure/1.0"?><?xml-model href="https://music-encoding.org/schema/5.1/mei-basic.rng" type="application/xml" schematypens="http://purl.oclc.org/dsdl/schematron"?><mei xmlns="http://www.music-encoding.org/ns/mei" meiversion="5.1+basic"><music><body><mdiv><score><scoreDef><staffGrp><staffDef n="1" lines="5" clef.shape="G" clef.line="2" keysig="0" meter.sym="common" /></staffGrp></scoreDef><section><measure right="end" n="1"><staff n="1"><layer n="1"><chord dur="1"><note oct="4" pname="c" /><note oct="4" pname="e" /><note oct="4" pname="g" /></chord></layer></staff></measure></section></score></mdiv></body></music></mei>)";
//    std::string mei_data = R"(<?xml version="1.0" encoding="UTF-8"?><?xml-model href="https://music-encoding.org/schema/5.1/mei-basic.rng" type="application/xml" schematypens="http://relaxng.org/ns/structure/1.0"?><?xml-model href="https://music-encoding.org/schema/5.1/mei-basic.rng" type="application/xml" schematypens="http://purl.oclc.org/dsdl/schematron"?><mei xmlns="http://www.music-encoding.org/ns/mei" meiversion="5.1+basic"><music><body><mdiv><score><scoreDef><staffGrp><staffDef n="1" lines="5" clef.shape="G" clef.line="2" /></staffGrp></scoreDef><section><measure n="1"><staff n="1"><layer n="1"><beam><note dur="8" oct="4" pname="d" /><note dur="8" oct="4" pname="f" /></beam><beam><note dur="8" oct="4" pname="e" /><note dur="8" oct="4" pname="f" /></beam></layer></staff></measure></section></score></mdiv></body></music></mei>)";

    // beam-009
    std::string svg_data = tk.RenderData(mei_data, R"({"pageWidth": 400, "pageHeight": 400, "footer": "none", "header": "none"})");
//    __android_log_print(ANDROID_LOG_INFO, "Verovio", "%s", svg_data.c_str());
//    std::string svg_data = "<svg/>";
    return env->NewStringUTF(svg_data.c_str());
}