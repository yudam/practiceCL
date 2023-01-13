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


void cl_1::setPoint() {

    int a = 10;
    int b = 10;


    // 常量指针，指针的指向可以修改，指针指向的值不可以修改
    const int *point1 = &a;
    //point1 = &b;   可以修改
    //*point1 = 100; 错误，不可以修改


    // 指针常量，指针的指向不可以修改，指针指向的值可以修改
    int * const point2 = &a;
    //point2 = &b; 错误，指针的指向不可以修改
    //*point2 = 100; 指针指向的值可以修改

    //指针的指向和指针指向的值都不可以修改
    const int * const point3 = &a;



}