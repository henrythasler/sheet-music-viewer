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
    std::string mei_data = R"(<?xml version="1.0" encoding="UTF-8"?><?xml-model href="https://music-encoding.org/schema/5.1/mei-all.rng" type="application/xml" schematypens="http://relaxng.org/ns/structure/1.0"?><?xml-model href="https://music-encoding.org/schema/5.1/mei-all.rng" type="application/xml" schematypens="http://purl.oclc.org/dsdl/schematron"?><mei xmlns="http://www.music-encoding.org/ns/mei" meiversion="5.1"><meiHead><fileDesc><titleStmt><title>Basic chord</title></titleStmt><pubStmt><date isodate="2017-05-17">2021-04-23</date></pubStmt><notesStmt><annot /></notesStmt></fileDesc><encodingDesc><appInfo><application version="3.3.0" label="2"><name>Verovio</name></application></appInfo></encodingDesc></meiHead><music><body><mdiv xml:id="moqsi69"><score xml:id="sjft1g2"><scoreDef xml:id="s168fb37"><staffGrp xml:id="sfnjo7e"><staffDef xml:id="smlovzj" n="1" lines="5" clef.shape="G" clef.line="2" keysig="0" meter.sym="common" /></staffGrp></scoreDef><section xml:id="s6ug31v"><measure xml:id="m1bcu6sn" right="end" n="1"><staff xml:id="s1ffvwuh" n="1"><layer xml:id="lzosgrv" n="1"><chord xml:id="c1q84ocf" dur="1"><note xml:id="n1gzuhvg" oct="4" pname="c" /><note xml:id="n1vw1hrc" oct="4" pname="e" /><note xml:id="n36inxr" oct="4" pname="g" /></chord></layer></staff></measure></section></score></mdiv></body></music></mei>)";
    std::string svg_data = tk.RenderData(mei_data, "{\"pageWidth\": 2000}");
//    __android_log_print(ANDROID_LOG_INFO, "Verovio", "SVG: %s", svg_data.c_str());
//    std::string svg_data = "<svg/>";
    return env->NewStringUTF(svg_data.c_str());
}