package cn.iecas.geoai.labelplatform.entity.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VideoFrame implements Serializable {
    private static final long serialVersionUID = 8232510116210249982L;

    /**
     * 运行状态
     * <1> 获取的frame数据为空，正常运行 </>
     * <2> FFmpegFrameGrabber处理异常，运行终止 </>
     * <3> IO异常，运行终止 </>
     */
    private int status;

    /**
     * 视频路径
     */
    private String videoPath;

    /**
     * 最大帧数
     */
    private int lengthInFrames;

    /**
     * 第几帧
     */
    private int frameNumber;

    /**
     * 对应帧数据
     */
    private String frameBase64Str;

    /**
     * 帧率
     */
    private double fps;

    /**
     * 标注信息
     */
    private String label;

    @Override
    public String toString() {
        return "VF{" +
                "status=" + status +
                ", lengthInFrames=" + lengthInFrames +
                ", frameNumber=" + frameNumber +
                ", fps=" + fps +
                '}';
    }
}
