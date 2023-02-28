//
// Created by 毛大宇 on 2023/2/13.
//

#include "ffmpeg_decoder.h"


#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,"ffmpeg_decoder",__VA_ARGS__)






/**
 * AVFormatContext：解封装功能的结构体、包含文件名、音视频流、时长、比特率等信息
 * AVCodecContext：编解码器上下文，必须使用到的结构体，包含编解码器类型、视频宽高、音频通道和采样率等信息
 * AVCodec：存储编解码器信息的结构体
 * AVStream：存储音频或视频流信息的结构体
 * AVPacket：存储音频或视频编码数据
 * AVFrame：存储音频或视频解码数据（原始数据）
 */

void ffmpeg_decoder::onPrepare(JNIEnv *m_env, jobject m_Surface) {

    env = m_env;
    surface = m_Surface;
}

void ffmpeg_decoder::onDecoder(const char *m_url) {

    LOGI("  m_url: %s", m_url);

    int m_stream_index = -1;

    av_register_all();
    /**
     * 1 解封装阶段
     */
    // 创建封装格式上下文
    m_AVFormatContext = avformat_alloc_context();
    // 打开输入文件
    if (int ret = avformat_open_input(&m_AVFormatContext, m_url, NULL, NULL) != 0) {
        LOGI("avformat_open_input failed");
        char *buffer = new char[1024];
        av_strerror(ret, buffer, 1024);
        LOGI(" error: %d    cause: %s", ret, buffer);
        return;
    }

    // 获取音视频流信息
    if (avformat_find_stream_info(m_AVFormatContext, NULL) < 0) {
        LOGI("avformat_find_stream_info failed");
        return;
    }

    // 获取音视频流索引
    for (int i = 0; i < m_AVFormatContext->nb_streams; i++) {
        if (m_AVFormatContext->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            m_stream_index = i;
            break;
        }
    }


    if (m_stream_index == -1) {
        LOGI(" m_stream_index = -1");
        return;
    }

    /**
     * 2 初始化解码器
     */
    // 获取解码器参数
    AVStream *m_AVStream = m_AVFormatContext->streams[m_stream_index];

    // 根据解码器codec_id获取合适的解码器
    m_AVCodec = avcodec_find_decoder(m_AVStream->codecpar->codec_id);
    if (m_AVCodec == nullptr) {
        LOGI("m_AVCodec is null");
        return;
    }

    // 创建解码器上下文
    m_AVCodecContext = avcodec_alloc_context3(m_AVCodec);
    // 将视频媒体流的参数配置到解码器上下文
    if (avcodec_parameters_to_context(m_AVCodecContext, m_AVStream->codecpar) != 0) {
        LOGI(" avcodec_parameters_from_context ");
        return;
    }

    // 打开解码器
    int ret = avcodec_open2(m_AVCodecContext, m_AVCodec, NULL);
    if (ret < 0) {
        LOGI("avcodec_open2 ");
        return;
    }

    //初始化格式转换的代码
    onInit();

    /**
     * 3 循环解码
     */

    // 创建存储编码数据和解码数据的结构体
    m_AVPacket = av_packet_alloc();
    m_AVFrame = av_frame_alloc();

    // 循环解码，读取帧
    while (av_read_frame(m_AVFormatContext, m_AVPacket) >= 0) {
        if (m_AVPacket->stream_index == m_stream_index) {
            // 视频解码
            if (avcodec_send_packet(m_AVCodecContext, m_AVPacket) != 0) {
                LOGI("avcodec_send_packet  ");
                return;
            }

            while (avcodec_receive_frame(m_AVCodecContext, m_AVFrame) == 0) {
                // 获取到解码后的数据
                onFrameAvailable(m_AVFrame,m_AVStream);
                LOGI("avcodec_receive_frame:   ");
            }
        }
        // 释放m_AVPacket引用，防止内存泄漏
        av_packet_unref(m_AVPacket);

    }

    // 解码完成，释放资源


    if (m_RGBFrame != nullptr) {
        av_frame_free(&m_RGBFrame);
        m_RGBFrame = nullptr;
    }

    if (m_FrameBuffer != nullptr) {
        free(m_FrameBuffer);
        m_FrameBuffer = nullptr;
    }

    if (m_SwsContext != nullptr) {
        sws_freeContext(m_SwsContext);
        m_SwsContext = nullptr;
    }

    if (m_AVFrame != nullptr) {
        av_frame_free(&m_AVFrame);
        m_AVFrame = nullptr;
    }


    if (m_AVPacket != nullptr) {
        av_packet_free(&m_AVPacket);
        m_AVPacket = nullptr;
    }

    if (m_AVCodecContext != nullptr) {
        avcodec_close(m_AVCodecContext);
        avcodec_free_context(&m_AVCodecContext);
        m_AVCodecContext = nullptr;
        m_AVCodec = nullptr;
    }

    if (m_AVFormatContext != nullptr) {
        avformat_close_input(&m_AVFormatContext);
        avformat_free_context(m_AVFormatContext);
        m_AVFormatContext = nullptr;
    }

    // 释放ANativeWindow
    if(m_ANativeWindow){
        ANativeWindow_release(m_ANativeWindow);
    }

}


