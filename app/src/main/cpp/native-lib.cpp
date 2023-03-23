#include <jni.h>
#include <string>
#include <android/log.h>
#include "ffmpeg_test.h"
#include "ffmpeg_play.h"
#include "NALUParse.h"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,"class_struct",__VA_ARGS__)


extern "C" JNIEXPORT jstring JNICALL
Java_com_mdy_practicecl_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    //std::string hello = test_ffmpeg();
    //std::string hello = "avcodec_configuration()";

    //将char转化为string，注意不可以 std::string config = learn_ffmpeg_base（）没这种构造函数
    std::string config;
    config = learn_ffmpeg_base();
    return env->NewStringUTF(config.c_str());
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_mdy_practicecl_MainActivity_simpleCl(JNIEnv *env, jobject thiz) {

    return 100;
}


extern "C"
JNIEXPORT void JNICALL
Java_com_mdy_practicecl_MainActivity_testStruct(JNIEnv *env, jobject thiz) {

}

extern "C"
JNIEXPORT void JNICALL
Java_com_mdy_practicecl_MainActivity_ffmpeg_1release(JNIEnv *env, jobject thiz) {

}


extern "C"
JNIEXPORT jlong JNICALL
Java_com_mdy_practicecl_MainActivity_ffmpeg_1init(JNIEnv *env, jobject thiz, jstring url) {
    ffmpeg_play *m_ffmpeg_play = new ffmpeg_play();
    jlong m_nativePtr = reinterpret_cast<jlong>(m_ffmpeg_play);
    return m_nativePtr;
}


extern "C"
JNIEXPORT void JNICALL
Java_com_mdy_practicecl_MainActivity_ffmpeg_1prepare(JNIEnv *env, jobject thiz, jlong native_ptr) {


    ffmpeg_play *m_ffmpeg_play = reinterpret_cast<ffmpeg_play *>(native_ptr);
}


extern "C"
JNIEXPORT void JNICALL
Java_com_mdy_practicecl_codec_H264Activity_findStartCode(JNIEnv *env, jobject thiz, jbyteArray buffer, jint offset) {

}
extern "C"
JNIEXPORT void JNICALL
Java_com_mdy_practicecl_codec_H264Activity_parseH264Stream(JNIEnv *env, jobject thiz, jstring url) {
    NALUParse *naluParse = new NALUParse();
    const char *h264_url = env->GetStringUTFChars(url, nullptr);
    naluParse->parse_h264stream(h264_url);
    delete naluParse;
}