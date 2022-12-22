//
// Created by 毛大宇 on 2022/10/19.
//

#include "../include/class_simple1.h"
#include <string>


#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,"class_simple1",__VA_ARGS__)


void class_simple1::type_op() {

    m_name = "test-1";
    m_name.append("add_test_2");
}

void class_simple1::type_pointer() {

    // datatype *var_name;

    int *m_int_pointer;

    int x = 10;

    // 为指针赋值的两种形式
    *m_int_pointer = x;
    int *m_pint = &x;


    //数组名称作为指针，默认指向第一个元素
    int arr[] = {10, 100, 1000};

    //将arr[0]的地址分配给指针
    int *ptr_1 = arr;

    for (int i = 0; i < 3; i++) {

        //通过+1操作将数组元素的地址赋值给指针
        ptr_1++;
    }
}

//引用传递，函数内部变量值的改变会通知到外部的变量
void class_simple1::type_swap(int &a, int &b) {
    a=  100;
}

class_simple1::class_simple1(const int16_t &mInt16T) : m_int16_t(mInt16T) {}
