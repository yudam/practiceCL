#include <jni.h>
#include <string>


#define NATIVE_LINK_CLASS "com/mdy/practicecl/MainActivity"



/**
 * 动态注册
 * Java中调用 System.loadLibrary时，会主动调用JNI_OnLoad方法，通过RegisterNatives方法
 * 调用m_Jni_Method来注册Java和native函数之间的关联
 */
extern "C" {

jstring native_stringFromJNI(JNIEnv *jniEnv, jobject object) {
    return jniEnv->NewStringUTF("abc");
}

jint native_simpleCl(JNIEnv *jniEnv, jobject object) {
    return 1;
}

void native_testStruct(JNIEnv *jniEnv, jobject object) {
}

/**
* 利用 JNINativeMethod结构体数组来保存Java Native函数和JNI函数的对应关系
*  Java中方法
*  对应的方法签名    :JNI中属性描述符和方法描述， [I表示int数组
*  Native映射的函数 :将native函数名转化为void*类型
*/
static JNINativeMethod m_Jni_Method[] = {
        {"stringFromJNI", "()Ljava/lang/String;", (void *) native_stringFromJNI},
        {"simpleCl",      "()I",                  (void *) native_simpleCl},
        {"testStruct",    "()V",                  (void *) native_testStruct}
};

jint JNI_OnLoad(JavaVM *jvm, void *p) {

    JNIEnv *env = NULL;
    if ((jvm->GetEnv((void **) &env, JNI_VERSION_1_6)) != JNI_OK) {
        return JNI_ERR;
    }
    jclass clazz = env->FindClass(NATIVE_LINK_CLASS);

    if (clazz = NULL) {
        return JNI_ERR;
    }
    env->RegisterNatives(clazz, m_Jni_Method, sizeof(m_Jni_Method) / sizeof(m_Jni_Method[0]));

    return JNI_VERSION_1_6;
}

}