package com.whispercppdemo.media

import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun decodeWaveFile(file: File): FloatArray {
    val bytes = file.readBytes()
    val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

    fun readString(offset: Int, length: Int): String {
        return String(bytes, offset, length, Charsets.US_ASCII)
    }

    require(readString(0, 4) == "RIFF") { "Invalid WAV: missing RIFF" }
    require(readString(8, 4) == "WAVE") { "Invalid WAV: missing WAVE" }

    var offset = 12

    var audioFormat = -1
    var channels = -1
    var sampleRate = -1
    var bitsPerSample = -1
    var dataOffset = -1
    var dataSize = -1

    while (offset + 8 <= bytes.size) {
        val chunkId = readString(offset, 4)
        val chunkSize = buffer.getInt(offset + 4)

        when (chunkId) {
            "fmt " -> {
                audioFormat = buffer.getShort(offset + 8).toInt()
                channels = buffer.getShort(offset + 10).toInt()
                sampleRate = buffer.getInt(offset + 12)
                bitsPerSample = buffer.getShort(offset + 22).toInt()
            }
            "data" -> {
                dataOffset = offset + 8
                dataSize = chunkSize
                break
            }
        }

        offset += 8 + chunkSize
        if (chunkSize % 2 == 1) offset += 1
    }

    require(audioFormat == 1) { "Unsupported WAV: only PCM supported, audioFormat=$audioFormat" }
    require(channels == 1 || channels == 2) { "Unsupported WAV channels: $channels" }
    require(bitsPerSample == 16) { "Unsupported WAV bit depth: $bitsPerSample" }
    require(dataOffset >= 0 && dataSize > 0) { "Invalid WAV: data chunk not found" }

    val shortCount = dataSize / 2
    val shortBuffer = ByteBuffer
        .wrap(bytes, dataOffset, dataSize)
        .order(ByteOrder.LITTLE_ENDIAN)
        .asShortBuffer()

    val pcm = ShortArray(shortCount)
    shortBuffer.get(pcm)

    val mono = FloatArray(pcm.size / channels)

    when (channels) {
        1 -> {
            for (i in mono.indices) {
                mono[i] = (pcm[i] / 32768.0f).coerceIn(-1f, 1f)
            }
        }
        2 -> {
            for (i in mono.indices) {
                val left = pcm[i * 2] / 32768.0f
                val right = pcm[i * 2 + 1] / 32768.0f
                mono[i] = ((left + right) / 2.0f).coerceIn(-1f, 1f)
            }
        }
    }

    if (sampleRate == 16000) {
        return mono
    }

    return resampleTo16k(mono, sampleRate)
}

private fun resampleTo16k(input: FloatArray, inputSampleRate: Int): FloatArray {
    if (inputSampleRate == 16000) return input

    val outputSize = (input.size * 16000L / inputSampleRate).toInt()
    val output = FloatArray(outputSize)

    val ratio = inputSampleRate.toDouble() / 16000.0

    for (i in 0 until outputSize) {
        val srcIndex = i * ratio
        val left = srcIndex.toInt()
        val right = minOf(left + 1, input.lastIndex)
        val frac = (srcIndex - left).toFloat()

        val leftSample = input[left]
        val rightSample = input[right]

        output[i] = leftSample + (rightSample - leftSample) * frac
    }

    return output
}

fun encodeWaveFile(file: File, data: ShortArray) {
    file.outputStream().use {
        it.write(headerBytes(data.size * 2))
        val buffer = ByteBuffer.allocate(data.size * 2)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.asShortBuffer().put(data)
        val bytes = ByteArray(buffer.limit())
        buffer.get(bytes)
        it.write(bytes)
    }
}

private fun headerBytes(totalLength: Int): ByteArray {
    require(totalLength >= 44)
    ByteBuffer.allocate(44).apply {
        order(ByteOrder.LITTLE_ENDIAN)

        put('R'.code.toByte())
        put('I'.code.toByte())
        put('F'.code.toByte())
        put('F'.code.toByte())

        putInt(totalLength - 8)

        put('W'.code.toByte())
        put('A'.code.toByte())
        put('V'.code.toByte())
        put('E'.code.toByte())

        put('f'.code.toByte())
        put('m'.code.toByte())
        put('t'.code.toByte())
        put(' '.code.toByte())

        putInt(16)
        putShort(1.toShort())
        putShort(1.toShort())
        putInt(16000)
        putInt(32000)
        putShort(2.toShort())
        putShort(16.toShort())

        put('d'.code.toByte())
        put('a'.code.toByte())
        put('t'.code.toByte())
        put('a'.code.toByte())

        putInt(totalLength - 44)
        position(0)
    }.also {
        val bytes = ByteArray(it.limit())
        it.get(bytes)
        return bytes
    }
}
