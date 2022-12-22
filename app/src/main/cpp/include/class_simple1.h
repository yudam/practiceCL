//
// Created by 毛大宇 on 2022/10/19.
//

#include <valarray>
#include <string>
#include <android/log.h>


#ifndef PRACTICECL_CLASS_SIMPLE1_H
#define PRACTICECL_CLASS_SIMPLE1_H

/**
 * 预处理指令
 * #include  文件包含，告诉编译器在源代码程序中包含一个文件
 *
 * #define 宏 定义了全局变量
 *
 * #ifndef  编译程序的特定部分，或者根据条件跳过程序的某些部分
 */
#define SIMPLE_NUM = 10

using namespace std;

class class_simple1 {

private:
    //数据类型
    char m_char;
    bool m_isOpen;
    static const int m_int = 0;
    float m_float;
    double m_double;
    wchar_t m_wchar_t;

    //int整型，占用字节大小不一样，等于short，int，long，long long
    int16_t  m_int16_t;
    int_fast16_t  m_int_fast16_t;
    int_least16_t m_int_least16_t;

    //定义数组
    int arr1[10];
    int arr2[m_int];
    int arr3[6] = {1, 2, 3, 4};


    //定义字符串
    string m_name;

public:
    //签名类型
    signed int m_signed_int = INT_MAX;
    //有签名类型
    unsigned int m_unsigned_int = INT_MAX;
    //短类型
    short int m_short_int = 12;
    //长类型
    long int m_long_int = 99;


    class_simple1(const int16_t &mInt16T);

    void temp(int a, int b) {
        if (a > b || a == b) {
        }

        for (int i = a; i < b; i++) {

            if (i == 5) {
                continue;
            } else if (i == 6) {
                break;
            }
        }

        while (a < b) {
            a++;
        }
    }

    void gotolabel(int a, int b) {

        switch (a) {
            case 1:
                break;
            case 2:
                break;
            default:
                break;

        }
    }

    // 值传递 fun(10)

    void fun(int a) {
        a = 100;
    }

    //引用传递 dun(&a)
    void fun(int *p) {
        *p = 30;
    }


    void type_op();


    void type_pointer();

    void type_swap(int &a,int &b);
};


#endif //PRACTICECL_CLASS_SIMPLE1_H
