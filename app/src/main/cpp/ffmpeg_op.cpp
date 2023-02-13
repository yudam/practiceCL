//
// Created by 毛大宇 on 2023/2/10.
//

#include <jni.h>
#include <android/log.h>
#include "ffmpeg_play.h"

/**
 * static修饰的全局变量
 */
static ffmpeg_play *mFFmpegPlay = nullptr;

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