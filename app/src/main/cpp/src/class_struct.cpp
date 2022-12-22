//
// Created by 毛大宇 on 2022/10/20.
//

#include "../include/class_struct.h"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,"class_struct",__VA_ARGS__)


void class_struct::createStruct() {

    //直接创建结构体变量，注意创建结构体变量名可以省略struct
    AudioSample m_audioSample1;
    m_audioSample1.m_channel = 2;
    m_audioSample1.m_sample_rate = 44100;

    LOGI("m_audioSample1: channel=%d ,  rate = %d",m_audioSample1.m_channel,m_audioSample1.m_sample_rate);

    //创建变量的通知赋值
    AudioSample m_audioSample2 = {3,48000};
    LOGI("m_audioSample2: channel=%d ,  rate = %d",m_audioSample2.m_channel,m_audioSample2.m_sample_rate);


    // 创建结构体指针，通过指针访问结构体中成员
    AudioSample *m_audioSample4;
    m_audioSample4->m_channel = 4;
    m_audioSample4->m_sample_rate = 96000;
    LOGI("m_audioSample4: channel=%d ,  rate = %d",m_audioSample4->m_channel,m_audioSample4->m_sample_rate);

}

/**
 * 值传递
 */
void class_struct::funValue(AudioSample audioSample) {
    LOGI("值传递前: channel=%d ,  rate = %d",audioSample.m_channel,audioSample.m_sample_rate);
    audioSample.m_channel = 100;
    audioSample.m_sample_rate = 123;
    LOGI("值传递后: channel=%d ,  rate = %d",audioSample.m_channel,audioSample.m_sample_rate);
}

/**
 * 引用传递
 */
void class_struct::funObj(AudioSample *audioSample) {
    LOGI("值传递前: channel=%d ,  rate = %d",audioSample->m_channel,audioSample->m_sample_rate);
    audioSample->m_channel = 100;
    audioSample->m_sample_rate = 123;
    LOGI("值传递后: channel=%d ,  rate = %d",audioSample->m_channel,audioSample->m_sample_rate);
}


/**
 * const修饰的引用对象不可修改
 */
void class_struct::funFinalObj(const AudioSample *audioSample) {
    //audioSample->m_channel = 6;
}