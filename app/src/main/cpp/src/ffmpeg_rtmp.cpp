//
// Created by 毛大宇 on 2023/1/11.
//

#include "ffmpeg_rtmp.h"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,"ffmpeg_rtmp",__VA_ARGS__)


int ffmpeg_rtmp::write_audio_frame(AVFormatContext *m_AVFormatContext, AVStream *m_AVStream) {

    AVPacket pkt;
    av_init_packet(&pkt);
    pkt.data = nullptr;
    pkt.dts = 1;
    pkt.pts = 1;
    pkt.size = 1;
    pkt.duration = 1;
    pkt.stream_index = m_AVStream->index;
    int ret = push_streaming(m_AVFormatContext, &pkt);
    return ret;
}

int ffmpeg_rtmp::write_video_frame(AVFormatContext *m_AVFormatContext, AVStream *m_AVStream) {

}

int ffmpeg_rtmp::push_streaming(AVFormatContext *m_AVFormatContext, AVPacket *m_AVPacket) {

    int dts = m_AVPacket->dts;
    int pts = m_AVPacket->pts;
    int size = m_AVPacket->size;
    LOGI("dts = %d    pts = %d     size = %d ", dts, pts, size);
    int ret = av_interleaved_write_frame(m_AVFormatContext, m_AVPacket);
    return ret;
}