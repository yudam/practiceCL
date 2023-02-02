# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.
# cmake需要的最低版本

cmake_minimum_required(VERSION 3.18.1)

# Declares and names the project.

project("practicecl")

# 设置so库生成的路径
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI})


# 设置头文件搜索路径，这样可以直接添加头文件，而不需要加上头文件的路径
include_directories(${CMAKE_SOURCE_DIR}/include/)
include_directories(${CMAKE_SOURCE_DIR}/thirdparty/ffmpeg/)
include_directories(${CMAKE_SOURCE_DIR}/thirdparty/ffmpeg/include/)
include_directories(${CMAKE_SOURCE_DIR}/thirdparty/ffmpeg/libyuv/)


# 输出msg

message("CMAKE_CURRENT_SOURCE_DIR:"+${CMAKE_CURRENT_SOURCE_DIR})
message("CMAKE_SOURCE_DIR:"+${CMAKE_SOURCE_DIR})
message("PROJECT_SOURCE_DIR:"+${PROJECT_SOURCE_DIR})
message("PROJECT_NAME:"+${PROJECT_NAME})
message("ANDROID_ABI:"+${ANDROID_ABI})


# 对ffmpeg相关的库设置别名 方便链接
set(FFMPEG_libavcodec ${CMAKE_SOURCE_DIR}/thirdparty/prebuilt/${ANDROID_ABI}/libavcodec.a)
set(FFMPEG_libavfilter ${CMAKE_SOURCE_DIR}/thirdparty/prebuilt/${ANDROID_ABI}/libavfilter.a)
set(FFMPEG_libavformat ${CMAKE_SOURCE_DIR}/thirdparty/prebuilt/${ANDROID_ABI}/libavformat.a)
set(FFMPEG_libavutil ${CMAKE_SOURCE_DIR}/thirdparty/prebuilt/${ANDROID_ABI}/libavutil.a)
set(FFMPEG_libcrypto ${CMAKE_SOURCE_DIR}/thirdparty/prebuilt/${ANDROID_ABI}/libcrypto.a)
set(FFMPEG_libfdk_aac ${CMAKE_SOURCE_DIR}/thirdparty/prebuilt/${ANDROID_ABI}/libfdk-aac.a)
set(FFMPEG_libpostproc ${CMAKE_SOURCE_DIR}/thirdparty/prebuilt/${ANDROID_ABI}/libpostproc.a)
set(FFMPEG_librtmp ${CMAKE_SOURCE_DIR}/thirdparty/prebuilt/${ANDROID_ABI}/librtmp.a)
set(FFMPEG_libssl ${CMAKE_SOURCE_DIR}/thirdparty/prebuilt/${ANDROID_ABI}/libssl.a)
set(FFMPEG_libswresample ${CMAKE_SOURCE_DIR}/thirdparty/prebuilt/${ANDROID_ABI}/libswresample.a)
set(FFMPEG_libswscale ${CMAKE_SOURCE_DIR}/thirdparty/prebuilt/${ANDROID_ABI}/libswscale.a)
set(FFMPEG_libx264 ${CMAKE_SOURCE_DIR}/thirdparty/prebuilt/${ANDROID_ABI}/libx264.a)
set(FFMPEG_libyuv ${CMAKE_SOURCE_DIR}/thirdparty/prebuilt/${ANDROID_ABI}/libyuv.a)



message("FFMPEG_libavcodec:"+${FFMPEG_libavcodec})

# 设置cpp文件的位置 ，方便后面添加

file(GLOB FILE_C "${PROJECT_SOURCE_DIR}/src/*.cpp")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
# 添加源文件或者库，主要用以生成对应的so库，如果想生成多个so库，可以添加多个add_library

add_library( # Sets the name of the library.
        # 设置生成so库的名称
        practicecl

        # Sets the library as a shared library.
        # 设置库的类型，一种是静态的STATIC .a文件，一种是动态的SHARED so文件，一般是so文件
        SHARED

        # Provides a relative path to your source file(s).

        # 要编译的c/c++文件
        ${FILE_C}
        native-lib.cpp)

# 添加ffmpeg库
message("FFMPEG_libcrypto : ${FFMPEG_libcrypto}")



# 用来添加编译本地库时需要用到的一些库，主要用于查找系统库
# 由于cmake已经知道系统路径，所以这里请log库直接添加，起了个别名log-lib，方便后面链接

find_library( # Sets the name of the path variable.
        # 定义路径变量的名称，并用这个变量存储NDK库的位置
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        # 指定Cmake需要定位的NDK库的名称
        log)

# 编译so时需要链接所有的库，包括本地库、系统库（需要使用${}包裹起来）

target_link_libraries( # Specifies the target library.

        # 指定目标库(这里的practicecl指的是我们自己创建的源生库)
        practicecl

        # 不加入下面两个，直接编译报错，第三方库找不到，原因未知
        z
        android

        # 链接ffmpeg相关的库
        ${FFMPEG_libavfilter}
        ${FFMPEG_libavformat}
        ${FFMPEG_libavcodec}
        ${FFMPEG_libpostproc}
        ${FFMPEG_libswresample}
        ${FFMPEG_libswscale}
        ${FFMPEG_libavutil}
        ${FFMPEG_libfdk_aac}
        ${FFMPEG_libx264}
        ${FFMPEG_librtmp}
        ${FFMPEG_libyuv}
        ${FFMPEG_libssl}
        ${FFMPEG_libcrypto}

        # Links the target library to the log library
        # included in the NDK.
        # 将日志库链接到目标库，也就是我们创建的practicecl库
        ${log-lib}
        )
