//
// Created by 毛大宇 on 2023/1/9.
//

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,"ffmpeg_play",__VA_ARGS__)


#include "../include/ffmpeg_play.h"
#include <string>


/**
 * ffmpeg播放本地，网络视频流
 *
 */

ffmpeg_play::ffmpeg_play() {

    LOGI("ffmpeg_play 对象初始化了");
}

ffmpeg_play::~ffmpeg_play() {

}


void ffmpeg_play::init(char *url) {
    m_Url = url;
}


void ffmpeg_play::prepare() {

    /**
     * 1. 创建封装格式上下文
     */
    m_AVFormatContext = avformat_alloc_context();

    /**
     * 2. 打开输入文件，解封装
     */
    if (avformat_open_input(&m_AVFormatContext, m_Url, NULL, NULL) != 0) {
        LOGI("avformat_open_input error");
        return;
    }

    /**
     * 3. 获取音视频流信息
     */
    if (avformat_find_stream_info(m_AVFormatContext, NULL) < 0) {
        LOGI("avformat_find_stream_info error");
        return;
    }

    /**
     * 4. 获取音视频流索引
     * 循环查找视频中包含的流信息，直到找到视频类型的流，记录下来
     */
    for (int i = 0; i < m_AVFormatContext->nb_streams; i++) {
        if (m_AVFormatContext->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            m_StreamIndex = i;
            break;
        }
    }

    if (m_StreamIndex == -1) {
        LOGI("m_StreamIndex error");
        return;
    }

    /**
     * 5. 获取解码器参数
     */
    AVCodecParameters *codecpar = m_AVFormatContext->streams[m_StreamIndex]->codecpar;


    /**
     * 6. 根据codeid获取解码器
     */
    m_AVCodec = avcodec_find_decoder(codecpar->codec_id);
    if (m_AVCodec == NULL) {
        LOGI("avcodec_find_decoder error");
        return;
    }

    /**
     * 7. 创建解码器上下文
     */
    m_AVCodecContext =  avcodec_alloc_context3(m_AVCodec);

    /**
     * 8. 打开解码器
     */
   if(avcodec_open2(m_AVCodecContext,m_AVCodec,NULL) != 0){
       LOGI("avcodec_open2  error");
       return;
   }

   /**
    * 9. 创建存储编码和解码数据的结构体
    */
    m_AVPacket = av_packet_alloc();
    m_AVFrame = av_frame_alloc();

    /**
     * 10. 解码循环
     */

    //读取一帧数据
    while (av_read_frame(m_AVFormatContext,m_AVPacket) >= 0){
        if(m_AVPacket->stream_index == m_StreamIndex){
            //发送数据包到解码队列，视频解码
            if(avcodec_send_packet(m_AVCodecContext,m_AVPacket) != 0){
                return;
            }
            //接收一帧解码数据，数据存放在m_AVFrame结构体中
            while (avcodec_receive_frame(m_AVCodecContext,m_AVFrame) == 0){

            }
        }
        //释放m_AVPacket引用
        av_packet_unref(m_AVPacket);
    }

    /**
     * 11. 释放资源，解码完成
     */
     if(m_AVPacket != nullptr){
         av_packet_free(&m_AVPacket);
     }

     if(m_AVFrame  != nullptr){
         av_frame_free(&m_AVFrame);
     }

     if(m_AVCodecContext != nullptr){
         avcodec_close(m_AVCodecContext);
         avcodec_free_context(&m_AVCodecContext);
         m_AVCodecContext = nullptr;
         m_AVCodec = nullptr;
     }

     if(m_AVFormatContext != nullptr) {
         avformat_close_input(&m_AVFormatContext);
         avformat_free_context(m_AVFormatContext);
         m_AVFormatContext = nullptr;
     }
}


void ffmpeg_play::release() {


}