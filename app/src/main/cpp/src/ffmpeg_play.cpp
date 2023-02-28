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


}


void ffmpeg_play::release() {


}