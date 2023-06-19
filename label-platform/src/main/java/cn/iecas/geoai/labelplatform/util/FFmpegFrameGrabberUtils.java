package cn.iecas.geoai.labelplatform.util;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.springframework.stereotype.Component;
import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * FFmpegFrameGrabber工具类
 */
@Component
@Slf4j
public class FFmpegFrameGrabberUtils {

    private final static Map<String, FFmpegFrameGrabber> VIDEO_STORAGE = new ConcurrentHashMap<>();

    /**
     * 启动应用即加载FFmpegFrameGrabber资源
     */
    static {
        try {
            FFmpegFrameGrabber.tryLoad();
            FFmpegFrameRecorder.tryLoad();
        } catch (FFmpegFrameGrabber.Exception e) {
            e.printStackTrace();
        } catch (FFmpegFrameRecorder.Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取videlpath视频Ffmpeg对象
     *
     * @param videoPath
     * @return
     */
    public FFmpegFrameGrabber getFfmpegFrameGrabberByPath(String videoPath) {
        long start = System.currentTimeMillis();
        if (VIDEO_STORAGE.get(videoPath) == null) {
            start = System.currentTimeMillis();
            FFmpegFrameGrabber fFmpegFrameGrabber = new FFmpegFrameGrabber(new File(videoPath));
            VIDEO_STORAGE.put(videoPath, fFmpegFrameGrabber);
            log.info("新的Ffmpeg对象 {} 加入内存成功，耗时：{}", videoPath, System.currentTimeMillis() - start);
        }
        log.info("获取视频Ffmpeg耗时：{}", System.currentTimeMillis() - start);
        return VIDEO_STORAGE.get(videoPath);
    }
}
