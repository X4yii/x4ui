package com.x4yi.x4ui.impl.client.media.backend.jcodec;
import com.x4yi.x4ui.api.client.media.IVideoDecoder;
import com.x4yi.x4ui.api.client.media.RingBuffer;
import com.x4yi.x4ui.api.client.media.VideoFrame;
import org.jcodec.codecs.h264.H264Decoder;
import org.jcodec.codecs.h264.H264Utils;
import org.jcodec.codecs.h264.mp4.AvcCBox;
import org.jcodec.common.DemuxerTrack;
import org.jcodec.common.DemuxerTrackMeta;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Packet;
import org.jcodec.common.model.Picture;
import org.jcodec.containers.mp4.boxes.Box;
import org.jcodec.containers.mp4.boxes.SampleEntry;
import org.jcodec.containers.mp4.demuxer.AbstractMP4DemuxerTrack;
import org.jcodec.containers.mp4.demuxer.MP4Demuxer;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
public class JCodecVideoDecoder implements IVideoDecoder {
    private String url;
    private int width;
    private int height;
    private double duration = -1;
    private RingBuffer ringBuffer;
    private DecoderThread thread;
    private boolean error = false;
    private String errorMessage = null;
    @Override
    public void init(String url) throws Exception {
        this.url = url;
        File file = new File(url);
        if (!file.exists() || file.length() < 100) {
            setError("File not found or corrupted: " + file.getName());
            return;
        }
        FileChannelWrapper ch = null;
        try {
            ch = NIOUtils.readableChannel(file);
            MP4Demuxer demuxer = MP4Demuxer.createRawMP4Demuxer(ch);
            DemuxerTrack rawVideoTrack = demuxer.getVideoTrack();
            if (rawVideoTrack == null) {
                setError("No video track found");
                return;
            }
            DemuxerTrackMeta meta = rawVideoTrack.getMeta();
            if (meta == null || meta.getVideoCodecMeta() == null || meta.getVideoCodecMeta().getSize() == null) {
                setError("Cannot read video metadata");
                return;
            }
            this.width = meta.getVideoCodecMeta().getSize().getWidth();
            this.height = meta.getVideoCodecMeta().getSize().getHeight();
            this.duration = meta.getTotalDuration();
            if (width <= 0 || height <= 0) {
                setError("Invalid video dimensions");
                return;
            }
            ringBuffer = new RingBuffer(6, width, height);
            thread = new DecoderThread(file, ringBuffer, this);
            thread.setDaemon(true);
        } catch (Exception e) {
            setError("Init Error: " + e.getMessage());
            throw e;
        } finally {
            NIOUtils.closeQuietly(ch);
        }
    }
    @Override
    public double getDuration() {
        return duration;
    }
    @Override
    public boolean isError() {
        return error;
    }
    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
    @Override
    public void close() {
        if (thread != null) {
            thread.stopThread();
            thread = null;
        }
        if (ringBuffer != null) {
            ringBuffer.clear();
            ringBuffer = null;
        }
    }
    @Override
    public int getVideoWidth() {
        return width;
    }
    @Override
    public int getVideoHeight() {
        return height;
    }
    @Override
    public VideoFrame getNextFrame(long currentTimeMs) {
        if (ringBuffer == null || ringBuffer.isEmpty()) return null;
        double currentSec = currentTimeMs / 1000.0;
        VideoFrame frame = ringBuffer.getHead();
        if (frame != null && frame.ready) {
            if (frame.pts <= currentSec) {
                return frame;
            }
        }
        return null;
    }
    public void popFrame() {
        if (ringBuffer != null) {
            ringBuffer.popHead();
        }
    }
    @Override
    public void start() {
        if (thread != null && !thread.isAlive()) {
            thread.start();
        }
    }
    @Override
    public void pause() {
    }
    @Override
    public void seek(double timestampSec) {
        if (ringBuffer != null) {
            ringBuffer.clear();
        }
    }
    protected void setError(String msg) {
        this.error = true;
        this.errorMessage = msg;
    }
    private static class DecoderThread extends Thread {
        private final File videoFile;
        private final RingBuffer buffer;
        private final JCodecVideoDecoder parent;
        private final AtomicBoolean running = new AtomicBoolean(true);
        public DecoderThread(File videoFile, RingBuffer buffer, JCodecVideoDecoder parent) {
            this.videoFile = videoFile;
            this.buffer = buffer;
            this.parent = parent;
            this.setName("X4UI-JCodecDecoder-" + videoFile.getName());
        }
        public void stopThread() {
            running.set(false);
            this.interrupt();
        }
        @Override
        public void run() {
            FileChannelWrapper ch = null;
            try {
                ch = NIOUtils.readableChannel(videoFile);
                MP4Demuxer demuxer = MP4Demuxer.createRawMP4Demuxer(ch);
                AbstractMP4DemuxerTrack videoTrack = (AbstractMP4DemuxerTrack) demuxer.getVideoTrack();
                DemuxerTrackMeta meta = videoTrack.getMeta();
                int frameTotal = meta.getTotalFrames();
                double duration = meta.getTotalDuration();
                double frameRate = (frameTotal > 0 && duration > 0) ? (double) frameTotal / duration : 30.0;
                SampleEntry[] sampleEntries = videoTrack.getSampleEntries();
                AvcCBox avcC = null;
                for (Box b : sampleEntries[0].getBoxes()) {
                    if (b instanceof AvcCBox) {
                        avcC = (AvcCBox) b;
                        break;
                    }
                }
                if (avcC == null) {
                    parent.setError("No AvcC (H.264 header) in video");
                    return;
                }
                H264Decoder decoder = new H264Decoder();
                decoder.addSps(avcC.getSpsList());
                decoder.addPps(avcC.getPpsList());
                int w = meta.getVideoCodecMeta().getSize().getWidth();
                int h = meta.getVideoCodecMeta().getSize().getHeight();
                byte[][] decodeBuffer = new byte[][] {
                    new byte[w * h * 2],
                    new byte[w * h * 2],
                    new byte[w * h * 2]
                };
                int frameIndex = 0;
                while (running.get()) {
                    if (buffer.isFull()) {
                        Thread.sleep(8);
                        continue;
                    }
                    VideoFrame frame = buffer.getTail();
                    if (frame != null && !frame.ready) {
                        Packet pkt = videoTrack.nextFrame();
                        if (pkt == null) {
                            try {
                                videoTrack.gotoFrame(0);
                                frameIndex = 0;
                                continue;
                            } catch (Exception seekEx) {
                                break;
                            }
                        }
                        List<ByteBuffer> nals = H264Utils.splitMOVPacket(pkt.getData(), avcC);
                        Picture pic = decoder.decodeFrameFromNals(nals, decodeBuffer);
                        if (pic == null) {
                            continue;
                        }
                        convertYUV420ToARGB(pic, frame.pixels, w, h);
                        frame.pts = frameIndex / frameRate;
                        frame.ready = true;
                        buffer.pushTail();
                        frameIndex++;
                    }
                }
            } catch (Throwable e) {
                if (running.get()) {
                    parent.setError("Decoder Error: " + e.getMessage());
                    org.apache.logging.log4j.LogManager.getLogger("x4ui").warn("[X4UI Video] JCodec decoder error: {}", e.getMessage());
                }
            } finally {
                NIOUtils.closeQuietly(ch);
            }
        }
        private void convertYUV420ToARGB(Picture pic, int[] dest, int w, int h) {
            byte[] y = pic.getPlaneData(0);
            byte[] u = pic.getPlaneData(1);
            byte[] v = pic.getPlaneData(2);
            int uvStride = pic.getPlaneWidth(1);
            int maxPixels = Math.min(dest.length, w * h);
            for (int j = 0; j < h; j++) {
                int yRow = j * w;
                int uvRow = (j >> 1) * uvStride;
                for (int i = 0; i < w; i++) {
                    int pixelIdx = yRow + i;
                    if (pixelIdx >= maxPixels) break;
                    int yVal = y[pixelIdx] & 0xFF;
                    int uVal = (u[uvRow + (i >> 1)] & 0xFF) - 128;
                    int vVal = (v[uvRow + (i >> 1)] & 0xFF) - 128;
                    int r = yVal + ((359 * vVal) >> 8);
                    int g = yVal - ((88 * uVal + 183 * vVal) >> 8);
                    int b = yVal + ((454 * uVal) >> 8);
                    if (r < 0) r = 0; else if (r > 255) r = 255;
                    if (g < 0) g = 0; else if (g > 255) g = 255;
                    if (b < 0) b = 0; else if (b > 255) b = 255;
                    dest[pixelIdx] = 0xFF000000 | (r << 16) | (g << 8) | b;
                }
            }
        }
    }
}