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
};

class ffmpeg_rtmp {

private:

    AVFormatContext *infmt_ctx, *outfmt_ctx;

    char *in_filename, *out_filename;


public:

    ffmpeg_rtmp();

    ~ffmpeg_rtmp();

    int write_audio_frame(AVFormatContext *m_AVFormatContext, AVStream *m_AVStream);

    int write_video_frame(AVFormatContext *m_AVFormatContext, AVStream *m_AVStream);

    int push_streaming(AVFormatContext *m_AVFormatContext, AVPacket *m_AVPacket);
};


#endif //PRACTICECL_FFMPEG_RTMP_H
