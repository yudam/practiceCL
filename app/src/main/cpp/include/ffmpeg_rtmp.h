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
#include "libavutil/log.h"
};

class ffmpeg_rtmp {

private:

    AVFormatContext *ifmt_ctx;
    AVFormatContext *ofmt_ctx;

    const char *in_filename;
    const char *out_filename;

public:

    void preInit(const char * url,const char * rtmp);

    void init();

    void release();
};


#endif //PRACTICECL_FFMPEG_RTMP_H
