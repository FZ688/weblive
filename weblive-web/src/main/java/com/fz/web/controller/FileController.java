package com.fz.web.controller;

import com.fz.component.KafkaComponent;
import com.fz.web.annotation.GlobalInterceptor;
import com.fz.component.RedisComponent;
import com.fz.entity.config.AppConfig;
import com.fz.entity.constants.Constants;
import com.fz.entity.dto.SysSettingDto;
import com.fz.entity.dto.TokenUserInfoDto;
import com.fz.entity.dto.UploadingFileDto;
import com.fz.entity.dto.VideoPlayInfoDto;
import com.fz.entity.enums.DateTimePatternEnum;
import com.fz.entity.enums.ResponseCodeEnum;
import com.fz.entity.po.VideoInfoFile;
import com.fz.entity.vo.ResponseVO;
import com.fz.exception.BusinessException;
import com.fz.service.VideoInfoFileService;
import com.fz.utils.DateUtil;
import com.fz.utils.FFmpegUtils;
import com.fz.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

/**
 * @Author: fz
 * @Date: 2024/12/7 21:54
 * @Description:
 */
@RestController
@RequestMapping("/file")
@Validated
@Slf4j
public class FileController extends ABaseController {
    @Resource
    private AppConfig appConfig;
    @Resource
    private RedisComponent redisComponent;
    @Resource
    private KafkaComponent kafkaComponent;
    @Resource
    private FFmpegUtils fFmpegUtils;
    @Resource
    private VideoInfoFileService videoInfoFileService;

