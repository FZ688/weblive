package com.fz.web.task;

import com.fz.entity.config.AppConfig;
import com.fz.entity.constants.Constants;
import com.fz.entity.enums.DateTimePatternEnum;
import com.fz.service.StatisticsInfoService;
import com.fz.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.io.File;
import java.io.IOException;

/**
 * @Author: fz
 * @Date: 2025/1/15 10:10
 * @Description:
 */
@Component
@Slf4j
public class SysTask {

    @Resource
    private StatisticsInfoService statisticsInfoService;

    @Resource
    private AppConfig appConfig;

    /**
     * @description: 每天午夜（0点0分0秒）执行一次统计数据的任务
     * @author fz
     * 2025/1/15 10:11
     */
    @Scheduled(cron = "0 0 0  * * ?")
    public void statisticData(){
        statisticsInfoService.statisticsData();
    }

    /**
     * @description: 每天凌晨3点执行一次删除临时文件的任务
     * @author fz
     * 2025/1/15 10:12
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void delTempFile() {
        String tempFolderName = appConfig.getProjectFolder() + Constants.FILE_FOLDER + Constants.FILE_FOLDER_TEMP;
        File folder = new File(tempFolderName);
        File[] listFile = folder.listFiles();
        if (listFile == null) {
            return;
        }
        String twodaysAgo = DateUtil.format(DateUtil.getDayAgo(2), DateTimePatternEnum.YYYYMMDD.getPattern()).toLowerCase();
        int dayInt = Integer.parseInt(twodaysAgo);
        for (File file : listFile) {
            int fileDate = Integer.parseInt(file.getName());
            if (fileDate <= dayInt) {
                try {
                    FileUtils.deleteDirectory(file);
                } catch (IOException e) {
                    log.info("删除临时文件失败", e);
                }
            }
        }
    }
}
