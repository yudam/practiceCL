//
// Created by 毛大宇 on 2023/2/10.
//

#include <jni.h>
#include <android/log.h>
#include "ffmpeg_play.h"
#include "ffmpeg_rtmp.h"
#include "ffmpeg_decoder.h"

/**
 * static修饰的全局变量
 */
static ffmpeg_play *mFFmpegPlay = nullptr;

static ffmpeg_rtmp * ffmpegRtmp = nullptr;

static ffmpeg_decoder * ffmpegDecoder = nullptr;

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,"ffmpeg",__VA_ARGS__)

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_mdy_practicecl_FFmpegctivity_ffmpegCreate(JNIEnv *env, jobject thiz) {
    if (mFFmpegPlay == nullptr) {
        mFFmpegPlay = new ffmpeg_play();
    }
    return mFFmpegPlay != nullptr;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_mdy_practicecl_FFmpegctivity_ffmpegRelease(JNIEnv *env, jobject thiz) {

    if (mFFmpegPlay != nullptr) {
        mFFmpegPlay->release();
        mFFmpegPlay = nullptr;
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_mdy_practicecl_FFmpegctivity_startRtmp(JNIEnv *env, jobject thiz) {

    if(ffmpegRtmp == nullptr){
        ffmpegRtmp = new ffmpeg_rtmp();
    }
    ffmpegRtmp->init();
}
extern "C"
JNIEXPORT void JNICALL
Java_com_mdy_practicecl_FFmpegctivity_stopRtmp(JNIEnv *env, jobject thiz) {
    if(ffmpegRtmp != nullptr){
        ffmpegRtmp->release();
        ffmpegRtmp = nullptr;
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_mdy_practicecl_FFmpegctivity_startDecoder(JNIEnv *env, jobject thiz, jstring url, jobject surface) {

    if(ffmpegDecoder == nullptr){
        ffmpegDecoder = new ffmpeg_decoder();
    }
    ffmpegDecoder->onPrepare(env,surface);
    const char * m_url = env->GetStringUTFChars(url, nullptr);
    ffmpegDecoder->onDecoder(m_url);
}