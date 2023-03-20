/**
 *  Created by 毛大宇 on 2023/1/11.
 *  推送本地视频到流媒体服务器
 *
 */

#include "ffmpeg_rtmp.h"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,"ffmpeg_rtmp",__VA_ARGS__)
#define LOG_BUF_PREFIX_SIZE 512
#define LOG_BUF_SIZE 1024
char libffmpeg_log_buf_prefix[LOG_BUF_PREFIX_SIZE];
char libffmpeg_log_buf[LOG_BUF_SIZE];


/**
 * 设置ffmpeg的日志回调，用于打印输出
 * 这里设置的日志级别是AV_LOG_ERROR，也就是错误日志的输出
 */
static void yunxi_ffmpeg_log_callback(void *ptr, int level, const char *fmt, va_list vl) {
    int cnt;
    memset(libffmpeg_log_buf_prefix, 0, LOG_BUF_PREFIX_SIZE);
    memset(libffmpeg_log_buf, 0, LOG_BUF_SIZE);

    cnt = snprintf(libffmpeg_log_buf_prefix, LOG_BUF_PREFIX_SIZE, "%s", fmt);
    cnt = vsnprintf(libffmpeg_log_buf, LOG_BUF_SIZE, libffmpeg_log_buf_prefix, vl);

    if (level == AV_LOG_ERROR) {
        LOGI("%s", libffmpeg_log_buf);
    }
    return;
}


/**
 * 预初始化参数，设置ffmpeg日志回调
 */
void ffmpeg_rtmp::preInit(const char *url, const char *rtmp) {
    in_filename = url;
    out_filename = rtmp;
    av_log_set_level(AV_LOG_ERROR);
    av_log_set_flags(AV_LOG_SKIP_REPEATED);
    av_log_set_callback(yunxi_ffmpeg_log_callback);
}


/**
 * 读取本地文件并推送到rtmp服务器
 */
