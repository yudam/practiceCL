//
// Created by 毛大宇 on 2023/3/21.
// H.264码流分析程序

// 解析H264文件，并输出NALU列表，H264裸流由一系列的NALU单元组成，NALU之间通过StartCode进行分割
// NALU分为SPS、PPS、SEI、I Frame、P Frame、B Frame
// SPS：序列参数集合 nal_unit_type = 7 保存了视频编码序列的全局参数，一半SPS和PPS位于起始位置，中间也可能出现
// PPS：图像参数集合 nal_unit_type = 8
// SEI：补充和增强信息 nal_unit_type = 6
// I Frame : nal_unit_type = 5   slice_type = 7
// P Frame : nal_unit_type = 1   slice_type = 5
// B Frame : nal_unit_type = 1   slice_type = 6
// 每一个IDR帧NALU之前都会先插入SPS和PPS的NALU

#include "NALUParse.h"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,"cTypeLearn",__VA_ARGS__)

/**
 * c++中不存在byte类型，所以通过  unsigned char 表示一个byte字节，那么字节数组就是uint8_t指针
 * 指针数组无法获取数组的长度sizeof
 */
int NALUParse::findNalStartCode3(uint8_t *buffer) {
    if (buffer[0] == 0 && buffer[1] == 0 && buffer[2] == 1) {
       // LOGI("startCode is 0x000001");
        return 1;
    }
   // LOGI(" parse is failed");
    return 0;
}

int NALUParse::findNalStartCode4(uint8_t *buffer) {
    if (buffer[0] == 0 && buffer[1] == 0
        && buffer[2] == 0 && buffer[3] == 1) {
        //LOGI("startCode is 0x00000001");
        return 1;
    }
    //LOGI(" parse is failed");
    return 0;
}

int NALUParse::getAnnexbNALU(NALU_t *nalu) {

    int pos = 0;
    unsigned char *Buf;

    int find3 = 0;
    int find4 = 0;
    int rewind = 0;

    // 在堆中申请nalu->max_size个连续的char类型大小的空间
    if ((Buf = (unsigned char *) calloc(nalu->max_size, sizeof(char))) == nullptr) {
        LOGI("getAnnexbNALU Alloc error");
        return -1;
    }

    nalu->startcodeprefix_len = 3;

    /**
     *  fread函数表示读取数据存放在buffer中，字节大小为 1 * 3，
     *  返回3表示读取成功，0表示读取失败或者到达尾部
     */
    if (3 != fread(Buf, sizeof(char), 3, h264bitStream)) {
        free(Buf);
        LOGI("fread error");
        return -1;
    }

    find3 = findNalStartCode3(Buf);
    if (find3 != 1) {

        if (fread(Buf + 3, 1, 1, h264bitStream) != 1) {
            free(Buf);
            return -1;
        }

        find4 = findNalStartCode4(Buf);
        if (find4 != 1) {
            free(Buf);
            return -1;
        } else {
            pos = 4;
            nalu->startcodeprefix_len = 4;
        }

    } else {
        pos = 3;
        nalu->startcodeprefix_len = 3;
    }

    int StartCodeFound = 0;

    // 查找下一个StartCode的位置
    while (!StartCodeFound) {

        /**
         * 找不到下一个StartCode又读到了文件尾，因此将Buf中从StartCode之后的字节复制dao
         * nalu->buf中，
         */
        if (feof(h264bitStream)) {
            nalu->len = (pos - 1) - nalu->startcodeprefix_len;
            memcpy(nalu->buf, &Buf[nalu->startcodeprefix_len], nalu->len);
            nalu->forbidden_bit = nalu->buf[0] & 0x80;   // 1bit
            nalu->nal_reference_idc = nalu->buf[0] & 0x60;  // 2bit
            nalu->nal_unit_type = nalu->buf[0] & 0x1f; // 5bit
            free(Buf);
            return pos - 1;
        }

        // fgetc表示从文件中读取一个字节，指针往后移
        Buf[pos++] = fgetc(h264bitStream);
        find3 = findNalStartCode3(&Buf[pos - 3]);
        if (find3 != 1) {
            find4 = findNalStartCode4(&Buf[pos - 4]);
        }
        StartCodeFound = (find3 == 1 || find4 == 1);
    }

    /**
     * rewind表示找到下一个StartCode后，需要回退到上一个NALU的字节尾部
     * find3==1表示当前StartCode是0x000001，占三个字节，所以回退3个字节，反之4个字节
     */
    rewind = (find3 == 1) ? -3 : -4;

    // fseek表示设置文件指针到指定位置，这里表示回退当前指针rewind个位置
    if (0 != fseek(h264bitStream, rewind, SEEK_CUR)) {
        free(Buf);
        return -1;
    }

    // 设置NALU结构体参数
    nalu->len = (pos + rewind) - nalu->startcodeprefix_len;
    // 将Buf中从StartCode之后的字节复制到buf中
    memcpy(nalu->buf, &Buf[nalu->startcodeprefix_len], nalu->len);
    nalu->forbidden_bit = nalu->buf[0] & 0x80;   // 1bit
    nalu->nal_reference_idc = nalu->buf[0] & 0x60;  // 2bit
    nalu->nal_unit_type = nalu->buf[0] & 0x1f; // 5bit
    free(Buf);
    return pos + rewind;

}