void ffmpeg_decoder::onInit() {
    m_VideoWidth = m_AVCodecContext->width;
    m_VideoHeight = m_AVCodecContext->height;

    m_RenderWidth = m_VideoWidth;
    m_RenderHeight = m_VideoHeight;

    m_RGBFrame = av_frame_alloc();

    // 计算Buffer的大小
    int bufferSize = av_image_get_buffer_size(AV_PIX_FMT_RGBA, m_VideoWidth, m_VideoHeight, 1);
    // 为m_RGBFrame分配内存空间
    m_FrameBuffer = (uint8_t *) av_malloc(bufferSize * sizeof(uint8_t));

    av_image_fill_arrays(m_RGBFrame->data, m_RGBFrame->linesize, m_FrameBuffer, AV_PIX_FMT_RGBA,
                         m_VideoWidth, m_VideoHeight, 1);

    // 获取转转的上下文
    m_SwsContext = sws_getContext(m_VideoWidth, m_VideoHeight, m_AVCodecContext->pix_fmt,
                                  m_RenderWidth, m_RenderHeight, AV_PIX_FMT_RGBA,
                                  SWS_FAST_BILINEAR, NULL, NULL, NULL);
}

void ffmpeg_decoder::onScaleFrame(AVFrame *frame) {
    // 格式转换

    sws_scale(m_SwsContext, frame->data, frame->linesize, 0, m_VideoHeight, m_RGBFrame->data,
              m_RGBFrame->linesize);
}


/**
 * 可用的帧数据
 */
void ffmpeg_decoder::onFrameAvailable(AVFrame *frame, AVStream *stream) {

    onScaleFrame(frame);

    // pts换算成时间戳
    //int decode_time_ms = frame->pts * 1000 / stream->time_base.den;


    m_ANativeWindow = ANativeWindow_fromSurface(env, surface);

    // 设置渲染区域和输入格式
    ANativeWindow_setBuffersGeometry(m_ANativeWindow, m_VideoWidth, m_VideoHeight, WINDOW_FORMAT_RGBA_8888);


    // 渲染
    ANativeWindow_Buffer m_ANativeWindow_Buffer;

    // 锁定当前Window，获取屏幕缓冲区Buffer的指针
    ANativeWindow_lock(m_ANativeWindow, &m_ANativeWindow_Buffer, nullptr);
    uint8_t *dstBuffer = static_cast<uint8_t *>(m_ANativeWindow_Buffer.bits);

    // 输入的图的步长（一行像素有多少字节）
    int srcLinesize = m_RGBFrame->linesize[0];
    // RGBA缓冲区步长
    int dstLInesize = m_ANativeWindow_Buffer.stride * 4;

    for (int i = 0; i < m_VideoHeight; i++) {
        // 一行一行的拷贝图像数据
        memcpy(dstBuffer+i*dstLInesize,m_FrameBuffer + i * srcLinesize,srcLinesize);
    }

    // 解锁当前Window，渲染缓冲区数据
    ANativeWindow_unlockAndPost(m_ANativeWindow);

}