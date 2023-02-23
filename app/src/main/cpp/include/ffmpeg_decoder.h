//
// Created by 毛大宇 on 2023/2/13.
//

#ifndef PRACTICECL_FFMPEG_DECODER_H
#define PRACTICECL_FFMPEG_DECODER_H

#include <iostream>
#include <string>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <time.h>
#include "android/log.h"


extern "C" {
#include "include/libavformat/avformat.h"
#include "include/libavcodec/avcodec.h"
#include "include/libswscale/swscale.h"
#include "include/libswresample/swresample.h"
#include "include/libavutil/imgutils.h"
};

class ffmpeg_decoder {

private:

    JNIEnv* env;
    jobject surface;
    ANativeWindow * m_ANativeWindow;

    AVFormatContext *m_AVFormatContext;
    AVCodecContext *m_AVCodecContext;
    AVCodec *m_AVCodec;
    AVPacket *m_AVPacket;
    AVFrame *m_AVFrame;
    AVFrame *m_RGBFrame;
    SwsContext *m_SwsContext;

    uint8_t*  m_FrameBuffer;
    int m_VideoWidth ;
    int m_VideoHeight;

    int m_RenderWidth ;
    int m_RenderHeight;

public:

    void onPrepare(JNIEnv* m_env,jobject m_Surface);

    void onInit();

    void onDecoder(const char * m_url);

    void onScaleFrame(AVFrame *frame);

    void onFrameAvailable(AVFrame *frame,AVStream * stream);

};


#endif //PRACTICECL_FFMPEG_DECODER_H
