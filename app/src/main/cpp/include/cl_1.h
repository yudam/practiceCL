//
// Created by 毛大宇 on 2022/10/12.
//

#include <android/log.h>
#include <jni.h>


#ifndef PRACTICECL_CL_1_H
#define PRACTICECL_CL_1_H


class cl_1 {

private:
    // 私有属性
    static  const int m_months = 12;
    int m_age;
    char m_msg;

public:

    //构造函数
    cl_1(int a,char msg);

    //析构函数
    ~cl_1();

    void setAge(int a);
    int getAge();

};


#endif //PRACTICECL_CL_1_H
