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

    AVFormatContext *ifmt_ctx;
    AVFormatContext *ofmt_ctx;

    char *in_filename = "/storage/9016-4EF8/VideoRecord/20221110-194720.mp4";
    char *out_filename = "rtmp://box-stream-push-test.yololiv.com/yololiv/1004690261428666369?txSecret=15afbe687082490c46364c86ad3f9e5e&txTime=76b8c856";


public:

    void init();

    void release();
};


#endif //PRACTICECL_FFMPEG_RTMP_H
