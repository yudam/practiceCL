//
// Created by 毛大宇 on 2022/10/12.
//

#include "cl_1.h"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,"MDY",__VA_ARGS__)



cl_1::cl_1(int a, char msg) {
    m_age = a;
    m_msg = msg;
    LOGI("构造函数调用了-------");
}

cl_1::~cl_1() {
    LOGI("析构函数调用了------");
}

void cl_1::setAge(int a) {
    m_age = a;
}

int cl_1::getAge() {

    return m_age;
}