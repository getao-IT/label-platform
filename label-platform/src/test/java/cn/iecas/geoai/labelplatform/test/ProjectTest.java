package cn.iecas.geoai.labelplatform.test;

import cn.iecas.geoai.labelplatform.entity.domain.LabelDatasetFile;
import cn.iecas.geoai.labelplatform.entity.domain.LabelTask;
import cn.iecas.geoai.labelplatform.entity.emun.LabelStatus;
import cn.iecas.geoai.labelplatform.service.LabelDatasetFileService;
import cn.iecas.geoai.labelplatform.service.LabelTaskService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class ProjectTest {

    @Autowired
    private LabelDatasetFileService labelDatasetFileService;

    @Autowired
    private LabelTaskService labelTaskService;

    /**
     * 给userIds分配任务
     */
    @Test
    public void applyDatasetFile() {
        int labelProjectId = 9;
        int datasetId = 17;
        int[] userIds = new int[]{1,2,3};  // 标注员
        StringJoiner fileStringJoiner = new StringJoiner(",");
        StringJoiner taskStringJoiner = new StringJoiner(",");

        List<LabelDatasetFile> labelDatasetFileList = labelDatasetFileService.list(
                new QueryWrapper<LabelDatasetFile>().eq("dataset_id", datasetId)); // 根据数据集id更新标注员
        int len = labelDatasetFileList.size();
        if (labelDatasetFileList.size() > userIds.length) {
            len = userIds.length;
        }
        for (int i = 0; i < len; i++) {
            labelDatasetFileList.get(i).setLabelUserId(userIds[i]);
            labelDatasetFileList.get(i).setStatus(LabelStatus.LABELING);
            labelDatasetFileService.updateById(labelDatasetFileList.get(i));
            fileStringJoiner.add(String.valueOf(labelDatasetFileList.get(i).getFileId()));
            LabelTask labelTask = labelTaskService.getOne(new QueryWrapper<LabelTask>().
                    eq("label_project_id", labelProjectId).
                    eq("task_type", 0).eq("user_id", userIds[i]));
            labelTask.setProcessingList(String.valueOf(labelDatasetFileList.get(i).getFileId()));
            labelTaskService.updateById(labelTask);
            taskStringJoiner.add(String.valueOf(labelTask.getId()));
            System.out.println("已分配的labelDatasetFile 文件ID：" + fileStringJoiner);
            System.out.println("已分配的labelStask 任务ID：" + taskStringJoiner);
        }
    }
}
