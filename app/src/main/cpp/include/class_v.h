//
// Created by 毛大宇 on 2022/10/21.
//

#include <android/log.h>

#ifndef PRACTICECL_CLASS_V_H
#define PRACTICECL_CLASS_V_H

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,"class_v",__VA_ARGS__)

/**
 * 虚函数必须在基类中使用virtual关键字标记，且必须实现
 *
 * 可以声明纯虚函数，则虚函数不必实现
 */

class class_v {

public:

    void executeVt();

    virtual void fun_vt1(){

        LOGI("------我是class_v的虚函数------");
    }

    // 纯虚函数
    virtual void fun_vt2() = 0;
};



class class_v1:public class_v{

    void fun_vt1() override{
        LOGI("------我是class_v1的虚函数------");
    }
};

#endif //PRACTICECL_CLASS_V_H
