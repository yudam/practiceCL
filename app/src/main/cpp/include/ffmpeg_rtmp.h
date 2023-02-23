//
// Created by 毛大宇 on 2023/1/11.
//

#ifndef PRACTICECL_FFMPEG_RTMP_H
#define PRACTICECL_FFMPEG_RTMP_H

#include <iostream>
#include <string>
#include "android/log.h"

extern "C" {

#include "libavformat/avformat.h"
#include "include/libavutil/mathematics.h"
#include "include/libavutil/time.h"
};

class ffmpeg_rtmp {

private:

    AVFormatContext *ifmt_ctx = NULL;
    AVFormatContext *ofmt_ctx = NULL;

    char *in_filename = "/Users/mdy/Desktop/Andoird_studio/practiceCL/app/src/main/cpp/media_muxer.mp4";
    char *out_filename = "rtmp://box-stream-push-test.yololiv.com/yololiv/1007168844428476417?txSecret=4aedb345e30150f8be854ee1252ffc22&txTime=76c1ccb2";

public:

    void init();

    void release();
};


#endif //PRACTICECL_FFMPEG_RTMP_H
