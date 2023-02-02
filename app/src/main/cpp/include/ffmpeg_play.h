//
// Created by 毛大宇 on 2023/1/9.
//

#ifndef PRACTICECL_FFMPEG_PLAY_H
#define PRACTICECL_FFMPEG_PLAY_H

#include <string>
#include <iostream>
#include <android/log.h>

extern "C" {

#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libswscale/swscale.h"
#include "libavutil/pixfmt.h"
}

class ffmpeg_play {

public:

    //解封装功能的结构体，包含文件名、音视频流、时长。比特率等信息
    AVFormatContext *m_AVFormatContext;
    //编解码的上下文，编码和解码时用到的结构体，包含编解码器类型，视频宽高，音频通道数和采样率等信息
    AVCodecContext *m_AVCodecContext;
    //存储编解码信息的结构体
    const AVCodec *m_AVCodec;
    //存储音视频编码数据
    AVPacket *m_AVPacket;
    //存储音视频解码数据
    AVFrame *m_AVFrame;

    char *m_Url;
    int m_StreamIndex = -1;

    void init(char *url);

    void prepare();

    void release();

    ffmpeg_play();
    ~ffmpeg_play();

};


#endif //PRACTICECL_FFMPEG_PLAY_H
