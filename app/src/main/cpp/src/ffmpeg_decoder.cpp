//
// Created by 毛大宇 on 2023/2/13.
//

#include "ffmpeg_decoder.h"


void ffmpeg_decoder::initDecoder() {

    const char *out_file = "out.h264";

    av_register_all();

    m_AVFormatCtx = avformat_alloc_context();

    m_AVOutputfmt = av_guess_format(NULL, out_file, NULL);
    m_AVFormatCtx->oformat = m_AVOutputfmt;
}