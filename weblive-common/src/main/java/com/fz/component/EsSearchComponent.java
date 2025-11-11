package com.fz.component;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.fz.entity.config.AppConfig;
import com.fz.entity.dto.VideoInfoEsDto;
import com.fz.entity.enums.PageSize;
import com.fz.entity.enums.SearchOrderTypeEnum;
import com.fz.entity.po.UserInfo;
import com.fz.entity.po.VideoInfo;
import com.fz.entity.query.SimplePage;
import com.fz.entity.query.UserInfoQuery;
import com.fz.entity.vo.PaginationResultVO;
import com.fz.exception.BusinessException;
import com.fz.mappers.UserInfoMapper;
import com.fz.utils.CopyTools;
import com.fz.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.fz.entity.constants.Constants.JSON_FILE_SUFFIX;

/**
 * @Author: fz
 * @Date: 2025/1/13 20:17
 * @Description: Elasticsearch Java client based implementation (8.x)
 */
@Component
@Slf4j
public class EsSearchComponent {
    @Resource
    private AppConfig appConfig;
    @Resource
    private ElasticsearchClient esClient;
    @Resource
    private UserInfoMapper<UserInfo,UserInfoQuery> userInfoMapper;

    /**
     * 判断索引是否存在
     * @return true:存在 false:不存在
     */
    private Boolean isExistIndex() {
        try {
            BooleanResponse exists = esClient.indices().exists(e -> e.index(appConfig.getEsIndexVideoName()));
            return exists.value();
        } catch (Exception e) {
            log.error("判断索引是否存在失败",e);
            throw new BusinessException("ES索引判断失败");
        }
    }