void NALUParse::parse_h264stream(const char *url) {
    NALU_t *n;
    int buffersize = 100000;
    /**
     * 打开文件，注意 这里的h264bitStream一定要声明为空指针
     */
    h264bitStream = fopen(url, "rb+");
    if (!h264bitStream) {
        LOGI("file parse error");
        return;
    }

    // 在堆中申请1个NALU_t类型大小的内存空间
    n = (NALU_t *) calloc(1, sizeof(NALU_t));

    if (n == nullptr) {
        LOGI("Alloc NALU_t error");
        return;
    }

    n->max_size = buffersize;
    n->buf = (char *) calloc(buffersize, sizeof(char));
    if (n->buf == nullptr) {
        free(n);
        LOGI("Alloc buf error");
        return;
    }

    int data_offset = 0;
    int nal_num = 0;

    LOGI("-----+-------- NALU Table ------+---------+");
    LOGI(" NUM |    POS  |    IDC |  TYPE |   LEN   |");
    LOGI("-----+---------+--------+-------+---------+");

    // feof == 0表示未到达文件末尾，feof !=0 表示已经到达文件尾部
    while (!feof(h264bitStream)) {

        int data_length = getAnnexbNALU(n);
        char type_str[20] = {0};
        switch (n->nal_unit_type) {
            case NALU_TYPE_SLICE:
                // 将后一个字符串赋值给type_str
                sprintf(type_str, "SLICE");
                break;
            case NALU_TYPE_DPA :
                sprintf(type_str, "DPA");
                break;
            case NALU_TYPE_DPB   :
                sprintf(type_str, "DPB");
                break;
            case NALU_TYPE_DPC     :
                sprintf(type_str, "DPC");
                break;
            case NALU_TYPE_IDR      :
                sprintf(type_str, "IDR");
                break;
            case NALU_TYPE_SEI     :
                sprintf(type_str, "SEI");
                break;
            case NALU_TYPE_SPS      :
                sprintf(type_str, "SPS");
                break;
            case NALU_TYPE_PPS      :
                sprintf(type_str, "PPS");
                break;
            case NALU_TYPE_AUD      :
                sprintf(type_str, "AUD");
                break;
            case NALU_TYPE_EOSEQ    :
                sprintf(type_str, "EOSEQ");
                break;
            case NALU_TYPE_EOSTREAM :
                sprintf(type_str, "EOSTREAM");
                break;
            case NALU_TYPE_FILL     :
                sprintf(type_str, "FILL");
                break;

        }

        char idc_str[20] = {0};
        switch (n->nal_reference_idc >> 5) {
            case NALU_PRIORITY_DISPOSABLE:
                sprintf(idc_str, "DISPOS");
                break;
            case NALU_PRIRITY_LOW :
                sprintf(idc_str, "LOW");
                break;
            case NALU_PRIORITY_HIGH  :
                sprintf(idc_str, "HIGH ");
                break;
            case NALU_PRIORITY_HIGHEST :
                sprintf(idc_str, "HIGHEST");
                break;
        }

        LOGI("%5d| %8d| %7s| %6s| %8d|", nal_num, data_offset, idc_str, type_str, n->len);

        data_offset = data_offset + data_length;
        nal_num++;
    }

}