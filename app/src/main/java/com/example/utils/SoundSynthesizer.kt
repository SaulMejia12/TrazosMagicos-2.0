package com.example.utils

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundSynthesizer {
    private val scope = CoroutineScope(Dispatchers.Default)

    fun playDrawingTick() {
        // Gentle soft pluck
        playTone(frequency = 1100f, durationMs = 25, volume = 0.3f)
    }

    fun playPointCompleted() {
        // Bright bell sound (rising double beep)
        scope.launch {
            playTone(frequency = 1400f, durationMs = 60, volume = 0.5f)
            delay(50)
            playTone(frequency = 1800f, durationMs = 80, volume = 0.5f)
        }
    }

    fun playSuccess() {
        // Grand arpeggio of C major chord!
        scope.launch {
            val notes = listOf(523.25f, 659.25f, 783.99f, 1046.50f, 1318.51f) // C5, E5, G5, C6, E6
            for (note in notes) {
                playTone(frequency = note, durationMs = 120, volume = 0.6f)
                delay(90)
            }
        }
    }

    fun playError() {
        // Low funny wobble slide
        playToneSlide(startFreq = 320f, endFreq = 160f, durationMs = 220, volume = 0.5f)
    }

    fun playStarPop(index: Int) {
        // Rising pitch depending on star order
        val frequency = 700f + (index * 350f)
        playTone(frequency = frequency, durationMs = 150, volume = 0.6f)
    }

    private fun playTone(frequency: Float, durationMs: Int, volume: Float) {
        scope.launch {
            try {
                val sampleRate = 44100
                val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
                if (numSamples <= 0) return@launch
                
                val buffer = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val t = i.toFloat() / sampleRate
                    // Enveloping to prevent harsh clicks
                    val envelope = if (i > numSamples * 0.8) {
                        ((numSamples - i).toFloat() / (numSamples * 0.2f))
                    } else if (i < numSamples * 0.1) {
                        (i.toFloat() / (numSamples * 0.1f))
                    } else {
                        1.0f
                    }
                    val sampleVal = sin(2.0 * Math.PI * frequency * t) * volume * envelope
                    buffer[i] = (sampleVal * Short.MAX_VALUE).toInt().toShort()
                }

                val audioTrack = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    buffer.size * 2,
                    AudioTrack.MODE_STATIC
                )

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                
                delay(durationMs.toLong() + 30)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun playToneSlide(startFreq: Float, endFreq: Float, durationMs: Int, volume: Float) {
        scope.launch {
            try {
                val sampleRate = 44100
                val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
                if (numSamples <= 0) return@launch

                val buffer = ShortArray(numSamples)
                var phase = 0.0
                for (i in 0 until numSamples) {
                    val progress = i.toFloat() / numSamples
                    val currentFreq = startFreq + (endFreq - startFreq) * progress
                    
                    val envelope = if (i > numSamples * 0.8) {
                        ((numSamples - i).toFloat() / (numSamples * 0.2f))
                    } else if (i < numSamples * 0.1) {
                        (i.toFloat() / (numSamples * 0.1f))
                    } else {
                        1.0f
                    }

                    val sampleValue = sin(phase) * volume * envelope
                    buffer[i] = (sampleValue * Short.MAX_VALUE).toInt().toShort()

                    phase += 2.0 * Math.PI * currentFreq / sampleRate
                    if (phase > 2.0 * Math.PI) {
                        phase -= 2.0 * Math.PI
                    }
                }

                val audioTrack = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    buffer.size * 2,
                    AudioTrack.MODE_STATIC
                )

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()

                delay(durationMs.toLong() + 30)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
