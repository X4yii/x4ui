package com.x4yi.x4ui.impl.client.media.backend;

import com.x4yi.x4ui.api.client.media.IAudioSink;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class OpenALAudioSink implements IAudioSink {
    private static final int NUM_BUFFERS = 8;
    private static final int BUFFER_SIZE = 8192; // 8KB chunks
    
    private int sourceId = -1;
    private final int[] bufferIds = new int[NUM_BUFFERS];
    
    private int sampleRate;
    private int format;
    private int bytesPerSample;
    
    private boolean initialized = false;
    private boolean playing = false;
    
    // Tracking for A/V sync
    private long totalSamplesProcessed = 0; // Samples that have finished playing and unqueued
    
    // Intermediate staging buffer for incoming PCM
    private ByteBuffer stagingBuffer;

    @Override
    public void init(int sampleRate, int channels, int bitDepth) throws Exception {
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
        
        // Generate source
        sourceId = AL10.alGenSources();
        if (AL10.alGetError() != AL10.AL_NO_ERROR) {
            throw new Exception("Failed to generate OpenAL source");
        }
        
        // Generate buffers
        for (int i = 0; i < NUM_BUFFERS; i++) {
            bufferIds[i] = AL10.alGenBuffers();
        }
        
        // Setup source parameters
        AL10.alSourcef(sourceId, AL10.AL_PITCH, 1.0f);
        AL10.alSourcef(sourceId, AL10.AL_GAIN, 1.0f);
        AL10.alSource3f(sourceId, AL10.AL_POSITION, 0.0f, 0.0f, 0.0f);
        AL10.alSource3f(sourceId, AL10.AL_VELOCITY, 0.0f, 0.0f, 0.0f);
        
        initialized = true;
    }

    @Override
    public void writePCM(byte[] data, int offset, int length) {
        if (!initialized) return;
        
        int bytesWritten = 0;
        while (bytesWritten < length) {
            int toWrite = Math.min(length - bytesWritten, stagingBuffer.remaining());
            stagingBuffer.put(data, offset + bytesWritten, toWrite);
            bytesWritten += toWrite;
            
            if (stagingBuffer.position() == stagingBuffer.capacity()) {
                stagingBuffer.flip();
                queueBuffer(stagingBuffer);
                stagingBuffer.clear();
            }
        }
    }
    
    private void queueBuffer(ByteBuffer data) {
        // Wait until a buffer is free
        int processed;
        while ((processed = AL10.alGetSourcei(sourceId, AL10.AL_BUFFERS_PROCESSED)) == 0) {
            // If we haven't queued all buffers initially, just use one of the available ones
            int queued = AL10.alGetSourcei(sourceId, AL10.AL_BUFFERS_QUEUED);
            if (queued < NUM_BUFFERS) {
                break;
            }
            
            // Wait for OpenAL to finish playing a buffer
            try { Thread.sleep(2); } catch (InterruptedException ignored) {}
            
            // Check if starvation occurred
            if (playing && AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE) != AL10.AL_PLAYING) {
                AL10.alSourcePlay(sourceId);
            }
        }
        
        int bufferToUse;
        if (processed > 0) {
            bufferToUse = AL10.alSourceUnqueueBuffers(sourceId);
            // Track time: We unqueued a buffer, so those samples were successfully played
            int sizeBytes = AL10.alGetBufferi(bufferToUse, AL10.AL_SIZE);
            totalSamplesProcessed += (sizeBytes / bytesPerSample);
        } else {
            // Find a free buffer (one that is not queued)
            int queued = AL10.alGetSourcei(sourceId, AL10.AL_BUFFERS_QUEUED);
            bufferToUse = bufferIds[queued];
        }
        
        AL10.alBufferData(bufferToUse, format, data, sampleRate);
        AL10.alSourceQueueBuffers(sourceId, bufferToUse);
        
        if (playing && AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE) != AL10.AL_PLAYING) {
            AL10.alSourcePlay(sourceId);
        }
    }

    @Override
    public long getPlaybackTimeMs() {
        if (!initialized) return -1;
        
        // Samples that were already processed and unqueued
        long samplesPlayed = totalSamplesProcessed;
        
        // Plus samples currently played in the active buffer
        int currentBufferOffset = AL10.alGetSourcei(sourceId, AL11.AL_SAMPLE_OFFSET);
        samplesPlayed += currentBufferOffset;
        
        return (long) ((samplesPlayed / (double) sampleRate) * 1000.0);
    }

    @Override
    public void start() {
        if (initialized && !playing) {
            playing = true;
            AL10.alSourcePlay(sourceId);
        }
    }

    @Override
    public void pause() {
        if (initialized && playing) {
            playing = false;
            AL10.alSourcePause(sourceId);
        }
    }

    @Override
    public void flush() {
        if (!initialized) return;
        AL10.alSourceStop(sourceId);
        int queued = AL10.alGetSourcei(sourceId, AL10.AL_BUFFERS_QUEUED);
        while (queued-- > 0) {
            AL10.alSourceUnqueueBuffers(sourceId);
        }
        totalSamplesProcessed = 0;
        if (stagingBuffer != null) {
            stagingBuffer.clear();
        }
    }

    @Override
    public void close() {
        if (initialized) {
            flush();
            AL10.alDeleteSources(sourceId);
            for (int i = 0; i < NUM_BUFFERS; i++) {
                AL10.alDeleteBuffers(bufferIds[i]);
            }
            initialized = false;
        }
    }
}
