#include <jni.h>
#include <string>
#include <android/log.h>
#include "include/class_simple1.h"
#include "include/cl_1.h"
#include "include/class_struct.h"
#include "ffmpeg_test.h"
#include "ffmpeg_play.h"

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


    cl_1 cl1 = cl_1(200, 'aaaa');


//    cl_1 *cl2 = new cl_1(200,'bbbbbb');
//
//    cl2->setAge(300);
//    int _age = cl2->getAge();
//
//    cl1.getAge();
//    cl1.setAge(123);

    return cl1.getAge();
}


extern "C"
JNIEXPORT void JNICALL
Java_com_mdy_practicecl_MainActivity_testStruct(JNIEnv *env, jobject thiz) {

    AudioSample audioSample = {1, 1};
    class_struct classStruct;
    classStruct.funValue(audioSample);
    LOGI("值传递  audioSample   channel: %d      rate:  %d", audioSample.m_channel, audioSample.m_sample_rate);

    LOGI("---------------------------------");

    AudioSample audioSample2 = {2, 2};
    classStruct.funObj(&audioSample2);
    LOGI("引用传递传递  audioSample   channel: %d      rate:  %d", audioSample2.m_channel, audioSample2.m_sample_rate);

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