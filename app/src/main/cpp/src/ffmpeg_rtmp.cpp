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
static void yunxi_ffmpeg_log_callback(void *ptr, int level, const char *fmt, va_list vl){
    int cnt;
    memset(libffmpeg_log_buf_prefix, 0, LOG_BUF_PREFIX_SIZE);
    memset(libffmpeg_log_buf, 0, LOG_BUF_SIZE);

    cnt = snprintf(libffmpeg_log_buf_prefix, LOG_BUF_PREFIX_SIZE, "%s", fmt);
    cnt = vsnprintf(libffmpeg_log_buf, LOG_BUF_SIZE, libffmpeg_log_buf_prefix, vl);

    if(level == AV_LOG_ERROR){
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

    int videoIndex;
    int frame_index = 0;
    int ret;
    int64_t start_time;


    AVPacket pkt;

    av_register_all();

    avformat_network_init();

    /**
     * 1. 初始化文件读取模块
     * ifmt_ctx初始化为NULL，如果文件打开成功，ifmt_ctx会被设置成非NULL的值
     * avformat_open_input可以打开多种来源的数据，比如本地路径、rtmp拉流地址等
     */
    if (avformat_open_input(&ifmt_ctx, in_filename, NULL, NULL) < 0) {
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


    for (int i = 0; i < ifmt_ctx->nb_streams; i++) {
        if (ifmt_ctx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            videoIndex = i;
            break;
        }
    }

    // 打印关于输入或者输出格式的详细信息，包括比特率，流，容器等数据，最后一个参数0代表输入，1代表输出
    av_dump_format(ifmt_ctx, 0, in_filename, 0);
//
//    LOGI("av_dump_format   %d ", videoIndex);
//
//    const char *mime_type = ifmt_ctx->iformat->mime_type;
//
//    const char *name = ifmt_ctx->iformat->name;
//
//
//    LOGI("duration =   %d  bitrate = %d    filename = %s  name = %s   mime = %s", ifmt_ctx->duration, ifmt_ctx->bit_rate, ifmt_ctx->filename, name, mime_type);

    /**
     * 2. 初始化ffmpeg输出模块
     * ofmt_ctx初始化为NULL，如果打开成功，ofmt_ctx会被设置为非NULL的值
     * 输出流采用flv格式
     */

    avformat_alloc_output_context2(&ofmt_ctx, NULL, "flv", out_filename);

    if (!ofmt_ctx) {
        LOGI(" 创建 ofmt_ctx 失败 ");
        return;
    }

    AVOutputFormat *ofmt = ofmt_ctx->oformat;

    /**
     * 遍历输入流的所有轨道，拷贝编解码参数到输出流
     * 注意的是如果视频轨道超出1个，则后续avformat_write_header会一直返回失败-22
     */
    for (int i = 0; i < ifmt_ctx->nb_streams; i++) {
        AVStream *in_stream = ifmt_ctx->streams[i];
        LOGI("stream is : %d    is video:  %d",in_stream->codecpar->codec_tag,in_stream->codecpar->codec_type == AVMEDIA_TYPE_VIDEO);

        // 根据输入流创建输出码流的AVStream
        AVStream *out_stream = avformat_new_stream(ofmt_ctx, in_stream->codec->codec);
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
        out_stream->codec->codec_tag = 0;
        if (ofmt_ctx->oformat->flags & AVFMT_GLOBALHEADER) {
            out_stream->codec->flags = out_stream->codec->flags | CODEC_FLAG_GLOBAL_HEADER;
        }

    }

    // 打印输出流的格式信息
    av_dump_format(ofmt_ctx, 0, out_filename, 1);


    if (!(ofmt->flags & AVFMT_NOFILE)) {
        // 打开输出文件
        ret = avio_open(&ofmt_ctx->pb, out_filename, AVIO_FLAG_WRITE);
        if (ret < 0) {
            LOGI(" 输入URL打不开： %s", out_filename);
            return;
        }
    }

    AVCodecParameters *codecpar = ofmt_ctx->streams[0]->codecpar;

    LOGI("bit_rate: %d  ,  width: %d , height: %d  ,  codec_tag: %d ",
         codecpar->bit_rate,codecpar->width,codecpar->height,codecpar->codec_tag);


    // 写头文件
    ret = avformat_write_header(ofmt_ctx, NULL);

    if (ret < 0) {
        LOGI(" 写入头文件失败 ：%d", ret);
        return;
    }
    /** 3. 读取ts流的每一帧，并进行时间基转换，然后推流到RTMP服务器*/
    start_time = av_gettime();

    while (1) {
        AVStream *in_stream, *out_stream;

        ret = av_read_frame(ifmt_ctx, &pkt);

        if (ret < 0) {
            LOGI(" av_read_frame 失败 ");
            break;
        }

        // 写入pts
        if (pkt.pts == AV_NOPTS_VALUE) {
            // time_base：时基，通过该值可以将pts和dts转化为真正的时间
            AVRational time_base1 = ifmt_ctx->streams[videoIndex]->time_base;
            // 计算两帧之间的时间
            int64_t calc_duration = (double) AV_TIME_BASE / av_q2d(ifmt_ctx->streams[videoIndex]->r_frame_rate);
            // 匹配参数
            pkt.pts = double(frame_index * calc_duration) / (double) (av_q2d(time_base1) * AV_TIME_BASE);
            pkt.dts = pkt.pts;
            pkt.duration = (double) calc_duration / (double) (av_q2d(time_base1) * AV_TIME_BASE);
        }

        // 延迟发送，防止发送过快
        if (pkt.stream_index == videoIndex) {
            AVRational time_base = ifmt_ctx->streams[videoIndex]->time_base;
            AVRational time_base_q = {1, AV_TIME_BASE};
            //计算视频播放时长
            int64_t pts_time = av_rescale_q(pkt.dts, time_base, time_base_q);
            //计算实际视频的播放时长
            int64_t now_time = av_gettime() - start_time;

            if (pts_time > now_time) {
                // 睡眠一段时间，目的是实现播放时间同步
                av_usleep(pts_time - now_time);
            }
        }

        in_stream = ifmt_ctx->streams[pkt.stream_index];
        out_stream = ofmt_ctx->streams[pkt.stream_index];


        pkt.pts = av_rescale_q_rnd(pkt.pts, in_stream->time_base, out_stream->time_base, (AVRounding) (AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX));
        pkt.dts = av_rescale_q_rnd(pkt.pts, in_stream->time_base, out_stream->time_base, (AVRounding) (AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX));


        // 可能pts < dts,直接下一帧
        if(pkt.pts <0){
            pkt.pts = 0;
        }

        if(pkt.dts < 0){
            pkt.dts = 0;
        }

        if(pkt.pts < pkt.dts) continue;

        pkt.duration = av_rescale_q(pkt.duration,in_stream->time_base,out_stream->time_base);
        pkt.pos = -1;
        if (pkt.stream_index == videoIndex) {
            LOGI(" send frame to screen  %d", frame_index);
            frame_index++;
        }

        LOGI("  pts:  %d,   dts:  %d ,  duration:  %d",pkt.pts,pkt.dts,pkt.duration);
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
        avio_close(ofmt_ctx->pb);
    }
    avformat_free_context(ofmt_ctx);
}