package com.xperia.prolog.media

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import android.view.Surface
import java.io.File
import java.nio.ByteBuffer

class VideoEncoder(private val config: EncoderConfig, private val outputFile: File) {
    private var mediaCodec: MediaCodec? = null
    private var mediaMuxer: MediaMuxer? = null
    private var trackIndex: Int = -1
    private var isMuxerStarted = false
    private var inputSurface: Surface? = null

    @Volatile
    private var isRecording = false
    private var encoderThread: Thread? = null

    fun prepare(): Surface {
        val format = config.toMediaFormat()
        
        mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC)
        mediaCodec?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        
        inputSurface = mediaCodec?.createInputSurface()
        
        mediaMuxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        
        trackIndex = -1
        isMuxerStarted = false
        
        return inputSurface ?: throw IllegalStateException("Could not create input surface for encoder")
    }

    fun start() {
        mediaCodec?.start()
        isRecording = true
        encoderThread = Thread {
            encoderLoop()
        }
        encoderThread?.start()
    }

    private fun encoderLoop() {
        while (isRecording) {
            drainEncoder(false)
        }
        drainEncoder(true)
    }

    private fun drainEncoder(endOfStream: Boolean) {
        val codec = mediaCodec ?: return
        val muxer = mediaMuxer ?: return

        if (endOfStream) {
            try {
                codec.signalEndOfInputStream()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val bufferInfo = MediaCodec.BufferInfo()
        while (true) {
            val encoderStatus = try {
                codec.dequeueOutputBuffer(bufferInfo, 10000)
            } catch (e: Exception) {
                break
            }
            
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) break
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (isMuxerStarted) throw RuntimeException("Format changed twice")
                val newFormat = codec.outputFormat
                trackIndex = muxer.addTrack(newFormat)
                muxer.start()
                isMuxerStarted = true
            } else if (encoderStatus < 0) {
                // Ignore other statuses
            } else {
                val encodedData = codec.getOutputBuffer(encoderStatus)
                    ?: throw RuntimeException("Encoder output buffer $encoderStatus was null")

                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                    bufferInfo.size = 0
                }

                if (bufferInfo.size != 0) {
                    if (!isMuxerStarted) {
                        throw RuntimeException("Muxer hasn't started")
                    }

                    encodedData.position(bufferInfo.offset)
                    encodedData.limit(bufferInfo.offset + bufferInfo.size)
                    try {
                        muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                codec.releaseOutputBuffer(encoderStatus, false)
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    break
                }
            }
        }
    }

    fun stop() {
        isRecording = false
        try {
            encoderThread?.join(2000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        
        try {
            mediaCodec?.stop()
            mediaCodec?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        try {
            if (isMuxerStarted) {
                mediaMuxer?.stop()
            }
            mediaMuxer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        mediaCodec = null
        mediaMuxer = null
        inputSurface = null
    }
}
