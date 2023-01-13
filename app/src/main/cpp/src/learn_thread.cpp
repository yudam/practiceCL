//
// Created by 毛大宇 on 2023/1/11.
//

#include "learn_thread.h"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,"thread",__VA_ARGS__)


using namespace std;

void print(){
    LOGI("创建了 Thread  : ");
    cout << "" << endl;
}

void learn_thread::event() {

    thread t1(print);
}

void learn_thread::message() {


}