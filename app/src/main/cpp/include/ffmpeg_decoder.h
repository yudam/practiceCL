//
// Created by 毛大宇 on 2023/2/13.
//

#ifndef PRACTICECL_FFMPEG_DECODER_H
#define PRACTICECL_FFMPEG_DECODER_H

#include <iostream>

extern "C" {
#include "include/libavformat/avformat.h"
#include "include/libavcodec/avcodec.h"
};

class ffmpeg_decoder {

private:

    AVFormatContext *m_AVFormatCtx;
    AVOutputFormat *m_AVOutputfmt;
    AVStream *m_AVst;
    AVCodecContext *m_AVCodecCtx;
    AVCodec *m_AVcc;
    AVPacket *m_AVpt;
    AVFrame *m_AVfe;

public:
    void initDecoder();
};


#endif //PRACTICECL_FFMPEG_DECODER_H
