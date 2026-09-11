package com.x4yi.x4ui.impl.client.media;
import com.x4yi.x4ui.api.client.media.IAudioDecoder;
import com.x4yi.x4ui.api.client.media.IVideoDecoder;
public class MediaPlayer {
    private IVideoDecoder videoDecoder;
    private IAudioDecoder audioDecoder;
    private boolean playing = false;
    private long playStartTime = -1;
    private double pausedTimeSec = 0;
    public MediaPlayer() {
    }
    public void setVideoDecoder(IVideoDecoder decoder) {
        this.videoDecoder = decoder;
    }
    public void setAudioDecoder(IAudioDecoder decoder) {
        this.audioDecoder = decoder;
    }
    public void play() {
        if (!playing) {
            playing = true;
            if (playStartTime == -1) {
                playStartTime = System.currentTimeMillis() - (long)(pausedTimeSec * 1000);
            } else {
                playStartTime = System.currentTimeMillis() - (long)(pausedTimeSec * 1000);
            }
            if (videoDecoder != null) videoDecoder.start();
            if (audioDecoder != null) audioDecoder.start();
        }
    }
    public void pause() {
        if (playing) {
            playing = false;
            pausedTimeSec = getPlaybackTimeSec();
            if (videoDecoder != null) videoDecoder.pause();
            if (audioDecoder != null) audioDecoder.pause();
        }
    }
    public void stop() {
        playing = false;
        playStartTime = -1;
        pausedTimeSec = 0;
        if (videoDecoder != null) videoDecoder.close();
        if (audioDecoder != null) audioDecoder.close();
    }
    public double getPlaybackTimeSec() {
        if (!playing) return pausedTimeSec;
        if (audioDecoder != null) {
            long audioTimeMs = audioDecoder.getCurrentAudioTimeMs();
            if (audioTimeMs >= 0) {
                return audioTimeMs / 1000.0;
            }
        }
        return (System.currentTimeMillis() - playStartTime) / 1000.0;
    }
    public IVideoDecoder getVideoDecoder() {
        return videoDecoder;
    }
    public IAudioDecoder getAudioDecoder() {
        return audioDecoder;
    }
    public void seek(double timestampSec) {
        timestampSec = Math.max(0, timestampSec);
        double dur = getDuration();
        if (dur > 0) {
            timestampSec = Math.min(dur, timestampSec);
        }
        pausedTimeSec = timestampSec;
        playStartTime = System.currentTimeMillis() - (long)(timestampSec * 1000);
        if (videoDecoder != null) {
            videoDecoder.seek(timestampSec);
        }
        if (audioDecoder != null) {
            audioDecoder.seek(timestampSec);
        }
    }
    public double getDuration() {
        if (videoDecoder != null && videoDecoder.getDuration() > 0) {
            return videoDecoder.getDuration();
        }
        if (audioDecoder != null && audioDecoder.getDuration() > 0) {
            return audioDecoder.getDuration();
        }
        return -1;
    }
    public boolean isPlaying() {
        return playing;
    }
}