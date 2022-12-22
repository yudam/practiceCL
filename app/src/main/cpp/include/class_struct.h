//
// Created by 毛大宇 on 2022/10/20.
//



#ifndef PRACTICECL_CLASS_STRUCT_H
#define PRACTICECL_CLASS_STRUCT_H

#include <android/log.h>

/**
 * 创建结构体
 * typedef struct className
 */
typedef struct AudioSample {
    int m_channel;
    int m_sample_rate;
} m_audioSample3;

class class_struct {

    AudioSample m_in_AudioSample;
    AudioSample m_out_AudiSample;
    int m_model;

public:

    void createStruct();

    void funValue(AudioSample audioSample);

    void funObj(AudioSample *audioSample);

    void funFinalObj(const AudioSample *audioSample);
};


#endif //PRACTICECL_CLASS_STRUCT_H