    /**
     * 创建索引和映射
     */
    public void createIndex() {
        try {
            Boolean existIndex = isExistIndex();
            // 如果索引已经存在，则不需要创建
            if (existIndex) {
                log.info("索引{}已存在，无需创建", appConfig.getEsIndexVideoName());
                return;
            }
            /*设置索引的映射和分析器
             这里的映射定义了每个字段的类型和属性
             videoId, userId, videoCover, playCount, danmuCount, collectCount, createTime 只做存储，不需要建立倒排索引
             videoId, userId, videoCover 使用text类型，设置"index": false
             videoName 使用ik_max_word分词
             tags 使用自定义的comma分词器
             playCount, danmuCount, collectCount 不需要被索引
             createTime 使用date类型，格式为yyyy-MM-dd HH:mm:ss
             注意：如果需要对某些字段进行全文检索，则需要将其类型设置为"text"并指定分词器
             如果不需要全文检索，则可以将其类型设置为"keyword"或"text"并设置"index": false
             */
            try(InputStream input = appConfig.getEsVideoIndexMapping().getInputStream()) {
                // 创建索引weblive_video
                CreateIndexRequest req = CreateIndexRequest.of(b -> b
                        .index(appConfig.getEsIndexVideoName())
                        .withJson(input)
                );
                // 检查索引是否创建成功
                boolean created = esClient.indices().create(req).acknowledged();
                // 如果创建索引失败，抛出异常
                if (!created) {
                    throw new BusinessException("初始化es失败");
                }
                log.info("创建索引{}成功", appConfig.getEsIndexVideoName());
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("初始化es失败", e);
            throw new BusinessException("初始化es失败");
        }
    }

    /**
     * 判断文档是否存在
     * @param id 文档ID
     * @return  true:存在 false:不存在
     */
    private Boolean docExist(String id) {
        try {
            BooleanResponse exists = esClient.exists(e -> e
                    .index(appConfig.getEsIndexVideoName())
                    .id(id)
            );
            return exists.value();
        } catch (IOException e) {
            log.error("判断文档是否存在失败，文档id:{},异常信息:{}", id, e.getMessage());
            throw new BusinessException("ES文档判断失败");
        }
    }

    /**
     * 新增或更新视频文档到ES
     * @param videoInfo 视频信息
     */
    public void saveDoc(VideoInfo videoInfo) {
        try {
            // 检查索引是否存在，如果不存在则创建
            if (docExist(videoInfo.getVideoId())) {
                // 如果文档已存在，则更新
                updateDoc(videoInfo);
            } else {
                VideoInfoEsDto videoInfoEsDto = CopyTools.copy(videoInfo, VideoInfoEsDto.class);
                videoInfoEsDto.setCollectCount(0);
                videoInfoEsDto.setPlayCount(0);
                videoInfoEsDto.setDanmuCount(0);
                //设置文档id为视频Id
                IndexResponse response = esClient.index(i -> i
                        .index(appConfig.getEsIndexVideoName())
                        .id(videoInfo.getVideoId())
                        .document(videoInfoEsDto)
                );
                log.info("新增视频到es成功，视频id:{}, response={}", videoInfo.getVideoId(), response);
            }
        } catch (Exception e) {
            log.error("新增视频到es失败", e);
            throw new BusinessException("保存失败");
        }
    }

    /**
     * 更新文档
     * @param videoInfo 视频信息
     */
    private void updateDoc(VideoInfo videoInfo) {
        try {
            //时间不更新
            videoInfo.setLastUpdateTime(null);
            videoInfo.setCreateTime(null);
            // 创建一个Map来存储需要更新的字段
            Map<String, Object> dataMap = new HashMap<>();
            // 反射获取所有字段
            Field[] fields = videoInfo.getClass().getDeclaredFields();
            for (Field field : fields) {
                // 拼接出getter方法名,比如字段 videoId，方法名就是 getVideoId。
                String methodName = "get" + StringTools.upperCaseFirstLetter(field.getName());
                // 通过反射获取方法
                Method method = videoInfo.getClass().getMethod(methodName);
                // 调用方法获取值
                Object val = method.invoke(videoInfo);
                // 如果值不为空且不是空字符串，则添加到map中
                // 注意：如果是String类型且不为空字符串，则也添加到map中
                // 如果是非String类型，则直接添加到map中
                // 这里的逻辑是为了避免将空字符串或null值存储到ES中，这样可以减少存储空间和索引的大小，提高查询性能
                if (val != null) {
                    if (val instanceof String s) {
                        if (!StringTools.isEmpty(s)) {
                            // 将字段名和对应的值添加到Map中
                            dataMap.put(field.getName(), s);
                        }
                    } else {
                        // 将字段名和对应的值添加到Map中
                        dataMap.put(field.getName(), val);
                    }
                }
            }
            // 如果没有任何字段需要更新，则直接返回
            if (dataMap.isEmpty()) {
                return;
            }
            UpdateResponse<VideoInfoEsDto> resp = esClient.update(u -> u
                            .index(appConfig.getEsIndexVideoName())
                            .id(videoInfo.getVideoId())
                            .doc(dataMap),
                    VideoInfoEsDto.class);
            log.info("更新视频到es成功，视频id:{}, response={}", videoInfo.getVideoId(), resp);
        } catch (Exception e) {
            log.error("新增视频到es失败", e);
            throw new BusinessException("保存失败");
        }
    }

    /**
     * 更新文档中的计数字段
     * 例如：更新播放量、弹幕数、收藏数等
     * 注意：这里的count是增量更新的值，比如如果当前播放量是1000，count是5，那么更新后播放量就是1005
     * 如果需要设置为具体的值，请使用updateDoc方法
     * @param videoId 视频ID
     * @param fieldName 字段名称
     * @param count 增量值
     */
    public void updateDocCount(String videoId, String fieldName, Integer count) {
        try {
            //UpdateRequest updateRequest = new UpdateRequest(appConfig.getEsIndexVideoName(), videoId);
            //Painless 内联脚本: ctx._source.<fieldName> += params.count， 取出当前文档 _source 中对应字段的值, 加上传入的增量 count, 再写回。
            //ES 在服务端执行脚本，保证并发下的增量正确(单次脚本原子于该文档更新)。
            esClient.update(u -> u
                            .index(appConfig.getEsIndexVideoName())
                            .id(videoId)
                            .script(s -> s
                                    .source("ctx._source." + fieldName + " += params.count")
                                    .lang("painless")
                                    .params("count", JsonData.of(count))
                            ),
                    VideoInfoEsDto.class
            );
            log.info("更新数量到es成功 videoId={}, fieldName={}, count={}", videoId, fieldName, count);
        } catch (Exception e) {
            log.error("更新数量到es失败 videoId={}, fieldName={}, count={}", videoId, fieldName, count, e);
            throw new BusinessException("保存失败");
        }
    }

    /**
     * 删除视频文档
     * @param videoId 视频ID
     */
    public void delDoc(String videoId) {
        try {
            DeleteResponse response = esClient.delete(d -> d
                    .index(appConfig.getEsIndexVideoName())
                    .id(videoId)
            );
            log.info("从es删除视频，videoId={}, response={}", videoId, response);
        } catch (Exception e) {
            log.error("从es删除视频失败", e);
            throw new BusinessException("删除视频失败");
        }

    }

    /**
     * 搜索视频
     * @param highlight 是否高亮
     * @param keyword 关键字
     * @param orderType 排序字段
     * @param pageNo 页号
     * @param pageSize 页大小
     * @return PaginationResultVO<VideoInfo>
     */
    public PaginationResultVO<VideoInfo> search(Boolean highlight, String keyword, Integer orderType, Integer pageNo, Integer pageSize) {
        try {

            SearchOrderTypeEnum searchOrderTypeEnum = SearchOrderTypeEnum.getByType(orderType);

            pageNo = pageNo == null ? 1 : pageNo;
            //分页查询
            pageSize = pageSize == null ? PageSize.SIZE20.getSize() : pageSize;

            Integer finalPageNo = pageNo;
            Integer finalPageSize = pageSize;
            SearchResponse<VideoInfoEsDto> response = esClient.search(s -> {
                s.index(appConfig.getEsIndexVideoName())
                        .query(q -> {
                            // 当 keyword 为空时改用 match_all，避免 multiMatch 空 query。
                            if (keyword == null || keyword.trim().isEmpty()) {
                                return q.matchAll(ma -> ma);
                            }
                            return q.multiMatch(m -> m
                                    .query(keyword)
                                    .fields("videoName", "tags")
                            );
                        });
                if (Boolean.TRUE.equals(highlight) && keyword != null && !keyword.trim().isEmpty()) {
                    s.highlight(h -> h
                            .fields("videoName", hf -> hf
                                    .preTags("<span class='highlight'>")
                                    .postTags("</span>")
                            )
                    );
                }
                // from 是从第几条数据开始查询，默认是0
                s.from((finalPageNo - 1) * finalPageSize);
                // size 是每页显示多少条，默认是10
                s.size(finalPageSize);
                // 其次按传入的排序字段排序
                if (orderType != null && searchOrderTypeEnum != null) {
                    s.sort(so -> so
                            .field(f -> f
                                    .field(searchOrderTypeEnum.getField())
                                    .order(SortOrder.Desc)
                            )
                    );
                } else {
                    // 默认按相关度排序
                    s.sort(so -> so
                            .field(f -> f
                                    .field("_score")
                                    .order(SortOrder.Desc)
                            )
                    );
                }
                return s;
            }, VideoInfoEsDto.class);

            // 获取命中的总条数
            TotalHits totalHits = response.hits().total();
            int totalCount = totalHits == null ? 0 : (int) totalHits.value();

            // 搜索结果的文档数组
            List<Hit<VideoInfoEsDto>> hits = response.hits().hits();

            List<VideoInfo> videoInfoList = new ArrayList<>();
            List<String> userIdList = new ArrayList<>();
            for (Hit<VideoInfoEsDto> hit : hits) {
                VideoInfoEsDto dto = hit.source();
                if (dto == null) {
                    log.info("ES搜索视频，命中文档为空，hit={}", hit);
                    continue;
                }
                VideoInfo videoInfo = CopyTools.copy(dto, VideoInfo.class);
                // 如果需要高亮，则替换videoName字段
                if (highlight && hit.highlight() != null && hit.highlight().get("videoName") != null) {
                    List<String> frags = hit.highlight().get("videoName");
                    if (frags != null && !frags.isEmpty()) {
                        // 从高亮字段中获取高亮片段，将第一个片段中的字符串设置为videoInfo.videoName
                        videoInfo.setVideoName(frags.get(0));
                    }
                }
                // 这里的videoInfo是从ES中查询出来的，只有部分字段，如果需要其他字段，可以根据videoId再去数据库中查询
                videoInfoList.add(videoInfo);
                // 收集userId，后面批量查询用户信息
                userIdList.add(videoInfo.getUserId());
            }
            UserInfoQuery userInfoQuery = new UserInfoQuery();
            if (!userIdList.isEmpty()) {
                // 去重userId列表
                userIdList = userIdList.stream().distinct().collect(Collectors.toList());
            }
            userInfoQuery.setUserIdList(userIdList);
            List<UserInfo> userInfoList = userInfoMapper.selectList(userInfoQuery);
            // 将用户信息转换为Map，key是userId，value是UserInfo对象
            Map<String, UserInfo> userInfoMap = userInfoList.stream()
                    .collect(Collectors
                            .toMap(UserInfo::getUserId, Function.identity(), (data1, data2) -> data2));
            // 将用户昵称设置到视频信息中
            videoInfoList.forEach(item -> {
                UserInfo userInfo = userInfoMap.get(item.getUserId());
                if (userInfo != null) {
                    item.setNickName(userInfo.getNickName());
                }
            });
            SimplePage page = new SimplePage(pageNo, totalCount, pageSize);
            return new PaginationResultVO<>(totalCount, page.getPageSize(), page.getPageNo(), page.getPageTotal(), videoInfoList);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询视频到es失败 rootCause={}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage(), e);
            throw new BusinessException("查询失败");
        }
    }

}
