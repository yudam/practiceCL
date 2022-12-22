//
// Created by 毛大宇 on 2022/11/23.
//

#include <android/log.h>

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,"learn_c_base_yf",__VA_ARGS__)

#ifndef PRACTICECL_LEARN_C_BASE_YF_H
#define PRACTICECL_LEARN_C_BASE_YF_H

struct IntModel {

    signed int int_model;
    unsigned int us_int_model;
};


typedef struct CPoint {

    int64_t long_point;
    uint64_t u_long_point;

} CPoint;


#endif //PRACTICECL_LEARN_C_BASE_YF_H
