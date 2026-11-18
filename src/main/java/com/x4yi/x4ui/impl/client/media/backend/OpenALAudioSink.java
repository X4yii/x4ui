package com.x4yi.x4ui.impl.client.media.backend;

import com.x4yi.x4ui.api.client.media.IAudioSink;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;

import java.nio.ByteBuffer;

@SideOnly(Side.CLIENT)
public class OpenALAudioSink implements IAudioSink {
    private static final int NUM_BUFFERS = 8;
    private static final int BUFFER_SIZE = 8192;

    private final Object lock = new Object();
    private int sourceId = -1;
    private final int[] bufferIds = new int[NUM_BUFFERS];
    private int sampleRate;
    private int format;
    private int bytesPerSample;
    private volatile boolean initialized = false;
    private volatile boolean playing = false;
    private long totalSamplesProcessed = 0;
    private ByteBuffer stagingBuffer;
    private float volume = 1.0f;
    private final java.util.Queue<Integer> freeBuffers = new java.util.ArrayDeque<>();

    @Override
    public void init(int sampleRate, int channels, int bitDepth) throws Exception {
        synchronized (lock) {
            this.sampleRate = sampleRate;
            this.bytesPerSample = channels * (bitDepth / 8);
            this.stagingBuffer = BufferUtils.createByteBuffer(BUFFER_SIZE);
            if (channels == 1) {
                format = (bitDepth == 16) ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_MONO8;
            } else if (channels == 2) {
                format = (bitDepth == 16) ? AL10.AL_FORMAT_STEREO16 : AL10.AL_FORMAT_STEREO8;
            } else {
                throw new Exception("Unsupported audio channels: " + channels);
            }
            sourceId = AL10.alGenSources();
            if (AL10.alGetError() != AL10.AL_NO_ERROR) {
                throw new Exception("Failed to generate OpenAL source");
            }
            freeBuffers.clear();
            for (int i = 0; i < NUM_BUFFERS; i++) {
                bufferIds[i] = AL10.alGenBuffers();
                freeBuffers.add(bufferIds[i]);
            }
            AL10.alSourcef(sourceId, AL10.AL_PITCH, 1.0f);
            AL10.alSourcef(sourceId, AL10.AL_GAIN, volume);
            AL10.alSource3f(sourceId, AL10.AL_POSITION, 0.0f, 0.0f, 0.0f);
            AL10.alSource3f(sourceId, AL10.AL_VELOCITY, 0.0f, 0.0f, 0.0f);
            initialized = true;
        }
    }

    @Override
    public void writePCM(byte[] data, int offset, int length) {
        if (!initialized) return;
        int bytesWritten = 0;
        while (bytesWritten < length && initialized && !Thread.currentThread().isInterrupted()) {
            ByteBuffer readyToQueue = null;
            synchronized (lock) {
                if (!initialized || stagingBuffer == null) return;
                int toWrite = Math.min(length - bytesWritten, stagingBuffer.remaining());
                stagingBuffer.put(data, offset + bytesWritten, toWrite);
                bytesWritten += toWrite;
                if (stagingBuffer.position() == stagingBuffer.capacity()) {
                    stagingBuffer.flip();
                    readyToQueue = stagingBuffer;
                }
            }
            if (readyToQueue != null) {
                if (!queueBuffer(readyToQueue)) {
                    return;
                }
                synchronized (lock) {
                    if (stagingBuffer != null) {
                        stagingBuffer.clear();
                    }
                }
            }
        }
    }

    private boolean queueBuffer(ByteBuffer data) {
        while (initialized && !Thread.currentThread().isInterrupted()) {
            synchronized (lock) {
                if (!initialized) return false;
                recycleProcessedBuffersLocked();
                Integer bufferToUse = freeBuffers.poll();
                if (bufferToUse != null) {
                    AL10.alBufferData(bufferToUse, format, data, sampleRate);
                    AL10.alSourceQueueBuffers(sourceId, bufferToUse);
                    if (playing && AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE) != AL10.AL_PLAYING) {
                        AL10.alSourcePlay(sourceId);
                    }
                    return true;
                }
                if (playing && AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE) != AL10.AL_PLAYING) {
                    AL10.alSourcePlay(sourceId);
                }
            }
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    private void recycleProcessedBuffersLocked() {
        if (!initialized || sourceId == -1) return;
        int processed = AL10.alGetSourcei(sourceId, AL10.AL_BUFFERS_PROCESSED);
        while (processed > 0) {
            int unqueued = AL10.alSourceUnqueueBuffers(sourceId);
            if (unqueued != 0 && AL10.alGetError() == AL10.AL_NO_ERROR) {
                int sizeBytes = AL10.alGetBufferi(unqueued, AL10.AL_SIZE);
                if (bytesPerSample > 0) {
                    totalSamplesProcessed += (sizeBytes / bytesPerSample);
                }
                freeBuffers.add(unqueued);
            }
            processed--;
        }
    }

    @Override
    public long getPlaybackTimeMs() {
        synchronized (lock) {
            if (!initialized || sampleRate <= 0) return -1;
            long samplesPlayed = totalSamplesProcessed;
            int currentBufferOffset = AL10.alGetSourcei(sourceId, AL11.AL_SAMPLE_OFFSET);
            samplesPlayed += currentBufferOffset;
            return (long) ((samplesPlayed / (double) sampleRate) * 1000.0);
        }
    }

    @Override
    public void start() {
        synchronized (lock) {
            if (initialized && !playing) {
                playing = true;
                AL10.alSourcePlay(sourceId);
            }
        }
    }

    @Override
    public void pause() {
        synchronized (lock) {
            if (initialized && playing) {
                playing = false;
                AL10.alSourcePause(sourceId);
            }
        }
    }

    @Override
    public void flush() {
        synchronized (lock) {
            if (!initialized) return;
            AL10.alSourceStop(sourceId);
            int queued = AL10.alGetSourcei(sourceId, AL10.AL_BUFFERS_QUEUED);
            while (queued-- > 0) {
                AL10.alSourceUnqueueBuffers(sourceId);
            }
            totalSamplesProcessed = 0;
            freeBuffers.clear();
            for (int id : bufferIds) {
                freeBuffers.add(id);
            }
            if (stagingBuffer != null) {
                stagingBuffer.clear();
            }
        }
    }

    public void flush(double newTimeSec) {
        synchronized (lock) {
            flush();
            this.totalSamplesProcessed = (long) (Math.max(0, newTimeSec) * sampleRate);
        }
    }

    @Override
    public void close() {
        synchronized (lock) {
            if (initialized) {
                initialized = false;
                playing = false;
                AL10.alSourceStop(sourceId);
                int queued = AL10.alGetSourcei(sourceId, AL10.AL_BUFFERS_QUEUED);
                while (queued-- > 0) {
                    AL10.alSourceUnqueueBuffers(sourceId);
                }
                freeBuffers.clear();
                AL10.alDeleteSources(sourceId);
                sourceId = -1;
                for (int i = 0; i < NUM_BUFFERS; i++) {
                    AL10.alDeleteBuffers(bufferIds[i]);
                    bufferIds[i] = 0;
                }
            }
        }
    }

    @Override
    public void setVolume(float volume) {
        synchronized (lock) {
            this.volume = Math.max(0.0f, Math.min(1.0f, volume));
            if (initialized && sourceId != -1) {
                AL10.alSourcef(sourceId, AL10.AL_GAIN, this.volume);
            }
        }
    }

    @Override
    public float getVolume() {
        synchronized (lock) {
            return this.volume;
        }
    }
}