    /**
     * 从本地获取文件
     *
     * @param
     * @return
     * @author fz
     * 2024/12/7 23:01
     */
    @RequestMapping("/getResource")
    public void getResource(HttpServletResponse response, @NotNull String sourceName) {
        if (!StringTools.pathIsOk(sourceName)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        String suffix = StringTools.getFileSuffix(sourceName);
        response.setContentType("image/" + suffix.replace(".", ""));
        response.setHeader("Cache-Control", "max-age=2592000");
        readFile(response, sourceName);
    }

    /**
     * 视频预上传
     *
     * @param chunks 文件分为多少片，由前端完成分片，使用JavaScript的 File API获取文件对象，并使用slice(方法将文件切割为多个切片。
     * @return
     * @author fz
     * 2024/12/8 11:43
     */
    @RequestMapping("/preUploadVideo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO preUploadVideo(HttpServletRequest request, @NotEmpty String fileName, @NotNull Integer chunks) {
        // 根据token从redis拿出信息
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        // 将预上传的文件信息保存到redis
        String uploadId = redisComponent.savePreVideoFileInfo(tokenUserInfoDto.getUserId(), fileName, chunks);
        return getSuccessResponseVO(uploadId);
    }

    /**
     * 分片上传
     *
     * @param chunkFile 一片文件 chunkIndex 当前片的索引 uploadId 当前上传id
     * @return
     * @author fz
     * 2024/12/8 13:17
     */
    @RequestMapping("/uploadVideo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO uploadVideo(HttpServletRequest request, @NotNull MultipartFile chunkFile, @NotNull Integer chunkIndex, @NotNull String uploadId) throws IOException {
        // 根据token去拿用户信息
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        // 取出视频文件的信息
        UploadingFileDto fileDto = redisComponent.getUploadVideoFileInfo(tokenUserInfoDto.getUserId(), uploadId);
        if (fileDto == null) {
            throw new BusinessException("文件过期 请重新上传");
        }
        // 获取系统设置
        SysSettingDto sysSettingDto = redisComponent.getSysSettingDto();
        // 是否超过系统默认大小
        if (fileDto.getFileSize() > sysSettingDto.getVideoSize() * Constants.MB_SIZE) {
            throw new BusinessException("文件超过大小限制");
        }
        // 判断当前分片参数是否异常
        log.info("chunkIndex:{} fileDto.getChunks():{} fileDto.getChunkIndex():{}", chunkIndex, fileDto.getChunks(), fileDto.getChunkIndex());
        if (chunkIndex > fileDto.getChunks() - 1 || chunkIndex - 1 > fileDto.getChunkIndex()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        // 拿到视频文件所在目录，也就是预上传时创建的临时目录
        String folder = appConfig.getProjectFolder() + Constants.FILE_FOLDER + Constants.FILE_FOLDER_TEMP + fileDto.getFilePath();
        // 上传到本地
        File targetFile = new File(folder + "/" + chunkIndex);
        chunkFile.transferTo(targetFile);
        // 更新视频文件信息
        fileDto.setChunkIndex(chunkIndex);
        fileDto.setFileSize(fileDto.getFileSize() + chunkFile.getSize());
        redisComponent.updateVideoFileInfo(tokenUserInfoDto.getUserId(), fileDto);
        return getSuccessResponseVO(null);
    }


    /**
     * 当文件成功上传到服务器后，在前端页面可以点击选择删除该视频
     * @param request
     * @param uploadId
     * @return
     * @throws IOException
     */
    @RequestMapping("/delUploadVideo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO delUploadVideo(HttpServletRequest request, @NotEmpty String uploadId) throws IOException {
        // 根据token取出uid
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        // 根据uid取出文件信息
        UploadingFileDto fileDto = redisComponent.getUploadVideoFileInfo(tokenUserInfoDto.getUserId(), uploadId);
        // 文件信息不存在
        if (fileDto == null){
            throw  new BusinessException("文件不存在 请重新上传");
        }
        // 删除文件信息
        redisComponent.delVideoInfo(tokenUserInfoDto.getUserId(),uploadId);
        // 删除文件
        FileUtils.deleteDirectory(new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER + Constants.FILE_FOLDER_TEMP + fileDto.getFilePath()));
        return getSuccessResponseVO(uploadId);
    }

    /**
     * 上传图片
     * @param file 图片文件 createThumbnail 是否生成缩略图
     * @return
     * @author fz
     * 2024/12/8 15:28
     */
    @RequestMapping("/uploadImage")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO uploadImage(@NotNull MultipartFile file,@NotNull Boolean createThumbnail) throws IOException {
        // 获取文件后缀
        String fileSuffix = StringTools.getFileSuffix(file.getOriginalFilename());
        // 校验文件格式
        if (!Constants.IMAGE_SUFFIX_LIST.contains(fileSuffix.toLowerCase())) {
            throw new BusinessException("不支持该图片格式上传");
        }
        // 创建图片存放目录
        String day = DateUtil.format(new Date(),DateTimePatternEnum.YYYYMMDD.getPattern());
        String folder = appConfig.getProjectFolder() + Constants.FILE_FOLDER +  Constants.FILE_COVER + day;
        File folderFile = new File(folder);
        if (!folderFile.exists()){
            folderFile.mkdirs();
        }
        // 创建文件名
        String fileName  = StringTools.getRandomString(Constants.LENGTH_10) + fileSuffix;
        // 文件上传
        String path = folder + "/" + fileName;
        // 保存文件到path：projectFolder/file/cover/YYYYMMDD/文件名
        file.transferTo(new File(path));
        if (createThumbnail) {
            //生成缩略图
            fFmpegUtils.createImageThumbnail(path);
        }
        return getSuccessResponseVO(Constants.FILE_COVER + day + "/" + fileName);
    }

    /**
     * @description: 获取分p视频的索引文件
     * @param response 要把文件结果写回这里
     * @param fileId 分P的id
     * @return
     * @author fz
     * 2024/12/10 21:43
     */
    @RequestMapping("/videoResource/{fileId}/")
    public void videoResource(HttpServletResponse response, @PathVariable @NotEmpty String fileId) {
        VideoInfoFile videoInfoFile = this.videoInfoFileService.getVideoInfoFileByFileId(fileId);
        String filePath = videoInfoFile.getFilePath();
        readFile(response,filePath + "/" + Constants.M3U8_NAME);
        //  更新视频的播放信息
        VideoPlayInfoDto videoPlayInfoDto = new VideoPlayInfoDto();
        videoPlayInfoDto.setVideoId(videoInfoFile.getVideoId());
        videoPlayInfoDto.setFileIndex(videoInfoFile.getFileIndex());

        // 这个请求是播放器发过来的 没有token
        // 因此需要去cookie中取数据
        TokenUserInfoDto tokenUserInfoDto = getTokenInfoFromCookie();
        if (tokenUserInfoDto != null){
            videoPlayInfoDto.setUserId(tokenUserInfoDto.getUserId());
        }
        // 将视频的播放信息加入消息队列 异步处理
        //redisComponent.addVideoPlay(videoPlayInfoDto);
        kafkaComponent.publishVideoPlay(videoPlayInfoDto);
    }

    /**
     * @description: 获取分p视频的ts分片
     * @param response 将ts分片放入这里
     * @param fileId 分p视频id
     * @param ts     ts分片文件名
     * @return
     * @author fz
     * 2024/12/10 21:56
     */
    @RequestMapping("/videoResource/{fileId}/{ts}")
    public void videoResourceTs(HttpServletResponse response, @PathVariable @NotEmpty String fileId, @PathVariable @NotEmpty String ts) {
        VideoInfoFile videoInfoFile = this.videoInfoFileService.getVideoInfoFileByFileId(fileId);
        String filePath = videoInfoFile.getFilePath();
        readFile(response,filePath + "/" + ts);
    }

    /**
     * @description: 将文件写入response
     * @param response
     * @param filePath
     * @return
     * @author fz
     * 2024/12/10 21:31
     */
    protected void readFile(HttpServletResponse response, String filePath) {
        File file = new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER + filePath);
        if (!file.exists()) {
            return;
        }
        try (OutputStream out = response.getOutputStream(); FileInputStream in = new FileInputStream(file)) {
            byte[] byteData = new byte[1024];
            int len = 0;
            while ((len = in.read(byteData)) != -1) {
                out.write(byteData, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            log.error("读取文件异常", e);
        }
    }
}
