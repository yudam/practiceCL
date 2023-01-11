//
// Created by 毛大宇 on 2022/12/20.
//

#ifndef PRACTICECL_FFMPEG_TEST_H
#define PRACTICECL_FFMPEG_TEST_H
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,"ffmpeg",__VA_ARGS__)


//告诉编译器将指定的函数用C规则编译，用于函数重载
extern "C" {

#include <string>
#include "libavcodec/avcodec.h"


using namespace std;

string learn_ffmpeg_base() {

    const char *config = avcodec_configuration();

    LOGI("learn_ffmpeg_base: %s", config);
    return config;
}

string test_ffmpeg() {

    string hello = "ffmpeg - test :";

    int version = avcodec_version();

    LOGI("ffmpeg version: %d", version);

    return hello + to_string(version);

}


typedef struct AVFormat {

};

class AVSample {

    void testSample(void *p) {

    }

    void *testSample2() {

    }

    void *testSample3(void *p) {

    }
};
};

#endif //PRACTICECL_FFMPEG_TEST_H
