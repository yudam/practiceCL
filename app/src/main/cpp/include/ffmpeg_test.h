//
// Created by 毛大宇 on 2022/12/20.
//

#ifndef PRACTICECL_FFMPEG_TEST_H
#define PRACTICECL_FFMPEG_TEST_H

//告诉编译器将指定的函数用C规则编译，用于函数重载
extern "C" {

#include "../thirdparty/ffmpeg/include/libavutil/samplefmt.h"
#include "../thirdparty/ffmpeg/include/libavutil/eval.h"


typedef struct AVFormat {

    enum AVSampleFormat avSampleFormat;
};

    class AVSample{

        void testSample(void *p){

        }

        void* testSample2(){

        }

        void* testSample3(void *p){

        }
    };
};

#endif //PRACTICECL_FFMPEG_TEST_H
