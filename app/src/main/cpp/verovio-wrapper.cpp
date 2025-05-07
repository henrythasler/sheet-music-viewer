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
Java_com_henrythasler_sheetmusic_VerovioViewModel_renderData(JNIEnv *env, jobject thiz, jstring fileToLoad) {
    const char* nativePath = env->GetStringUTFChars(fileToLoad, nullptr);

    // barline-003.mei
    std::string mei_data = R"(<?xml version="1.0" encoding="UTF-8"?><?xml-model href="https://music-encoding.org/schema/5.1/mei-all.rng" type="application/xml" schematypens="http://relaxng.org/ns/structure/1.0"?><?xml-model href="https://music-encoding.org/schema/5.1/mei-all.rng" type="application/xml" schematypens="http://purl.oclc.org/dsdl/schematron"?><mei xmlns="http://www.music-encoding.org/ns/mei" meiversion="5.1"><meiHead><fileDesc><titleStmt><title>Barline example</title></titleStmt><pubStmt><date isodate="2018-05-29">2018-05-29</date></pubStmt><seriesStmt><title>Verovio test suite</title></seriesStmt><notesStmt><annot>Verovio supports both various bar line types. Bar lines should be spaces approrpately</annot></notesStmt></fileDesc><encodingDesc><appInfo><application version="2.0.0" label="2"><name>Verovio</name></application></appInfo></encodingDesc></meiHead><music><body><mdiv><score><scoreDef meter.count="2" meter.unit="4"><staffGrp bar.thru="false"><staffGrp bar.thru="true" symbol="bracket"><staffDef n="1" lines="5" clef.shape="G" clef.line="2" keysig="2s"><label>Violino I</label></staffDef><staffDef n="2" lines="5" clef.shape="G" clef.line="2" keysig="2s"><label>Violino II</label></staffDef><staffDef n="3" lines="5" clef.shape="C" clef.line="3" keysig="2s"><label>Viola</label></staffDef><staffDef n="4" lines="5" clef.shape="F" clef.line="4" keysig="2s"><label>Violoncello</label></staffDef></staffGrp></staffGrp></scoreDef><section><ending n="1"><measure right="rptend" metcon="false" n="44a-1"><staff n="1"><layer n="1"><note dur="8" oct="4" pname="a" tstamp="1" /><rest dur="8" tstamp="1.5" /></layer></staff><staff n="2"><layer n="1"><beam><note xml:id="note_16590" dur="32" oct="4" pname="c" tstamp="1" accid.ges="s" /><note dur="32" oct="4" pname="e" tstamp="1.125" /><note dur="32" oct="4" pname="a" tstamp="1.25" /><note dur="32" oct="4" pname="g" tstamp="1.375" accid="s" /></beam><beam><note dur="32" oct="4" pname="g" tstamp="1.5" accid="n" /><note dur="32" oct="4" pname="f" tstamp="1.625" accid.ges="s" /><note dur="32" oct="4" pname="g" tstamp="1.75" accid.ges="n" /><note xml:id="note_16641" dur="32" oct="4" pname="e" tstamp="1.875" /></beam></layer></staff><staff n="3"><layer n="1"><note dur="8" oct="4" pname="a" tstamp="1" /><rest dur="8" tstamp="1.5" /></layer></staff><staff n="4"><layer n="1"><note dur="8" oct="2" pname="a" tstamp="1" /><rest dur="8" tstamp="1.5" /></layer></staff><slur staff="2" startid="#note_16590" endid="#note_16641" /></measure></ending><ending n="2"><measure right="dbl" metcon="false" n="44a-2"><staff n="1"><layer n="1"><note dur="8" oct="4" pname="a" tstamp="1" /><rest dur="8" tstamp="1.5" /></layer></staff><staff n="2"><layer n="1"><beam><note xml:id="note_16788" dur="32" oct="4" pname="c" tstamp="1" accid.ges="s" /><note dur="32" oct="4" pname="e" tstamp="1.125" /><note dur="32" oct="4" pname="f" tstamp="1.25" accid.ges="s" /><note dur="32" oct="4" pname="g" tstamp="1.375" accid="s" /></beam><beam><note dur="32" oct="4" pname="a" tstamp="1.5" /><note dur="32" oct="4" pname="g" tstamp="1.625" accid.ges="s" /><note dur="32" oct="4" pname="b" tstamp="1.75" /><note xml:id="note_16839" dur="32" oct="4" pname="a" tstamp="1.875" /></beam></layer></staff><staff n="3"><layer n="1"><note dur="8" oct="4" pname="a" tstamp="1" /><rest dur="8" tstamp="1.5" /></layer></staff><staff n="4"><layer n="1"><note dur="8" oct="2" pname="a" tstamp="1" /><rest dur="8" tstamp="1.5" /></layer></staff><slur staff="2" startid="#note_16788" endid="#note_16839" /></measure></ending><measure left="rptstart" metcon="false" n="44b"><staff n="1"><layer n="1"><beam><note xml:id="note_16968" dur="8" oct="4" pname="a" tstamp="1"><supplied><artic artic="stacc" /></supplied></note><note dur="8" oct="4" pname="a" tstamp="1.5"><supplied><artic artic="stacc" /></supplied></note></beam></layer></staff><staff n="2"><layer n="1"><beam><note xml:id="note_17007" dur="32" oct="5" pname="c" tstamp="1" accid="n" /><note dur="32" oct="5" pname="d" tstamp="1.125" /><note dur="32" oct="5" pname="c" tstamp="1.25" accid.ges="n" /><note dur="32" oct="4" pname="b" tstamp="1.375" /></beam><beam><note dur="32" oct="4" pname="a" tstamp="1.5" /><note dur="32" oct="4" pname="g" tstamp="1.625" accid="n" /><note dur="32" oct="4" pname="f" tstamp="1.75" accid.ges="s" /><note xml:id="note_17058" dur="32" oct="4" pname="e" tstamp="1.875" /></beam></layer></staff><staff n="3"><layer n="1"><rest dur="4" tstamp="1" /></layer></staff><staff n="4"><layer n="1"><rest dur="4" tstamp="1" /></layer></staff><dynam staff="1" startid="#note_16968">p</dynam><dynam staff="2" startid="#note_17007">p</dynam><slur staff="2" startid="#note_17007" endid="#note_17058" /></measure></section></score></mdiv></body></music></mei>)";

    std::string svg_data = tk.RenderData(mei_data, R"({"pageWidth": 1600, "pageHeight": 900, "footer": "none"})");

    // workaround for https://github.com/rism-digital/verovio/issues/4038
//    replaceAll(svg_data,"overflow=\"inherit\"", "overflow=\"visible\"");

    env->ReleaseStringUTFChars(fileToLoad, nativePath);
    return env->NewStringUTF(svg_data.c_str());
}