void ffmpeg_rtmp::init() {

    int ret;
    int stream_index = 0;
    int stream_mapping_size = 0;
    int *stream_mapping = nullptr;


    AVPacket pkt;

    av_register_all();

    avformat_network_init();

    /**
     * 1. 初始化文件读取模块
     * ifmt_ctx初始化为NULL，如果文件打开成功，ifmt_ctx会被设置成非NULL的值
     * avformat_open_input可以打开多种来源的数据，比如本地路径、rtmp拉流地址等
     */
    if (avformat_open_input(&ifmt_ctx, in_filename, nullptr, nullptr) < 0) {
        LOGI(" 打不开输入文件  : %s", in_filename);
        return;
    }

    /**
     * 通过AVFormatContext获取媒体信息
     */
    if (avformat_find_stream_info(ifmt_ctx, NULL) < 0) {
        LOGI(" 输入流信息错误 ");
        return;
    }

    // 打印关于输入或者输出格式的详细信息，包括比特率，流，容器等数据，最后一个参数0代表输入，1代表输出
    av_dump_format(ifmt_ctx, 0, in_filename, 0);

    /**
     * 2. 初始化ffmpeg输出模块
     * ofmt_ctx初始化为NULL，如果打开成功，ofmt_ctx会被设置为非NULL的值
     * 输出流采用flv格式
     */

    avformat_alloc_output_context2(&ofmt_ctx, nullptr, "flv", out_filename);

    if (!ofmt_ctx) {
        LOGI(" 创建 ofmt_ctx 失败 ");
        return;
    }

    stream_mapping_size = ifmt_ctx->nb_streams;
    stream_mapping = static_cast<int *>(av_mallocz_array(stream_mapping_size, sizeof(*stream_mapping)));

    AVOutputFormat *ofmt = ofmt_ctx->oformat;

    AVRational frame_rate;
    double duration;

    /**
     * 遍历输入流的所有轨道，拷贝编解码参数到输出流
     * 注意的是如果视频轨道超出1个，则后续avformat_write_header会一直返回失败-22
     */
    for (int i = 0; i < ifmt_ctx->nb_streams; i++) {
        AVStream *out_stream;
        AVStream *in_stream = ifmt_ctx->streams[i];
        AVCodecParameters *codecpar = in_stream->codecpar;

        if (codecpar->codec_type != AVMEDIA_TYPE_VIDEO &&
            codecpar->codec_type != AVMEDIA_TYPE_AUDIO &&
            codecpar->codec_type != AVMEDIA_TYPE_SUBTITLE) {
            stream_mapping[i] = -1;
            continue;
        }

        if (codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            frame_rate = av_guess_frame_rate(ifmt_ctx, in_stream, nullptr);
            duration = (frame_rate.num && frame_rate.den ? av_q2d((AVRational) {frame_rate.den, frame_rate.num}) : 0);
        }

        stream_mapping[i] = stream_index++;

        LOGI("stream is : %d    is video:  %d", codecpar->codec_tag, codecpar->codec_type == AVMEDIA_TYPE_VIDEO);
        // 根据输入流创建输出码流的AVStream
        out_stream = avformat_new_stream(ofmt_ctx, nullptr);
        if (!out_stream) {
            LOGI(" 输出流失败 ");
            return;
        }

        // 拷贝输入流的上下文参数
        ret = avcodec_parameters_copy(out_stream->codecpar, in_stream->codecpar);
        if (ret < 0) {
            LOGI(" 复制 context  失败 ");
            return;
        }

        // codec_tag要重置为0，否则后续的avformat_write_header可能返回失败
        out_stream->codecpar->codec_tag = 0;
    }

    // 打印输出流的格式信息
    av_dump_format(ofmt_ctx, 0, out_filename, 1);


    if (!(ofmt->flags & AVFMT_NOFILE)) {
        // 创建并初始化一个AVIOContext，用来访问URL指定的资源
        ret = avio_open(&ofmt_ctx->pb, out_filename, AVIO_FLAG_WRITE);
        if (ret < 0) {
            LOGI(" 输入URL打不开： %s", out_filename);
            return;
        }
    }

    AVCodecParameters *codecpar = ofmt_ctx->streams[0]->codecpar;

    LOGI("bit_rate: %d  ,  width: %d , height: %d  ,  codec_tag: %d ",
         codecpar->bit_rate, codecpar->width, codecpar->height, codecpar->codec_tag);


    // 写头文件
    ret = avformat_write_header(ofmt_ctx, NULL);

    if (ret < 0) {
        LOGI(" 写入头文件失败 ：%d", ret);
        return;
    }

    /**
     * 循环读取packet，然后推流
     */
    while (1) {

        AVStream *in_stream, *out_stream;

        // 从输入流中读取一个packet
        ret = av_read_frame(ifmt_ctx, &pkt);
        //  -541478725表示文件读取结束
        if (ret < 0) {
            break;
        }

        // 根据packet中stream_index获取输入的AVStream
        in_stream = ifmt_ctx->streams[pkt.stream_index];
        int codec_type = in_stream->codecpar->codec_type;
        // 根据视频的时间戳来休眠指定时间
        if (codec_type == AVMEDIA_TYPE_VIDEO) {
            LOGI("   sleep  time : %d", duration * AV_TIME_BASE);
            av_usleep((int64_t) (duration * AV_TIME_BASE));
        }

        // 根据packet中stream_index获取输出的AVStream
        out_stream = ofmt_ctx->streams[pkt.stream_index];
        av_packet_rescale_ts(&pkt, in_stream->time_base, out_stream->time_base);
        pkt.pos = -1;

        // 将AVPacket（存储视频压缩码流）写入文件
        ret = av_interleaved_write_frame(ofmt_ctx, &pkt);
        if (ret < 0) {
            LOGI("error muxing packet");
            break;
        }
        av_free_packet(&pkt);
    }
    // 写文件尾
    av_write_trailer(ofmt_ctx);

    release();
}

void ffmpeg_rtmp::release() {

    avformat_close_input(&ifmt_ctx);
    if (ofmt_ctx && !(ofmt_ctx->flags & AVFMT_NOFILE)) {
        avio_closep(&ofmt_ctx->pb);
    }
    avformat_free_context(ofmt_ctx);
}