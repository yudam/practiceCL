//
// Created by 毛大宇 on 2023/3/21.
//

#ifndef PRACTICECL_NALUPARSE_H
#define PRACTICECL_NALUPARSE_H

#include "android/log.h"
#include <iostream>
#include <fstream>

/**
 *
 * H264原始裸流是由一组NALU单元组成
 *
 * GOP图像组：主要用作形容一个IDR帧到下一个IDR帧的间隔
 *
 * 片 :
 *
 * 宏块 :
 *
 *
 *
 * NALU = [StartCode]+[NALU Header] + [NALU Payload]
 *
 * StartCode: 表示一个NALU的起始,0x00000001 或者0x000001,
 *            三个字节的起始码只在一个完整的帧被编为多个Slice片的时候使用，
 *            包含这些Slice的NALU使用三个字节的起始码
 *
 * NALU Header : 占一个字节，共8位
 *    1  : 禁止位,forbidden_zero_bit,固定为0
 *    2~3: 重要性指示位置,nal_ref_idc,取值00～11，也就是0到3，值越高表示重要性越高
 *    3~8: nal_unit_type, H264使用1到12的值
 */

typedef unsigned char BYTE;

typedef struct {
    int startcodeprefix_len;
    unsigned len;
    unsigned max_size;
    int forbidden_bit;
    int nal_reference_idc;
    int nal_unit_type;
    char * buf;

}NALU_t;

typedef enum {
    NALU_TYPE_SLICE    = 1,
    NALU_TYPE_DPA      = 2,
    NALU_TYPE_DPB      = 3,
    NALU_TYPE_DPC      = 4,
    NALU_TYPE_IDR      = 5,
    NALU_TYPE_SEI      = 6,
    NALU_TYPE_SPS      = 7,
    NALU_TYPE_PPS      = 8,
    NALU_TYPE_AUD      = 9,
    NALU_TYPE_EOSEQ    = 10,
    NALU_TYPE_EOSTREAM = 11,
    NALU_TYPE_FILL     = 12,
}NaluType;

typedef enum {
    NALU_PRIORITY_DISPOSABLE = 0,
    NALU_PRIRITY_LOW         = 1,
    NALU_PRIORITY_HIGH       = 2,
    NALU_PRIORITY_HIGHEST    = 3
}NaluPriority;

class NALUParse {

public:
    int findNalStartCode3(uint8_t *buffer);

    int findNalStartCode4(uint8_t *buffer);

    int getAnnexbNALU(NALU_t *nalu);

    void parse_h264stream(const char * url);

private:
    FILE * h264bitStream = nullptr;
};


#endif //PRACTICECL_NALUPARSE_H
