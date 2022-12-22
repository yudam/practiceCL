//
// Created by 毛大宇 on 2022/11/23.
//

#include "learn_c_base_yf.h"
#include <string>

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,"learn_c_base_yf",__VA_ARGS__)


using namespace std;


/**
 * NULL默认为0，在调用函数时会变成常量参数，如test(int)和test(int*)
 * nullptr是C++11提出来替代NULL的方案
 */

/**
 * void指针作为函数的输入和输出
 * 表示接受任意类型的指针输入和指针输出
 */
void* parseVoidPoint(void* input) {

    void* p1 = 0;
    void* p2 = NULL;

    return input;
}
