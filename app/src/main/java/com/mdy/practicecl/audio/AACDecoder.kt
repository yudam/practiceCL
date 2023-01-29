package com.mdy.practicecl.audio

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import java.nio.ByteBuffer
import kotlin.concurrent.thread

/**
 * AAC音频解码
 * 注意点在于解码的AAC数据是否包含ADTS Header，如果包含，则必须给MediaFormat设置csd-0,
 */
class AACDecoder(audioPath: String) {

    companion object {

        private const val TIMEOUT_DECODER = 10000L
        private const val AAC_MIME_TYPE = "audio/mp4a-latm"
        private const val CSD_INDEX = "csd-0"
    }

    private var callback: AudioFrameCallback? = null

    private var mediaExtractor: MediaExtractor
    private var aacDecoder: MediaCodec
    private var audioFormat: MediaFormat? = null
    private var headerArray = byteArrayOf()
    private var sampleRate = 44100
    private var channelCount = 2
    private var isDecoder = true

    fun setAudioCallback(audioCallback: AudioFrameCallback) {
        callback = audioCallback
    }

    init {
        mediaExtractor = getMediaExtractor(audioPath)
        val mMediaFormat = MediaFormat.createAudioFormat(AAC_MIME_TYPE, sampleRate, channelCount)
        //mMediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, 2)
        //mMediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 262144)
        //解码的是AAC音频且带有 ADTS header，则必须将KEY_IS_ADTS重置为1，否则后面解码会一直失败
        mMediaFormat.setInteger(MediaFormat.KEY_IS_ADTS, 1)
        //解码的是AAC音频需要设置cds-0，不然可能解析失败
        val headerBuffer = ByteBuffer.allocate(headerArray.size)
        headerBuffer.put(headerArray)
        headerBuffer.flip()
        mMediaFormat.setByteBuffer(CSD_INDEX, headerBuffer)

        aacDecoder = MediaCodec.createDecoderByType(AAC_MIME_TYPE)
//        if(audioFormat != null){
//            aacDecoder.configure(audioFormat, null, null, 0)
//        } else {
//            aacDecoder.configure(mMediaFormat, null, null, 0)
//        }
        aacDecoder.configure(mMediaFormat, null, null, 0)
        aacDecoder.start()
    }


    private fun getMediaExtractor(oriPath: String): MediaExtractor {
        val mMediaExtractor = MediaExtractor()
        mMediaExtractor.setDataSource(oriPath)
        for (i in 0 until mMediaExtractor.trackCount) {
            val format = mMediaExtractor.getTrackFormat(i)
            val mimeType = format.getString(MediaFormat.KEY_MIME)

            if (mimeType.equals(AAC_MIME_TYPE)) {
                mMediaExtractor.selectTrack(i)
                headerArray = format.getByteBuffer(CSD_INDEX)!!.array()
                sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                Log.i("AACDecoder","header:" + headerArray.size + "  \n"
                    + headerArray[0].toInt() + "  \n"
                    + headerArray[1].toInt() + "  \n")

                Log.i("AACDecoder", "format: " + format.toString())

                audioFormat = format
            }
        }

        return mMediaExtractor
    }


    fun aacToPcm() {
        thread {
            val info = MediaCodec.BufferInfo()
            while (isDecoder) {
                val inputIndex = aacDecoder.dequeueInputBuffer(TIMEOUT_DECODER)
                if (inputIndex >= 0) {
                    val inputBuffer = aacDecoder.getInputBuffer(inputIndex)
                    if (inputBuffer != null) {
                        val readSampleDataSize = mediaExtractor.readSampleData(inputBuffer, 0)
                        //Log.i("AACDecoder", "readSampleDataSize: $readSampleDataSize")
                        if (readSampleDataSize >= 0) {
                            System.currentTimeMillis()
                            val sampleTime = mediaExtractor.sampleTime
                            Log.i("AACDecoder", "sampleTime: "+sampleTime)
                            aacDecoder.queueInputBuffer(inputIndex, 0, readSampleDataSize, 0, 0)
                            mediaExtractor.advance()
                        } else {
                            isDecoder = false
                        }
                    }
                }

                val outputIndex = aacDecoder.dequeueOutputBuffer(info, TIMEOUT_DECODER)
                if (outputIndex >= 0) {
                    val outputBuffer = aacDecoder.getOutputBuffer(outputIndex)
                    if (outputBuffer != null) {
                        val pcmBuffer = ByteBuffer.allocate(outputBuffer.remaining())
                        pcmBuffer.put(outputBuffer)
                        pcmBuffer.flip()
                        val mediaPacket = MediaPacket().apply {
                            data = pcmBuffer
                        }
                        callback?.frameBuffer(mediaPacket)

                        aacDecoder.releaseOutputBuffer(outputIndex, false)
                    }
                }
            }

            aacDecoder.stop()
            aacDecoder.release()
            mediaExtractor.release()
        }
    }
}